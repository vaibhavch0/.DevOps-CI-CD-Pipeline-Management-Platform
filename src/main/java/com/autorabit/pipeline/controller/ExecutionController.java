package com.autorabit.pipeline.controller;

import com.autorabit.pipeline.dto.ApiResponse;
import com.autorabit.pipeline.dto.PipelineExecutionDTO;
import com.autorabit.pipeline.model.StepLog;
import com.autorabit.pipeline.repository.StepLogRepository;
import com.autorabit.pipeline.service.PipelineExecutionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pipeline Execution REST controller.
 *
 * POST /api/pipelines/{id}/trigger     — trigger a new execution
 * GET  /api/pipelines/{id}/executions  — list executions for a pipeline
 * GET  /api/executions/{id}            — get execution detail with steps
 * GET  /api/executions/{id}/logs       — get all logs for an execution
 * POST /api/executions/{id}/cancel     — cancel a running execution
 * GET  /api/executions/recent          — latest N executions across all pipelines
 */
@RestController
public class ExecutionController {

    private final PipelineExecutionService executionService;
    private final StepLogRepository stepLogRepository;

    public ExecutionController(PipelineExecutionService executionService, StepLogRepository stepLogRepository) {
        this.executionService = executionService;
        this.stepLogRepository = stepLogRepository;
    }

    // ---- Trigger ----

    @PostMapping("/api/pipelines/{pipelineId}/trigger")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<PipelineExecutionDTO>> triggerExecution(
            @PathVariable Long pipelineId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PipelineExecutionDTO execution = executionService.triggerExecution(
                pipelineId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Pipeline triggered successfully", execution));
    }

    // ---- Executions per Pipeline ----

    @GetMapping("/api/pipelines/{pipelineId}/executions")
    public ResponseEntity<ApiResponse<Page<PipelineExecutionDTO>>> getExecutionsByPipeline(
            @PathVariable Long pipelineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                executionService.getExecutionsByPipeline(pipelineId, pageable)));
    }

    // ---- Execution Detail ----

    @GetMapping("/api/executions/{executionId}")
    public ResponseEntity<ApiResponse<PipelineExecutionDTO>> getExecution(
            @PathVariable Long executionId) {
        return ResponseEntity.ok(ApiResponse.success(
                executionService.getExecutionById(executionId)));
    }

    // ---- Logs ----

    @GetMapping("/api/executions/{executionId}/logs")
    public ResponseEntity<ApiResponse<List<StepLog>>> getExecutionLogs(
            @PathVariable Long executionId) {
        List<StepLog> logs = stepLogRepository
                .findByExecutionIdOrderByLoggedAtAsc(executionId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    // ---- Cancel ----

    @PostMapping("/api/executions/{executionId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<Void>> cancelExecution(
            @PathVariable Long executionId) {
        executionService.cancelExecution(executionId);
        return ResponseEntity.ok(ApiResponse.success("Execution cancelled", null));
    }

    // ---- Recent ----

    @GetMapping("/api/executions/recent")
    public ResponseEntity<ApiResponse<List<PipelineExecutionDTO>>> getRecentExecutions(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                executionService.getRecentExecutions(Math.min(limit, 50))));
    }
}
