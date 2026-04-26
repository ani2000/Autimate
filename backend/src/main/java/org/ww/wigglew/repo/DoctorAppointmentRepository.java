package org.ww.wigglew.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.ww.wigglew.entity.doctor.DoctorAppointmentEntity;

import java.util.List;
import java.util.Optional;

public interface DoctorAppointmentRepository extends MongoRepository<DoctorAppointmentEntity, String> {
    List<DoctorAppointmentEntity> findByChildIdOrderByAppointmentAtDesc(String childId);
    Optional<DoctorAppointmentEntity> findByIdAndChildId(String id, String childId);
}
