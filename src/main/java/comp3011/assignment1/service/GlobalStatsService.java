package comp3011.assignment1.service;

import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.GlobalStatsResponse;

@Service
public class GlobalStatsService {

    private long inputTokens = 0;
    private long outputTokens = 0;

    public synchronized void addUsage(long input, long output) {
        inputTokens += input;
        outputTokens += output;
    }

    public synchronized GlobalStatsResponse getStats() {
        return new GlobalStatsResponse(
                inputTokens,
                outputTokens
        );
    }
}