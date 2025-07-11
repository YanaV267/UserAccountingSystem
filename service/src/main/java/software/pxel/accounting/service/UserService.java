package software.pxel.accounting.service;

import software.pxel.accounting.dto.PageCacheDto;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;

public interface UserService {
    PageCacheDto<UserReadDto> searchUsers(UserSearchDto request);
}