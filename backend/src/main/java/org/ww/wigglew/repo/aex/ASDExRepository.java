package org.ww.wigglew.repo.aex;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.ww.wigglew.entity.aex.ASDExEntity;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface ASDExRepository extends MongoRepository<ASDExEntity, String> {
    List<ASDExEntity> findByUsername(String username); //username is actually just child id.
    Optional<ASDExEntity> findByRequestID(String requestID);
}