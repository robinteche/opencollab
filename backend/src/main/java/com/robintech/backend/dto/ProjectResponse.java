package com.robintech.backend.dto;
import com.robintech.backend.model.Project;
import com.robintech.backend.model.Project.CommitmentLevel;
import com.robintech.backend.model.Project.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private Set<String> techStack;
    private Set<String> rolesNeeded;
    private CommitmentLevel commitmentLevel;
    private ProjectStatus status;
    private Long ownerId;
    private String ownerUsername;
    private Set<UserResponse> collaborators;
    private LocalDateTime createdAt;
    private String githubRepo;

    public static ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .techStack(project.getTechStack())
                .rolesNeeded(project.getRolesNeeded())
                .commitmentLevel(project.getCommitmentLevel())
                .status(project.getStatus())
                .ownerId(project.getOwner().getId())
                .ownerUsername(project.getOwner().getUsername())
                .collaborators(project.getCollaborators() == null ? new HashSet<>() :
                        project.getCollaborators().stream()
                                .map(UserResponse::fromEntity)
                                .collect(Collectors.toSet()))
                .createdAt(project.getCreatedAt())
                .githubRepo(project.getGithubRepo())
                .build();
    }
}