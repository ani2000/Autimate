package org.ww.wigglew.repo.aex;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.ww.wigglew.entity.aex.QuestionExamEntity;


public interface QuestionExamRepository extends MongoRepository<QuestionExamEntity, Long> {
}

