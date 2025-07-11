package software.pxel.accounting.entity.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneDataDocument implements Serializable {
    @Field(type = FieldType.Text)
    private String value;
}

