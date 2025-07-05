package software.pxel.accounting.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import software.pxel.accounting.dto.account.AccountReadDto;
import software.pxel.accounting.entity.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    AccountReadDto toDto(Account user);

    Account toEntity(AccountReadDto userDto);
}
