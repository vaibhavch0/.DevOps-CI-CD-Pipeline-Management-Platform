package com.autorabit.pipeline.repository;

import com.autorabit.pipeline.model.StepLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StepLogRepository extends JpaRepository<StepLog, Long> {

    List<StepLog> findByExecutionIdOrderByLoggedAtAsc(Long executionId);

    List<StepLog> findByExecutionIdAndStepNameOrderByLoggedAtAsc(Long executionId, String stepName);

    @Query("SELECT l FROM StepLog l WHERE l.execution.id = :execId AND l.level = :level ORDER BY l.loggedAt ASC")
    List<StepLog> findByExecutionIdAndLevel(@Param("execId") Long executionId, @Param("level") StepLog.LogLevel level);

    @Query("SELECT l FROM StepLog l WHERE l.execution.id = :execId ORDER BY l.loggedAt DESC")
    List<StepLog> findLatestByExecutionId(@Param("execId") Long executionId, Pageable pageable);

    long countByExecutionId(Long executionId);
}
