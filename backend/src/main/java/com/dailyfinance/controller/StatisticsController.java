package com.dailyfinance.controller;

import com.dailyfinance.common.ApiResponse;
import com.dailyfinance.common.UserContext;
import com.dailyfinance.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        return ApiResponse.ok(statisticsService.home(UserContext.requireUserId()));
    }

    @GetMapping("/monthly")
    public ApiResponse<Map<String, Object>> monthly(@RequestParam(required = false) String month) {
        return ApiResponse.ok(statisticsService.monthly(UserContext.requireUserId(), month));
    }

    @GetMapping("/category")
    public ApiResponse<List<Map<String, Object>>> category(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(statisticsService.category(UserContext.requireUserId(), startDate, endDate));
    }

    @GetMapping("/trend")
    public ApiResponse<List<Map<String, Object>>> trend(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(statisticsService.trend(UserContext.requireUserId(), startDate, endDate));
    }
}
