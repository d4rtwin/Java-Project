package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.MangakaDashboardStatsRes;
import com.mangakousei.mangakousei_backend.dto.response.MangakaDeadlineRes;
import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRankRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.MangakaDashboardService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mangaka/dashboard")
@RequiredArgsConstructor
public class MangakaDashboardController {

    private final MangakaDashboardService dashboardService;

    private void requireMangaka() {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        requireMangaka();
        Long mangakaId = SecurityUtils.getCurrentUserId();
        MangakaDashboardStatsRes result = dashboardService.getStats(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("OK", result));
    }

    @GetMapping("/deadlines")
    public ResponseEntity<?> getDeadlines() {
        requireMangaka();
        Long mangakaId = SecurityUtils.getCurrentUserId();
        List<MangakaDeadlineRes> result = dashboardService.getDeadlineAlerts(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("OK", result));
    }

    @GetMapping("/top-series")
    public ResponseEntity<?> getTopSeries() {
        requireMangaka();
        Long mangakaId = SecurityUtils.getCurrentUserId();
        List<TantouSeriesRankRes> result = dashboardService.getTopSeries(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("OK", result));
    }
}