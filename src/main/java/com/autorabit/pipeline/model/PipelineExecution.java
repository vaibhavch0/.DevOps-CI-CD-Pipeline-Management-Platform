package com.autorabit.pipeline.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single run/instance of a Pipeline.
 * e.g., "Build #47 of Salesforce Deploy Pipeline"
 */
@Entity
@Table(name = "pipeline_executions")
public class PipelineExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @Column(name = "build_number", nullable = false)
    private Integer buildNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status = ExecutionStatus.QUEUED;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Column(name = "commit_sha", length = 40)
    private String commitSha;

    @Column(name = "commit_message", length = 500)
    private String commitMessage;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("stepOrder ASC")
    private List<PipelineStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StepLog> logs = new ArrayList<>();

    // ---- Enums ----

    public enum ExecutionStatus {
        QUEUED, RUNNING, SUCCESS, FAILED, CANCELLED, SKIPPED
    }

    // ---- Constructors ----

    public PipelineExecution() {}

    private PipelineExecution(Builder b) {
        this.pipeline      = b.pipeline;
        this.buildNumber   = b.buildNumber;
        this.status        = b.status != null ? b.status : ExecutionStatus.QUEUED;
        this.triggeredBy   = b.triggeredBy;
        this.commitSha     = b.commitSha;
        this.commitMessage = b.commitMessage;
        this.branchName    = b.branchName;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Pipeline pipeline;
        private Integer buildNumber;
        private ExecutionStatus status;
        private String triggeredBy;
        private String commitSha;
        private String commitMessage;
        private String branchName;

        public Builder pipeline(Pipeline p)         { this.pipeline = p; return this; }
        public Builder buildNumber(Integer n)       { this.buildNumber = n; return this; }
        public Builder status(ExecutionStatus s)    { this.status = s; return this; }
        public Builder triggeredBy(String u)        { this.triggeredBy = u; return this; }
        public Builder commitSha(String sha)        { this.commitSha = sha; return this; }
        public Builder commitMessage(String msg)    { this.commitMessage = msg; return this; }
        public Builder branchName(String branch)    { this.branchName = branch; return this; }
        public PipelineExecution build()            { return new PipelineExecution(this); }
    }

    // ---- Getters & Setters ----

    public Long getId()                            { return id; }
    public Pipeline getPipeline()                  { return pipeline; }
    public void setPipeline(Pipeline pipeline)     { this.pipeline = pipeline; }
    public Integer getBuildNumber()                { return buildNumber; }
    public void setBuildNumber(Integer n)          { this.buildNumber = n; }
    public ExecutionStatus getStatus()             { return status; }
    public void setStatus(ExecutionStatus s)       { this.status = s; }
    public String getTriggeredBy()                 { return triggeredBy; }
    public void setTriggeredBy(String u)           { this.triggeredBy = u; }
    public String getCommitSha()                   { return commitSha; }
    public void setCommitSha(String sha)           { this.commitSha = sha; }
    public String getCommitMessage()               { return commitMessage; }
    public void setCommitMessage(String msg)       { this.commitMessage = msg; }
    public String getBranchName()                  { return branchName; }
    public void setBranchName(String branch)       { this.branchName = branch; }
    public LocalDateTime getStartedAt()            { return startedAt; }
    public void setStartedAt(LocalDateTime t)      { this.startedAt = t; }
    public LocalDateTime getFinishedAt()           { return finishedAt; }
    public void setFinishedAt(LocalDateTime t)     { this.finishedAt = t; }
    public Long getDurationSeconds()               { return durationSeconds; }
    public void setDurationSeconds(Long d)         { this.durationSeconds = d; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public List<PipelineStep> getSteps()           { return steps; }
    public void setSteps(List<PipelineStep> steps) { this.steps = steps; }
    public List<StepLog> getLogs()                 { return logs; }
    public void setLogs(List<StepLog> logs)        { this.logs = logs; }
}
