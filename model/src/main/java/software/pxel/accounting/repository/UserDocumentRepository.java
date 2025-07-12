package software.pxel.accounting.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import software.pxel.accounting.entity.document.UserDocument;

@Repository
public interface UserDocumentRepository extends ElasticsearchRepository<UserDocument, Long> {

}
