package com.autorabit.pipeline.controller;

import com.autorabit.pipeline.dto.ApiResponse;
import com.autorabit.pipeline.dto.MetricsDTO;
import com.autorabit.pipeline.service.MetricsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * Metrics REST + SSE controller.
 *
 * GET /api/metrics         — snapshot metrics (REST, one-time)
 * GET /api/metrics/stream  — SSE stream pushed every 5s (real-time)
 * GET /api/metrics/status  — SSE subscriber count
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * One-shot REST endpoint — returns current metrics snapshot.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MetricsDTO>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(metricsService.computeMetrics()));
    }

    /**
     * Server-Sent Events endpoint — pushes live metrics every 5 seconds.
     * Clients subscribe: EventSource("/api/metrics/stream")
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMetrics() {
        String clientId = UUID.randomUUID().toString();
        return metricsService.createEmitter(clientId);
    }

    /**
     * Returns number of active SSE subscribers — useful for debugging.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Object>> getStreamStatus() {
        return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of(
                        "activeSubscribers", metricsService.getActiveSubscriberCount(),
                        "streamEndpoint", "/api/metrics/stream"
                )
        ));
    }
}
