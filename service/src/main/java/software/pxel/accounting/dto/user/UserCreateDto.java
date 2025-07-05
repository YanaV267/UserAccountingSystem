package software.pxel.accounting.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    @Size(min = 8, max = 500)
    private String password;

    @NotEmpty
    private Set<@Email String> emails;

    @NotEmpty
    private Set<@Pattern(regexp = "^79\\d{9}$") String> phones;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal initialBalance;
}
