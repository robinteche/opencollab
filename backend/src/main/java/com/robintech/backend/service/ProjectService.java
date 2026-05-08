package com.robintech.backend.service;

import com.robintech.backend.dto.ProjectRequest;
import com.robintech.backend.dto.ProjectResponse;
import com.robintech.backend.model.Project;
import com.robintech.backend.model.User;
import com.robintech.backend.repository.ProjectRepository;
import com.robintech.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ProjectResponse createProject(ProjectRequest request) {
        User owner = getCurrentUser();
        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .rolesNeeded(request.getRolesNeeded())
                .commitmentLevel(request.getCommitmentLevel())
                .githubRepo(request.getGithubRepo())
                .owner(owner)
                .build();
        return ProjectResponse.fromEntity(projectRepository.save(project));
    }

    public List<ProjectResponse> getAllOpenProjects(String tech, String role,
                                                    Project.CommitmentLevel commitment) {
        return projectRepository.findWithFilters(tech, role, commitment)
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }


public ProjectResponse getProjectById(Long id) {
    Project project = projectRepository.findByIdWithDetails(id);
    if (project == null) {
        throw new RuntimeException("Project not found");
    }
    return ProjectResponse.fromEntity(project);
}

    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User currentUser = getCurrentUser();

        if (!project.getOwner().getId().equals(currentUser.getId()))
            throw new RuntimeException("Not authorized to update this project");

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setRolesNeeded(request.getRolesNeeded());
        project.setCommitmentLevel(request.getCommitmentLevel());
        project.setGithubRepo(request.getGithubRepo());
        return ProjectResponse.fromEntity(projectRepository.save(project));
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User currentUser = getCurrentUser();

        if (!project.getOwner().getId().equals(currentUser.getId()))
            throw new RuntimeException("Not authorized to delete this project");

        projectRepository.delete(project);
    }

    public List<ProjectResponse> getMyProjects() {
        User currentUser = getCurrentUser();
        return projectRepository.findByOwnerWithDetails(currentUser)
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }
}