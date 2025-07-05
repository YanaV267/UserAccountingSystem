package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import software.pxel.accounting.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
