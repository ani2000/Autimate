package org.ww.wigglew.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ww.wigglew.entity.speech.SpeechAnimalEntity;
import org.ww.wigglew.entity.speech.SpeechAttemptEntity;
import org.ww.wigglew.entity.speech.SpeechChildProfileEntity;
import org.ww.wigglew.entity.speech.SpeechSessionEntity;
import org.ww.wigglew.models.request.speech.CreateSpeechChildRequest;
import org.ww.wigglew.models.request.speech.EndSpeechSessionRequest;
import org.ww.wigglew.models.request.speech.RecordVoiceRequest;
import org.ww.wigglew.models.request.speech.SpeakRequest;
import org.ww.wigglew.models.request.speech.StartSpeechSessionRequest;
import org.ww.wigglew.repo.speech.SpeechAnimalRepository;
import org.ww.wigglew.repo.speech.SpeechAttemptRepository;
import org.ww.wigglew.repo.speech.SpeechChildProfileRepository;
import org.ww.wigglew.repo.speech.SpeechSessionRepository;

import java.time.LocalDateTime;
import java.util.*;

@RestController
public class SpeechTherapyController {

    @Autowired
    private SpeechChildProfileRepository speechChildProfileRepository;

    @Autowired
    private SpeechAnimalRepository speechAnimalRepository;

    @Autowired
    private SpeechSessionRepository speechSessionRepository;

    @Autowired
    private SpeechAttemptRepository speechAttemptRepository;

    @PostMapping("/create-child")
    public ResponseEntity<?> createChild(@RequestBody CreateSpeechChildRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "name is required"));
        }
        if (request.getAge() == null || request.getAge() < 1 || request.getAge() > 18) {
            return ResponseEntity.badRequest().body(Map.of("message", "age must be between 1 and 18"));
        }

        SpeechChildProfileEntity profile = new SpeechChildProfileEntity();
        profile.setChildId(UUID.randomUUID().toString());
        profile.setName(request.getName().trim());
        profile.setAge(request.getAge());
        profile.setTherapyLevel(request.getTherapyLevel() == null || request.getTherapyLevel().isBlank() ? "Beginner" : request.getTherapyLevel().trim());
        profile.setCreatedAt(LocalDateTime.now());

        SpeechChildProfileEntity saved = speechChildProfileRepository.save(profile);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/animals")
    public ResponseEntity<?> getAnimals() {
        ensureAnimalSeed();
        return ResponseEntity.ok(speechAnimalRepository.findAll());
    }

    @PostMapping("/start-session")
    public ResponseEntity<?> startSession(@RequestBody StartSpeechSessionRequest request) {
        if (request.getChildId() == null || request.getChildId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "childId is required"));
        }

        SpeechSessionEntity session = new SpeechSessionEntity();
        session.setSessionId(UUID.randomUUID().toString());
        session.setChildId(request.getChildId());
        session.setStartTime(LocalDateTime.now());
        session.setDuration(0L);

        SpeechSessionEntity saved = speechSessionRepository.save(session);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/record-voice")
    public ResponseEntity<?> recordVoice(@RequestBody RecordVoiceRequest request) {
        if (request.getChildId() == null || request.getChildId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "childId is required"));
        }

        String target = normalize(request.getTargetText());
        String transcript = normalize(request.getTranscript());
        double similarity = request.getSimilarity() != null ? clamp(request.getSimilarity(), 0.0, 1.0) : similarity(target, transcript);
        boolean correct = request.getCorrect() != null ? request.getCorrect() : similarity >= 0.7;

        SpeechAttemptEntity attempt = new SpeechAttemptEntity();
        attempt.setSessionId(request.getSessionId());
        attempt.setChildId(request.getChildId());
        attempt.setTargetText(request.getTargetText());
        attempt.setVoiceType(request.getVoiceType());
        attempt.setTranscript(request.getTranscript());
        attempt.setSimilarity(similarity);
        attempt.setCorrect(correct);
        attempt.setCreatedAt(LocalDateTime.now());

        SpeechAttemptEntity saved = speechAttemptRepository.save(attempt);
        return ResponseEntity.ok(Map.of(
                "attempt", saved,
                "feedback", correct ? "Great job!" : "Try again with a clearer pronunciation.",
                "similarity", similarity
        ));
    }

    @PostMapping("/end-session")
    public ResponseEntity<?> endSession(@RequestBody EndSpeechSessionRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "sessionId is required"));
        }

        Optional<SpeechSessionEntity> maybeSession = speechSessionRepository.findBySessionId(request.getSessionId());
        if (maybeSession.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "session not found"));
        }

        SpeechSessionEntity session = maybeSession.get();
        LocalDateTime end = LocalDateTime.now();
        session.setEndTime(end);

        long duration;
        if (request.getDuration() != null && request.getDuration() >= 0) {
            duration = request.getDuration();
        } else {
            duration = Math.max(0L, java.time.Duration.between(session.getStartTime(), end).toSeconds());
        }
        session.setDuration(duration);

        SpeechSessionEntity saved = speechSessionRepository.save(session);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getProgress(@RequestParam("childId") String childId) {
        if (childId == null || childId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "childId is required"));
        }

        List<SpeechAttemptEntity> attempts = speechAttemptRepository.findByChildIdOrderByCreatedAtDesc(childId);
        List<SpeechSessionEntity> sessions = speechSessionRepository.findByChildIdOrderByStartTimeDesc(childId);

        int totalAttempts = attempts.size();
        int correct = (int) attempts.stream().filter(a -> Boolean.TRUE.equals(a.getCorrect())).count();
        double accuracy = totalAttempts == 0 ? 0.0 : ((double) correct / (double) totalAttempts) * 100.0;
        long sessionDuration = sessions.stream().map(SpeechSessionEntity::getDuration).filter(Objects::nonNull).mapToLong(Long::longValue).sum();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accuracy", round2(accuracy));
        payload.put("attempts", totalAttempts);
        payload.put("correct", correct);
        payload.put("sessionDuration", sessionDuration);
        payload.put("recentAttempts", attempts.stream().limit(20).toList());
        payload.put("recentSessions", sessions.stream().limit(20).toList());

        return ResponseEntity.ok(payload);
    }

    @PostMapping("/api/speak")
    public ResponseEntity<?> speak(@RequestBody SpeakRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "text is required"));
        }

        String voiceType = request.getVoiceType() == null || request.getVoiceType().isBlank() ? "FEMALE" : request.getVoiceType().trim().toUpperCase(Locale.ROOT);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", request.getText().trim());
        payload.put("voiceType", voiceType);
        payload.put("audioUrl", null);
        payload.put("pitch", request.getPitch() == null ? 1.0 : clamp(request.getPitch(), 0.1, 2.0));
        payload.put("speed", request.getSpeed() == null ? 1.0 : clamp(request.getSpeed(), 0.5, 2.0));
        payload.put("volume", request.getVolume() == null ? 1.0 : clamp(request.getVolume(), 0.0, 1.0));
        payload.put("message", "Client-side TTS playback should use this voice profile.");
        payload.put("generatedAt", LocalDateTime.now().toString());

        return ResponseEntity.ok(payload);
    }

    private void ensureAnimalSeed() {
        if (speechAnimalRepository.count() > 0) return;

        List<SpeechAnimalEntity> seeds = new ArrayList<>();
        seeds.add(animal("Dog", "woof woof"));
        seeds.add(animal("Cat", "meow meow"));
        seeds.add(animal("Cow", "moo moo"));
        seeds.add(animal("Lion", "roar"));
        seeds.add(animal("Sheep", "baa baa"));
        seeds.add(animal("Duck", "quack quack"));
        seeds.add(animal("Horse", "neigh"));
        seeds.add(animal("Elephant", "pawoo"));

        speechAnimalRepository.saveAll(seeds);
    }

    private SpeechAnimalEntity animal(String name, String soundText) {
        SpeechAnimalEntity animal = new SpeechAnimalEntity();
        animal.setName(name);
        animal.setSoundText(soundText);
        return animal;
    }

    private String normalize(String input) {
        if (input == null) return "";
        return input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private double similarity(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        int distance = levenshtein(a, b);
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) distance / (double) maxLen);
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
