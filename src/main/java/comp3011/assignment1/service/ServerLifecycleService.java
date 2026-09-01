package comp3011.assignment1.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class ServerLifecycleService {

    private final Instant utcServerStart = Instant.now();

    private boolean shutdownInProgress = false;

    public Instant getUtcServerStart() {
        return utcServerStart;
    }

    public synchronized boolean beginShutdown() {

        if (shutdownInProgress) {
            return false;
        }

        shutdownInProgress = true;
        return true;
    }
}