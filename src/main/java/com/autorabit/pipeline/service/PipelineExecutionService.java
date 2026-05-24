package com.autorabit.pipeline.service;

import com.autorabit.pipeline.dto.LogMessageDTO;
import com.autorabit.pipeline.dto.NotificationDTO;
import com.autorabit.pipeline.dto.PipelineExecutionDTO;
import com.autorabit.pipeline.model.*;
import com.autorabit.pipeline.repository.PipelineExecutionRepository;
import com.autorabit.pipeline.repository.StepLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core pipeline execution engine.
 *
 * When a pipeline is triggered:
 *   1. Creates a PipelineExecution record with steps
 *   2. Runs each step asynchronously (on pipelineExecutor thread pool)
 *   3. Streams real-time logs via WebSocket after each log line
 *   4. Updates execution status and broadcasts notifications
 */
@Service
public class PipelineExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PipelineExecutionService.class);

    private final PipelineExecutionRepository executionRepository;
    private final StepLogRepository stepLogRepository;
    private final PipelineService pipelineService;
    private final WebSocketNotificationService wsNotifier;

    @Value("${app.pipeline.execution.step-delay-ms:1500}")
    private long stepDelayMs;

    @Value("${app.pipeline.execution.log-interval-ms:500}")
    private long logIntervalMs;

    public PipelineExecutionService(PipelineExecutionRepository executionRepository,
                                    StepLogRepository stepLogRepository,
                                    PipelineService pipelineService,
                                    WebSocketNotificationService wsNotifier) {
        this.executionRepository = executionRepository;
        this.stepLogRepository = stepLogRepository;
        this.pipelineService = pipelineService;
        this.wsNotifier = wsNotifier;
    }

    // ---- Step definitions per pipeline type ----
    private static final Map<Pipeline.PipelineType, List<String[]>> STEP_DEFINITIONS = Map.of(
        Pipeline.PipelineType.SALESFORCE_DEPLOY, List.of(
            new String[]{"Source Checkout",       "Clone repository and checkout branch"},
            new String[]{"Validate Credentials",  "Authenticate with Salesforce org"},
            new String[]{"Run Apex Tests",         "Execute all Apex test classes"},
            new String[]{"Static Code Analysis",   "Run PMD & CodeScan rules"},
            new String[]{"Package Components",     "Bundle metadata components"},
            new String[]{"Deploy to Target Org",   "Push components to Salesforce"},
            new String[]{"Post-Deploy Validation", "Verify deployment success"}
        ),
        Pipeline.PipelineType.METADATA_BACKUP, List.of(
            new String[]{"Connect to Org",         "Establish Salesforce connection"},
            new String[]{"Retrieve Metadata",      "Download all metadata types"},
            new String[]{"Compare with Previous",  "Diff against last backup"},
            new String[]{"Commit Changes",         "Commit delta to git repository"},
            new String[]{"Push to Remote",         "Push backup to remote repository"},
            new String[]{"Generate Report",        "Create backup summary report"}
        ),
        Pipeline.PipelineType.CODE_QUALITY_SCAN, List.of(
            new String[]{"Checkout PR Branch",     "Fetch pull request changes"},
            new String[]{"Run PMD Analysis",       "Apex PMD static analysis"},
            new String[]{"Check Code Coverage",    "Validate 75%+ test coverage"},
            new String[]{"Security Scan",          "SAST vulnerability scan"},
            new String[]{"Lint & Format",          "Code style enforcement"},
            new String[]{"Publish Report",         "Post quality report to PR"}
        ),
        Pipeline.PipelineType.DATA_MIGRATION, List.of(
            new String[]{"Validate Source",        "Connect & validate source org"},
            new String[]{"Extract Data",           "Query and export data"},
            new String[]{"Mask PII Fields",        "Anonymize sensitive data"},
            new String[]{"Transform Schema",       "Map source to target schema"},
            new String[]{"Load to Target",         "Import data into target org"},
            new String[]{"Reconcile Records",      "Verify record counts match"}
        ),
        Pipeline.PipelineType.RELEASE_MANAGER, List.of(
            new String[]{"Create Release Branch",  "Branch from develop"},
            new String[]{"Run Regression Suite",   "Full test suite execution"},
            new String[]{"Security Gate",          "Security compliance check"},
            new String[]{"Staging Deploy",         "Deploy to staging environment"},
            new String[]{"Smoke Tests",            "Critical path smoke testing"},
            new String[]{"Smoke Tests",            "Critical path smoke testing"},
            new String[]{"Production Deploy",      "Blue/green production deploy"},
            new String[]{"Post-Release Monitor",   "Monitor error rates & alerts"}
        )
    );

    // ---- Trigger Execution ----

    @Transactional
    public PipelineExecutionDTO triggerExecution(Long pipelineId, String triggeredBy) {
        Pipeline pipeline = pipelineService.findOrThrow(pipelineId);

        // Check if already running
        List<PipelineExecution> running = executionRepository.findByStatus(
                PipelineExecution.ExecutionStatus.RUNNING);
        boolean alreadyRunning = running.stream()
                .anyMatch(e -> e.getPipeline().getId().equals(pipelineId));
        if (alreadyRunning) {
            throw new IllegalStateException("Pipeline '" + pipeline.getName() + "' is already running");
        }

        // Build number
        int buildNumber = executionRepository
                .findTopByPipelineIdOrderByBuildNumberDesc(pipelineId)
                .map(e -> e.getBuildNumber() + 1)
                .orElse(1);

        // Create execution record
        PipelineExecution execution = PipelineExecution.builder()
                .pipeline(pipeline)
                .buildNumber(buildNumber)
                .status(PipelineExecution.ExecutionStatus.QUEUED)
                .triggeredBy(triggeredBy)
                .branchName(pipeline.getBranchName())
                .commitSha(generateFakeCommitSha())
                .commitMessage(generateFakeCommitMessage())
                .build();

        // Attach steps
        List<String[]> stepDefs = STEP_DEFINITIONS.getOrDefault(
                pipeline.getType(), STEP_DEFINITIONS.get(Pipeline.PipelineType.SALESFORCE_DEPLOY));

        List<PipelineStep> steps = new ArrayList<>();
        for (int i = 0; i < stepDefs.size(); i++) {
            String[] def = stepDefs.get(i);
            steps.add(PipelineStep.builder()
                    .execution(execution)
                    .stepOrder(i + 1)
                    .name(def[0])
                    .description(def[1])
                    .status(PipelineStep.StepStatus.PENDING)
                    .build());
        }
        execution.setSteps(steps);

        PipelineExecution saved = executionRepository.save(execution);

        // Update pipeline status
        pipelineService.updatePipelineStatus(pipelineId, Pipeline.PipelineStatus.RUNNING);

        // Run async
        runExecutionAsync(saved.getId());

        log.info("Triggered pipeline '{}' → Execution #{} [id={}]",
                pipeline.getName(), buildNumber, saved.getId());

        return PipelineExecutionDTO.fromExecution(saved);
    }

    @Async("pipelineExecutor")
    public void runExecutionAsync(Long executionId) {
        try {
            PipelineExecution execution = executionRepository.findById(executionId)
                    .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));

            execution.setStatus(PipelineExecution.ExecutionStatus.RUNNING);
            execution.setStartedAt(LocalDateTime.now());
            executionRepository.save(execution);

            wsNotifier.sendStatusUpdate(executionId, PipelineExecution.ExecutionStatus.RUNNING);
            wsNotifier.sendNotification(NotificationDTO.pipelineStarted(
                    executionId,
                    execution.getPipeline().getName(),
                    execution.getBuildNumber()
            ));

            emitLog(execution, "SYSTEM", "🚀 Pipeline execution started — Build #" + execution.getBuildNumber(), StepLog.LogLevel.INFO);
            emitLog(execution, "SYSTEM", "📌 Repository: " + execution.getPipeline().getRepository(), StepLog.LogLevel.INFO);
            emitLog(execution, "SYSTEM", "🌿 Branch: " + execution.getBranchName(), StepLog.LogLevel.INFO);
            emitLog(execution, "SYSTEM", "🔖 Commit: " + execution.getCommitSha(), StepLog.LogLevel.INFO);

            boolean overallSuccess = true;

            // Execute each step sequentially
            List<PipelineStep> steps = execution.getSteps();
            for (PipelineStep step : steps) {
                boolean stepSuccess = executeStep(execution, step);
                if (!stepSuccess) {
                    overallSuccess = false;
                    // Mark remaining steps as SKIPPED
                    for (PipelineStep remaining : steps) {
                        if (remaining.getStatus() == PipelineStep.StepStatus.PENDING) {
                            remaining.setStatus(PipelineStep.StepStatus.SKIPPED);
                            executionRepository.save(execution);
                        }
                    }
                    break;
                }
            }

            // Finalize execution
            execution = executionRepository.findById(executionId).orElseThrow();
            LocalDateTime finishedAt = LocalDateTime.now();
            execution.setFinishedAt(finishedAt);
            execution.setDurationSeconds(
                    java.time.Duration.between(execution.getStartedAt(), finishedAt).getSeconds()
            );

            if (overallSuccess) {
                execution.setStatus(PipelineExecution.ExecutionStatus.SUCCESS);
                emitLog(execution, "SYSTEM",
                        "✅ Pipeline completed successfully in " + execution.getDurationSeconds() + "s",
                        StepLog.LogLevel.SUCCESS);
                wsNotifier.sendNotification(NotificationDTO.pipelineCompleted(
                        executionId, execution.getPipeline().getName(), execution.getBuildNumber()));
                pipelineService.updatePipelineStatus(
                        execution.getPipeline().getId(), Pipeline.PipelineStatus.SUCCESS);
            } else {
                execution.setStatus(PipelineExecution.ExecutionStatus.FAILED);
                emitLog(execution, "SYSTEM", "❌ Pipeline FAILED", StepLog.LogLevel.ERROR);
                wsNotifier.sendNotification(NotificationDTO.pipelineFailed(
                        executionId, execution.getPipeline().getName(), execution.getBuildNumber()));
                pipelineService.updatePipelineStatus(
                        execution.getPipeline().getId(), Pipeline.PipelineStatus.FAILED);
            }

            executionRepository.save(execution);
            wsNotifier.sendStatusUpdate(executionId, execution.getStatus());
            wsNotifier.triggerMetricsRefresh();

        } catch (Exception e) {
            log.error("Fatal error in pipeline execution {}: {}", executionId, e.getMessage(), e);
        }
    }

    private boolean executeStep(PipelineExecution execution, PipelineStep step) {
        step.setStatus(PipelineStep.StepStatus.RUNNING);
        step.setStartedAt(LocalDateTime.now());
        executionRepository.save(execution);

        String stepName = step.getName();
        emitLog(execution, stepName, "▶ Starting: " + stepName, StepLog.LogLevel.INFO);
        emitLog(execution, stepName, "  " + step.getDescription(), StepLog.LogLevel.DEBUG);

        // Simulate step logs
        List<String> stepLogs = generateStepLogs(stepName, execution.getPipeline().getType());
        for (String logLine : stepLogs) {
            sleep(logIntervalMs + randomDelay(200));
            StepLog.LogLevel level = detectLevel(logLine);
            emitLog(execution, stepName, logLine, level);
        }

        sleep(stepDelayMs);

        // Simulate ~10% failure rate (deterministic based on execution ID to be consistent)
        boolean shouldFail = (execution.getId() % 11 == 0) &&
                step.getStepOrder() == 3 &&
                execution.getPipeline().getType() == Pipeline.PipelineType.CODE_QUALITY_SCAN;

        if (shouldFail) {
            step.setStatus(PipelineStep.StepStatus.FAILED);
            step.setFinishedAt(LocalDateTime.now());
            step.setDurationSeconds(
                    java.time.Duration.between(step.getStartedAt(), step.getFinishedAt()).getSeconds());
            step.setErrorMessage("Code coverage is below the required 75% threshold");
            emitLog(execution, stepName, "✗ FAILED: " + step.getErrorMessage(), StepLog.LogLevel.ERROR);
            executionRepository.save(execution);
            return false;
        }

        step.setStatus(PipelineStep.StepStatus.SUCCESS);
        step.setFinishedAt(LocalDateTime.now());
        step.setDurationSeconds(
                java.time.Duration.between(step.getStartedAt(), step.getFinishedAt()).getSeconds());
        emitLog(execution, stepName, "✓ " + stepName + " completed successfully", StepLog.LogLevel.SUCCESS);
        executionRepository.save(execution);
        return true;
    }

    // ---- Log Helpers ----

    private void emitLog(PipelineExecution execution, String stepName, String message, StepLog.LogLevel level) {
        StepLog log = StepLog.builder()
                .execution(execution)
                .stepName(stepName)
                .message(message)
                .level(level)
                .build();
        stepLogRepository.save(log);

        LogMessageDTO dto = LogMessageDTO.builder()
                .executionId(execution.getId())
                .stepName(stepName)
                .message(message)
                .level(level)
                .timestamp(LocalDateTime.now())
                .build();
        wsNotifier.sendLog(execution.getId(), dto);
    }

    private List<String> generateStepLogs(String stepName, Pipeline.PipelineType type) {
        // Realistic simulated log lines per step type
        Map<String, List<String>> logMap = new HashMap<>();
        logMap.put("Source Checkout", List.of(
            "  Cloning repository... done",
            "  Checking out branch: " + randomBranch(),
            "  HEAD is now at " + randomSha(),
            "  Submodules initialized",
            "  Workspace prepared"
        ));
        logMap.put("Validate Credentials", List.of(
            "  Connecting to Salesforce org...",
            "  OAuth2 token refresh successful",
            "  Org: prod.salesforce.com",
            "  API version: 59.0",
            "  Connected App: AutoRABIT Deploy Agent",
            "  Permissions validated ✓"
        ));
        logMap.put("Run Apex Tests", List.of(
            "  Discovering test classes...",
            "  Found 128 test classes (847 methods)",
            "  Running test suite asynchronously...",
            "  [████████████████████] 100%",
            "  Tests passed: 843 / 847",
            "  Test failures: 0",
            "  Code coverage: 82.4%"
        ));
        logMap.put("Static Code Analysis", List.of(
            "  Loading PMD ruleset: AutoRABIT-Standard.xml",
            "  Scanning 1,204 Apex classes...",
            "  Scanning 342 LWC components...",
            "  Issues found: 3 (2 warnings, 1 info)",
            "  No critical violations",
            "  CodeScan score: 94/100"
        ));
        logMap.put("Package Components", List.of(
            "  Resolving metadata dependencies...",
            "  Packaging 256 components",
            "  CustomObjects: 47  Flows: 12  Triggers: 8",
            "  ApexClasses: 128  LWC: 61",
            "  Package validation: PASSED",
            "  Package size: 24.6 MB"
        ));
        logMap.put("Deploy to Target Org", List.of(
            "  Initiating deployment to PRODUCTION...",
            "  Deployment ID: 0Af8Z000003hMxASAU",
            "  Uploading package...",
            "  [████████████████████] 100%",
            "  Running Apex tests in target...",
            "  Deployment Status: SUCCEEDED"
        ));
        logMap.put("Post-Deploy Validation", List.of(
            "  Verifying component deployment...",
            "  All 256 components verified",
            "  Running smoke tests...",
            "  Health check: PASSED",
            "  Deployment verified ✓"
        ));
        logMap.put("Check Code Coverage", List.of(
            "  Fetching coverage data...",
            "  Org-wide coverage: 78.2%",
            "  Minimum required: 75%",
            "  Coverage requirement: MET ✓"
        ));
        logMap.put("Security Scan", List.of(
            "  Running SAST analysis...",
            "  Checking for SOQL injection patterns...",
            "  Checking for XSS vulnerabilities...",
            "  Checking for insecure data exposure...",
            "  Critical issues: 0",
            "  Security scan: PASSED ✓"
        ));

        return logMap.getOrDefault(stepName, List.of(
            "  Initializing " + stepName + "...",
            "  Processing...",
            "  [████████████████████] 100%",
            "  Completed"
        ));
    }

    private StepLog.LogLevel detectLevel(String message) {
        if (message.contains("FAILED") || message.contains("ERROR") || message.contains("✗"))
            return StepLog.LogLevel.ERROR;
        if (message.contains("WARN") || message.contains("warning"))
            return StepLog.LogLevel.WARN;
        if (message.contains("✓") || message.contains("PASSED") || message.contains("SUCCEEDED") || message.contains("SUCCESS"))
            return StepLog.LogLevel.SUCCESS;
        if (message.startsWith("  ") && !message.contains("✓"))
            return StepLog.LogLevel.DEBUG;
        return StepLog.LogLevel.INFO;
    }

    // ---- Query Methods ----

    @Transactional(readOnly = true)
    public Page<PipelineExecutionDTO> getExecutionsByPipeline(Long pipelineId, Pageable pageable) {
        return executionRepository.findByPipelineId(pipelineId, pageable)
                .map(PipelineExecutionDTO::fromExecution);
    }

    @Transactional(readOnly = true)
    public PipelineExecutionDTO getExecutionById(Long executionId) {
        PipelineExecution exec = executionRepository.findById(executionId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Execution not found: " + executionId));
        return PipelineExecutionDTO.fromExecution(exec);
    }

    @Transactional(readOnly = true)
    public List<PipelineExecutionDTO> getRecentExecutions(int limit) {
        return executionRepository.findLatest(PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(PipelineExecutionDTO::fromExecution)
                .toList();
    }

    @Transactional
    public void cancelExecution(Long executionId) {
        PipelineExecution exec = executionRepository.findById(executionId)
                .orElseThrow();
        if (exec.getStatus() == PipelineExecution.ExecutionStatus.RUNNING ||
                exec.getStatus() == PipelineExecution.ExecutionStatus.QUEUED) {
            exec.setStatus(PipelineExecution.ExecutionStatus.CANCELLED);
            exec.setFinishedAt(LocalDateTime.now());
            executionRepository.save(exec);
            wsNotifier.sendStatusUpdate(executionId, PipelineExecution.ExecutionStatus.CANCELLED);
            pipelineService.updatePipelineStatus(
                    exec.getPipeline().getId(), Pipeline.PipelineStatus.CANCELLED);
        }
    }

    // ---- Utilities ----

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private long randomDelay(int maxMs) {
        return (long) (Math.random() * maxMs);
    }

    private String generateFakeCommitSha() {
        return Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 8)
             + Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 32)
                    .substring(0, 32).substring(0, 32);
    }

    private String generateFakeCommitMessage() {
        String[] messages = {
            "feat: Add validation for Salesforce metadata components",
            "fix: Resolve SOQL query optimization in AccountService",
            "refactor: Improve test coverage for OrderTrigger",
            "feat: Implement LWC for opportunity dashboard",
            "fix: Handle null pointer in ContactBatch class",
            "chore: Update API version to 59.0",
            "feat: Add custom metadata type for config management"
        };
        return messages[(int)(Math.random() * messages.length)];
    }

    private String randomSha() {
        return Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 7);
    }

    private String randomBranch() {
        String[] branches = {"main", "develop", "release/2.0", "feature/lwc-dashboard"};
        return branches[(int)(Math.random() * branches.length)];
    }
}
