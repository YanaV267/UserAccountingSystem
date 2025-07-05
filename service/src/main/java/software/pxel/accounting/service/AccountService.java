package software.pxel.accounting.service;

import software.pxel.accounting.dto.account.AccountReadDto;
import software.pxel.accounting.dto.account.AccountUpdateDto;
import software.pxel.accounting.dto.account.TransferRequestDto;

import java.math.BigDecimal;

public interface AccountService {
    void transferMoney(Long senderUserId, TransferRequestDto dto);

    BigDecimal getBalance(Long userId);

    AccountReadDto updateBalance(AccountUpdateDto dto);
}
