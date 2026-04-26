package org.ww.wigglew.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ww.wigglew.models.request.DoctorAppointmentRequest;
import org.ww.wigglew.models.request.DoctorAppointmentStatusRequest;
import org.ww.wigglew.models.request.DoctorRatingRequest;
import org.ww.wigglew.service.DoctorAppointmentService;

@RestController
@RequestMapping("/api/v1/doctor/appointments")
public class DoctorAppointmentController {

    @Autowired
    private DoctorAppointmentService doctorAppointmentService;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @RequestHeader("Authorization") String token,
            @RequestBody DoctorAppointmentRequest request) {
        return doctorAppointmentService.bookAppointment(token, request);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestHeader("Authorization") String token) {
        return doctorAppointmentService.getHistory(token);
    }

    @PatchMapping("/{appointmentId}/visit")
    public ResponseEntity<?> markVisited(
            @RequestHeader("Authorization") String token,
            @PathVariable String appointmentId,
            @RequestBody(required = false) DoctorAppointmentStatusRequest request) {
        return doctorAppointmentService.markVisited(token, appointmentId, request);
    }

    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @RequestHeader("Authorization") String token,
            @PathVariable String appointmentId) {
        return doctorAppointmentService.cancelAppointment(token, appointmentId);
    }

    @PostMapping("/rate")
    public ResponseEntity<?> submitRating(
            @RequestHeader("Authorization") String token,
            @RequestBody DoctorRatingRequest request) {
        return doctorAppointmentService.submitRating(token, request);
    }

    @GetMapping("/ratings/{doctorId}")
    public ResponseEntity<?> getDoctorRatings(@PathVariable String doctorId) {
        return doctorAppointmentService.getDoctorRatings(doctorId);
    }

    @GetMapping("/ratings/me")
    public ResponseEntity<?> getMyRatings(@RequestHeader("Authorization") String token) {
        return doctorAppointmentService.getMyRatings(token);
    }

    @PatchMapping("/{appointmentId}/payment")
    public ResponseEntity<?> updatePaymentStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable String appointmentId,
            @RequestParam("status") String paymentStatus,
            @RequestParam(value = "method", required = false) String paymentMethod) {
        return doctorAppointmentService.updatePaymentStatus(token, appointmentId, paymentStatus, paymentMethod);
    }

    @PatchMapping("/{appointmentId}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
            @RequestHeader("Authorization") String token,
            @PathVariable String appointmentId,
            @RequestBody DoctorAppointmentRequest request) {
        return doctorAppointmentService.rescheduleAppointment(token, appointmentId, request);
    }
}
