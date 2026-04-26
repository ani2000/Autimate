package org.ww.wigglew.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "call")
public class CallEntity {
    @Id
    private String id;
    private String callId;
}