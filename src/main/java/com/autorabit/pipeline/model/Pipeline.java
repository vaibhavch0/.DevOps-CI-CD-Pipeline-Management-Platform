package com.autorabit.pipeline.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a CI/CD Pipeline definition (e.g., "Salesforce Deploy Pipeline").
 * A Pipeline has many PipelineExecutions (runs).
 */
@Entity
@Table(name = "pipelines")
public class Pipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineType type = PipelineType.SALESFORCE_DEPLOY;

    @Column(nullable = false)
    private String repository;

    @Column(name = "branch_name", nullable = false)
    private String branchName = "main";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineStatus status = PipelineStatus.IDLE;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PipelineExecution> executions = new ArrayList<>();

    // ---- Enums ----

    public enum PipelineType {
        SALESFORCE_DEPLOY,
        METADATA_BACKUP,
        CODE_QUALITY_SCAN,
        DATA_MIGRATION,
        RELEASE_MANAGER
    }

    public enum PipelineStatus {
        IDLE,
        RUNNING,
        SUCCESS,
        FAILED,
        CANCELLED
    }

    // ---- Constructors ----

    public Pipeline() {}

    private Pipeline(Builder builder) {
        this.name        = builder.name;
        this.description = builder.description;
        this.type        = builder.type != null ? builder.type : PipelineType.SALESFORCE_DEPLOY;
        this.repository  = builder.repository;
        this.branchName  = builder.branchName != null ? builder.branchName : "main";
        this.status      = builder.status != null ? builder.status : PipelineStatus.IDLE;
        this.createdBy   = builder.createdBy;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String description;
        private PipelineType type;
        private String repository;
        private String branchName;
        private PipelineStatus status;
        private String createdBy;

        public Builder name(String name)               { this.name = name; return this; }
        public Builder description(String desc)         { this.description = desc; return this; }
        public Builder type(PipelineType type)          { this.type = type; return this; }
        public Builder repository(String repo)          { this.repository = repo; return this; }
        public Builder branchName(String branch)        { this.branchName = branch; return this; }
        public Builder status(PipelineStatus status)    { this.status = status; return this; }
        public Builder createdBy(String user)           { this.createdBy = user; return this; }
        public Pipeline build()                         { return new Pipeline(this); }
    }

    // ---- Getters & Setters ----

    public Long getId()                         { return id; }
    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }
    public String getDescription()              { return description; }
    public void setDescription(String desc)     { this.description = desc; }
    public PipelineType getType()               { return type; }
    public void setType(PipelineType type)      { this.type = type; }
    public String getRepository()               { return repository; }
    public void setRepository(String repo)      { this.repository = repo; }
    public String getBranchName()               { return branchName; }
    public void setBranchName(String branch)    { this.branchName = branch; }
    public PipelineStatus getStatus()           { return status; }
    public void setStatus(PipelineStatus status){ this.status = status; }
    public String getCreatedBy()                { return createdBy; }
    public void setCreatedBy(String user)       { this.createdBy = user; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public List<PipelineExecution> getExecutions() { return executions; }
    public void setExecutions(List<PipelineExecution> executions) { this.executions = executions; }
}
