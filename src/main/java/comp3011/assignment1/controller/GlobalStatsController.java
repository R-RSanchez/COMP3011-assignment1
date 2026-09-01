package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.dto.GlobalStatsResponse;
import comp3011.assignment1.service.GlobalStatsService;

@RestController
public class GlobalStatsController {

    private final GlobalStatsService globalStatsService;

    public GlobalStatsController(GlobalStatsService globalStatsService) {
        this.globalStatsService = globalStatsService;
    }

    @GetMapping("/api/v1/global/stats")
    public GlobalStatsResponse getGlobalStats() {
        return globalStatsService.getStats();
    }
}