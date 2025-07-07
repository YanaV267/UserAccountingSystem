package software.pxel.accounting.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") //изменила название таблицы в соответствии с корректным неймингом
public class User implements Serializable {
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_DATE_OF_BIRTH = "date_of_birth";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_JOIN_FIELD_USER = "user";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = FIELD_ID)
    private Long id;

    @Column(name = FIELD_NAME)
    private String name;

    @Column(name = FIELD_DATE_OF_BIRTH)
    private LocalDate dateOfBirth;

    @Column(name = FIELD_PASSWORD)
    private char[] password;

    @OneToOne(mappedBy = FIELD_JOIN_FIELD_USER, cascade = CascadeType.ALL)
    private Account account;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = FIELD_JOIN_FIELD_USER, cascade = CascadeType.ALL)
    private Set<EmailData> emailData = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = FIELD_JOIN_FIELD_USER, cascade = CascadeType.ALL)
    private Set<PhoneData> phoneData = new HashSet<>();

    public User(Long id) {
        this.id = id;
    }
}