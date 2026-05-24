package com.autorabit.pipeline.controller;

import com.autorabit.pipeline.dto.LogMessageDTO;
import com.autorabit.pipeline.dto.NotificationDTO;
import com.autorabit.pipeline.service.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket STOMP message controller.
 *
 * Clients SEND TO:
 *   /app/ping                      — heartbeat ping
 *   /app/execution/{id}/subscribe  — subscribe to execution logs
 *
 * Server PUSHES TO:
 *   /topic/execution/{id}/logs     — log streaming
 *   /topic/execution/{id}/status   — status updates
 *   /topic/notifications           — global alerts
 *   /topic/metrics/refresh         — metrics refresh signal
 */
@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    private final WebSocketNotificationService wsNotifier;

    public WebSocketController(WebSocketNotificationService wsNotifier) {
        this.wsNotifier = wsNotifier;
    }

    /**
     * Heartbeat — client pings, server pongs back to /user/queue/pong
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public Map<String, Object> handlePing(Principal principal) {
        log.debug("WebSocket ping from: {}", principal != null ? principal.getName() : "anonymous");
        return Map.of(
                "type", "PONG",
                "timestamp", LocalDateTime.now().toString(),
                "server", "AutoRABIT Pipeline Monitor"
        );
    }

    /**
     * Client subscribes to a specific execution to receive logs.
     * Server broadcasts a confirmation back to all subscribers of that execution.
     */
    @MessageMapping("/execution/{executionId}/subscribe")
    @SendTo("/topic/execution/{executionId}/status")
    public Map<String, Object> handleSubscription(
            @DestinationVariable Long executionId,
            Principal principal) {
        String user = principal != null ? principal.getName() : "anonymous";
        log.info("Client '{}' subscribed to execution: {}", user, executionId);
        return Map.of(
                "type", "SUBSCRIBED",
                "executionId", executionId,
                "message", "Now streaming execution #" + executionId,
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
