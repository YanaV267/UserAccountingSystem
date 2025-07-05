package software.pxel.accounting.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.mapper.UserMapper;
import software.pxel.accounting.repository.UserRepository;
import software.pxel.accounting.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    @Cacheable(value = "userSearch", key = "{#request.name, #request.email, #request.phone, #request.dateOfBirth, #request.page, #request.size}")
    public Page<UserReadDto> searchUsers(UserSearchDto request) {
        Page<User> users = userRepository.searchUsers(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getDateOfBirth(),
                PageRequest.of(request.getPage(), request.getSize())
        );

        return users.map(mapper::toDto);
    }
}