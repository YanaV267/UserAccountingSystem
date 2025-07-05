package software.pxel.accounting.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    @NotNull
    private Long targetUserId;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}