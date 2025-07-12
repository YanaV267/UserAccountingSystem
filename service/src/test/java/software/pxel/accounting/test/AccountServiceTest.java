package software.pxel.accounting.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import software.pxel.accounting.dto.account.AccountReadDto;
import software.pxel.accounting.dto.account.AccountUpdateDto;
import software.pxel.accounting.dto.account.TransferRequestDto;
import software.pxel.accounting.entity.Account;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.repository.AccountRepository;
import software.pxel.accounting.service.impl.AccountServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account senderAccount;
    private Account recipientAccount;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(transactionManager, accountRepository, mapper);
        ReflectionTestUtils.setField(accountService, "balanceIncreasePercentage", "10");
        ReflectionTestUtils.setField(accountService, "balanceMaxMultiplier", "2");

        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setUser(new User(101L));
        senderAccount.setBalance(BigDecimal.valueOf(1000));

        recipientAccount = new Account();
        recipientAccount.setId(2L);
        senderAccount.setUser(new User(102L));
        recipientAccount.setBalance(BigDecimal.valueOf(500));
    }

    @Test
    void getBalance_AccountExists_ReturnsBalance() {
        when(accountRepository.findByUserId(anyLong())).thenReturn(Optional.of(senderAccount));

        BigDecimal result = accountService.getBalance(101L);

        assertEquals(BigDecimal.valueOf(1000), result);
        verify(accountRepository).findByUserId(101L);
    }

    @Test
    void getBalance_AccountNotFound_ThrowsException() {
        when(accountRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> accountService.getBalance(101L),
                "Expected ERR_ACCOUNT_NOT_FOUND exception");
        verify(accountRepository).findByUserId(101L);
    }

    @Test
    void updateBalance_AccountExists_UpdatesAndReturnsDto() {
        AccountUpdateDto updateDto = new AccountUpdateDto();
        updateDto.setUserId(101L);
        updateDto.setBalance(BigDecimal.valueOf(1500));

        Account updatedAccount = new Account();
        updatedAccount.setId(1L);
        updatedAccount.setUser(new User(102L));
        updatedAccount.setBalance(BigDecimal.valueOf(1500));

        AccountReadDto expectedDto = new AccountReadDto();
        expectedDto.setId(1L);
        expectedDto.setBalance(BigDecimal.valueOf(1500));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
        when(mapper.map(updatedAccount, AccountReadDto.class)).thenReturn(expectedDto);
        when(cacheManager.getCache("accounts")).thenReturn(cache);

        AccountReadDto result = accountService.updateBalance(updateDto);

        assertEquals(BigDecimal.valueOf(1500), result.getBalance());
        verify(accountRepository).save(senderAccount);
        verify(cache).evict(101L);
    }

    @Test
    void updateBalance_AccountNotFound_ThrowsException() {
        AccountUpdateDto updateDto = new AccountUpdateDto();
        updateDto.setUserId(101L);
        updateDto.setBalance(BigDecimal.valueOf(1500));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> accountService.updateBalance(updateDto),
                "Expected ERR_ACCOUNT_NOT_FOUND exception");
    }

    @Test
    void transferMoney_ValidTransfer_UpdatesBalances() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setTargetUserId(102L);
        transferDto.setAmount(BigDecimal.valueOf(200));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(102L)).thenReturn(Optional.of(recipientAccount));
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        accountService.transferMoney(101L, transferDto);

        verify(accountRepository).save(argThat(account ->
                account.getId().equals(1L) && account.getBalance().equals(BigDecimal.valueOf(800))));
        verify(accountRepository).save(argThat(account ->
                account.getId().equals(2L) && account.getBalance().equals(BigDecimal.valueOf(700))));
        verify(transactionManager).commit(transactionStatus);
    }

    @Test
    void transferMoney_SenderNotFound_ThrowsException() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setTargetUserId(102L);
        transferDto.setAmount(BigDecimal.valueOf(200));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        assertThrows(ServiceException.class, () -> accountService.transferMoney(101L, transferDto),
                "Expected ERR_SENDER_ACCOUNT_NOT_FOUND exception");
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void transferMoney_RecipientNotFound_ThrowsException() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setTargetUserId(102L);
        transferDto.setAmount(BigDecimal.valueOf(200));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(102L)).thenReturn(Optional.empty());
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        assertThrows(ServiceException.class, () -> accountService.transferMoney(101L, transferDto),
                "Expected ERR_RECEIVER_ACCOUNT_NOT_FOUND exception");
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void transferMoney_InsufficientFunds_ThrowsException() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setTargetUserId(102L);
        transferDto.setAmount(BigDecimal.valueOf(1500));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(102L)).thenReturn(Optional.of(recipientAccount));
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        assertThrows(ServiceException.class, () -> accountService.transferMoney(101L, transferDto),
                "Expected ERR_INSUFFICIENT_FUNDS exception");
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void transferMoney_SameAccount_ThrowsException() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setTargetUserId(101L);
        transferDto.setAmount(BigDecimal.valueOf(200));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.of(senderAccount));
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        assertThrows(ServiceException.class, () -> accountService.transferMoney(101L, transferDto),
                "Expected ERR_TRANSFER_TO_SAME_ACCOUNT exception");
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void transferMoney_InvalidAmount_ThrowsException() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setTargetUserId(102L);
        transferDto.setAmount(BigDecimal.valueOf(-100));

        when(accountRepository.findByUserId(101L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(102L)).thenReturn(Optional.of(recipientAccount));
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        assertThrows(IllegalArgumentException.class, () -> accountService.transferMoney(101L, transferDto),
                "Expected IllegalArgumentException for negative amount");
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void applyInterest_NewAccount_SetsInitialDeposit() {
        Account newAccount = new Account();
        newAccount.setId(3L);
        newAccount.setUser(new User(103L));
        newAccount.setBalance(BigDecimal.valueOf(1000));
        newAccount.setInitialDeposit(null);

        when(accountRepository.findAll()).thenReturn(List.of(newAccount));

        accountService.applyInterest();

        verify(accountRepository).save(argThat(account ->
                account.getInitialDeposit().equals(BigDecimal.valueOf(1000))));
    }

    @Test
    void applyInterest_ExistingAccountBelowMax_IncreasesBalance() {
        Account existingAccount = new Account();
        existingAccount.setId(3L);
        existingAccount.setUser(new User(103L));
        existingAccount.setBalance(BigDecimal.valueOf(1000));
        existingAccount.setInitialDeposit(BigDecimal.valueOf(500));

        when(accountRepository.findAll()).thenReturn(List.of(existingAccount));

        accountService.applyInterest();

        verify(accountRepository).save(argThat(account ->
                account.getBalance().compareTo(BigDecimal.valueOf(1100)) == 0));
    }

    @Test
    void applyInterest_ExistingAccountAtMax_DoesNotIncrease() {
        Account existingAccount = new Account();
        existingAccount.setId(3L);
        existingAccount.setUser(new User(103L));
        existingAccount.setBalance(BigDecimal.valueOf(1000));
        existingAccount.setInitialDeposit(BigDecimal.valueOf(500));

        when(accountRepository.findAll()).thenReturn(List.of(existingAccount));

        accountService.applyInterest();

        verify(accountRepository).save(argThat(account ->
                account.getBalance().compareTo(BigDecimal.valueOf(1000)) == 0));
    }

    @Test
    void applyInterest_ExistingAccountNearMax_IncreasesToMax() {
        Account existingAccount = new Account();
        existingAccount.setId(3L);
        existingAccount.setUser(new User(103L));
        existingAccount.setBalance(BigDecimal.valueOf(900));
        existingAccount.setInitialDeposit(BigDecimal.valueOf(500));

        when(accountRepository.findAll()).thenReturn(List.of(existingAccount));

        accountService.applyInterest();

        verify(accountRepository).save(argThat(account ->
                account.getBalance().compareTo(BigDecimal.valueOf(1000)) == 0));
    }
}