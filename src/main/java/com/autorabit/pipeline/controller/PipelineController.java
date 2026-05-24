package com.autorabit.pipeline.controller;

import com.autorabit.pipeline.dto.ApiResponse;
import com.autorabit.pipeline.dto.PipelineDTO;
import com.autorabit.pipeline.service.PipelineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pipeline CRUD REST controller.
 *
 * GET    /api/pipelines           — list all pipelines
 * GET    /api/pipelines/page      — paginated list
 * GET    /api/pipelines/{id}      — get pipeline by ID
 * POST   /api/pipelines           — create new pipeline (ADMIN/DEVELOPER)
 * PUT    /api/pipelines/{id}      — update pipeline (ADMIN/DEVELOPER)
 * DELETE /api/pipelines/{id}      — delete pipeline (ADMIN only)
 */
@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PipelineDTO>>> getAllPipelines() {
        return ResponseEntity.ok(ApiResponse.success(pipelineService.getAllPipelines()));
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<PipelineDTO>>> getPipelinesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(pipelineService.getPipelinesPage(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PipelineDTO>> getPipelineById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pipelineService.getPipelineById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<PipelineDTO>> createPipeline(
            @Valid @RequestBody PipelineDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        PipelineDTO created = pipelineService.createPipeline(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pipeline created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<PipelineDTO>> updatePipeline(
            @PathVariable Long id,
            @Valid @RequestBody PipelineDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pipeline updated", pipelineService.updatePipeline(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePipeline(@PathVariable Long id) {
        pipelineService.deletePipeline(id);
        return ResponseEntity.ok(ApiResponse.success("Pipeline deleted", null));
    }
}
