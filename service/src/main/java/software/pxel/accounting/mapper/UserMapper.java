package software.pxel.accounting.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.entity.EmailData;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

//    @Mapping(target = "emailData", source = "emailData", qualifiedByName = "emailDataToSet")
//    @Mapping(target = "phoneData", source = "phoneData", qualifiedByName = "phoneDataToSet")
    UserReadDto toDto(User user);

//    @Mapping(target = "emailData", source = "emailData", qualifiedByName = "emailDataToSet")
//    @Mapping(target = "phoneData", source = "phoneData", qualifiedByName = "phoneDataToSet")
    User toEntity(UserReadDto userDto);

    @Named("emailDataToSet")
    default Set<String> mapEmails(Set<EmailData> emails) {
        return emails.stream()
                .map(EmailData::getValue)
                .collect(Collectors.toSet());
    }

    @Named("phoneDataToSet")
    default Set<String> mapPhones(Set<PhoneData> phones) {
        return phones.stream()
                .map(PhoneData::getValue)
                .collect(Collectors.toSet());
    }

    default Page<UserReadDto> toDtoPage(Page<User> userPage) {
        return userPage.map(this::toDto);
    }
}
