package software.pxel.accounting.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.elasticsearch.UserSearchDocument;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserSearchMapper {

//    @Mapping(target = "balance", ignore = true)
    UserReadDto toDto(UserSearchDocument document);

//    @Mapping(target = "balance", ignore = true)
    UserSearchDocument toDocument(UserReadDto dto);
}