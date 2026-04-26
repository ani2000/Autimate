package org.ww.wigglew.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.ww.wigglew.entity.doctor.DoctorRatingEntity;

import java.util.List;
import java.util.Optional;

public interface DoctorRatingRepository extends MongoRepository<DoctorRatingEntity, String> {
    List<DoctorRatingEntity> findByDoctorIdOrderByCreatedAtDesc(String doctorId);
    List<DoctorRatingEntity> findByChildId(String childId);
    Optional<DoctorRatingEntity> findByAppointmentIdAndChildId(String appointmentId, String childId);
}
