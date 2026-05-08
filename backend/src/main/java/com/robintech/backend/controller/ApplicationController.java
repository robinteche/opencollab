package com.robintech.backend.controller;

import com.robintech.backend.dto.ApplicationRequest;
import com.robintech.backend.dto.ApplicationResponse;
import com.robintech.backend.model.Application.ApplicationStatus;
import com.robintech.backend.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;


    @PostMapping("/project/{projectId}")
    public ResponseEntity<ApplicationResponse> apply(
            @PathVariable Long projectId,
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.applyToProject(projectId, request));
    }


    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ApplicationResponse>> getProjectApplications(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(applicationService.getApplicationsForProject(projectId));
    }


    @GetMapping("/incoming")
    public ResponseEntity<List<ApplicationResponse>> getIncomingApplications() {
        return ResponseEntity.ok(applicationService.getIncomingApplications());
    }


    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(applicationService.getMyApplications());
    }


    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(applicationId, status));
    }


    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> withdraw(@PathVariable Long applicationId) {
        applicationService.withdrawApplication(applicationId);
        return ResponseEntity.noContent().build();
    }
}

