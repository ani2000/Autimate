package org.ww.wigglew.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.ww.wigglew.entity.CallEntity;
import org.ww.wigglew.entity.auth.UserEntity;
import org.ww.wigglew.entity.doctor.DoctorEntity;

import java.util.Optional;

@RepositoryRestResource
public interface CallRepository extends MongoRepository<CallEntity, String> {
    Optional<CallEntity> findByCallId(String phone);
}