package org.ww.wigglew.entity.speech;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "speech_animals")
public class SpeechAnimalEntity {
    @Id
    private String id;
    private String name;
    private String soundText;
}
