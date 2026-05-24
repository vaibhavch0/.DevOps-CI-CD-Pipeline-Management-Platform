package com.autorabit.pipeline.dto;

import com.autorabit.pipeline.model.StepLog;
import java.time.LocalDateTime;

/**
 * DTO for log messages streamed in real-time via WebSocket.
 * Published to: /topic/execution/{executionId}/logs
 */
public class LogMessageDTO {

    private Long executionId;
    private String stepName;
    private String message;
    private StepLog.LogLevel level;
    private LocalDateTime timestamp;

    public LogMessageDTO() {}

    private LogMessageDTO(Builder b) {
        this.executionId = b.executionId;
        this.stepName    = b.stepName;
        this.message     = b.message;
        this.level       = b.level;
        this.timestamp   = b.timestamp;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long executionId;
        private String stepName;
        private String message;
        private StepLog.LogLevel level;
        private LocalDateTime timestamp;

        public Builder executionId(Long id)        { this.executionId = id; return this; }
        public Builder stepName(String name)       { this.stepName = name; return this; }
        public Builder message(String msg)         { this.message = msg; return this; }
        public Builder level(StepLog.LogLevel lvl) { this.level = lvl; return this; }
        public Builder timestamp(LocalDateTime t)  { this.timestamp = t; return this; }
        public LogMessageDTO build()               { return new LogMessageDTO(this); }
    }

    // ANSI-style color hint for terminal UI
    public String getLevelColor() {
        if (level == null) return "#a0aec0";
        return switch (level) {
            case DEBUG   -> "#718096";
            case INFO    -> "#63b3ed";
            case WARN    -> "#f6e05e";
            case ERROR   -> "#fc8181";
            case SUCCESS -> "#68d391";
        };
    }

    public Long getExecutionId()         { return executionId; }
    public String getStepName()          { return stepName; }
    public String getMessage()           { return message; }
    public StepLog.LogLevel getLevel()   { return level; }
    public LocalDateTime getTimestamp()  { return timestamp; }
}
