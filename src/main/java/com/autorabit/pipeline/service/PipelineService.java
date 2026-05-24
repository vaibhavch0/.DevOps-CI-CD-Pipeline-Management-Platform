package com.autorabit.pipeline.service;

import com.autorabit.pipeline.dto.PipelineDTO;
import com.autorabit.pipeline.model.Pipeline;
import com.autorabit.pipeline.model.PipelineExecution;
import com.autorabit.pipeline.repository.PipelineExecutionRepository;
import com.autorabit.pipeline.repository.PipelineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for Pipeline CRUD operations and stats aggregation.
 */
@Service
public class PipelineService {

    private static final Logger log = LoggerFactory.getLogger(PipelineService.class);

    private final PipelineRepository pipelineRepository;
    private final PipelineExecutionRepository executionRepository;

    public PipelineService(PipelineRepository pipelineRepository, PipelineExecutionRepository executionRepository) {
        this.pipelineRepository = pipelineRepository;
        this.executionRepository = executionRepository;
    }

    // ---- CRUD ----

    @Transactional(readOnly = true)
    public List<PipelineDTO> getAllPipelines() {
        return pipelineRepository.findAll().stream()
                .map(this::enrichWithStats)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PipelineDTO> getPipelinesPage(Pageable pageable) {
        return pipelineRepository.findAll(pageable).map(this::enrichWithStats);
    }

    @Transactional(readOnly = true)
    public PipelineDTO getPipelineById(Long id) {
        Pipeline pipeline = findOrThrow(id);
        return enrichWithStats(pipeline);
    }

    @Transactional
    public PipelineDTO createPipeline(PipelineDTO dto, String createdBy) {
        if (pipelineRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Pipeline with name '" + dto.getName() + "' already exists");
        }

        Pipeline pipeline = Pipeline.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .type(dto.getType())
                .repository(dto.getRepository())
                .branchName(dto.getBranchName() != null ? dto.getBranchName() : "main")
                .status(Pipeline.PipelineStatus.IDLE)
                .createdBy(createdBy)
                .build();

        Pipeline saved = pipelineRepository.save(pipeline);
        log.info("Created pipeline: {} [id={}]", saved.getName(), saved.getId());
        return PipelineDTO.fromPipeline(saved);
    }

    @Transactional
    public PipelineDTO updatePipeline(Long id, PipelineDTO dto) {
        Pipeline pipeline = findOrThrow(id);
        pipeline.setName(dto.getName());
        pipeline.setDescription(dto.getDescription());
        pipeline.setRepository(dto.getRepository());
        pipeline.setBranchName(dto.getBranchName());
        Pipeline saved = pipelineRepository.save(pipeline);
        return enrichWithStats(saved);
    }

    @Transactional
    public void deletePipeline(Long id) {
        Pipeline pipeline = findOrThrow(id);
        pipelineRepository.delete(pipeline);
        log.info("Deleted pipeline: {} [id={}]", pipeline.getName(), id);
    }

    @Transactional
    public void updatePipelineStatus(Long id, Pipeline.PipelineStatus status) {
        Pipeline pipeline = findOrThrow(id);
        pipeline.setStatus(status);
        pipelineRepository.save(pipeline);
    }

    // ---- Stats Enrichment ----

    private PipelineDTO enrichWithStats(Pipeline p) {
        PipelineDTO dto = PipelineDTO.fromPipeline(p);

        long total = executionRepository.countByStatus(PipelineExecution.ExecutionStatus.SUCCESS)
                   + executionRepository.countByStatus(PipelineExecution.ExecutionStatus.FAILED)
                   + executionRepository.countByStatus(PipelineExecution.ExecutionStatus.CANCELLED);

        // Per-pipeline counts
        var executions = executionRepository.findByPipelineId(p.getId(), Pageable.unpaged());
        long pTotal = executions.getTotalElements();
        long pSuccess = executions.stream()
                .filter(e -> e.getStatus() == PipelineExecution.ExecutionStatus.SUCCESS).count();
        long pFailed = executions.stream()
                .filter(e -> e.getStatus() == PipelineExecution.ExecutionStatus.FAILED).count();

        dto.setTotalExecutions(pTotal);
        dto.setSuccessCount(pSuccess);
        dto.setFailureCount(pFailed);
        dto.setSuccessRate(pTotal > 0 ? (pSuccess * 100.0 / pTotal) : 0.0);

        executionRepository.findTopByPipelineIdOrderByBuildNumberDesc(p.getId())
                .ifPresent(e -> dto.setLastBuildNumber(e.getBuildNumber().longValue()));

        return dto;
    }

    public Pipeline findOrThrow(Long id) {
        return pipelineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pipeline not found with id: " + id));
    }
}
