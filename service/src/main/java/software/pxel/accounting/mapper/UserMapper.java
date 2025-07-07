package software.pxel.accounting.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserReadDto toDto(User user);

    User toEntity(UserReadDto userDto);
}
