package org.ww.wigglew.models.request.speech;

import lombok.Data;

@Data
public class CreateSpeechChildRequest {
    private String name;
    private Integer age;
    private String therapyLevel;
}
