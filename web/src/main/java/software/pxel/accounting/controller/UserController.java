package software.pxel.accounting.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.PageCacheDto;
import software.pxel.accounting.dto.user.UserSearchDto;
import software.pxel.accounting.service.UserService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "Users")
public class UserController {
    private final UserService service;

    @GetMapping("/search")
    public ResponseEntity<PageCacheDto> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserSearchDto request = new UserSearchDto(name, dateOfBirth, email, phone, page, size);

        return ResponseEntity.ok(service.searchUsers(request));
    }
}