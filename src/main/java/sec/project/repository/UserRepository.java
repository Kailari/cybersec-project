package sec.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sec.project.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
