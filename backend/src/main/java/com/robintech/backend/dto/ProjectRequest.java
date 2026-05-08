package com.robintech.backend.dto;
import com.robintech.backend.model.Project.CommitmentLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private Set<String> techStack;

    private Set<String> rolesNeeded;

    @NotNull
    private CommitmentLevel commitmentLevel;

    private String githubRepo;
}