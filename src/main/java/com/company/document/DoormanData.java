package com.company.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("doorman_data")
@Data
@NoArgsConstructor
public class DoormanData {

    private String xmlData;

    @Indexed(name = "date_time_index", direction = IndexDirection.ASCENDING, expireAfterSeconds = 86400)
    private Date dateTime;

    private Boolean isProcessed;
    private String batchUUID;
}
