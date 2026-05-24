package com.autorabit.pipeline.repository;

import com.autorabit.pipeline.model.PipelineExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineExecutionRepository extends JpaRepository<PipelineExecution, Long> {

    Page<PipelineExecution> findByPipelineId(Long pipelineId, Pageable pageable);

    List<PipelineExecution> findByStatus(PipelineExecution.ExecutionStatus status);

    Optional<PipelineExecution> findTopByPipelineIdOrderByBuildNumberDesc(Long pipelineId);

    @Query("SELECT COUNT(e) FROM PipelineExecution e WHERE e.status = :status")
    long countByStatus(@Param("status") PipelineExecution.ExecutionStatus status);

    @Query("SELECT COUNT(e) FROM PipelineExecution e WHERE e.status = 'SUCCESS'")
    long countSuccessful();

    @Query("SELECT COUNT(e) FROM PipelineExecution e WHERE e.status = 'FAILED'")
    long countFailed();

    @Query("SELECT COUNT(e) FROM PipelineExecution e WHERE e.status = 'RUNNING'")
    long countRunning();

    @Query("SELECT AVG(e.durationSeconds) FROM PipelineExecution e WHERE e.status = 'SUCCESS' AND e.durationSeconds IS NOT NULL")
    Double avgDurationSeconds();

    @Query("SELECT e FROM PipelineExecution e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<PipelineExecution> findRecentExecutions(@Param("since") LocalDateTime since);

    @Query("SELECT e FROM PipelineExecution e ORDER BY e.createdAt DESC")
    List<PipelineExecution> findLatest(Pageable pageable);
}
