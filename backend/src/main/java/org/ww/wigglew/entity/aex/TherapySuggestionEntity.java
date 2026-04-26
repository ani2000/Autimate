package org.ww.wigglew.entity.aex;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "therapy_suggestion")
public class TherapySuggestionEntity {
    @Id
    private String id;

    private String childId;

    @Indexed(unique = true)
    private String requestId;
    private String suggestedTherapy;
}
