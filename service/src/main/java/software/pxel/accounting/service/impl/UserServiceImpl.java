package software.pxel.accounting.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;
import software.pxel.accounting.elasticsearch.UserSearchRepository;
import software.pxel.accounting.mapper.UserSearchMapper;
import software.pxel.accounting.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserSearchRepository userSearchRepository;
    private final UserSearchMapper mapper;

    public UserServiceImpl(UserSearchRepository userSearchRepository,
                           UserSearchMapper mapper) {
        this.userSearchRepository = userSearchRepository;
        this.mapper = mapper;
    }

    @Override
    @Cacheable(value = "userSearch", keyGenerator = "userKeyGenerator")
    public Page<UserReadDto> searchUsers(UserSearchDto dto) {
        return userSearchRepository.search(
                dto.getName(),
                dto.getDateOfBirth(),
                dto.getEmail(),
                dto.getPhone(),
                PageRequest.of(dto.getPage(), dto.getSize())
        ).map(mapper::toDto);
    }
}