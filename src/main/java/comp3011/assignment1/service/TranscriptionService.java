package comp3011.assignment1.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    private final String apiKey;
    private final RestClient restClient;
    private final GlobalStatsService globalStatsService;

    public TranscriptionService(
            @Value("${OPENAI_API_KEY:}") String apiKey,
            RestClient.Builder restClientBuilder,
            GlobalStatsService globalStatsService) {

        this.apiKey = apiKey;
        this.globalStatsService = globalStatsService;

        this.restClient = restClientBuilder
                .baseUrl("https://api.openai.com")
                .build();
    }

    public String transcribe(MultipartFile file) {

        if (apiKey.isBlank()) {
            return "OPENAI_API_KEY is not configured";
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("model", "gpt-4o-mini-transcribe");
        body.add("file", file.getResource());

        TranscriptionResponse response = restClient.post()
                .uri("/v1/audio/transcriptions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(TranscriptionResponse.class);

        if (response == null) {
            return "No transcription received";
        }

        if (response.usage() != null) {
            globalStatsService.addUsage(
                    response.usage().inputTokens(),
                    response.usage().outputTokens()
            );
        }

        return response.text();
    }

    private record TranscriptionResponse(
            String text,
            Usage usage) {
    }

    private record Usage(
            @JsonProperty("input_tokens") long inputTokens,
            @JsonProperty("output_tokens") long outputTokens) {
    }
}