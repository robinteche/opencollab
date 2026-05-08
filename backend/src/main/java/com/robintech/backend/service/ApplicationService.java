package com.robintech.backend.service;


import com.robintech.backend.dto.ApplicationRequest;
import com.robintech.backend.dto.ApplicationResponse;
import com.robintech.backend.model.Application;
import com.robintech.backend.model.Application.ApplicationStatus;
import com.robintech.backend.model.Project;
import com.robintech.backend.model.User;
import com.robintech.backend.repository.ApplicationRepository;
import com.robintech.backend.repository.ProjectRepository;
import com.robintech.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ApplicationResponse applyToProject(Long projectId, ApplicationRequest request) {
        User applicant = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getOwner().getId().equals(applicant.getId()))
            throw new RuntimeException("You cannot apply to your own project");

        java.util.Optional<Application> existingOpt = applicationRepository.findByProjectAndApplicant(project, applicant);
        if (existingOpt.isPresent()) {
            Application existing = existingOpt.get();
            if (existing.getStatus() == ApplicationStatus.REJECTED) {
                applicationRepository.delete(existing);
                applicationRepository.flush();
            } else {
                throw new RuntimeException("You have already applied to this project");
            }
        }
        if (project.getStatus() != Project.ProjectStatus.OPEN)
            throw new RuntimeException("This project is not accepting applications");

        Application application = Application.builder()
                .project(project)
                .applicant(applicant)
                .message(request.getMessage())
                .roleAppliedFor(request.getRoleAppliedFor())
                .build();

        Application saved = applicationRepository.save(application);

        notificationService.createNotification(
                project.getOwner(),
                applicant.getUsername() + " applied to your project: " + project.getTitle(),
                com.robintech.backend.model.Notification.NotificationType.APPLICATION_RECEIVED
        );

        return ApplicationResponse.fromEntity(saved);
    }


    public List<ApplicationResponse> getApplicationsForProject(Long projectId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId()))
            throw new RuntimeException("Not authorized to view these applications");

        return applicationRepository.findByProject(project)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getIncomingApplications() {
        User currentUser = getCurrentUser();
        return applicationRepository.findByProjectOwner(currentUser)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }


    public List<ApplicationResponse> getMyApplications() {
        User currentUser = getCurrentUser();
        return applicationRepository.findByApplicant(currentUser)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ApplicationResponse updateApplicationStatus(Long applicationId,
                                                       ApplicationStatus status) {
        User currentUser = getCurrentUser();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getProject().getOwner().getId().equals(currentUser.getId()))
            throw new RuntimeException("Not authorized to update this application");

        application.setStatus(status);
        
        if (status == ApplicationStatus.ACCEPTED) {
            Project project = application.getProject();
            if (project.getCollaborators() == null) {
                project.setCollaborators(new HashSet<>());
            }
            project.getCollaborators().add(application.getApplicant());
            projectRepository.save(project);

            // Notify applicant
            notificationService.createNotification(
                    application.getApplicant(),
                    "Your application to " + project.getTitle() + " has been ACCEPTED!",
                    com.robintech.backend.model.Notification.NotificationType.APPLICATION_ACCEPTED
            );
        } else if (status == ApplicationStatus.REJECTED) {
             // Notify applicant
             notificationService.createNotification(
                     application.getApplicant(),
                     "Your application to " + application.getProject().getTitle() + " was not accepted at this time.",
                     com.robintech.backend.model.Notification.NotificationType.APPLICATION_REJECTED
             );
        }
        
        return ApplicationResponse.fromEntity(applicationRepository.save(application));
    }

    // Applicant withdraws their own application
    public void withdrawApplication(Long applicationId) {
        User currentUser = getCurrentUser();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getApplicant().getId().equals(currentUser.getId()))
            throw new RuntimeException("Not authorized to withdraw this application");

        applicationRepository.delete(application);
    }
}