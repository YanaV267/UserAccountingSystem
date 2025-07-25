package software.pxel.accounting.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.PageCacheDto;
import software.pxel.accounting.dto.email.EmailReadDto;
import software.pxel.accounting.dto.phone.PhoneReadDto;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;
import software.pxel.accounting.entity.document.UserDocument;
import software.pxel.accounting.repository.UserRepository;
import software.pxel.accounting.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ModelMapper mapper;
    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "users", key = "(#dto.name ?: '') + " +
            "(#dto.dateOfBirth ?: '') + " +
            "(#dto.email ?: '') + " +
            "(#dto.phone ?: '') + " +
            "#dto.page + #dto.size")
    public PageCacheDto<UserReadDto> searchUsers(UserSearchDto dto) {
        log.info("Searching users with parameters: name={}, dateOfBirth={}, email={}, phone={}, page={}, size={}",
                dto.getName(),
                dto.getDateOfBirth(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getPage(),
                dto.getSize());

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (dto.getName() != null) {
            boolQuery.must(QueryBuilders.matchBoolPrefixQuery("name", dto.getName()));
        }
        if (dto.getDateOfBirth() != null) {
            boolQuery.must(QueryBuilders.rangeQuery("dateOfBirth")
                    .gt(dto.getDateOfBirth().toString())
            );
        }
        if (dto.getEmail() != null) {
            boolQuery.must(QueryBuilders.nestedQuery("emailData",
                    QueryBuilders.matchQuery("emailData.value", dto.getEmail()),
                    ScoreMode.Avg));
        }

        if (dto.getPhone() != null) {
            boolQuery.must(QueryBuilders.nestedQuery("phoneData",
                    QueryBuilders.matchQuery("phoneData.value", dto.getPhone()),
                    ScoreMode.Avg));
        }

        NativeSearchQuery query = new NativeSearchQuery(boolQuery);
        query.setPageable(dto.toPageable());

        List<UserReadDto> result = elasticsearchOperations.search(query, UserDocument.class)
                .map(hit -> {
                    UserDocument source = hit.getContent();
                    UserReadDto readDto = mapper.map(source, UserReadDto.class);

                    if (readDto.getEmailData() == null) {
                        readDto.setEmailData(source.getEmailData().stream()
                                .map(v -> new EmailReadDto(v.getValue()))
                                .collect(Collectors.toSet()));
                    }
                    if (readDto.getPhoneData() == null) {
                        readDto.setPhoneData(source.getPhoneData().stream()
                                .map(v -> new PhoneReadDto(v.getValue()))
                                .collect(Collectors.toSet()));
                    }
                    return readDto;
                })
                .get()
                .collect(Collectors.toList());

        log.info("Found {} users on page {}", result.size(), query.getPageable().getPageNumber());

        return new PageCacheDto<>(result, query.getPageable().getPageNumber(), query.getPageable().getPageSize());
    }
}