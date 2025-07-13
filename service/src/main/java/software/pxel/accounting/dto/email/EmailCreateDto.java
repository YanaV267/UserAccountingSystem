package software.pxel.accounting.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.pxel.accounting.dto.AbstractDataCreateDto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailCreateDto extends AbstractDataCreateDto {
    @Email(message = "Email isn't correct")
    @NotBlank(message = "Email is not provided")
    private String email;

}