package software.pxel.accounting.dto.phone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static software.pxel.accounting.util.ValidationRule.PHONE_NUMBER_REGEX;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneLoginDto {
    @Pattern(regexp = PHONE_NUMBER_REGEX, message = "Phone must be in format 79157654321")
    @NotBlank
    private String phone;

    @NotBlank
    @Size(min = 8, max = 500)
    private String password;
}