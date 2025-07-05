package software.pxel.accounting.service;

import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.phone.PhoneUpdateDto;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.repository.DataRepository;
import software.pxel.accounting.repository.UserRepository;

import javax.transaction.Transactional;

import static software.pxel.accounting.exception.ServiceException.Code.ERR_PHONE_ALREADY_IN_USE;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_PHONE_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_THE_ONLY_PHONE;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_USER_NOT_FOUND;

@Service
public class PhoneService extends DataService<PhoneData, PhoneUpdateDto> {

    public PhoneService(
            UserRepository userRepository,
            DataRepository<PhoneData> dataRepository
    ) {
        super(userRepository, dataRepository);
    }

    @Override
    @Transactional
    public void create(Long userId, String phone) {
        if (dataRepository.existsByValue(phone)) {
            throw new ServiceException(ERR_PHONE_ALREADY_IN_USE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));

        PhoneData phoneData = new PhoneData();
        phoneData.setValue(phone);
        phoneData.setUser(user);
        user.getPhoneData().add(phoneData);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void update(Long userId, PhoneUpdateDto dto) {
        if (dataRepository.existsByValue(dto.getNewPhone())) {
            throw new ServiceException(ERR_PHONE_ALREADY_IN_USE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));
        user.getPhoneData().stream().findAny()
                .orElseThrow(() -> new ServiceException(ERR_PHONE_NOT_FOUND));

        user.getPhoneData().removeIf(e -> e.getValue().equals(dto.getOldValue()));

        PhoneData newPhone = new PhoneData();
        newPhone.setValue(dto.getNewPhone());
        newPhone.setUser(user);
        user.getPhoneData().add(newPhone);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long userId, String data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));

        if (user.getPhoneData().size() <= 1) {
            throw new ServiceException(ERR_THE_ONLY_PHONE);
        }
        user.getPhoneData().stream().findAny()
                .orElseThrow(() -> new ServiceException(ERR_PHONE_NOT_FOUND));

        user.getPhoneData().removeIf(e ->
                e.getValue().equals(data) && e.getUser().getId().equals(userId));

        userRepository.save(user);
    }
}