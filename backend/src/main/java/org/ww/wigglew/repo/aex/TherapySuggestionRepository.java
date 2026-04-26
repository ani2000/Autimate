package org.ww.wigglew.repo.aex;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.ww.wigglew.entity.aex.TherapySuggestionEntity;
import org.ww.wigglew.entity.auth.UserEntity;
import org.ww.wigglew.entity.doctor.DoctorEntity;

import java.util.Optional;

@RepositoryRestResource
public interface TherapySuggestionRepository extends MongoRepository<TherapySuggestionEntity, String> {
    Optional<TherapySuggestionEntity> findByRequestId(String requestId);
}