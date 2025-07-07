package software.pxel.accounting.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.account.AccountReadDto;
import software.pxel.accounting.dto.account.AccountUpdateDto;
import software.pxel.accounting.dto.account.TransferRequestDto;
import software.pxel.accounting.service.AccountService;
import software.pxel.accounting.service.AuthService;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "AccountController", description = "Balance & transfers")
public class AccountController {
    private final AccountService accountService;
    private final AuthService authService;

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(
            @RequestBody TransferRequestDto dto,
            @RequestHeader("Authorization") String token) {
        Long userId = authService.getUserId(token);
        accountService.transferMoney(userId, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable @Min(1) Long userId) {
        BigDecimal balance = accountService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/balance")
    public ResponseEntity<AccountReadDto> updateBalance(@RequestBody AccountUpdateDto dto) {
        return ResponseEntity.ok(accountService.updateBalance(dto));
    }
}

