package software.pxel.accounting.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, Long> {

    @Query("{\"bool\": {\"must\": [" +
            "{\"match\": {\"name\": \"?0\"}}, " +
            "{\"match\": {\"dateOfBirth\": \"?1\"}}, " +
            "{\"match\": {\"email\": \"?2\"}}, " +
            "{\"match\": {\"phone\": \"?3\"}}" +
            "]}}")
    Page<UserSearchDocument> search(
            String name,
            LocalDate dateOfBirth,
            String email,
            String phone,
            Pageable pageable
    );


}
