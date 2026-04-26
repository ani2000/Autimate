package org.ww.wigglew.repo.speech;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.ww.wigglew.entity.speech.SpeechChildProfileEntity;

import java.util.Optional;

public interface SpeechChildProfileRepository extends MongoRepository<SpeechChildProfileEntity, String> {
    Optional<SpeechChildProfileEntity> findByChildId(String childId);
}
