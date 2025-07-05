package software.pxel.accounting.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationRule {
    public static final String PHONE_NUMBER_REGEX = "^79\\d{9}$";
}
