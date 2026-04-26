package org.ww.wigglew.models.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ChildRequest {
    private String name;
    private LocalDate dob;
    private String gender;
}
