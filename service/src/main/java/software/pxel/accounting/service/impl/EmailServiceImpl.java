package software.pxel.accounting.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.email.EmailCreateDto;
import software.pxel.accounting.dto.email.EmailUpdateDto;
import software.pxel.accounting.entity.EmailData;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.repository.DataRepository;
import software.pxel.accounting.repository.UserRepository;
import software.pxel.accounting.service.DataService;

import javax.transaction.Transactional;

import static software.pxel.accounting.exception.ServiceException.Code.*;

@Service
public class EmailServiceImpl extends DataService<EmailData, EmailCreateDto, EmailUpdateDto> {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    public EmailServiceImpl(
            UserRepository userRepository,
            DataRepository<EmailData> dataRepository
    ) {
        super(userRepository, dataRepository);
    }

    @Override
    @Transactional
    public void create(Long userId, EmailCreateDto dto) {
        log.info("Creating email for user ID: {}, email: {}", userId, dto.getEmail());

        if (dataRepository.existsByValue(dto.getEmail())) {
            log.warn("Email already exists: {}", dto.getEmail());
            throw new ServiceException(ERR_EMAIL_ALREADY_IN_USE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ServiceException(ERR_USER_NOT_FOUND);
                });

        EmailData emailData = new EmailData();
        emailData.setValue(dto.getEmail());
        emailData.setUser(user);
        user.getEmailData().add(emailData);

        log.debug("Saving email data: {}", emailData);
        userRepository.save(user);
        log.info("Email created successfully for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void update(Long userId, EmailUpdateDto dto) {
        log.info("Updating email for user ID: {}, old email: {}, new email: {}",
                userId, dto.getOldValue(), dto.getNewEmail());

        if (dataRepository.existsByValue(dto.getNewEmail())) {
            log.warn("New email already exists: {}", dto.getNewEmail());
            throw new ServiceException(ERR_EMAIL_ALREADY_IN_USE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ServiceException(ERR_USER_NOT_FOUND);
                });

        user.getEmailData().stream().findAny()
                .orElseThrow(() -> {
                    log.error("No emails found for user ID: {}", userId);
                    return new ServiceException(ERR_EMAIL_NOT_FOUND);
                });

        user.getEmailData().removeIf(e -> e.getValue().equals(dto.getOldValue()));

        EmailData newEmail = new EmailData();
        newEmail.setValue(dto.getNewEmail());
        newEmail.setUser(user);
        user.getEmailData().add(newEmail);

        log.debug("Saving updated email data: {}", newEmail);
        userRepository.save(user);
        log.info("Email updated successfully for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void delete(Long userId, String email) {
        log.info("Deleting email for user ID: {}, email: {}", userId, email);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ServiceException(ERR_USER_NOT_FOUND);
                });

        if (user.getEmailData().size() <= 1) {
            log.warn("Attempt to delete the only email for user ID: {}", userId);
            throw new ServiceException(ERR_THE_ONLY_EMAIL);
        }

        user.getEmailData().stream().findAny()
                .orElseThrow(() -> {
                    log.error("No emails found for user ID: {}", userId);
                    return new ServiceException(ERR_EMAIL_NOT_FOUND);
                });

        boolean removed = user.getEmailData().removeIf(e ->
                e.getValue().equals(email) && e.getUser().getId().equals(userId));

        if (removed) {
            log.debug("Email removed, saving user: {}", user);
            userRepository.save(user);
            log.info("Email deleted successfully for user ID: {}", userId);
        } else {
            log.warn("Email not found for deletion: user ID: {}, email: {}", userId, email);
        }
    }
}