package org.ww.wigglew.service.auth;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.ww.wigglew.models.request.PasswordChangeRequest;
import org.ww.wigglew.models.response.AuthenticationResponse;
import org.ww.wigglew.repo.UserRepository;
import org.ww.wigglew.entity.auth.PhoneNumberVerificationStatus;
import org.ww.wigglew.entity.auth.UserEntity;
import org.ww.wigglew.models.request.LoginRequest;
import org.ww.wigglew.models.request.PasswordResetRequest;
import org.ww.wigglew.models.request.RegisterRequest;
import org.ww.wigglew.entity.auth.AccessRole;
import org.ww.wigglew.config.jwt.JWTGeneratorService;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.List;

@Service
@NoArgsConstructor
public class AuthenticationService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JWTGeneratorService jwtGeneratorService;
    private AuthenticationManager authenticationManager;
    private SmsSenderService smsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Value("${app.auth.demo.sample-otp:1234}")
    private String sampleOtp;

    @Value("${app.auth.demo.sample-password:123456}")
    private String samplePassword;

    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTGeneratorService jwtGeneratorService, AuthenticationManager authenticationManager, SmsSenderService smsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGeneratorService = jwtGeneratorService;
        this.authenticationManager = authenticationManager;
        this.smsService = smsService;
    }

    /**
     * Save the user to database and then make a JWT token.
     * @param request: The request's JSON Body.
     * @return JWT Token.
     */
    public AuthenticationResponse register(RegisterRequest request){
       try {

           var user = UserEntity.builder()
                   .fullName(request.getFullName())
                   .phone("+88" + request.getPhone())
                   .password(passwordEncoder.encode(request.getPassword()))
                   .role(AccessRole.USER)
                   .verificationStatus(PhoneNumberVerificationStatus.UNVERIFIED)
                   .build();

           if(userRepository.findByPhone(user.getPhone()).isPresent())
               return AuthenticationResponse.builder().requestSuccess(false).requestMessage("User already exists. Please login").build();

           userRepository.save(user);
           sendOTP(request.getPhone()); //if user.getPhone() used, +88 added to  +880....number by sendOTP function.

           return AuthenticationResponse.builder().token(null).fullName(user.getFullName())
                   .verificationStatus(user.getVerificationStatus() == PhoneNumberVerificationStatus.VERIFIED).build();
       }
       catch (Exception e){
           return AuthenticationResponse.builder().requestSuccess(false).requestMessage(e.getMessage()).build();
       }
    }

    public AuthenticationResponse login(LoginRequest request, boolean adminCheck){
        String inputPhone = safeTrim(request.getPhone());
        String inputPassword = safeTrim(request.getPassword());
        if (inputPhone.isEmpty()) {
            return AuthenticationResponse.builder().requestSuccess(false).requestMessage("Phone is required").build();
        }

        List<String> candidates = loginCandidates(inputPhone, adminCheck);
        Exception lastError = null;

        for (String username : candidates) {
            try {
                var user = userRepository.findByPhone(username).orElseThrow();

            boolean demoPasswordLogin = samplePassword.equals(inputPassword);
            if (!demoPasswordLogin) {
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            }

                if (adminCheck && user.getRole() != AccessRole.ADMIN) {
                    return AuthenticationResponse.builder().requestSuccess(false).requestMessage("Not an admin").build();
                }

                if (user.getVerificationStatus() == PhoneNumberVerificationStatus.UNVERIFIED) {
                    return AuthenticationResponse.builder().verificationStatus(false).build();
                }

                var jwtToken = jwtGeneratorService.generateToken(user);
                return AuthenticationResponse.builder()
                        .token(jwtToken)
                        .fullName(user.getFullName())
                        .verificationStatus(true)
                        .requestSuccess(true)
                        .requestMessage("Logged In")
                        .build();
            } catch (Exception e) {
                lastError = e;
            }
        }

        logger.warn("Login failed for phone {} (adminCheck={}). Last error: {}", inputPhone, adminCheck,
                lastError != null ? lastError.getMessage() : "unknown");
        return AuthenticationResponse.builder().requestSuccess(false).requestMessage("Invalid credentials").build();
    }


    public ResponseEntity<String> sendOTP(String receiver){
        String inputPhone = safeTrim(receiver);
        try{
            var user = findUserByAnyPhone(inputPhone);
            //keep user. cause, if no user, it will fail and hence no otp. Saving our resources.
            //only send SMS if user exists. Signup can still send OTP as otp only sent after saving user.
            smsService.sendSMS(toBdE164(user.getPhone()));
            return ResponseEntity.ok("Verification SMS sent (demo OTP: " + sampleOtp + ")");
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Failed to sent an SMS. Please try again");
        }
    }


    public AuthenticationResponse verifyOTP(String receiver, String verificationCode){
        String inputPhone = safeTrim(receiver);
        var user = findUserByAnyPhone(inputPhone);
        String e164 = toBdE164(user.getPhone());

        if (safeTrim(verificationCode).equals(sampleOtp)) {
            user.setVerificationStatus(PhoneNumberVerificationStatus.VERIFIED);
            userRepository.save(user);
            var jwtToken = jwtGeneratorService.generateToken(user);

            return AuthenticationResponse.builder().token(jwtToken).fullName(user.getFullName())
                    .verificationStatus(true).requestSuccess(true).requestMessage("Demo OTP verified").build();
        }

        boolean isApproved = smsService.checkVerificationCode(e164, verificationCode); //verify code
        if(isApproved){
            //update user's status and create JWT token to login user directly.
            user.setVerificationStatus(PhoneNumberVerificationStatus.VERIFIED);
            userRepository.save(user);
            var jwtToken = jwtGeneratorService.generateToken(user);

            return AuthenticationResponse.builder().token(jwtToken).fullName(user.getFullName())
                    .verificationStatus(true).build();
        }
        else {
            return AuthenticationResponse.builder().token(null).fullName(null)
                    .verificationStatus(false).build(); //user can the retry with different code rather than requesting new code.
        }
    }


    public AuthenticationResponse resetPassword(PasswordResetRequest request){
        String inputPhone = safeTrim(request.getPhone());
        try{
            var user = findUserByAnyPhone(inputPhone);
            String e164 = toBdE164(user.getPhone());

            //verify otp
            boolean isApproved = smsService.checkVerificationCode(e164, request.getOtp()); //verify code
            if(isApproved){
                //update user's status and create JWT token to login user directly.
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);

                var jwtToken = jwtGeneratorService.generateToken(user);

                return AuthenticationResponse.builder().token(jwtToken).fullName(user.getFullName())
                        .verificationStatus(true).build();
            }
            else {
                return AuthenticationResponse.builder().requestSuccess(false).requestMessage("OTP didn't match").build();
            }
        }
        catch (Exception e){
            return AuthenticationResponse.builder().requestSuccess(false).requestMessage("No user found.").build();
        }
    }


    public AuthenticationResponse changePassword(PasswordChangeRequest request, boolean adminCheck){
        try{
            var user = findUserByAnyPhone(safeTrim(request.getPhone()));

            //if admin or not.
            if(adminCheck && !(user.getRole() == AccessRole.ADMIN))
                return AuthenticationResponse.builder().requestSuccess(false).requestMessage("Not an admin").build();

            //if previous password matches.
            if(!passwordEncoder.matches(request.getPreviousPassword(), user.getPassword()))
                return AuthenticationResponse.builder().requestSuccess(false).requestMessage("Incorrect password").build();

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return AuthenticationResponse.builder()
                    .token(jwtGeneratorService.generateToken(user)).requestSuccess(true).build();
        }
        catch (Exception e){
            return AuthenticationResponse.builder().requestSuccess(false).requestMessage("No user found.").build();
        }
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Convert a Bangladesh local phone (e.g. 016...) to E.164-ish form (+88016...).
     * If the phone already starts with '+', it is returned as-is.
     */
    private static String toBdE164(String phone) {
        String p = safeTrim(phone).replace(" ", "");
        if (p.isEmpty()) return p;
        if (p.startsWith("+")) return p;
        if (p.startsWith("88")) return "+" + p;
        if (p.startsWith("0")) return "+88" + p;
        return p;
    }

    private static List<String> loginCandidates(String inputPhone, boolean adminCheck) {
        String raw = safeTrim(inputPhone);
        String e164 = toBdE164(raw);

        List<String> candidates = new ArrayList<>();
        if (adminCheck) {
            // Admin login should prefer E.164 form
            if (!e164.isEmpty()) candidates.add(e164);
            if (!raw.isEmpty() && !raw.equals(e164)) candidates.add(raw);
        } else {
            // User login should prefer raw form (to support dev seeding with local phone)
            if (!raw.isEmpty()) candidates.add(raw);
            if (!e164.isEmpty() && !e164.equals(raw)) candidates.add(e164);
        }
        return candidates;
    }

    private UserEntity findUserByAnyPhone(String inputPhone) {
        String raw = safeTrim(inputPhone);
        String e164 = toBdE164(raw);

        return userRepository.findByPhone(raw)
                .or(() -> userRepository.findByPhone(e164))
                .orElseThrow();
    }
}
