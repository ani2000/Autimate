package org.ww.wigglew.models.request.speech;

import lombok.Data;

@Data
public class SpeakRequest {
    private String text;
    private String voiceType;
    private Double pitch;
    private Double speed;
    private Double volume;
}
