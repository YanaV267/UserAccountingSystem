package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.PhoneData;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

    Optional<PhoneData> findByValue(String value);

    List<PhoneData> findByUserId(Long id);
}