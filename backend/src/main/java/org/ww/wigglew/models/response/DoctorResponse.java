package org.ww.wigglew.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ww.wigglew.entity.doctor.DoctorEntity;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponse {
    private boolean responseStat;
    private String responseMessage;
    private List<DoctorEntity> doctors;
}
