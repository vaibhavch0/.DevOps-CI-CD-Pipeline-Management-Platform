package com.autorabit.pipeline.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a single step within a pipeline execution.
 * e.g., "Checkout → Build → Test → Static Analysis → Deploy"
 */
@Entity
@Table(name = "pipeline_steps")
public class PipelineStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private PipelineExecution execution;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(nullable = false)
    private String name;

    @Column(length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // ---- Enums ----

    public enum StepStatus {
        PENDING, RUNNING, SUCCESS, FAILED, SKIPPED
    }

    // ---- Constructors ----

    public PipelineStep() {}

    private PipelineStep(Builder b) {
        this.execution   = b.execution;
        this.stepOrder   = b.stepOrder;
        this.name        = b.name;
        this.description = b.description;
        this.status      = b.status != null ? b.status : StepStatus.PENDING;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PipelineExecution execution;
        private Integer stepOrder;
        private String name;
        private String description;
        private StepStatus status;

        public Builder execution(PipelineExecution e)  { this.execution = e; return this; }
        public Builder stepOrder(Integer order)         { this.stepOrder = order; return this; }
        public Builder name(String name)                { this.name = name; return this; }
        public Builder description(String desc)         { this.description = desc; return this; }
        public Builder status(StepStatus status)        { this.status = status; return this; }
        public PipelineStep build()                     { return new PipelineStep(this); }
    }

    // ---- Getters & Setters ----

    public Long getId()                            { return id; }
    public PipelineExecution getExecution()        { return execution; }
    public void setExecution(PipelineExecution e)  { this.execution = e; }
    public Integer getStepOrder()                  { return stepOrder; }
    public void setStepOrder(Integer n)            { this.stepOrder = n; }
    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }
    public String getDescription()                 { return description; }
    public void setDescription(String desc)        { this.description = desc; }
    public StepStatus getStatus()                  { return status; }
    public void setStatus(StepStatus s)            { this.status = s; }
    public LocalDateTime getStartedAt()            { return startedAt; }
    public void setStartedAt(LocalDateTime t)      { this.startedAt = t; }
    public LocalDateTime getFinishedAt()           { return finishedAt; }
    public void setFinishedAt(LocalDateTime t)     { this.finishedAt = t; }
    public Long getDurationSeconds()               { return durationSeconds; }
    public void setDurationSeconds(Long d)         { this.durationSeconds = d; }
    public String getErrorMessage()                { return errorMessage; }
    public void setErrorMessage(String msg)        { this.errorMessage = msg; }
}
