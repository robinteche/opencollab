package com.robintech.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "project_tech_stack", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tech")
    private Set<String> techStack;

    @ElementCollection
    @CollectionTable(name = "project_roles_needed", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "role")
    private Set<String> rolesNeeded;

    @Enumerated(EnumType.STRING)
    private CommitmentLevel commitmentLevel;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_collaborators",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> collaborators;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "github_repo")
    private String githubRepo;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum CommitmentLevel {
        LOW, MEDIUM, HIGH
    }

    public enum ProjectStatus {
        OPEN, IN_PROGRESS, COMPLETED
    }
}
