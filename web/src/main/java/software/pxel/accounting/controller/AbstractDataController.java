package software.pxel.accounting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.AbstractDataUpdateDto;
import software.pxel.accounting.entity.AbstractData;
import software.pxel.accounting.service.DataService;
import software.pxel.accounting.util.JwtTokenProvider;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public abstract class AbstractDataController<E extends AbstractData, U extends AbstractDataUpdateDto> {
    private final JwtTokenProvider jwtTokenProvider;
    private final DataService<E, U> service;

    @PostMapping
    public ResponseEntity<Void> create(
            @PathVariable Long userId,
            @RequestBody @Valid String value,
            @RequestHeader("Authorization") String token) {
        validateOwnership(userId, token);
        service.create(userId, value);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(
            @PathVariable Long userId,
            @RequestBody @Valid U dto,
            @RequestHeader("Authorization") String token) {
        validateOwnership(userId, token);
        service.update(userId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @RequestBody @Valid String value,
            @RequestHeader("Authorization") String token) {
        validateOwnership(userId, token);
        service.delete(userId, value);
        return ResponseEntity.noContent().build();
    }

    private void validateOwnership(Long userId, String token) {
        Long authenticatedUserId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only modify your own data");
        }
    }
}