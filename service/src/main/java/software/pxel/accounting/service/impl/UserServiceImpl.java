package software.pxel.accounting.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;
import software.pxel.accounting.elasticsearch.UserSearchDocument;
import software.pxel.accounting.elasticsearch.UserSearchRepository;
import software.pxel.accounting.mapper.UserSearchMapper;
import software.pxel.accounting.service.UserService;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String SEARCH_CACHE_PREFIX = "userSearch:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final UserSearchRepository userSearchRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserSearchMapper mapper;

    @Override
    @Cacheable(value = "userSearch", keyGenerator = "customKeyGenerator")
    public Page<UserReadDto> searchUsers(UserSearchDto dto) {
        String cacheKey = generateCacheKey(dto);

        try {
            Optional<Page<UserReadDto>> cachedResult = getFromCache(cacheKey);
            if (cachedResult.isPresent()) {
                return cachedResult.get();
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve data from cache for key: {}", cacheKey, e);
        }

        Page<UserSearchDocument> page = userSearchRepository.search(
                dto.getName(),
                dto.getDateOfBirth(),
                dto.getEmail(),
                dto.getPhone(),
                PageRequest.of(dto.getPage(), dto.getSize())
        );
        Page<UserReadDto> result = page.map(mapper::toDto);
        try {
            cacheResult(cacheKey, result);
        } catch (Exception e) {
            log.warn("Failed to cache result for key: {}", cacheKey, e);
        }

        return result;
    }

    private Optional<Page<UserReadDto>> getFromCache(String cacheKey) throws JsonProcessingException {
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue == null) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readValue(cachedValue, new TypeReference<>() {
        }));
    }

    private void cacheResult(String key, Page<UserReadDto> result) throws JsonProcessingException {
        String value = objectMapper.writeValueAsString(result);
        redisTemplate.opsForValue().set(key, value, CACHE_TTL);
    }

    private String generateCacheKey(UserSearchDto dto) {
        return SEARCH_CACHE_PREFIX +
                Objects.toString(dto.getName(), "") + "|" +
                Objects.toString(dto.getEmail(), "") + "|" +
                Objects.toString(dto.getPhone(), "") + "|" +
                (dto.getDateOfBirth() != null ? dto.getDateOfBirth().toString() : "") + "|" +
                dto.getPage() + "|" +
                dto.getSize();
    }
}