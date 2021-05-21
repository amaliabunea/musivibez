package backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import backend.domain.User;

public interface UserRepository extends JpaRepository<User, String> {
}
