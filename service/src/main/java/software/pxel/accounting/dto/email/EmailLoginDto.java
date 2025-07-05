package software.pxel.accounting.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 500)
    private String password;
}
