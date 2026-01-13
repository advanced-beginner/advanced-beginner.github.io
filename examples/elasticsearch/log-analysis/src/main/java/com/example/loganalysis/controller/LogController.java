package com.example.loganalysis.controller;

import com.example.loganalysis.domain.LogEntry;
import com.example.loganalysis.service.LogAnalyticsService;
import com.example.loganalysis.service.LogSearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogSearchService searchService;
    private final LogAnalyticsService analyticsService;

    public LogController(LogSearchService searchService, LogAnalyticsService analyticsService) {
        this.searchService = searchService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/errors")
    public ResponseEntity<List<LogEntry>> searchErrors(
            @RequestParam(required = false) String query,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.searchErrors(query, from, to, page, size));
    }

    @GetMapping("/trace/{requestId}")
    public ResponseEntity<List<LogEntry>> traceRequest(@PathVariable String requestId) {
        return ResponseEntity.ok(searchService.traceRequest(requestId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LogEntry>> getUserLogs(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(searchService.getUserLogs(userId, from, to));
    }

    @GetMapping("/analytics/errors-by-hour")
    public ResponseEntity<Map<String, Long>> getErrorCountByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(analyticsService.getErrorCountByHour(from, to));
    }

    @GetMapping("/analytics/error-rate")
    public ResponseEntity<LogAnalyticsService.ErrorRateResult> getErrorRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(analyticsService.getErrorRate(from, to));
    }

    @GetMapping("/analytics/slow-requests")
    public ResponseEntity<List<LogAnalyticsService.SlowRequest>> getSlowRequests(
            @RequestParam(defaultValue = "10") int topN,
            @RequestParam(defaultValue = "1000") long thresholdMs) {
        return ResponseEntity.ok(analyticsService.getSlowRequests(topN, thresholdMs));
    }
}
