package org.ww.wigglew.entity.speech;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "speech_sessions")
public class SpeechSessionEntity {
    @Id
    private String id;
    private String sessionId;
    private String childId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
}
