package org.ww.wigglew.service.auth;

import com.twilio.Twilio;

import com.twilio.rest.verify.v2.service.Verification;

import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class SmsSenderService {
    private static final Logger logger = LoggerFactory.getLogger(SmsSenderService.class);
    
    @Value("${twilio.accountSid:DUMMY}")
    public String ACCOUNT_SID;

    @Value("${twilio.authToken:DUMMY}")
    public String AUTH_TOKEN;

    public void sendSMS(String receiver){
        // Skip SMS if using dummy credentials (development mode)
        if ("DUMMY".equals(ACCOUNT_SID) || ACCOUNT_SID.startsWith("AC_DUMMY") || ACCOUNT_SID.startsWith("DUMMY")) {
            logger.info("SMS sending skipped (development mode). Would have sent to: {}", receiver);
            return;
        }
        
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Verification verification =
                Verification.creator("VAd18c417e0717b41273894de00bc41df9", receiver, "sms").create();
    }

    public boolean checkVerificationCode(String receiver, String code) {
        // Auto-approve in development mode
        if ("DUMMY".equals(ACCOUNT_SID) || ACCOUNT_SID.startsWith("AC_DUMMY") || ACCOUNT_SID.startsWith("DUMMY")) {
            logger.info("OTP verification skipped (development mode). Auto-approving for: {}", receiver);
            return true; // Auto-approve in development mode
        }
        
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        VerificationCheck verificationCheck = VerificationCheck.creator(
                        "VAd18c417e0717b41273894de00bc41df9")
                .setTo(receiver)
                .setCode(code)
                .create();

        return "approved".equals(verificationCheck.getStatus()); // Check if the status is 'approved'
    }
}
