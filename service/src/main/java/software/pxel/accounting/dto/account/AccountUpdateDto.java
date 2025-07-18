package software.pxel.accounting.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateDto {
    private Long userId;
    private BigDecimal balance;
}
