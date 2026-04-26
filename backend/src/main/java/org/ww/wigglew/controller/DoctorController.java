package org.ww.wigglew.controller;


import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.ww.wigglew.entity.doctor.DoctorEntity;
import org.ww.wigglew.service.DoctorManagerService;
import org.ww.wigglew.service.BucketStorageService;

@RestController
@RequestMapping("/api/v1/doctor/")
public class DoctorController {

    @Autowired
    private DoctorManagerService doctorManagerService;

    @Autowired
    private BucketStorageService bucketStorageService;


    private boolean validateAuthority(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails)
            return !userDetails.getAuthorities().toString().equals("[ADMIN]");
        return true;
    }

    @GetMapping("/url/presigned")
    public String fetchPreSignedUrl() throws Exception {
        return bucketStorageService.getPreSignedUrl("jpg");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDoctor(
            @RequestPart("doctorData") DoctorEntity doctorEntity,
            @RequestPart("image") MultipartFile image) {

        if (validateAuthority()) return ResponseEntity.status(403).body("Forbidden");

        return doctorManagerService.createDoctor(doctorEntity, image);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable String id) {
        if (validateAuthority()) return ResponseEntity.status(403).body("Forbidden");
        return doctorManagerService.deleteDoctor(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDoctor(
            @PathVariable String id,
            @RequestPart("doctorData") DoctorEntity doctorEntity,
            @RequestPart(value = "image", required = false) MultipartFile image) {  // Image is optional
        if (validateAuthority()) return ResponseEntity.status(403).body("Forbidden");
        return doctorManagerService.updateDoctor(id, doctorEntity, image);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getDoctor(@PathVariable String id) {
        //if (validateAuthority()) return ResponseEntity.status(403).body("Forbidden"); // anyone can get doctor including users.
        return doctorManagerService.getDoctor(id);
    }

    @GetMapping("/list/nearby")
    public ResponseEntity<?> getAllDoctors(@RequestHeader("X-IP-Address") String ip) {
        return doctorManagerService.getAllDoctors(ip, true); //sorted by IP.
    }

    @GetMapping("/list/ratings")
    public ResponseEntity<?> getAllDoctors(
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return doctorManagerService.getAllDoctors("", false, minRating, limit); //default sorting is by ratings. ip sorting = false.
    }

    @GetMapping("/list/nearby/realtime")
    public ResponseEntity<?> getAllDoctorsRealtime(
            @RequestParam("lat") Double latitude,
            @RequestParam("lon") Double longitude) {
        return doctorManagerService.getAllDoctorsByCoordinates(latitude, longitude);
    }
}
