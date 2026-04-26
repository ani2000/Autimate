package org.ww.wigglew.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.ww.wigglew.entity.auth.UserEntity;
import org.ww.wigglew.entity.doctor.DoctorEntity;

import java.util.Optional;

@RepositoryRestResource
public interface DoctorsRepository extends MongoRepository<DoctorEntity, String> {
    Optional<DoctorEntity> findByPhone(String phone);
    Optional<DoctorEntity> findByName(String name);
}