package software.pxel.accounting.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Page<User> findByPhoneDataValue(String phone, Pageable pageable);

    Optional<User> findByPhoneDataValueAndPassword(String phone, String password);

    Optional<User> findByEmailDataValueAndPassword(String email, String password);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.emailData LEFT JOIN FETCH u.phoneData")
    List<User> findAllWithEmailsAndPhones();

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.emailData e " +
            "LEFT JOIN u.phoneData p " +
            "WHERE (:name IS NULL OR u.name LIKE :name%) " +
            "AND (:email IS NULL OR e.value = :email) " +
            "AND (:phone IS NULL OR p.value = :phone) " +
            "AND (:dateOfBirth IS NULL OR u.dateOfBirth > :dateOfBirth)")
    Page<User> searchUsers(
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("dateOfBirth") LocalDate dateOfBirth,
            Pageable pageable
    );


}
