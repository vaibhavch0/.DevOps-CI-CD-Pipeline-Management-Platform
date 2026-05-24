package com.autorabit.pipeline.dto;

import com.autorabit.pipeline.model.PipelineExecution;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard metrics DTO — pushed via SSE every 5 seconds.
 */
public class MetricsDTO {

    private long totalPipelines;
    private long totalExecutions;
    private long runningExecutions;
    private long successfulExecutions;
    private long failedExecutions;
    private long queuedExecutions;
    private double successRate;
    private double failureRate;
    private Double avgDurationSeconds;
    private List<DailyCount> dailyTrend;
    private List<ActiveExecution> activeExecutions;
    private LocalDateTime generatedAt;

    public MetricsDTO() {}

    private MetricsDTO(Builder b) {
        this.totalPipelines       = b.totalPipelines;
        this.totalExecutions      = b.totalExecutions;
        this.runningExecutions    = b.runningExecutions;
        this.successfulExecutions = b.successfulExecutions;
        this.failedExecutions     = b.failedExecutions;
        this.queuedExecutions     = b.queuedExecutions;
        this.successRate          = b.successRate;
        this.failureRate          = b.failureRate;
        this.avgDurationSeconds   = b.avgDurationSeconds;
        this.dailyTrend           = b.dailyTrend;
        this.activeExecutions     = b.activeExecutions;
        this.generatedAt          = b.generatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalPipelines, totalExecutions, runningExecutions;
        private long successfulExecutions, failedExecutions, queuedExecutions;
        private double successRate, failureRate;
        private Double avgDurationSeconds;
        private List<DailyCount> dailyTrend;
        private List<ActiveExecution> activeExecutions;
        private LocalDateTime generatedAt;

        public Builder totalPipelines(long n)           { this.totalPipelines = n; return this; }
        public Builder totalExecutions(long n)          { this.totalExecutions = n; return this; }
        public Builder runningExecutions(long n)        { this.runningExecutions = n; return this; }
        public Builder successfulExecutions(long n)     { this.successfulExecutions = n; return this; }
        public Builder failedExecutions(long n)         { this.failedExecutions = n; return this; }
        public Builder queuedExecutions(long n)         { this.queuedExecutions = n; return this; }
        public Builder successRate(double r)            { this.successRate = r; return this; }
        public Builder failureRate(double r)            { this.failureRate = r; return this; }
        public Builder avgDurationSeconds(Double d)     { this.avgDurationSeconds = d; return this; }
        public Builder dailyTrend(List<DailyCount> t)  { this.dailyTrend = t; return this; }
        public Builder activeExecutions(List<ActiveExecution> a) { this.activeExecutions = a; return this; }
        public Builder generatedAt(LocalDateTime t)    { this.generatedAt = t; return this; }
        public MetricsDTO build()                      { return new MetricsDTO(this); }
    }

    // ---- Nested: DailyCount ----

    public static class DailyCount {
        private String date;
        private long success;
        private long failed;
        private long total;

        public DailyCount() {}

        private DailyCount(Builder b) {
            this.date = b.date; this.success = b.success;
            this.failed = b.failed; this.total = b.total;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String date;
            private long success, failed, total;

            public Builder date(String d)    { this.date = d; return this; }
            public Builder success(long n)   { this.success = n; return this; }
            public Builder failed(long n)    { this.failed = n; return this; }
            public Builder total(long n)     { this.total = n; return this; }
            public DailyCount build()        { return new DailyCount(this); }
        }

        public String getDate()   { return date; }
        public long getSuccess()  { return success; }
        public long getFailed()   { return failed; }
        public long getTotal()    { return total; }
    }

    // ---- Nested: ActiveExecution ----

    public static class ActiveExecution {
        private Long executionId;
        private String pipelineName;
        private Integer buildNumber;
        private String currentStep;
        private String triggeredBy;
        private LocalDateTime startedAt;

        public ActiveExecution() {}

        private ActiveExecution(Builder b) {
            this.executionId  = b.executionId;
            this.pipelineName = b.pipelineName;
            this.buildNumber  = b.buildNumber;
            this.currentStep  = b.currentStep;
            this.triggeredBy  = b.triggeredBy;
            this.startedAt    = b.startedAt;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long executionId;
            private String pipelineName, currentStep, triggeredBy;
            private Integer buildNumber;
            private LocalDateTime startedAt;

            public Builder executionId(Long id)      { this.executionId = id; return this; }
            public Builder pipelineName(String n)    { this.pipelineName = n; return this; }
            public Builder buildNumber(Integer n)    { this.buildNumber = n; return this; }
            public Builder currentStep(String s)     { this.currentStep = s; return this; }
            public Builder triggeredBy(String u)     { this.triggeredBy = u; return this; }
            public Builder startedAt(LocalDateTime t){ this.startedAt = t; return this; }
            public ActiveExecution build()           { return new ActiveExecution(this); }
        }

        public Long getExecutionId()        { return executionId; }
        public String getPipelineName()     { return pipelineName; }
        public Integer getBuildNumber()     { return buildNumber; }
        public String getCurrentStep()      { return currentStep; }
        public String getTriggeredBy()      { return triggeredBy; }
        public LocalDateTime getStartedAt() { return startedAt; }
    }

    // ---- Getters ----

    public long getTotalPipelines()             { return totalPipelines; }
    public long getTotalExecutions()            { return totalExecutions; }
    public long getRunningExecutions()          { return runningExecutions; }
    public long getSuccessfulExecutions()       { return successfulExecutions; }
    public long getFailedExecutions()           { return failedExecutions; }
    public long getQueuedExecutions()           { return queuedExecutions; }
    public double getSuccessRate()              { return successRate; }
    public double getFailureRate()              { return failureRate; }
    public Double getAvgDurationSeconds()       { return avgDurationSeconds; }
    public List<DailyCount> getDailyTrend()     { return dailyTrend; }
    public List<ActiveExecution> getActiveExecutions() { return activeExecutions; }
    public LocalDateTime getGeneratedAt()       { return generatedAt; }
}
