package com.autorabit.pipeline.service;

import com.autorabit.pipeline.dto.LogMessageDTO;
import com.autorabit.pipeline.dto.NotificationDTO;
import com.autorabit.pipeline.model.PipelineExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Sends real-time messages to WebSocket clients via STOMP.
 *
 * Destinations:
 *   /topic/execution/{id}/logs     — log lines per execution
 *   /topic/execution/{id}/status   — status change events
 *   /topic/notifications           — global alerts
 *   /topic/metrics/refresh         — trigger metrics reload signal
 */
@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ---- Log Streaming ----

    public void sendLog(Long executionId, LogMessageDTO logMessage) {
        String destination = "/topic/execution/" + executionId + "/logs";
        messagingTemplate.convertAndSend(destination, logMessage);
    }

    // ---- Execution Status Updates ----

    public void sendStatusUpdate(Long executionId, PipelineExecution.ExecutionStatus status) {
        String destination = "/topic/execution/" + executionId + "/status";
        messagingTemplate.convertAndSend(destination, status.name());
        log.debug("Status update sent → {} : {}", destination, status);
    }

    // ---- Global Notifications ----

    public void sendNotification(NotificationDTO notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.debug("Notification sent: {}", notification.getTitle());
    }

    // ---- Metrics Refresh Signal ----

    public void triggerMetricsRefresh() {
        messagingTemplate.convertAndSend("/topic/metrics/refresh", "REFRESH");
    }
}
