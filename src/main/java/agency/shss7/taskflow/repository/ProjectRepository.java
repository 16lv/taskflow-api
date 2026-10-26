package agency.shss7.taskflow.repository;

import agency.shss7.taskflow.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
