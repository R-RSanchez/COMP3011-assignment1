package comp3011.assignment1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.service.TranscriptionService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrentRequestTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private TranscriptionService transcriptionService;

    @Test
    void shouldHandleMoreThan200BlockingRequestsConcurrently() throws Exception {

        int requestCount = 250;

        doAnswer(invocation -> {
            Thread.sleep(500);
            return "stub transcription";
        }).when(transcriptionService).transcribe(any(MultipartFile.class));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        CountDownLatch startGate = new CountDownLatch(1);

        List<Future<Integer>> futures = new ArrayList<>();

        long startTime = System.nanoTime();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < requestCount; i++) {

                futures.add(executor.submit(() -> {

                    startGate.await();

                    HttpRequest request = createAudioRequest();

                    HttpResponse<String> response = client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

                    return response.statusCode();
                }));
            }

            startGate.countDown();

            for (Future<Integer> future : futures) {
                assertEquals(200, future.get());
            }
        }

        double elapsedSeconds =
                (System.nanoTime() - startTime) / 1_000_000_000.0;

        System.out.println(
                requestCount
                + " concurrent blocking requests completed in "
                + elapsedSeconds
                + " seconds"
        );

        assertTrue(
                elapsedSeconds < 10,
                "Concurrent requests took too long: "
                + elapsedSeconds
                + " seconds"
        );
    }

    private HttpRequest createAudioRequest() throws Exception {

        String boundary =
                "----AssignmentBoundary" + UUID.randomUUID();

        ByteArrayOutputStream body = new ByteArrayOutputStream();

        body.write(("--" + boundary + "\r\n")
                .getBytes(StandardCharsets.UTF_8));

        body.write((
                "Content-Disposition: form-data; "
                + "name=\"file\"; filename=\"recording.webm\"\r\n"
        ).getBytes(StandardCharsets.UTF_8));

        body.write(
                "Content-Type: audio/webm\r\n\r\n"
                .getBytes(StandardCharsets.UTF_8)
        );

        body.write(
                "fake audio data"
                .getBytes(StandardCharsets.UTF_8)
        );

        body.write(("\r\n--" + boundary + "--\r\n")
                .getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:"
                        + port
                        + "/api/transcribe"
                ))
                .timeout(Duration.ofSeconds(10))
                .header(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                )
                .POST(HttpRequest.BodyPublishers.ofByteArray(
                        body.toByteArray()
                ))
                .build();
    }
}