package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.MangakaDashboardStatsRes;
import com.mangakousei.mangakousei_backend.dto.response.MangakaDeadlineRes;
import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRankRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import com.mangakousei.mangakousei_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MangakaDashboardService {

    private final SeriesRepository seriesRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;
    private final ReaderInteractionRepository interactionRepository;
    private final ReaderVoteRepository readerVoteRepository;
    private final TaskSubmissionService taskSubmissionService;

    private static final DateTimeFormatter VI_DATE = DateTimeFormatter.ofPattern("dd/MM");

    @Transactional(readOnly = true)
    public List<MangakaDeadlineRes> getDeadlineAlerts(Long mangakaId) {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3);

        return deadlineRepository
                .findUpcomingByMangakaId(mangakaId, soon)
                .stream()
                .filter(d -> !"approved".equals(d.getStatus()))
                .map(d -> toDeadlineRes(d, today))
                .sorted((a, b) -> Integer.compare(order(a.getLabelType()), order(b.getLabelType())))
                .collect(Collectors.toList());
    }

    private MangakaDeadlineRes toDeadlineRes(ChapterPageDeadline d, LocalDate today) {
        LocalDate due = d.getDueDate() != null ? d.getDueDate() : today;

        String labelType, label, timeTag;
        if (due.isBefore(today)) {
            labelType = "overdue";
            label = "QUÁ HẠN";
            long daysAgo = today.toEpochDay() - due.toEpochDay();
            timeTag = daysAgo == 1 ? "Hôm qua" : daysAgo + " ngày trước";
        } else if (due.isEqual(today)) {
            labelType = "due";
            label = "ĐẾN HẠN";
            timeTag = "Hôm nay";
        } else {
            labelType = "soon";
            label = "SẮP ĐẾN";
            long daysLeft = due.toEpochDay() - today.toEpochDay();
            timeTag = daysLeft == 1 ? "Ngày mai" : due.format(VI_DATE);
        }

        var chapter = d.getChapter();
        var series = chapter != null ? chapter.getSeries() : null;
        String tantouName = series != null && series.getEditor() != null
                ? series.getEditor().getFullName() : "—";
        String seriesTitle = series != null ? series.getTitle() : "—";
        String title = "Trang " + d.getPageFrom() + "–" + d.getPageTo()
                + (chapter != null ? " – Ch." + chapter.getChapterNumber() : "");

        return MangakaDeadlineRes.builder()
                .deadlineId(d.getDeadlineId())
                .labelType(labelType)
                .label(label)
                .timeTag(timeTag)
                .title(title)
                .tantouName(tantouName)
                .series(seriesTitle)
                .dueDate(due)
                .build();
    }

    private int order(String labelType) {
        return switch (labelType) {
            case "overdue" -> 0;
            case "due" -> 1;
            default -> 2;
        };
    }

    @Transactional(readOnly = true)
    public MangakaDashboardStatsRes getStats(Long mangakaId) {
        long urgentCount = getDeadlineAlerts(mangakaId).stream()
                .filter(d -> "overdue".equals(d.getLabelType()) || "due".equals(d.getLabelType()))
                .count();

        long revisionCount = deadlineRepository.countByMangakaIdAndStatus(mangakaId, "revision");

        long pendingReviewCount = taskSubmissionService.getPendingReviews(mangakaId).size();

        long totalSeries = seriesRepository.countByCreatorUserId(mangakaId);

        return MangakaDashboardStatsRes.builder()
                .urgentDeadlineCount(urgentCount)
                .revisionCount(revisionCount)
                .pendingReviewCount(pendingReviewCount)
                .totalSeries(totalSeries)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TantouSeriesRankRes> getTopSeries(Long mangakaId) {
        return seriesRepository
                .findByCreatorUserIdOrderByApprovedAtDesc(mangakaId)
                .stream()
                .map(s -> {
                    long importedVotes = readerVoteRepository.sumVotesBySeriesId(s.getSeriesId());
                    long readerVotes = interactionRepository.countBySeriesSeriesIdAndVotedTrue(s.getSeriesId());
                    long totalVotes = importedVotes + readerVotes;

                    double importedRating = readerVoteRepository.averageScoreBySeriesId(s.getSeriesId());
                    double readerRating = interactionRepository.averageRatingBySeriesId(s.getSeriesId());
                    long ratingCount = interactionRepository.countBySeriesSeriesIdAndRatingIsNotNull(s.getSeriesId());
                    double rating = ratingCount > 0 ? readerRating : importedRating;

                    var latestChapter = chapterRepository
                            .findTopBySeriesSeriesIdOrderByChapterNumberDesc(s.getSeriesId());

                    return TantouSeriesRankRes.builder()
                            .seriesId(s.getSeriesId())
                            .title(s.getTitle())
                            .mangakaName(null)
                            .latestChapter(latestChapter.map(Chapter::getChapterNumber).orElse(null))
                            .latestChapterTitle(latestChapter.map(Chapter::getTitle).orElse(null))
                            .voteCount(totalVotes)
                            .rating(Math.round(rating * 10.0) / 10.0)
                            .chapterCount(s.getChapters() != null ? s.getChapters().size() : 0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getVoteCount(), a.getVoteCount()))
                .limit(5)
                .collect(Collectors.toList());
    }
}