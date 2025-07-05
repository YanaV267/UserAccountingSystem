package software.pxel.accounting.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import software.pxel.accounting.entity.EmailData;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.entity.User;
import software.pxel.accounting.exception.ServiceException;
import software.pxel.accounting.repository.EmailDataRepository;
import software.pxel.accounting.repository.PhoneDataRepository;
import software.pxel.accounting.repository.UserRepository;
import software.pxel.accounting.util.UserPrincipal;

import java.util.ArrayList;
import java.util.List;

import static software.pxel.accounting.exception.ServiceException.Code.ERR_EMAIL_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_NO_EMAIL_AND_PHONE;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_PHONE_NOT_FOUND;
import static software.pxel.accounting.exception.ServiceException.Code.ERR_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long userId;
        if (username.contains("@")) {
            EmailData emailData = emailDataRepository.findByValue(username)
                    .orElseThrow(() -> new ServiceException(ERR_EMAIL_NOT_FOUND));

            userId = emailData.getUser().getId();
        } else {
            PhoneData phoneData = phoneDataRepository.findByValue(username)
                    .orElseThrow(() -> new ServiceException(ERR_PHONE_NOT_FOUND));
            userId = phoneData.getUser().getId();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));
        return new UserPrincipal(user.getId(), username, String.valueOf(user.getPassword()));
    }

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ERR_USER_NOT_FOUND));

        List<EmailData> emails = emailDataRepository.findByUserId(id);
        List<PhoneData> phones = phoneDataRepository.findByUserId(id);

        if (emails.isEmpty() && phones.isEmpty()) {
            throw new ServiceException(ERR_NO_EMAIL_AND_PHONE);
        }

        List<String> possibleUsernames = new ArrayList<>();
        emails.forEach(email -> possibleUsernames.add(email.getValue()));
        phones.forEach(phone -> possibleUsernames.add(phone.getValue()));

        String username = possibleUsernames.stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException(ERR_NO_EMAIL_AND_PHONE));

        return new UserPrincipal(user.getId(), username, String.valueOf(user.getPassword()));
    }
}
