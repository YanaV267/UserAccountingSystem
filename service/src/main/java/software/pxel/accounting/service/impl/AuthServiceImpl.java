package software.pxel.accounting.service.impl;

import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.pxel.accounting.dto.AuthResponseDto;
import software.pxel.accounting.dto.email.EmailLoginDto;
import software.pxel.accounting.dto.phone.PhoneLoginDto;
import software.pxel.accounting.service.AuthService;
import software.pxel.accounting.util.JwtTokenProvider;
import software.pxel.accounting.util.UserPrincipal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponseDto authenticateWithEmail(EmailLoginDto dto) {
        log.debug("Attempting email authentication for: {}", dto.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),
                            dto.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);
            log.info("Successful email authentication for: {}", dto.getEmail());

            return new AuthResponseDto(token);
        } catch (Exception e) {
            log.error("Email authentication failed for: {}. Reason: {}", dto.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Override
    public AuthResponseDto authenticateWithPhone(PhoneLoginDto dto) {
        log.debug("Attempting phone authentication for: {}", dto.getPhone());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getPhone(),
                            dto.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);
            log.info("Successful phone authentication for: {}", dto.getPhone());

            return new AuthResponseDto(token);
        } catch (Exception e) {
            log.error("Phone authentication failed for: {}. Reason: {}", dto.getPhone(), e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof UserPrincipal) {
                Long userId = ((UserPrincipal) principal).getId();
                log.debug("Retrieved user ID from UserPrincipal: {}", userId);
                return userId;
            } else if (principal instanceof Jwt) {
                Long userId = jwtTokenProvider.getUserIdFromToken(principal.toString());
                log.debug("Retrieved user ID from JWT: {}", userId);
                return userId;
            }

            String principalType = principal != null ? principal.getClass().getName() : "null";
            log.error("Unexpected principal type: {}", principalType);
            throw new IllegalStateException("Unexpected principal type: " + principalType);
        } catch (Exception e) {
            log.error("Failed to get user ID: {}", e.getMessage());
            throw e;
        }
    }
}