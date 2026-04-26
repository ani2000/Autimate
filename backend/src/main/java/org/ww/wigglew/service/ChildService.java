package org.ww.wigglew.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ww.wigglew.config.jwt.JWTExtractorService;
import org.ww.wigglew.entity.auth.Child;
import org.ww.wigglew.entity.auth.UserEntity;
import org.ww.wigglew.models.response.AuthenticationResponse;
import org.ww.wigglew.repo.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ChildService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTExtractorService jwtExtractorService;

    private UserEntity getUserFromToken(String token){
        String phone = jwtExtractorService.extractUsername(token.substring(7));
        return userRepository.findByPhone(phone).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AuthenticationResponse addChild(String token, String childName, LocalDate dob, String gender) {
        UserEntity user = getUserFromToken(token);
        int genderValue = "female".equalsIgnoreCase(gender) ? 0 : 1;
        Child newChild = Child.builder()
                .id(UUID.randomUUID().toString())
                .name(childName)
                .gender(genderValue)
                .dob(dob)
                .activeSession(false) // default to false
                .build();

        user.getChildren().add(newChild);
        userRepository.save(user);

        return AuthenticationResponse.builder()
                .requestSuccess(true)
                .requestMessage("Child added successfully")
                .build();
    }

    public AuthenticationResponse toggleActiveSession(String token, String childId) {
        UserEntity user = getUserFromToken(token);

        // Ensure only one child has an active session at a time
        for (Child child : user.getChildren()) {
            child.setActiveSession(child.getId().equals(childId));
        }

        userRepository.save(user);

        return AuthenticationResponse.builder()
                .requestSuccess(true)
                .requestMessage("Active session toggled")
                .build();
    }

    public List<Child> getChildren(String token) {
        UserEntity user = getUserFromToken(token);
        return user.getChildren();
    }

    public void deleteChild(String token, String childId) {
        UserEntity user = getUserFromToken(token);

        user.getChildren().removeIf(child -> child.getId().equals(childId));
        userRepository.save(user);
    }


    public String getActiveChild(String token) {
        UserEntity user = getUserFromToken(token);

        if (user.getChildren() == null || user.getChildren().isEmpty()) {
            return null;
        }

        String active = user.getChildren().stream()
                .filter(Child::isActiveSession)
                .map(Child::getId)
                .findFirst()
                .orElse(null);

        if (active != null) {
            return active;
        }

        // Auto-select first child as active to keep downstream flows usable.
        Child first = user.getChildren().get(0);
        for (Child child : user.getChildren()) {
            child.setActiveSession(child.getId().equals(first.getId()));
        }
        userRepository.save(user);
        return first.getId();
    }

}
