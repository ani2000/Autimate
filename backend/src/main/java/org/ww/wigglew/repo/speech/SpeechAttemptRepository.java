package org.ww.wigglew.repo.speech;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.ww.wigglew.entity.speech.SpeechAttemptEntity;

import java.util.List;

public interface SpeechAttemptRepository extends MongoRepository<SpeechAttemptEntity, String> {
    List<SpeechAttemptEntity> findByChildIdOrderByCreatedAtDesc(String childId);
    List<SpeechAttemptEntity> findBySessionId(String sessionId);
}
