package com.autorabit.pipeline.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * A single log line emitted during pipeline execution.
 * Streamed in real-time via WebSocket to connected clients.
 */
@Entity
@Table(name = "step_logs")
public class StepLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private PipelineExecution execution;

    @Column(name = "step_name")
    private String stepName;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level = LogLevel.INFO;

    @CreationTimestamp
    @Column(name = "logged_at", updatable = false)
    private LocalDateTime loggedAt;

    // ---- Enums ----

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, SUCCESS
    }

    // ---- Constructors ----

    public StepLog() {}

    private StepLog(Builder b) {
        this.execution = b.execution;
        this.stepName  = b.stepName;
        this.message   = b.message;
        this.level     = b.level != null ? b.level : LogLevel.INFO;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PipelineExecution execution;
        private String stepName;
        private String message;
        private LogLevel level;

        public Builder execution(PipelineExecution e)  { this.execution = e; return this; }
        public Builder stepName(String name)            { this.stepName = name; return this; }
        public Builder message(String msg)              { this.message = msg; return this; }
        public Builder level(LogLevel level)            { this.level = level; return this; }
        public StepLog build()                          { return new StepLog(this); }
    }

    // ---- Getters & Setters ----

    public Long getId()                           { return id; }
    public PipelineExecution getExecution()       { return execution; }
    public void setExecution(PipelineExecution e) { this.execution = e; }
    public String getStepName()                   { return stepName; }
    public void setStepName(String name)          { this.stepName = name; }
    public String getMessage()                    { return message; }
    public void setMessage(String msg)            { this.message = msg; }
    public LogLevel getLevel()                    { return level; }
    public void setLevel(LogLevel level)          { this.level = level; }
    public LocalDateTime getLoggedAt()            { return loggedAt; }
}
