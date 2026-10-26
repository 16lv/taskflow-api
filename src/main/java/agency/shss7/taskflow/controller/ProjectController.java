package agency.shss7.taskflow.controller;

import agency.shss7.taskflow.dto.ProjectDtos.*;
import agency.shss7.taskflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final WorkflowService workflowService;

    public ProjectController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
        @Valid @RequestBody CreateProjectRequest request,
        Authentication authentication
    ) {
        ProjectResponse created = workflowService.createProject(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<ProjectResponse> listProjects() {
        return workflowService.listProjects();
    }

    @GetMapping("/{code}")
    public ProjectResponse getProject(@PathVariable String code) {
        return workflowService.getProjectByCode(code);
    }
}
