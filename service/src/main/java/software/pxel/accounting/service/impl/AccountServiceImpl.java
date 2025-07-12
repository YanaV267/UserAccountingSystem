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
        log.info("Starting interest application process");
        List<Account> accounts = accountRepository.findAll();
        log.debug("Found {} accounts for interest processing", accounts.size());

        BigDecimal interestRate = new BigDecimal(balanceIncreasePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal maxMultiplier = new BigDecimal(balanceMaxMultiplier)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        for (Account account : accounts) {
            if (account.getInitialDeposit() == null) {
                log.debug("Setting initial deposit for account {}: {}", account.getId(), account.getBalance());
                account.setInitialDeposit(account.getBalance());
                accountRepository.save(account);
            }

            BigDecimal currentBalance = account.getBalance();
            BigDecimal initialDeposit = account.getInitialDeposit();
            BigDecimal maxAllowedBalance = initialDeposit.multiply(maxMultiplier);

            if (currentBalance.compareTo(maxAllowedBalance) >= 0) {
                log.debug("Account {} already at max allowed balance: {} (max: {})",
                        account.getId(), currentBalance, maxAllowedBalance);
                continue;
            }

            BigDecimal newBalance = currentBalance.multiply(BigDecimal.ONE.add(interestRate));
            if (newBalance.compareTo(maxAllowedBalance) > 0) {
                newBalance = maxAllowedBalance;
                log.debug("Limiting balance increase for account {} at max allowed value: {}",
                        account.getId(), maxAllowedBalance);
            }

            account.setBalance(newBalance);
            accountRepository.save(account);

            log.info("Updated balance for account {}: {} -> {} (initial deposit: {})",
                    account.getId(), currentBalance, newBalance, initialDeposit);
        }
        log.info("Interest application process completed");
    }

    @Override
    @Transactional
    public void transferMoney(Long senderUserId, TransferRequestDto dto) {
        log.info("Initiating transfer from user {} to user {} for amount {}",
                senderUserId, dto.getTargetUserId(), dto.getAmount());

        executeInTransaction(() -> {
            Account senderAccount = accountRepository.findByUserId(senderUserId)
                    .orElseThrow(() -> {
                        log.error("Sender account not found for user: {}", senderUserId);
                        return new ServiceException(ERR_SENDER_ACCOUNT_NOT_FOUND);
                    });

            Account recipientAccount = accountRepository.findByUserId(dto.getTargetUserId())
                    .orElseThrow(() -> {
                        log.error("Recipient account not found for user: {}", dto.getTargetUserId());
                        return new ServiceException(ERR_RECEIVER_ACCOUNT_NOT_FOUND);
                    });

            validateTransfer(senderAccount, recipientAccount, dto.getAmount());

            BigDecimal senderBalanceBefore = senderAccount.getBalance();
            BigDecimal recipientBalanceBefore = recipientAccount.getBalance();

            senderAccount.setBalance(senderBalanceBefore.subtract(dto.getAmount()));
            recipientAccount.setBalance(recipientBalanceBefore.add(dto.getAmount()));

            accountRepository.save(senderAccount);
            accountRepository.save(recipientAccount);

            log.info("Transfer completed: {} from user {} ({} -> {}) to user {} ({} -> {})",
                    dto.getAmount(),
                    senderUserId, senderBalanceBefore, senderAccount.getBalance(),
                    dto.getTargetUserId(), recipientBalanceBefore, recipientAccount.getBalance());
        });
    }

    private void validateTransfer(Account sender, Account recipient, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for transfer: account {} has {}, requested {}",
                    sender.getId(), sender.getBalance(), amount);
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
        log.debug("Starting new transaction");
        TransactionStatus status = transactionManager.getTransaction(
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            operation.run();
            transactionManager.commit(status);
            log.debug("Transaction committed successfully");
        } catch (Exception e) {
            log.error("Transaction failed, rolling back", e);
            transactionManager.rollback(status);
            throw e;
        }
    }

    @Override
    @Transactional
    @Cacheable(value = "accounts", key = "#userId")
    public BigDecimal getBalance(Long userId) {
        log.debug("Fetching balance for user: {}", userId);
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Account not found for user: {}", userId);
                    return new ServiceException(ERR_ACCOUNT_NOT_FOUND);
                })
                .getBalance();
    }

    @Override
    @Transactional
    @CacheEvict(value = "accounts", key = "#userId")
    public AccountReadDto updateBalance(AccountUpdateDto dto) {
        log.info("Updating balance for user {} to {}", dto.getUserId(), dto.getBalance());
        Account account = accountRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> {
                    log.error("Account not found for user: {}", dto.getUserId());
                    return new ServiceException(ERR_ACCOUNT_NOT_FOUND);
                });

        BigDecimal oldBalance = account.getBalance();
        account.setBalance(dto.getBalance());
        accountRepository.save(account);

        log.debug("Balance updated for user {}: {} -> {}", dto.getUserId(), oldBalance, dto.getBalance());
        return mapper.map(account, AccountReadDto.class);
    }
}