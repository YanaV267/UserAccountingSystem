package software.pxel.accounting.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.pxel.accounting.dto.AbstractDataUpdateDto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailUpdateDto extends AbstractDataUpdateDto {
    @Email
    @NotBlank
    private String newEmail;

}