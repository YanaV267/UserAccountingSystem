package software.pxel.accounting.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts") //изменила название таблицы в соответствии с корректным неймингом
public class Account implements Serializable {

    private static final String FIELD_ID = "id";
    private static final String FIELD_JOIN_FIELD_USER_ID = "user_id";
    private static final String FIELD_BALANCE = "balance";
    private static final String FIELD_INITIAL_DEPOSIT = "initial_deposit";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = FIELD_ID)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = FIELD_JOIN_FIELD_USER_ID)
    private User user;

    @Column(name = FIELD_BALANCE)
    private BigDecimal balance;

    @Column(name = FIELD_INITIAL_DEPOSIT)
    private BigDecimal initialDeposit;

}