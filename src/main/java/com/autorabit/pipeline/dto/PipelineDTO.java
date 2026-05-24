package com.autorabit.pipeline.dto;

import com.autorabit.pipeline.model.Pipeline;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Pipeline create/update requests and responses.
 */
public class PipelineDTO {

    private Long id;

    @NotBlank(message = "Pipeline name is required")
    private String name;

    private String description;

    @NotNull(message = "Pipeline type is required")
    private Pipeline.PipelineType type;

    @NotBlank(message = "Repository URL is required")
    private String repository;

    private String branchName;
    private Pipeline.PipelineStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long totalExecutions;
    private long successCount;
    private long failureCount;
    private Double successRate;
    private Long lastBuildNumber;
    private List<String> stepNames;

    public PipelineDTO() {}

    private PipelineDTO(Builder b) {
        this.id             = b.id;
        this.name           = b.name;
        this.description    = b.description;
        this.type           = b.type;
        this.repository     = b.repository;
        this.branchName     = b.branchName;
        this.status         = b.status;
        this.createdBy      = b.createdBy;
        this.createdAt      = b.createdAt;
        this.updatedAt      = b.updatedAt;
        this.totalExecutions= b.totalExecutions;
        this.successCount   = b.successCount;
        this.failureCount   = b.failureCount;
        this.successRate    = b.successRate;
        this.lastBuildNumber= b.lastBuildNumber;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name, description, repository, branchName, createdBy;
        private Pipeline.PipelineType type;
        private Pipeline.PipelineStatus status;
        private LocalDateTime createdAt, updatedAt;
        private long totalExecutions, successCount, failureCount;
        private Double successRate;
        private Long lastBuildNumber;

        public Builder id(Long id)                          { this.id = id; return this; }
        public Builder name(String n)                       { this.name = n; return this; }
        public Builder description(String d)                { this.description = d; return this; }
        public Builder type(Pipeline.PipelineType t)        { this.type = t; return this; }
        public Builder repository(String r)                 { this.repository = r; return this; }
        public Builder branchName(String b)                 { this.branchName = b; return this; }
        public Builder status(Pipeline.PipelineStatus s)    { this.status = s; return this; }
        public Builder createdBy(String u)                  { this.createdBy = u; return this; }
        public Builder createdAt(LocalDateTime t)           { this.createdAt = t; return this; }
        public Builder updatedAt(LocalDateTime t)           { this.updatedAt = t; return this; }
        public Builder totalExecutions(long n)              { this.totalExecutions = n; return this; }
        public Builder successCount(long n)                 { this.successCount = n; return this; }
        public Builder failureCount(long n)                 { this.failureCount = n; return this; }
        public Builder successRate(Double r)                { this.successRate = r; return this; }
        public Builder lastBuildNumber(Long n)              { this.lastBuildNumber = n; return this; }
        public PipelineDTO build()                          { return new PipelineDTO(this); }
    }

    public static PipelineDTO fromPipeline(Pipeline p) {
        return PipelineDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .repository(p.getRepository())
                .branchName(p.getBranchName())
                .status(p.getStatus())
                .createdBy(p.getCreatedBy())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .totalExecutions(p.getExecutions() != null ? p.getExecutions().size() : 0)
                .build();
    }

    // ---- Getters & Setters ----

    public Long getId()                                     { return id; }
    public String getName()                                 { return name; }
    public void setName(String name)                        { this.name = name; }
    public String getDescription()                          { return description; }
    public void setDescription(String d)                    { this.description = d; }
    public Pipeline.PipelineType getType()                  { return type; }
    public void setType(Pipeline.PipelineType t)            { this.type = t; }
    public String getRepository()                           { return repository; }
    public void setRepository(String r)                     { this.repository = r; }
    public String getBranchName()                           { return branchName; }
    public void setBranchName(String b)                     { this.branchName = b; }
    public Pipeline.PipelineStatus getStatus()              { return status; }
    public void setStatus(Pipeline.PipelineStatus s)        { this.status = s; }
    public String getCreatedBy()                            { return createdBy; }
    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public LocalDateTime getUpdatedAt()                     { return updatedAt; }
    public long getTotalExecutions()                        { return totalExecutions; }
    public void setTotalExecutions(long n)                  { this.totalExecutions = n; }
    public long getSuccessCount()                           { return successCount; }
    public void setSuccessCount(long n)                     { this.successCount = n; }
    public long getFailureCount()                           { return failureCount; }
    public void setFailureCount(long n)                     { this.failureCount = n; }
    public Double getSuccessRate()                          { return successRate; }
    public void setSuccessRate(Double r)                    { this.successRate = r; }
    public Long getLastBuildNumber()                        { return lastBuildNumber; }
    public void setLastBuildNumber(Long n)                  { this.lastBuildNumber = n; }
    public List<String> getStepNames()                      { return stepNames; }
    public void setStepNames(List<String> s)                { this.stepNames = s; }
}
