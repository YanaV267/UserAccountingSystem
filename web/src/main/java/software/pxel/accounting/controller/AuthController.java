package software.pxel.accounting.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.AuthResponseDto;
import software.pxel.accounting.dto.email.EmailLoginDto;
import software.pxel.accounting.dto.phone.PhoneLoginDto;
import software.pxel.accounting.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "Authentication")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login/email")
    public ResponseEntity<AuthResponseDto> loginWithEmail(@RequestBody EmailLoginDto request) {
        return ResponseEntity.ok(authService.authenticateWithEmail(request));
    }

    @PostMapping("/login/phone")
    public ResponseEntity<AuthResponseDto> loginWithPhone(@RequestBody PhoneLoginDto request) {
        return ResponseEntity.ok(authService.authenticateWithPhone(request));
    }
}
