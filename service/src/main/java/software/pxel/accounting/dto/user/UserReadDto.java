package software.pxel.accounting.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserReadDto {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private Set<String> emailData;
    private Set<String> phoneData;
    private BigDecimal balance;
}