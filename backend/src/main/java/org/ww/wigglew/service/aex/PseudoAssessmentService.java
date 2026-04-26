package org.ww.wigglew.service.aex;

import org.springframework.stereotype.Service;
import org.ww.wigglew.entity.aex.ASDExEntity;
import org.ww.wigglew.models.request.AsdExRequest;
import org.ww.wigglew.repo.aex.ASDExRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PseudoAssessmentService {

    private final ASDExRepository asdExRepository;

    public PseudoAssessmentService(ASDExRepository asdExRepository) {
        this.asdExRepository = asdExRepository;
    }

    public ASDExEntity generateAndSave(AsdExRequest request) {
        String requestId = extractRequestId(request.getVideo_name());
        double q10Score = calculateQ10Score(request.getArrq10());
        double videoConfidence = randomBetween(0.45, 0.93);
        double combinedRisk = (q10Score * 0.55) + (videoConfidence * 0.45);
        boolean likelyAutism = combinedRisk >= 0.50;

        ASDExEntity entity = new ASDExEntity();
        entity.setUsername(request.getChildId());
        entity.setRequestID(requestId);
        entity.setTestDate(LocalDateTime.now());
        entity.setQ10(String.format("%.4f", q10Score));
        entity.setVid_confid(String.format("%.4f", videoConfidence));
        entity.setVid_res(likelyAutism ? "0" : "1");
        entity.setSuggested_therapies(likelyAutism
                ? "Speech Therapy, Social Skill Therapy, Drawing Therapy"
                : "Play-based Communication, Routine Reinforcement, Group Interaction");
        entity.setSuggested_games(likelyAutism
                ? "Tap Game, Fun Alphabet, Story Social Skill"
                : "Snake Game, Memory Patterns, Group Video Chat Activities");

        return asdExRepository.save(entity);
    }

    private String extractRequestId(String videoName) {
        if (videoName == null || videoName.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        String trimmed = videoName.trim();
        if (trimmed.endsWith(".mp4")) {
            return trimmed.substring(0, trimmed.length() - 4);
        }
        return trimmed;
    }

    private double calculateQ10Score(List<Integer> answers) {
        if (answers == null || answers.isEmpty()) {
            return randomBetween(0.35, 0.80);
        }

        int count = Math.min(11, answers.size());
        double sum = 0;
        for (int index = 0; index < count; index++) {
            Integer value = answers.get(index);
            int normalized = value == null ? 0 : Math.max(0, Math.min(4, value));
            sum += normalized;
        }

        double normalizedScore = sum / (count * 4.0);
        return Math.max(0.05, Math.min(0.95, normalizedScore));
    }

    private double randomBetween(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
