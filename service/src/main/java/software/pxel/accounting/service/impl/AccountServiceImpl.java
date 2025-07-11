package software.pxel.accounting.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import software.pxel.accounting.dto.account.AccountReadDto;
import software.pxel.accounting.dto.account.AccountUpdateDto;
import software.pxel.accounting.dto.account.TransferRequestDto;
import software.pxel.accounting.entity.Account;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.repository.AccountRepository;
import software.pxel.accounting.service.AccountService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static software.pxel.accounting.exception.ServiceException.Code.ERR_ACCOUNT_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_INSUFFICIENT_FUNDS;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_RECEIVER_ACCOUNT_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_SENDER_ACCOUNT_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_TRANSFER_TO_SAME_ACCOUNT;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    @Value("${balance.increase-percentage}")
    private String balanceIncreasePercentage;

    @Value("${balance.max-multiplier}")
    private String balanceMaxMultiplier;

    @Transactional
    @Scheduled(fixedRate = 30000)
    public void applyInterest() {
        List<Account> accounts = accountRepository.findAll();
        BigDecimal maxAllowedMultiplier = new BigDecimal(balanceIncreasePercentage)
                .divide(BigDecimal.valueOf(100), RoundingMode.CEILING);
        BigDecimal balanceIncreaseValue = new BigDecimal(balanceIncreasePercentage)
                .divide(BigDecimal.valueOf(100), RoundingMode.CEILING)
                .add(BigDecimal.ONE);
        for (Account account : accounts) {
            if (account.getInitialDeposit() == null) {
                account.setInitialDeposit(account.getBalance());
                accountRepository.save(account);
                continue;
            }

            BigDecimal currentBalance = account.getBalance();
            BigDecimal initialDeposit = account.getInitialDeposit();
            BigDecimal maxAllowedBalance = initialDeposit.multiply(maxAllowedMultiplier);

            if (currentBalance.compareTo(maxAllowedBalance) < 0) {
                BigDecimal newBalance = currentBalance.multiply(balanceIncreaseValue);
                if (newBalance.compareTo(maxAllowedBalance) > 0) {
                    newBalance = maxAllowedBalance;
                }
                account.setBalance(newBalance);
                accountRepository.save(account);

                log.info("Updated balance for account {}: {} -> {}",
                        account.getId(), currentBalance, newBalance);
            }
        }
    }

    @Override
    @Transactional
    public void transferMoney(Long senderUserId, TransferRequestDto dto) {
        executeInTransaction(() -> {
            Account senderAccount = accountRepository.findByUserId(senderUserId)
                    .orElseThrow(() -> new ServiceException(ERR_SENDER_ACCOUNT_NOT_FOUND));

            Account recipientAccount = accountRepository.findByUserId(dto.getTargetUserId())
                    .orElseThrow(() -> new ServiceException(ERR_RECEIVER_ACCOUNT_NOT_FOUND));

            validateTransfer(senderAccount, recipientAccount, dto.getAmount());

            senderAccount.setBalance(senderAccount.getBalance().subtract(dto.getAmount()));
            recipientAccount.setBalance(recipientAccount.getBalance().add(dto.getAmount()));

            accountRepository.save(senderAccount);
            accountRepository.save(recipientAccount);

            log.info("Transfer completed from user {} to user {}", senderUserId, dto.getTargetUserId());
        });
    }

    private void validateTransfer(Account sender, Account recipient, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for transfer in amount {}", amount);
            throw new ServiceException(ERR_INSUFFICIENT_FUNDS);
        }

        if (sender.getId().equals(recipient.getId())) {
            log.warn("Attempt to transfer to same account: {}", sender.getId());
            throw new ServiceException(ERR_TRANSFER_TO_SAME_ACCOUNT);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid transfer amount: {}", amount);
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
    }

    private void executeInTransaction(Runnable operation) {
        TransactionStatus status = transactionManager.getTransaction(
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            operation.run();
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "accounts", key = "#userId")
    public BigDecimal getBalance(Long userId) {
        log.debug("Fetching balance for user: {}", userId);
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ServiceException(ERR_ACCOUNT_NOT_FOUND))
                .getBalance();
    }

    @Override
    @Transactional
    @CacheEvict(value = "accounts", key = "#userId")
    public AccountReadDto updateBalance(AccountUpdateDto dto) {
        Account account = accountRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new ServiceException(ERR_ACCOUNT_NOT_FOUND));

        log.info("Updating balance for user {}", dto.getUserId());
        account.setBalance(dto.getBalance());
        accountRepository.save(account);
        return mapper.map(account, AccountReadDto.class);
    }
}
