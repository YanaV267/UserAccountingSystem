package software.pxel.accounting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.entity.document.EmailDataDocument;
import software.pxel.accounting.entity.document.PhoneDataDocument;
import software.pxel.accounting.entity.document.UserDocument;
import software.pxel.accounting.repository.UserDocumentRepository;
import software.pxel.accounting.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchResetService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 24 17 * * ?")
    public void resetData() {
        try {
            log.info("Resetting data in Elasticsearch");
            elasticsearchOperations.indexOps(UserDocument.class).delete();

            elasticsearchOperations.indexOps(UserDocument.class).create();
            elasticsearchOperations.indexOps(UserDocument.class).putMapping();

            List<User> users = userRepository.findAll();

            log.info("{} users were saved to Elasticsearch...", users.size());
            users.forEach(user -> {
                UserDocument doc = convertToDocument(user);
                userDocumentRepository.save(doc);
            });
        } catch (Exception e) {
            log.error("Ошибка: ", e);
        }
    }

    private UserDocument convertToDocument(User user) {
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setName(user.getName());
        doc.setDateOfBirth(user.getDateOfBirth());
        doc.setEmailData(
                user.getEmailData().stream()
                        .map(e -> new EmailDataDocument(e.getValue()))
                        .collect(Collectors.toList())
        );
        doc.setPhoneData(
                user.getPhoneData().stream()
                        .map(e -> new PhoneDataDocument(e.getValue()))
                        .collect(Collectors.toList())
        );
        return doc;
    }
}