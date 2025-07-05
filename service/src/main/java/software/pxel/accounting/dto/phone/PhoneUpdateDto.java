package software.pxel.accounting.dto.phone;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.pxel.accounting.dto.AbstractDataUpdateDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static software.pxel.accounting.util.ValidationRule.PHONE_NUMBER_REGEX;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PhoneUpdateDto extends AbstractDataUpdateDto {
    @NotBlank
    @Pattern(regexp = PHONE_NUMBER_REGEX, message = "Phone must be in format 79157654321")
    private String newPhone;

}
