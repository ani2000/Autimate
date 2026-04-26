package org.ww.wigglew.models.response;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String fullName;
    private Boolean verificationStatus;
    private Boolean requestSuccess;
    private String requestMessage;
}
