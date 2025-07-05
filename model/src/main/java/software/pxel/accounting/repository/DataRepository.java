package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.AbstractData;

import java.util.Optional;

@Repository
public interface DataRepository<E extends AbstractData> extends JpaRepository<E, Long> {

    boolean existsByValue(String value);

    Optional<E> findByValue(String value);
}