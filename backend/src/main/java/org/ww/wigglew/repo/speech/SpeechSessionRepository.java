package org.ww.wigglew.repo.speech;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.ww.wigglew.entity.speech.SpeechSessionEntity;

import java.util.List;
import java.util.Optional;

public interface SpeechSessionRepository extends MongoRepository<SpeechSessionEntity, String> {
    Optional<SpeechSessionEntity> findBySessionId(String sessionId);
    List<SpeechSessionEntity> findByChildIdOrderByStartTimeDesc(String childId);
}
