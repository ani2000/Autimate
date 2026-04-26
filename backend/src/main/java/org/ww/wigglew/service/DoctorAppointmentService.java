package org.ww.wigglew.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.ww.wigglew.entity.doctor.DoctorAppointmentEntity;
import org.ww.wigglew.entity.doctor.DoctorEntity;
import org.ww.wigglew.entity.doctor.DoctorRatingEntity;
import org.ww.wigglew.models.request.DoctorAppointmentRequest;
import org.ww.wigglew.models.request.DoctorAppointmentStatusRequest;
import org.ww.wigglew.models.request.DoctorRatingRequest;
import org.ww.wigglew.repo.DoctorAppointmentRepository;
import org.ww.wigglew.repo.DoctorRatingRepository;
import org.ww.wigglew.repo.DoctorsRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DoctorAppointmentService {

    @Autowired
    private ChildService childService;

    @Autowired
    private DoctorsRepository doctorsRepository;

    @Autowired
    private DoctorAppointmentRepository doctorAppointmentRepository;

    @Autowired
    private DoctorRatingRepository doctorRatingRepository;

    public ResponseEntity<?> bookAppointment(String token, DoctorAppointmentRequest request) {
        try {
            String childId = childService.getActiveChild(token);
            if (childId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No active child selected"));
            }

            if (request.getDoctorId() == null || request.getDoctorId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Doctor is required"));
            }

            if (request.getAppointmentAt() == null || request.getAppointmentAt().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Appointment date/time is required"));
            }

            Optional<DoctorEntity> doctorOptional = doctorsRepository.findById(request.getDoctorId());
            if (doctorOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Doctor not found"));
            }

            LocalDateTime appointmentTime = LocalDateTime.parse(request.getAppointmentAt());
            if (appointmentTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
                return ResponseEntity.badRequest().body(Map.of("message", "Appointment time must be in the future"));
            }

            DoctorAppointmentEntity appointment = new DoctorAppointmentEntity();
            appointment.setChildId(childId);
            appointment.setDoctorId(doctorOptional.get().getId());
            appointment.setDoctorName(doctorOptional.get().getName());
            appointment.setAppointmentAt(appointmentTime);
            appointment.setReason(request.getReason());
            appointment.setStatus("BOOKED");
            appointment.setVisitNotes("");
            appointment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
            appointment.setPaymentStatus("PENDING");
            appointment.setPaymentAmount(request.getPaymentAmount() != null ? request.getPaymentAmount() : 0.0);
            appointment.setCreatedAt(LocalDateTime.now());
            appointment.setUpdatedAt(LocalDateTime.now());

            DoctorAppointmentEntity saved = doctorAppointmentRepository.save(appointment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to book appointment"));
        }
    }

    public ResponseEntity<?> getHistory(String token) {
        try {
            String childId = childService.getActiveChild(token);
            if (childId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No active child selected"));
            }

            List<DoctorAppointmentEntity> history = doctorAppointmentRepository.findByChildIdOrderByAppointmentAtDesc(childId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to load history"));
        }
    }

    public ResponseEntity<?> markVisited(String token, String appointmentId, DoctorAppointmentStatusRequest request) {
        return updateStatus(token, appointmentId, "VISITED", request != null ? request.getVisitNotes() : "");
    }

    public ResponseEntity<?> cancelAppointment(String token, String appointmentId) {
        return updateStatus(token, appointmentId, "CANCELLED", "");
    }

    private ResponseEntity<?> updateStatus(String token, String appointmentId, String status, String notes) {
        try {
            String childId = childService.getActiveChild(token);
            if (childId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No active child selected"));
            }

            Optional<DoctorAppointmentEntity> appointmentOptional = doctorAppointmentRepository.findByIdAndChildId(appointmentId, childId);
            if (appointmentOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Appointment not found"));
            }

            DoctorAppointmentEntity appointment = appointmentOptional.get();
            appointment.setStatus(status);
            if (notes != null && !notes.isBlank()) {
                appointment.setVisitNotes(notes);
            }
            appointment.setUpdatedAt(LocalDateTime.now());

            DoctorAppointmentEntity saved = doctorAppointmentRepository.save(appointment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update appointment");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    public ResponseEntity<?> submitRating(String token, DoctorRatingRequest request) {
        try {
            String childId = childService.getActiveChild(token);
            if (childId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No active child selected"));
            }

            if (request.getStars() < 1 || request.getStars() > 5) {
                return ResponseEntity.badRequest().body(Map.of("message", "Stars must be between 1 and 5"));
            }

            Optional<DoctorRatingEntity> existing = doctorRatingRepository
                    .findByAppointmentIdAndChildId(request.getAppointmentId(), childId);
            if (existing.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Already rated this appointment"));
            }

            DoctorRatingEntity rating = new DoctorRatingEntity();
            rating.setChildId(childId);
            rating.setDoctorId(request.getDoctorId());
            rating.setAppointmentId(request.getAppointmentId());
            rating.setStars(request.getStars());
            rating.setReviewText(request.getReviewText() != null ? request.getReviewText() : "");
            rating.setCreatedAt(LocalDateTime.now());

            DoctorRatingEntity saved = doctorRatingRepository.save(rating);

            updateDoctorAverageRating(request.getDoctorId());

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to submit rating"));
        }
    }

    public ResponseEntity<?> getDoctorRatings(String doctorId) {
        try {
            List<DoctorRatingEntity> ratings = doctorRatingRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
            double average = ratings.stream().mapToInt(DoctorRatingEntity::getStars).average().orElse(0.0);
            int count = ratings.size();
            Map<String, Object> resp = new HashMap<>();
            resp.put("average", Math.round(average * 10.0) / 10.0);
            resp.put("count", count);
            resp.put("ratings", ratings);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to load ratings"));
        }
    }

    private void updateDoctorAverageRating(String doctorId) {
        List<DoctorRatingEntity> ratings = doctorRatingRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
        if (ratings.isEmpty()) return;

        double average = ratings.stream().mapToInt(DoctorRatingEntity::getStars).average().orElse(0.0);
        Optional<DoctorEntity> doctorOpt = doctorsRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            DoctorEntity doctor = doctorOpt.get();
            doctor.setRatings(Math.round(average * 10.0) / 10.0);
            doctorsRepository.save(doctor);
        }
    }

    public ResponseEntity<?> rescheduleAppointment(String token, String appointmentId, DoctorAppointmentRequest request) {
        try {
            String childId = childService.getActiveChild(token);
            if (childId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No active child selected"));
            }

            Optional<DoctorAppointmentEntity> appointmentOpt = doctorAppointmentRepository.findByIdAndChildId(appointmentId, childId);
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Appointment not found"));
            }

            DoctorAppointmentEntity appointment = appointmentOpt.get();
            if (!"BOOKED".equals(appointment.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Only booked appointments can be rescheduled"));
            }

            if (request.getAppointmentAt() == null || request.getAppointmentAt().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "New appointment time is required"));
            }

            LocalDateTime newTime = LocalDateTime.parse(request.getAppointmentAt());
            if (newTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
                return ResponseEntity.badRequest().body(Map.of("message", "New appointment time must be in the future"));
            }
            appointment.setAppointmentAt(newTime);
            if (request.getReason() != null && !request.getReason().isBlank()) {
                appointment.setReason(request.getReason());
            }
            appointment.setUpdatedAt(LocalDateTime.now());

            DoctorAppointmentEntity saved = doctorAppointmentRepository.save(appointment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to reschedule appointment"));
        }
    }

    public ResponseEntity<?> updatePaymentStatus(String token, String appointmentId, String paymentStatus, String paymentMethod) {
        try {
            String childId = childService.getActiveChild(token);
            if (childId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No active child selected"));
            }

            Optional<DoctorAppointmentEntity> appointmentOpt = doctorAppointmentRepository.findByIdAndChildId(appointmentId, childId);
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Appointment not found"));
            }

            DoctorAppointmentEntity appointment = appointmentOpt.get();
            appointment.setPaymentStatus(paymentStatus);
            if (paymentMethod != null && !paymentMethod.isBlank()) {
                appointment.setPaymentMethod(paymentMethod);
            }
            appointment.setUpdatedAt(LocalDateTime.now());
            DoctorAppointmentEntity saved = doctorAppointmentRepository.save(appointment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to update payment"));
        }
    }

    public ResponseEntity<?> getMyRatings(String token) {
        try {
            String childId = childService.getActiveChild(token);
            if (childId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No active child selected"));
            }

            List<DoctorRatingEntity> ratings = doctorRatingRepository.findByChildId(childId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to load your ratings"));
        }
    }
}
