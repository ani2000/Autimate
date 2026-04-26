package org.ww.wigglew.entity.speech;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "speech_attempts")
public class SpeechAttemptEntity {
    @Id
    private String id;
    private String sessionId;
    private String childId;
    private String targetText;
    private String voiceType;
    private String transcript;
    private Double similarity;
    private Boolean correct;
    private LocalDateTime createdAt;
}
