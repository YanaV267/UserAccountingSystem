package software.pxel.accounting.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.pxel.accounting.dto.email.EmailReadDto;
import software.pxel.accounting.dto.phone.PhoneReadDto;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReadDto {
    private Long id;

    private String name;

    private String dateOfBirth;

    private Set<EmailReadDto> emailData;
    private Set<PhoneReadDto> phoneData;
}