package agency.shss7.taskflow.service;

import agency.shss7.taskflow.domain.*;
import agency.shss7.taskflow.dto.ProjectDtos.*;
import agency.shss7.taskflow.dto.TaskDtos.*;
import agency.shss7.taskflow.repository.ProjectRepository;
import agency.shss7.taskflow.repository.TaskItemRepository;
import agency.shss7.taskflow.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkflowService {

    private final ProjectRepository projectRepository;
    private final TaskItemRepository taskRepository;
    private final UserAccountRepository userRepository;

    public WorkflowService(
        ProjectRepository projectRepository,
        TaskItemRepository taskRepository,
        UserAccountRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String ownerEmail) {
        if (projectRepository.existsByCodeIgnoreCase(request.code())) {
            throw new IllegalArgumentException("Project code '" + request.code().toUpperCase() + "' already exists");
        }
        UserAccount owner = userRepository.findByEmailIgnoreCase(ownerEmail)
            .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
        Project project = new Project(request.code(), request.name(), request.description(), owner);
        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll()
            .stream()
            .map(this::toProjectResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectByCode(String code) {
        Project project = projectRepository.findByCodeIgnoreCase(code)
            .orElseThrow(() -> new EntityNotFoundException("Project with code '" + code + "' not found"));
        return toProjectResponse(project);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = projectRepository.findById(request.projectId())
            .orElseThrow(() -> new EntityNotFoundException("Project ID " + request.projectId() + " not found"));
        UserAccount assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new EntityNotFoundException("Assignee user ID " + request.assigneeId() + " not found"));
        }
        TaskItem item = new TaskItem(
            project,
            request.title(),
            request.description(),
            request.priority(),
            request.dueDate(),
            assignee
        );
        TaskItem saved = taskRepository.save(item);
        return toTaskResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        TaskItem task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task ID " + taskId + " not found"));
        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.assigneeId() != null) {
            UserAccount assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new EntityNotFoundException("Assignee user ID " + request.assigneeId() + " not found"));
            task.setAssignee(assignee);
        }
        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse transitionTaskStatus(Long taskId, TaskStatus targetStatus) {
        TaskItem task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task ID " + taskId + " not found"));
        task.transitionStatus(targetStatus);
        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Task ID " + taskId + " not found");
        }
        taskRepository.deleteById(taskId);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(
        Long projectId,
        TaskStatus status,
        TaskPriority priority,
        Long assigneeId,
        String search,
        Pageable pageable
    ) {
        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return taskRepository.searchTasks(projectId, status, priority, assigneeId, normalizedSearch, pageable)
            .map(this::toTaskResponse);
    }

    private ProjectResponse toProjectResponse(Project project) {
        long total = project.getTasks().size();
        long completed = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        return new ProjectResponse(
            project.getId(),
            project.getCode(),
            project.getName(),
            project.getDescription(),
            project.getOwner().getEmail(),
            total,
            completed,
            project.getCreatedAt()
        );
    }

    private TaskResponse toTaskResponse(TaskItem task) {
        return new TaskResponse(
            task.getId(),
            task.getProject().getId(),
            task.getProject().getCode(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getDueDate(),
            task.getAssignee() != null ? task.getAssignee().getId() : null,
            task.getAssignee() != null ? task.getAssignee().getEmail() : null,
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
