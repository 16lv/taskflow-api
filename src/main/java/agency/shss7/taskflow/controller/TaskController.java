package agency.shss7.taskflow.controller;

import agency.shss7.taskflow.domain.TaskPriority;
import agency.shss7.taskflow.domain.TaskStatus;
import agency.shss7.taskflow.dto.TaskDtos.*;
import agency.shss7.taskflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final WorkflowService workflowService;

    public TaskController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowService.createTask(request));
    }

    @GetMapping
    public Page<TaskResponse> searchTasks(
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) TaskPriority priority,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        int clampedSize = Math.min(Math.max(size, 1), 100);
        return workflowService.searchTasks(
            projectId,
            status,
            priority,
            assigneeId,
            q,
            PageRequest.of(Math.max(page, 0), clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    @PatchMapping("/{taskId}")
    public TaskResponse updateTask(
        @PathVariable Long taskId,
        @Valid @RequestBody UpdateTaskRequest request
    ) {
        return workflowService.updateTask(taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse transitionStatus(
        @PathVariable Long taskId,
        @Valid @RequestBody StatusTransitionRequest request
    ) {
        return workflowService.transitionTaskStatus(taskId, request.status());
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        workflowService.deleteTask(taskId);
    }
}
