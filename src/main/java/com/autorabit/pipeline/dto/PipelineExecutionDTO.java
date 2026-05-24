package com.autorabit.pipeline.dto;

import com.autorabit.pipeline.model.PipelineExecution;
import com.autorabit.pipeline.model.PipelineStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for PipelineExecution — used in list views and detail views.
 */
public class PipelineExecutionDTO {

    private Long id;
    private Long pipelineId;
    private String pipelineName;
    private Integer buildNumber;
    private PipelineExecution.ExecutionStatus status;
    private String triggeredBy;
    private String commitSha;
    private String commitMessage;
    private String branchName;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationSeconds;
    private LocalDateTime createdAt;
    private List<StepDTO> steps;

    public PipelineExecutionDTO() {}

    private PipelineExecutionDTO(Builder b) {
        this.id              = b.id;
        this.pipelineId      = b.pipelineId;
        this.pipelineName    = b.pipelineName;
        this.buildNumber     = b.buildNumber;
        this.status          = b.status;
        this.triggeredBy     = b.triggeredBy;
        this.commitSha       = b.commitSha;
        this.commitMessage   = b.commitMessage;
        this.branchName      = b.branchName;
        this.startedAt       = b.startedAt;
        this.finishedAt      = b.finishedAt;
        this.durationSeconds = b.durationSeconds;
        this.createdAt       = b.createdAt;
        this.steps           = b.steps;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id, pipelineId;
        private String pipelineName, triggeredBy, commitSha, commitMessage, branchName;
        private Integer buildNumber;
        private PipelineExecution.ExecutionStatus status;
        private LocalDateTime startedAt, finishedAt, createdAt;
        private Long durationSeconds;
        private List<StepDTO> steps;

        public Builder id(Long id)                                     { this.id = id; return this; }
        public Builder pipelineId(Long pid)                            { this.pipelineId = pid; return this; }
        public Builder pipelineName(String n)                          { this.pipelineName = n; return this; }
        public Builder buildNumber(Integer n)                          { this.buildNumber = n; return this; }
        public Builder status(PipelineExecution.ExecutionStatus s)     { this.status = s; return this; }
        public Builder triggeredBy(String u)                           { this.triggeredBy = u; return this; }
        public Builder commitSha(String sha)                           { this.commitSha = sha; return this; }
        public Builder commitMessage(String msg)                       { this.commitMessage = msg; return this; }
        public Builder branchName(String b)                            { this.branchName = b; return this; }
        public Builder startedAt(LocalDateTime t)                      { this.startedAt = t; return this; }
        public Builder finishedAt(LocalDateTime t)                     { this.finishedAt = t; return this; }
        public Builder durationSeconds(Long d)                         { this.durationSeconds = d; return this; }
        public Builder createdAt(LocalDateTime t)                      { this.createdAt = t; return this; }
        public Builder steps(List<StepDTO> steps)                      { this.steps = steps; return this; }
        public PipelineExecutionDTO build()                            { return new PipelineExecutionDTO(this); }
    }

    // ---- Nested StepDTO ----

    public static class StepDTO {
        private Long id;
        private Integer stepOrder;
        private String name;
        private String description;
        private PipelineStep.StepStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private Long durationSeconds;
        private String errorMessage;

        public StepDTO() {}

        private StepDTO(Builder b) {
            this.id              = b.id;
            this.stepOrder       = b.stepOrder;
            this.name            = b.name;
            this.description     = b.description;
            this.status          = b.status;
            this.startedAt       = b.startedAt;
            this.finishedAt      = b.finishedAt;
            this.durationSeconds = b.durationSeconds;
            this.errorMessage    = b.errorMessage;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Integer stepOrder;
            private String name, description, errorMessage;
            private PipelineStep.StepStatus status;
            private LocalDateTime startedAt, finishedAt;
            private Long durationSeconds;

            public Builder id(Long id)                              { this.id = id; return this; }
            public Builder stepOrder(Integer o)                     { this.stepOrder = o; return this; }
            public Builder name(String n)                           { this.name = n; return this; }
            public Builder description(String d)                    { this.description = d; return this; }
            public Builder status(PipelineStep.StepStatus s)        { this.status = s; return this; }
            public Builder startedAt(LocalDateTime t)               { this.startedAt = t; return this; }
            public Builder finishedAt(LocalDateTime t)              { this.finishedAt = t; return this; }
            public Builder durationSeconds(Long d)                  { this.durationSeconds = d; return this; }
            public Builder errorMessage(String m)                   { this.errorMessage = m; return this; }
            public StepDTO build()                                  { return new StepDTO(this); }
        }

        public Long getId()                  { return id; }
        public Integer getStepOrder()        { return stepOrder; }
        public String getName()              { return name; }
        public String getDescription()       { return description; }
        public PipelineStep.StepStatus getStatus() { return status; }
        public LocalDateTime getStartedAt()  { return startedAt; }
        public LocalDateTime getFinishedAt() { return finishedAt; }
        public Long getDurationSeconds()     { return durationSeconds; }
        public String getErrorMessage()      { return errorMessage; }
    }

    // ---- Static factory ----

    public static PipelineExecutionDTO fromExecution(PipelineExecution e) {
        List<StepDTO> stepDTOs = e.getSteps() == null ? List.of() :
                e.getSteps().stream().map(s -> StepDTO.builder()
                        .id(s.getId())
                        .stepOrder(s.getStepOrder())
                        .name(s.getName())
                        .description(s.getDescription())
                        .status(s.getStatus())
                        .startedAt(s.getStartedAt())
                        .finishedAt(s.getFinishedAt())
                        .durationSeconds(s.getDurationSeconds())
                        .errorMessage(s.getErrorMessage())
                        .build()).collect(Collectors.toList());

        return PipelineExecutionDTO.builder()
                .id(e.getId())
                .pipelineId(e.getPipeline() != null ? e.getPipeline().getId() : null)
                .pipelineName(e.getPipeline() != null ? e.getPipeline().getName() : null)
                .buildNumber(e.getBuildNumber())
                .status(e.getStatus())
                .triggeredBy(e.getTriggeredBy())
                .commitSha(e.getCommitSha())
                .commitMessage(e.getCommitMessage())
                .branchName(e.getBranchName())
                .startedAt(e.getStartedAt())
                .finishedAt(e.getFinishedAt())
                .durationSeconds(e.getDurationSeconds())
                .createdAt(e.getCreatedAt())
                .steps(stepDTOs)
                .build();
    }

    // ---- Getters ----

    public Long getId()                               { return id; }
    public Long getPipelineId()                       { return pipelineId; }
    public String getPipelineName()                   { return pipelineName; }
    public Integer getBuildNumber()                   { return buildNumber; }
    public PipelineExecution.ExecutionStatus getStatus() { return status; }
    public String getTriggeredBy()                    { return triggeredBy; }
    public String getCommitSha()                      { return commitSha; }
    public String getCommitMessage()                  { return commitMessage; }
    public String getBranchName()                     { return branchName; }
    public LocalDateTime getStartedAt()               { return startedAt; }
    public LocalDateTime getFinishedAt()              { return finishedAt; }
    public Long getDurationSeconds()                  { return durationSeconds; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
    public List<StepDTO> getSteps()                   { return steps; }
}
