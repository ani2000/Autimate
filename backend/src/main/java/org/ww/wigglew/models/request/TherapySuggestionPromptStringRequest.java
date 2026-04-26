package org.ww.wigglew.models.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TherapySuggestionPromptStringRequest {
    private String childId;
    private String prompt;
    private String request_id;

}
