package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.PhoneData;

@Repository
public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

}