package agency.shss7.taskflow.repository;

import agency.shss7.taskflow.domain.TaskItem;
import agency.shss7.taskflow.domain.TaskPriority;
import agency.shss7.taskflow.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {

    @Query("""
        SELECT t FROM TaskItem t
        WHERE (:projectId IS NULL OR t.project.id = :projectId)
          AND (:status IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
          AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<TaskItem> searchTasks(
        @Param("projectId") Long projectId,
        @Param("status") TaskStatus status,
        @Param("priority") TaskPriority priority,
        @Param("assigneeId") Long assigneeId,
        @Param("search") String search,
        Pageable pageable
    );

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
}
