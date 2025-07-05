package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.EmailData;

@Repository
public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

}