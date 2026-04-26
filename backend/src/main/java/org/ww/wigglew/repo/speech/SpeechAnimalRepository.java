package org.ww.wigglew.repo.speech;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.ww.wigglew.entity.speech.SpeechAnimalEntity;

public interface SpeechAnimalRepository extends MongoRepository<SpeechAnimalEntity, String> {
}
