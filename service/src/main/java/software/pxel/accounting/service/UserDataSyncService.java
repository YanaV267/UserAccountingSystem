package software.pxel.accounting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.pxel.accounting.elasticsearch.UserSearchDocument;
import software.pxel.accounting.elasticsearch.UserSearchRepository;
import software.pxel.accounting.entity.EmailData;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDataSyncService {
    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncUsersToElasticsearch() {
        List<User> users = userRepository.findAllWithEmailsAndPhones();
        List<UserSearchDocument> documents = users.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());

        userSearchRepository.saveAll(documents);
        clearSearchCache();
    }

    private UserSearchDocument convertToDocument(User user) {
        return new UserSearchDocument(
                user.getId(),
                user.getName(),
                user.getDateOfBirth(),
                user.getEmailData().stream().map(EmailData::getValue).collect(Collectors.toSet()),
                user.getPhoneData().stream().map(PhoneData::getValue).collect(Collectors.toSet())
        );
    }

    private void clearSearchCache() {
        Set<String> keys = redisTemplate.keys("userSearch:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
