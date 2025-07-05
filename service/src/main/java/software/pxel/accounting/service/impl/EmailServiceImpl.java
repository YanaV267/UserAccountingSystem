package software.pxel.accounting.service.impl;

import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.email.EmailUpdateDto;
import software.pxel.accounting.entity.EmailData;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.repository.DataRepository;
import software.pxel.accounting.repository.UserRepository;
import software.pxel.accounting.service.DataService;

import javax.transaction.Transactional;

import static software.pxel.accounting.exception.ServiceException.Code.ERR_EMAIL_ALREADY_IN_USE;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_EMAIL_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_THE_ONLY_EMAIL;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_USER_NOT_FOUND;

@Service
public class EmailServiceImpl extends DataService<EmailData, EmailUpdateDto> {


    public EmailServiceImpl(
            UserRepository userRepository,
            DataRepository<EmailData> dataRepository
    ) {
        super(userRepository, dataRepository);
    }

    @Override
    @Transactional
    public void create(Long userId, String email) {
        if (dataRepository.existsByValue(email)) {
            throw new ServiceException(ERR_EMAIL_ALREADY_IN_USE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));

        EmailData emailData = new EmailData();
        emailData.setValue(email);
        emailData.setUser(user);
        user.getEmailData().add(emailData);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void update(Long userId, EmailUpdateDto dto) {
        if (dataRepository.existsByValue(dto.getNewEmail())) {
            throw new ServiceException(ERR_EMAIL_ALREADY_IN_USE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));
        user.getEmailData().stream().findAny()
                .orElseThrow(() -> new ServiceException(ERR_EMAIL_NOT_FOUND));

        user.getEmailData().removeIf(e -> e.getValue().equals(dto.getOldValue()));

        EmailData newEmail = new EmailData();
        newEmail.setValue(dto.getNewEmail());
        newEmail.setUser(user);
        user.getEmailData().add(newEmail);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));

        if (user.getEmailData().size() <= 1) {
            throw new ServiceException(ERR_THE_ONLY_EMAIL);
        }
        user.getEmailData().stream().findAny()
                .orElseThrow(() -> new ServiceException(ERR_EMAIL_NOT_FOUND));

        user.getEmailData().removeIf(e ->
                e.getValue().equals(email) && e.getUser().getId().equals(userId));

        userRepository.save(user);
    }
}