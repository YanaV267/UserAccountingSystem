package software.pxel.accounting.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import software.pxel.accounting.dto.account.AccountReadDto;
import software.pxel.accounting.dto.account.AccountUpdateDto;
import software.pxel.accounting.dto.account.TransferRequestDto;
import software.pxel.accounting.entity.Account;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.mapper.AccountMapper;
import software.pxel.accounting.repository.AccountRepository;
import software.pxel.accounting.service.impl.AccountServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_ACCOUNT_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_INSUFFICIENT_FUNDS;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_RECEIVER_ACCOUNT_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_SENDER_ACCOUNT_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_TRANSFER_TO_SAME_ACCOUNT;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private TransactionStatus transactionStatus;

    @InjectMocks
    private AccountServiceImpl accountService;

    private final Long SENDER_ID = 1L;
    private final Long RECIPIENT_ID = 2L;
    private final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");
    private final BigDecimal TRANSFER_AMOUNT = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);
    }

    @Test
    void transferMoney_shouldTransferFundsSuccessfully() {
        Account senderAccount = createAccount(SENDER_ID, INITIAL_BALANCE);
        Account recipientAccount = createAccount(RECIPIENT_ID, INITIAL_BALANCE);

        TransferRequestDto request = new TransferRequestDto();
        request.setTargetUserId(RECIPIENT_ID);
        request.setAmount(TRANSFER_AMOUNT);

        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(RECIPIENT_ID)).thenReturn(Optional.of(recipientAccount));

        accountService.transferMoney(SENDER_ID, request);

        assertEquals(new BigDecimal("500.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("1500.00"), recipientAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferMoney_shouldThrowWhenSenderNotFound() {
        TransferRequestDto request = new TransferRequestDto();
        request.setTargetUserId(RECIPIENT_ID);
        request.setAmount(TRANSFER_AMOUNT);

        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.transferMoney(SENDER_ID, request));
        assertEquals(ERR_SENDER_ACCOUNT_NOT_FOUND, exception.getCode());
    }

    @Test
    void transferMoney_shouldThrowWhenRecipientNotFound() {
        Account senderAccount = createAccount(SENDER_ID, INITIAL_BALANCE);

        TransferRequestDto request = new TransferRequestDto();
        request.setTargetUserId(RECIPIENT_ID);
        request.setAmount(TRANSFER_AMOUNT);

        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(RECIPIENT_ID)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.transferMoney(SENDER_ID, request));
        assertEquals(ERR_RECEIVER_ACCOUNT_NOT_FOUND, exception.getCode());
    }

    @Test
    void transferMoney_shouldThrowWhenInsufficientFunds() {
        Account senderAccount = createAccount(SENDER_ID, new BigDecimal("400.00"));
        Account recipientAccount = createAccount(RECIPIENT_ID, INITIAL_BALANCE);

        TransferRequestDto request = new TransferRequestDto();
        request.setTargetUserId(RECIPIENT_ID);
        request.setAmount(TRANSFER_AMOUNT);

        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(RECIPIENT_ID)).thenReturn(Optional.of(recipientAccount));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.transferMoney(SENDER_ID, request));
        assertEquals(ERR_INSUFFICIENT_FUNDS, exception.getCode());
    }

    @Test
    void transferMoney_shouldThrowWhenTransferToSameAccount() {
        Account senderAccount = createAccount(SENDER_ID, INITIAL_BALANCE);

        TransferRequestDto request = new TransferRequestDto();
        request.setTargetUserId(SENDER_ID);
        request.setAmount(TRANSFER_AMOUNT);

        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.of(senderAccount));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.transferMoney(SENDER_ID, request));
        assertEquals(ERR_TRANSFER_TO_SAME_ACCOUNT, exception.getCode());
    }

    @Test
    void transferMoney_shouldThrowWhenAmountIsNegative() {
        // Arrange
        TransferRequestDto request = new TransferRequestDto();
        request.setTargetUserId(RECIPIENT_ID);
        request.setAmount(new BigDecimal("-100.00"));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.transferMoney(SENDER_ID, request));
    }

    @Test
    void transferMoney_shouldRollbackOnFailure() {
        // Arrange
        Account senderAccount = createAccount(SENDER_ID, INITIAL_BALANCE);
        Account recipientAccount = createAccount(RECIPIENT_ID, INITIAL_BALANCE);

        TransferRequestDto request = new TransferRequestDto();
        request.setTargetUserId(RECIPIENT_ID);
        request.setAmount(TRANSFER_AMOUNT);

        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(RECIPIENT_ID)).thenReturn(Optional.of(recipientAccount));
        doThrow(new RuntimeException("DB Error")).when(accountRepository).save(any(Account.class));

        assertThrows(RuntimeException.class,
                () -> accountService.transferMoney(SENDER_ID, request));
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void getBalance_shouldReturnBalance() {
        Account account = createAccount(SENDER_ID, INITIAL_BALANCE);
        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(SENDER_ID);

        assertEquals(INITIAL_BALANCE, balance);
    }

    @Test
    void getBalance_shouldThrowWhenAccountNotFound() {
        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.getBalance(SENDER_ID));
        assertEquals(ERR_ACCOUNT_NOT_FOUND, exception.getCode());
    }

    @Test
    void updateBalance_shouldUpdateBalance() {
        Account account = createAccount(SENDER_ID, INITIAL_BALANCE);
        Account updatedAccount = createAccount(SENDER_ID, new BigDecimal("1500.00"));
        AccountUpdateDto updateDto = new AccountUpdateDto();
        updateDto.setUserId(SENDER_ID);
        updateDto.setBalance(new BigDecimal("1500.00"));

        when(accountRepository.findByUserId(SENDER_ID)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(updatedAccount)).thenReturn(new AccountReadDto());

        accountService.updateBalance(updateDto);

        assertEquals(new BigDecimal("1500.00"), account.getBalance());
        verify(accountRepository).save(account);
    }

    private Account createAccount(Long userId, BigDecimal balance) {
        User user = new User();
        user.setName("Yana V");
        user.setDateOfBirth(LocalDate.of(2002, 02, 14));

        Account account = new Account();
        account.setId(userId);
        account.setUser(user);
        account.setBalance(balance);
        return account;
    }
}