package software.pxel.accounting;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.pxel.accounting.dto.error.ExceptionDto;
import software.pxel.accounting.exception.ServiceException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionDto> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionDto(
                        HttpStatus.FORBIDDEN.value(),
                        ex.getMessage(),
                        generateCausesList(ex)));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ExceptionDto> handleServiceException(ServiceException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ExceptionDto(
                        ex.getStatus(),
                        ex.getCode().name(),
                        generateCausesList(ex)));
    }

    private List<String> generateCausesList(Throwable e) {
        List<String> causes = new ArrayList<>();
        while (e != null) {
            causes.add(e.getMessage());
            e = e.getCause();
        }
        return causes;
    }
}