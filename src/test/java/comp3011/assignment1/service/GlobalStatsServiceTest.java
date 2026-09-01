package comp3011.assignment1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GlobalStatsServiceTest {

    @Test
    void shouldHandleConcurrentUpdatesWithoutLosingTokens() throws InterruptedException {

        GlobalStatsService statsService = new GlobalStatsService();

        int updatesPerThread = 100_000;

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < updatesPerThread; i++) {
                statsService.addUsage(1, 1);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < updatesPerThread; i++) {
                statsService.addUsage(1, 1);
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        var stats = statsService.getStats();

        assertEquals(200_000, stats.inputTokens());
        assertEquals(200_000, stats.outputTokens());
    }
}