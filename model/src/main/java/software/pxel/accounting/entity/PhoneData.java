package software.pxel.accounting.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "phone_data")
public class PhoneData extends AbstractData {
    private static final String FIELD_JOIN_FIELD_USER_ID = "user_id";

    @ManyToOne
    @JoinColumn(name = FIELD_JOIN_FIELD_USER_ID)
    private User user;

    public PhoneData(String value) {
        super(value);
    }
}

