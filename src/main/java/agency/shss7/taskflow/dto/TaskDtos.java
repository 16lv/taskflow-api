package agency.shss7.taskflow.dto;

import agency.shss7.taskflow.domain.TaskPriority;
import agency.shss7.taskflow.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public final class TaskDtos {

    private TaskDtos() {
    }

    public record CreateTaskRequest(
        @NotNull(message = "Project ID is required")
        Long projectId,

        @NotBlank(message = "Task title is required")
        @Size(min = 3, max = 180, message = "Task title must be between 3 and 180 characters")
        String title,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        TaskPriority priority,

        LocalDate dueDate,

        Long assigneeId
    ) {}

    public record UpdateTaskRequest(
        @Size(min = 3, max = 180, message = "Task title must be between 3 and 180 characters")
        String title,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        TaskPriority priority,

        LocalDate dueDate,

        Long assigneeId
    ) {}

    public record StatusTransitionRequest(
        @NotNull(message = "Target status is required")
        TaskStatus status
    ) {}

    public record TaskResponse(
        Long id,
        Long projectId,
        String projectCode,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long assigneeId,
        String assigneeEmail,
        Instant createdAt,
        Instant updatedAt
    ) {}
}
