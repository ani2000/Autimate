package org.ww.wigglew.models.request.speech;

import lombok.Data;

@Data
public class RecordVoiceRequest {
    private String sessionId;
    private String childId;
    private String targetText;
    private String voiceType;
    private String transcript;
    private Double similarity;
    private Boolean correct;
}
