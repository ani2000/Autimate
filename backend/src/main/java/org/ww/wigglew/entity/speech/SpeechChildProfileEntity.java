package org.ww.wigglew.entity.speech;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "speech_children")
public class SpeechChildProfileEntity {
    @Id
    private String id;
    private String childId;
    private String name;
    private Integer age;
    private String therapyLevel;
    private LocalDateTime createdAt;
}
