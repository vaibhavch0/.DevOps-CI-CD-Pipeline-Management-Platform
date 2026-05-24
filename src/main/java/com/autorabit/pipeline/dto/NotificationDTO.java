package com.autorabit.pipeline.dto;

import com.autorabit.pipeline.model.PipelineExecution;
import java.time.LocalDateTime;

/**
 * WebSocket notification pushed to /topic/notifications.
 */
public class NotificationDTO {

    public enum NotificationType {
        PIPELINE_STARTED, PIPELINE_COMPLETED, PIPELINE_FAILED,
        STEP_STARTED, STEP_COMPLETED, STEP_FAILED, SYSTEM_ALERT
    }

    private NotificationType type;
    private String title;
    private String message;
    private Long executionId;
    private Long pipelineId;
    private String pipelineName;
    private Integer buildNumber;
    private PipelineExecution.ExecutionStatus executionStatus;
    private LocalDateTime timestamp;

    public NotificationDTO() {}

    private NotificationDTO(Builder b) {
        this.type            = b.type;
        this.title           = b.title;
        this.message         = b.message;
        this.executionId     = b.executionId;
        this.pipelineId      = b.pipelineId;
        this.pipelineName    = b.pipelineName;
        this.buildNumber     = b.buildNumber;
        this.executionStatus = b.executionStatus;
        this.timestamp       = b.timestamp;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private NotificationType type;
        private String title, message, pipelineName;
        private Long executionId, pipelineId;
        private Integer buildNumber;
        private PipelineExecution.ExecutionStatus executionStatus;
        private LocalDateTime timestamp;

        public Builder type(NotificationType t)                          { this.type = t; return this; }
        public Builder title(String t)                                   { this.title = t; return this; }
        public Builder message(String m)                                 { this.message = m; return this; }
        public Builder executionId(Long id)                              { this.executionId = id; return this; }
        public Builder pipelineId(Long id)                               { this.pipelineId = id; return this; }
        public Builder pipelineName(String n)                            { this.pipelineName = n; return this; }
        public Builder buildNumber(Integer n)                            { this.buildNumber = n; return this; }
        public Builder executionStatus(PipelineExecution.ExecutionStatus s) { this.executionStatus = s; return this; }
        public Builder timestamp(LocalDateTime t)                        { this.timestamp = t; return this; }
        public NotificationDTO build()                                   { return new NotificationDTO(this); }
    }

    public static NotificationDTO pipelineStarted(Long execId, String pipelineName, int buildNo) {
        return NotificationDTO.builder()
                .type(NotificationType.PIPELINE_STARTED)
                .title("Pipeline Started")
                .message(String.format("🚀 %s #%d is now running", pipelineName, buildNo))
                .executionId(execId).pipelineName(pipelineName).buildNumber(buildNo)
                .executionStatus(PipelineExecution.ExecutionStatus.RUNNING)
                .timestamp(LocalDateTime.now()).build();
    }

    public static NotificationDTO pipelineCompleted(Long execId, String pipelineName, int buildNo) {
        return NotificationDTO.builder()
                .type(NotificationType.PIPELINE_COMPLETED)
                .title("Pipeline Succeeded")
                .message(String.format("✅ %s #%d completed successfully", pipelineName, buildNo))
                .executionId(execId).pipelineName(pipelineName).buildNumber(buildNo)
                .executionStatus(PipelineExecution.ExecutionStatus.SUCCESS)
                .timestamp(LocalDateTime.now()).build();
    }

    public static NotificationDTO pipelineFailed(Long execId, String pipelineName, int buildNo) {
        return NotificationDTO.builder()
                .type(NotificationType.PIPELINE_FAILED)
                .title("Pipeline Failed")
                .message(String.format("❌ %s #%d failed", pipelineName, buildNo))
                .executionId(execId).pipelineName(pipelineName).buildNumber(buildNo)
                .executionStatus(PipelineExecution.ExecutionStatus.FAILED)
                .timestamp(LocalDateTime.now()).build();
    }

    public NotificationType getType()                              { return type; }
    public String getTitle()                                       { return title; }
    public String getMessage()                                     { return message; }
    public Long getExecutionId()                                   { return executionId; }
    public Long getPipelineId()                                    { return pipelineId; }
    public String getPipelineName()                                { return pipelineName; }
    public Integer getBuildNumber()                                { return buildNumber; }
    public PipelineExecution.ExecutionStatus getExecutionStatus()  { return executionStatus; }
    public LocalDateTime getTimestamp()                            { return timestamp; }
}
