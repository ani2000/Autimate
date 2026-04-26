package org.ww.wigglew.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class PasswordChangeRequest {
    private String phone;
    private String previousPassword;
    private String newPassword;
}
