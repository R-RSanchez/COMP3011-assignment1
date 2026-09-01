package comp3011.assignment1.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.dto.ErrorResponse;
import comp3011.assignment1.dto.ShutdownResponse;
import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.ServerLifecycleService;

@RestController
public class AdminController {

    private final ServerLifecycleService serverLifecycleService;
    private final ConfigurableApplicationContext applicationContext;

    public AdminController(
            ServerLifecycleService serverLifecycleService,
            ConfigurableApplicationContext applicationContext) {

        this.serverLifecycleService = serverLifecycleService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/api/v1/admin/uptime")
    public UptimeResponse getServerUptime() {

        Instant start = serverLifecycleService.getUtcServerStart();
        Instant now = Instant.now();

        double uptimeSeconds =
                Duration.between(start, now).toNanos() / 1_000_000_000.0;

        return new UptimeResponse(
                start,
                now,
                uptimeSeconds
        );
    }

    @PostMapping("/api/v1/admin/shutdown")
    public ResponseEntity<?> shutdownServer() {

        boolean accepted = serverLifecycleService.beginShutdown();

        if (!accepted) {

            ErrorResponse error = new ErrorResponse(
                    Instant.now(),
                    409,
                    "Conflict",
                    "Graceful shutdown is already in progress.",
                    "/api/v1/admin/shutdown"
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(error);
        }

        Thread shutdownThread = new Thread(() -> {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            applicationContext.close();
        });

        shutdownThread.start();

        ShutdownResponse response =
                new ShutdownResponse("Graceful shutdown requested.");

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}