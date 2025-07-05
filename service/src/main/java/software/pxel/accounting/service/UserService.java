package software.pxel.accounting.service;

import org.springframework.data.domain.Page;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;

public interface UserService {
    Page<UserReadDto> searchUsers(UserSearchDto request);
}