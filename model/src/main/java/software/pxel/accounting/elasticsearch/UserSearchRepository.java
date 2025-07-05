package software.pxel.accounting.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, Long> {

    Page<UserSearchDocument> search(
            String name,
            LocalDate dateOfBirth,
            String email,
            String phone,
            Pageable pageable
    );
}
