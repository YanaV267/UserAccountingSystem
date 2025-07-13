package software.pxel.accounting.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.phone.PhoneCreateDto;
import software.pxel.accounting.dto.phone.PhoneUpdateDto;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.repository.DataRepository;
import software.pxel.accounting.repository.UserRepository;
import software.pxel.accounting.service.DataService;

import javax.transaction.Transactional;

import static software.pxel.accounting.exception.ServiceException.Code.*;

@Service
public class PhoneServiceImpl extends DataService<PhoneData, PhoneCreateDto, PhoneUpdateDto> {

    private static final Logger log = LoggerFactory.getLogger(PhoneServiceImpl.class);

    public PhoneServiceImpl(
            UserRepository userRepository,
            DataRepository<PhoneData> dataRepository
    ) {
        super(userRepository, dataRepository);
    }

    @Override
    @Transactional
    public void create(Long userId, PhoneCreateDto dto) {
        log.info("Creating phone for user ID: {}, phone: {}", userId, dto.getPhone());

        if (dataRepository.existsByValue(dto.getPhone())) {
            log.warn("Phone already exists: {}", dto.getPhone());
            throw new ServiceException(ERR_PHONE_ALREADY_IN_USE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ServiceException(ERR_USER_NOT_FOUND);
                });

        PhoneData phoneData = new PhoneData();
        phoneData.setValue(dto.getPhone());
        phoneData.setUser(user);
        user.getPhoneData().add(phoneData);

        log.debug("Saving phone data: {}", phoneData);
        userRepository.save(user);
        log.info("Phone created successfully for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void update(Long userId, PhoneUpdateDto dto) {
        log.info("Updating phone for user ID: {}, old phone: {}, new phone: {}",
                userId, dto.getOldValue(), dto.getNewPhone());

        if (dataRepository.existsByValue(dto.getNewPhone())) {
            log.warn("New phone already exists: {}", dto.getNewPhone());
            throw new ServiceException(ERR_PHONE_ALREADY_IN_USE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ServiceException(ERR_USER_NOT_FOUND);
                });

        user.getPhoneData().stream().findAny()
                .orElseThrow(() -> {
                    log.error("No phones found for user ID: {}", userId);
                    return new ServiceException(ERR_PHONE_NOT_FOUND);
                });

        user.getPhoneData().removeIf(e -> e.getValue().equals(dto.getOldValue()));

        PhoneData newPhone = new PhoneData();
        newPhone.setValue(dto.getNewPhone());
        newPhone.setUser(user);
        user.getPhoneData().add(newPhone);

        log.debug("Saving updated phone data: {}", newPhone);
        userRepository.save(user);
        log.info("Phone updated successfully for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void delete(Long userId, String data) {
        log.info("Deleting phone for user ID: {}, phone: {}", userId, data);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ServiceException(ERR_USER_NOT_FOUND);
                });

        if (user.getPhoneData().size() <= 1) {
            log.warn("Attempt to delete the only phone for user ID: {}", userId);
            throw new ServiceException(ERR_THE_ONLY_PHONE);
        }

        user.getPhoneData().stream().findAny()
                .orElseThrow(() -> {
                    log.error("No phones found for user ID: {}", userId);
                    return new ServiceException(ERR_PHONE_NOT_FOUND);
                });

        boolean removed = user.getPhoneData().removeIf(e ->
                e.getValue().equals(data) && e.getUser().getId().equals(userId));

        if (removed) {
            log.debug("Phone removed, saving user: {}", user);
            userRepository.save(user);
            log.info("Phone deleted successfully for user ID: {}", userId);
        } else {
            log.warn("Phone not found for deletion: user ID: {}, phone: {}", userId, data);
        }
    }
}