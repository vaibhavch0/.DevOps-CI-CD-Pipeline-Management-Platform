package com.autorabit.pipeline.repository;

import com.autorabit.pipeline.model.Pipeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    Optional<Pipeline> findByName(String name);

    List<Pipeline> findByStatus(Pipeline.PipelineStatus status);

    List<Pipeline> findByCreatedBy(String createdBy);

    Page<Pipeline> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Pipeline p WHERE p.type = :type ORDER BY p.updatedAt DESC")
    List<Pipeline> findByType(@Param("type") Pipeline.PipelineType type);

    @Query("SELECT COUNT(p) FROM Pipeline p WHERE p.status = :status")
    long countByStatus(@Param("status") Pipeline.PipelineStatus status);
}
