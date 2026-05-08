package com.robintech.backend.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.robintech.backend.model.Project;
import com.robintech.backend.model.Project.ProjectStatus;
import com.robintech.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByOwner(User owner);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.rolesNeeded " +
            "LEFT JOIN FETCH p.techStack " +
            "WHERE (:tech IS NULL OR :tech MEMBER OF p.techStack) AND " +
            "(:role IS NULL OR :role MEMBER OF p.rolesNeeded) AND " +
            "(:commitment IS NULL OR p.commitmentLevel = :commitment) AND " +
            "p.status = 'OPEN'")
    List<Project> findWithFilters(
            @Param("tech") String tech,
            @Param("role") String role,
            @Param("commitment") Project.CommitmentLevel commitment
    );

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.rolesNeeded " +
            "LEFT JOIN FETCH p.techStack " +
            "WHERE p.owner = :owner")
    List<Project> findByOwnerWithDetails(@Param("owner") User owner);

    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.rolesNeeded " +
            "LEFT JOIN FETCH p.techStack " +
            "WHERE p.id = :id")
    Project findByIdWithDetails(@Param("id") Long id);
}