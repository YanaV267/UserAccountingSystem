package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.EmailData;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

    Optional<EmailData> findByValue(String value);

    List<EmailData> findByUserId(Long id);
}