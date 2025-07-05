package software.pxel.accounting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import software.pxel.accounting.entity.User;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByDateOfBirthAfter(LocalDate date, Pageable pageable);

    Page<User> findByPhoneDataValue(String phone, Pageable pageable);

    Page<User> findByNameStartingWith(String name, Pageable pageable);

    Page<User> findByEmailDataValue(String email, Pageable pageable);

    Optional<User> findByPhoneDataValueAndPassword(String phone, String password);

    Optional<User> findByEmailDataValueAndPassword(String email, String password);
}
