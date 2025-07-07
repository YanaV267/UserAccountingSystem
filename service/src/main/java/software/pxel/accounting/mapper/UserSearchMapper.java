package software.pxel.accounting.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.elasticsearch.UserSearchDocument;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserSearchMapper {

    UserReadDto toDto(UserSearchDocument document);

    UserSearchDocument toDocument(UserReadDto dto);
}