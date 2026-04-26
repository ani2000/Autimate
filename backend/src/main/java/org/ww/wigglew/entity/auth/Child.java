package org.ww.wigglew.entity.auth;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Child {
    private String id;
    private String name;
    private int gender; //male = 1, female = 0
    private LocalDate dob;
    private boolean activeSession;

    public int getAgeInMonths() {
        return LocalDate.now().getYear() * 12 + LocalDate.now().getMonthValue() - (dob.getYear() * 12 + dob.getMonthValue());
    }
}
