package com.autorabit.pipeline.service;

import com.autorabit.pipeline.dto.MetricsDTO;
import com.autorabit.pipeline.model.PipelineExecution;
import com.autorabit.pipeline.repository.PipelineExecutionRepository;
import com.autorabit.pipeline.repository.PipelineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metrics service — computes dashboard stats and manages SSE connections.
 * SSE emitters are stored in a concurrent map and updated every 5 seconds.
 */
@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private final PipelineRepository pipelineRepository;
    private final PipelineExecutionRepository executionRepository;

    // SSE emitter registry — concurrent for thread safety
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public MetricsService(PipelineRepository pipelineRepository, PipelineExecutionRepository executionRepository) {
        this.pipelineRepository = pipelineRepository;
        this.executionRepository = executionRepository;
    }

    // ---- SSE Subscription ----

    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> {
            emitters.remove(clientId);
            log.debug("SSE emitter completed for client: {}", clientId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(clientId);
            log.debug("SSE emitter timed out for client: {}", clientId);
        });
        emitter.onError(e -> {
            emitters.remove(clientId);
            log.debug("SSE emitter error for client: {}", clientId);
        });

        emitters.put(clientId, emitter);

        // Send initial data immediately
        try {
            emitter.send(SseEmitter.event()
                    .name("metrics")
                    .data(computeMetrics()));
        } catch (IOException e) {
            emitters.remove(clientId);
        }

        log.info("New SSE subscriber: {} (total: {})", clientId, emitters.size());
        return emitter;
    }

    // ---- Scheduled Push — every 5 seconds ----

    @Scheduled(fixedDelay = 5000)
    @Transactional(readOnly = true)
    public void pushMetricsToAllSubscribers() {
        if (emitters.isEmpty()) return;

        MetricsDTO metrics = computeMetrics();
        List<String> deadClients = new ArrayList<>();

        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("metrics")
                        .data(metrics));
            } catch (IOException | IllegalStateException e) {
                deadClients.add(clientId);
            }
        });

        deadClients.forEach(emitters::remove);
        if (!deadClients.isEmpty()) {
            log.debug("Removed {} dead SSE connections", deadClients.size());
        }
    }

    // ---- Metrics Computation ----

    @Transactional(readOnly = true)
    public MetricsDTO computeMetrics() {
        long totalPipelines    = pipelineRepository.count();
        long totalExecutions   = executionRepository.count();
        long running           = executionRepository.countRunning();
        long successful        = executionRepository.countSuccessful();
        long failed            = executionRepository.countFailed();
        long queued            = executionRepository.countByStatus(PipelineExecution.ExecutionStatus.QUEUED);
        Double avgDuration     = executionRepository.avgDurationSeconds();

        double successRate = totalExecutions > 0
                ? (successful * 100.0 / totalExecutions) : 0.0;
        double failureRate = totalExecutions > 0
                ? (failed * 100.0 / totalExecutions) : 0.0;

        // Daily trend — last 7 days (simulated for demo)
        List<MetricsDTO.DailyCount> trend = buildDailyTrend();

        // Active executions
        List<PipelineExecution> activeExecs = executionRepository.findByStatus(
                PipelineExecution.ExecutionStatus.RUNNING);

        List<MetricsDTO.ActiveExecution> activeList = activeExecs.stream()
                .map(e -> MetricsDTO.ActiveExecution.builder()
                        .executionId(e.getId())
                        .pipelineName(e.getPipeline().getName())
                        .buildNumber(e.getBuildNumber())
                        .triggeredBy(e.getTriggeredBy())
                        .startedAt(e.getStartedAt())
                        .currentStep(e.getSteps().stream()
                                .filter(s -> s.getStatus() == com.autorabit.pipeline.model.PipelineStep.StepStatus.RUNNING)
                                .findFirst()
                                .map(s -> s.getName())
                                .orElse("Processing..."))
                        .build())
                .toList();

        return MetricsDTO.builder()
                .totalPipelines(totalPipelines)
                .totalExecutions(totalExecutions)
                .runningExecutions(running)
                .successfulExecutions(successful)
                .failedExecutions(failed)
                .queuedExecutions(queued)
                .successRate(Math.round(successRate * 10.0) / 10.0)
                .failureRate(Math.round(failureRate * 10.0) / 10.0)
                .avgDurationSeconds(avgDuration != null ? Math.round(avgDuration * 10.0) / 10.0 : null)
                .dailyTrend(trend)
                .activeExecutions(activeList)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private List<MetricsDTO.DailyCount> buildDailyTrend() {
        // Build last-7-days trend from actual recent executions
        List<MetricsDTO.DailyCount> trend = new ArrayList<>();
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<PipelineExecution> recent = executionRepository.findRecentExecutions(since);

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.toString();

            long daySuccess = recent.stream()
                    .filter(e -> e.getCreatedAt() != null &&
                            e.getCreatedAt().toLocalDate().equals(date) &&
                            e.getStatus() == PipelineExecution.ExecutionStatus.SUCCESS)
                    .count();
            long dayFailed = recent.stream()
                    .filter(e -> e.getCreatedAt() != null &&
                            e.getCreatedAt().toLocalDate().equals(date) &&
                            e.getStatus() == PipelineExecution.ExecutionStatus.FAILED)
                    .count();

            trend.add(MetricsDTO.DailyCount.builder()
                    .date(dateStr)
                    .success(daySuccess)
                    .failed(dayFailed)
                    .total(daySuccess + dayFailed)
                    .build());
        }
        return trend;
    }

    public int getActiveSubscriberCount() {
        return emitters.size();
    }
}
