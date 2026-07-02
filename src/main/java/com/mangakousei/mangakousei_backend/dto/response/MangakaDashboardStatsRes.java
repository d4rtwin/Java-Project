package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MangakaDashboardStatsRes {
    private long urgentDeadlineCount;

    private long revisionCount;

    private long pendingReviewCount;

    private long totalSeries;
}