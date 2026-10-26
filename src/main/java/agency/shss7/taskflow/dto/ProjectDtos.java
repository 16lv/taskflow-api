package agency.shss7.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record CreateProjectRequest(
        @NotBlank(message = "Project code is required")
        @Pattern(regexp = "^[A-Za-z0-9_-]{2,16}$", message = "Project code must be 2-16 alphanumeric characters")
        String code,

        @NotBlank(message = "Project name is required")
        @Size(min = 3, max = 120, message = "Project name must be between 3 and 120 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
    ) {}

    public record ProjectResponse(
        Long id,
        String code,
        String name,
        String description,
        String ownerEmail,
        long totalTasks,
        long completedTasks,
        Instant createdAt
    ) {}
}
