package software.pxel.accounting.entity.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "users")
public class UserDocument implements Serializable {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Date)
    private LocalDate dateOfBirth;

    @Field(type = FieldType.Nested)
    private Set<EmailDataDocument> emailData;

    @Field(type = FieldType.Nested)
    private Set<PhoneDataDocument> phoneData;

}
