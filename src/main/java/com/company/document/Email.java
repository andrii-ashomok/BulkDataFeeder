package com.company.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("emails")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    @Indexed
    private String email;
    private long count;
}
