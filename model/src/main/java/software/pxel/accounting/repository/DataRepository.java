package software.pxel.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import software.pxel.accounting.entity.AbstractData;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface DataRepository<E extends AbstractData> extends JpaRepository<E, Long> {

    boolean existsByValue(String value);

    Optional<E> findByValue(String value);

    List<E> findByUserId(Long id);

}