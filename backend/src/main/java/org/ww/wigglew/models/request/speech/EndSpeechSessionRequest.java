package org.ww.wigglew.models.request.speech;

import lombok.Data;

@Data
public class EndSpeechSessionRequest {
    private String sessionId;
    private Long duration;
}
