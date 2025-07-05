package software.pxel.accounting.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ServiceException extends RuntimeException {
    private final int status;
    private final Code code;
    private final String message;

    @Getter
    @AllArgsConstructor
    public enum Code {
        ERR_USER_NOT_FOUND("User with provided ID wasn't found", HttpStatus.NOT_FOUND),
        ERR_EMAIL_NOT_FOUND("User with provided email wasn't found", HttpStatus.NOT_FOUND),
        ERR_PHONE_NOT_FOUND("User with provided phone value wasn't found", HttpStatus.NOT_FOUND),
        ERR_PHONE_ALREADY_IN_USE("Current phone value is already in use", HttpStatus.BAD_REQUEST),
        ERR_EMAIL_ALREADY_IN_USE("Current email is already in use", HttpStatus.BAD_REQUEST),
        ERR_THE_ONLY_PHONE("The only phone can't be deleted", HttpStatus.BAD_REQUEST),
        ERR_THE_ONLY_EMAIL("The only email can't be deleted", HttpStatus.BAD_REQUEST),
        ERR_NO_EMAIL_AND_PHONE("The only email can't be deleted", HttpStatus.BAD_REQUEST);

        private final String message;
        private final HttpStatus status;
    }

    public ServiceException(Code code, Object... params) {
        super(String.format(code.getMessage(), params));
        this.status = code.getStatus().value();
        this.code = code;
        this.message = String.format(code.getMessage(), params);
    }
}
