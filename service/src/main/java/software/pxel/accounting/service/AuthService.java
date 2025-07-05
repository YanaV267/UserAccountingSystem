package software.pxel.accounting.service;

import software.pxel.accounting.dto.AuthResponseDto;
import software.pxel.accounting.dto.email.EmailLoginDto;
import software.pxel.accounting.dto.phone.PhoneLoginDto;

public interface AuthService {

    AuthResponseDto authenticateWithEmail(EmailLoginDto dto);

    AuthResponseDto authenticateWithPhone(PhoneLoginDto dto);

    Long getUserId(String token);
}