package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.emailData e " +
            "JOIN u.phoneData p")
    List<User> findAllWithEmailsAndPhones();
}
