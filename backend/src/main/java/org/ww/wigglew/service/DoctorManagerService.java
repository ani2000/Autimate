package org.ww.wigglew.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.ww.wigglew.controller.DoctorController;
import org.ww.wigglew.entity.doctor.DoctorEntity;
import org.ww.wigglew.models.response.DoctorResponse;
import org.ww.wigglew.repo.DoctorsRepository;
import org.ww.wigglew.service.DoctorImageService;
import org.ww.wigglew.service.geocoding.IpGeolocation;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.ww.wigglew.service.geocoding.ForwardGeoCoding.getLocationData;

@Service
public class DoctorManagerService {

    @Autowired
    private DoctorsRepository doctorsRepository;

    @Autowired
    private DoctorImageService doctorImageService;

    private DoctorResponse positiveResponse(String msg){
        return DoctorResponse.builder().responseStat(true).responseMessage(msg).build();
    }

    private DoctorResponse negativeResponse(String msg){
        return DoctorResponse.builder().responseStat(false).responseMessage(msg).build();
    }

    public ResponseEntity<DoctorResponse> createDoctor(DoctorEntity doctorEntity, MultipartFile image) {
        try {
            // Handle file upload and/or default to a non-repeating gender-based placeholder.
            if (image != null && !image.isEmpty()) {
                String imageUrl = uploadImage(image); // Store the uploaded image and use the URL.
                doctorEntity.setImage(imageUrl);
            } else {
                // Determine which /doctors/ images are already in use for this gender.
                Set<String> usedImages = doctorsRepository.findAll().stream()
                        .filter(d -> d.getGender() != null && doctorEntity.getGender() != null)
                        .filter(d -> d.getGender().equalsIgnoreCase(doctorEntity.getGender()))
                        .map(DoctorEntity::getImage)
                        .filter(Objects::nonNull)
                        .filter(img -> img.startsWith("/doctors/"))
                        .collect(Collectors.toSet());
                doctorEntity.setImage(doctorImageService.getNextAvailableImage(doctorEntity.getGender(), usedImages));
            }

            try {
                doctorEntity.setLatitude("0");
                doctorEntity.setLongitude("0");
                Map<String, String> locationData = getLocationData(doctorEntity.getAddress());
                if(locationData.get("error").isEmpty()) {
                    doctorEntity.setLatitude(locationData.get("latitude"));
                    doctorEntity.setLongitude(locationData.get("longitude"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Save doctor entity
            doctorsRepository.save(doctorEntity);
            return ResponseEntity.ok(positiveResponse("Doctor created successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(negativeResponse("Error creating doctor: " + e.getMessage()));
        }
    }

    private String uploadImage(MultipartFile image) throws IOException {
        String uploadDir = new File("src/main/resources/static/uploads").getAbsolutePath();

        // Create the directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the file to the static directory
        String filePath = uploadDir + "/" + image.getOriginalFilename();
        File destinationFile = new File(filePath);
        image.transferTo(destinationFile);

        return "/uploads/" + image.getOriginalFilename();  // Return the URL path for the file
    }




    public ResponseEntity<DoctorResponse> deleteDoctor(String id) {
        try {
            Optional<DoctorEntity> doctor = doctorsRepository.findById(id);
            if (doctor.isPresent()) {
                doctorsRepository.deleteById(id);
                return ResponseEntity.ok(DoctorResponse.builder().responseStat(true).build());
            } else {
                return ResponseEntity.status(404).body(negativeResponse("Doctor not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(negativeResponse("Error deleting doctor: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> updateDoctorOld(String id, DoctorEntity updatedDoctor) {
        try {
            Optional<DoctorEntity> doctor = doctorsRepository.findById(id);
            if (doctor.isPresent()) {
                updatedDoctor.setId(id);  // Ensure ID remains the same
                doctorsRepository.save(updatedDoctor);
                return ResponseEntity.ok(positiveResponse("Doctor updated successfully"));
            } else {
                return ResponseEntity.status(404).body(negativeResponse("Doctor not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(negativeResponse("Error updating doctor: " + e.getMessage()));
        }
    }

    public ResponseEntity<DoctorResponse> updateDoctor(String id, DoctorEntity updatedDoctor, MultipartFile image) {
        try {
            Optional<DoctorEntity> existingDoctorOpt = doctorsRepository.findById(id);
            if (existingDoctorOpt.isPresent()) {
                DoctorEntity existingDoctor = existingDoctorOpt.get();

                // Update fields from updatedDoctor
                existingDoctor.setName(updatedDoctor.getName());
                existingDoctor.setPhone(updatedDoctor.getPhone());
                existingDoctor.setDescription(updatedDoctor.getDescription());
                existingDoctor.setExperienceCount(updatedDoctor.getExperienceCount());
                existingDoctor.setGender(updatedDoctor.getGender());
                existingDoctor.setSpecialities(updatedDoctor.getSpecialities());
                existingDoctor.setOfficeHours(updatedDoctor.getOfficeHours());
                existingDoctor.setWebsite(updatedDoctor.getWebsite());
                existingDoctor.setAddress(updatedDoctor.getAddress());
                existingDoctor.setRatings(updatedDoctor.getRatings());

                // Update longitude/latitude from address when geocoding is available.
                try {
                    Map<String, String> locationData = getLocationData(updatedDoctor.getAddress());
                    if (locationData.get("error").isEmpty()) {
                        existingDoctor.setLatitude(locationData.get("latitude"));
                        existingDoctor.setLongitude(locationData.get("longitude"));
                    }
                } catch (IOException e) {
                    // Keep existing coordinates if geocoding fails.
                }

                // Handle uploaded image or ensure a unique gender-based placeholder.
                if (image != null && !image.isEmpty()) {
                    String imageUrl = uploadImage(image);
                    existingDoctor.setImage(imageUrl);
                } else {
                    // Collect images already assigned to other doctors of the same gender.
                    Set<String> usedImages = doctorsRepository.findAll().stream()
                            .filter(d -> d.getGender() != null && existingDoctor.getGender() != null)
                            .filter(d -> d.getGender().equalsIgnoreCase(existingDoctor.getGender()))
                            .filter(d -> !d.getId().equals(existingDoctor.getId()))
                            .map(DoctorEntity::getImage)
                            .filter(Objects::nonNull)
                            .filter(img -> img.startsWith("/doctors/"))
                            .collect(Collectors.toSet());

                    String currentImage = existingDoctor.getImage();
                    boolean currentImageValid = currentImage != null
                            && currentImage.startsWith("/doctors/")
                            && !usedImages.contains(currentImage);
                    if (!currentImageValid) {
                        existingDoctor.setImage(doctorImageService.getNextAvailableImage(existingDoctor.getGender(), usedImages));
                    }
                }

                doctorsRepository.save(existingDoctor);
                return ResponseEntity.ok(positiveResponse("Doctor updated successfully"));
            } else {
                return ResponseEntity.status(404).body(negativeResponse("Doctor not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(negativeResponse("Error updating doctor: " + e.getMessage()));
        }
    }


    public ResponseEntity<?> getDoctor(String id) {
        try {
            Optional<DoctorEntity> doctor = doctorsRepository.findById(id);
            return doctor.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(negativeResponse("Error fetching doctor: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> getAllDoctors(String ip, boolean ipSorted) {
        return getAllDoctors(ip, ipSorted, null, null);
    }

    // New: support filtering by minimum rating and limiting results
    public ResponseEntity<?> getAllDoctors(String ip, boolean ipSorted, Double minRating, Integer limit) {
        try {
            List<DoctorEntity> doctors = doctorsRepository.findAll();

            if (doctors.isEmpty()) {
                return ResponseEntity.ok(getSampleDoctors(ipSorted));
            }

            // If IP address is not null, find lon, lat and sort the doctors.
            if(ipSorted){
                if (!ip.isEmpty()) {
                    Map<String, String> geoResponse = IpGeolocation.getGeolocationFromIP(ip);
                    if (geoResponse.get("error") == null && geoResponse.get("exception") == null) {
                        double userLongitude = Double.parseDouble(geoResponse.get("longitude"));
                        double userLatitude = Double.parseDouble(geoResponse.get("latitude"));

                        System.out.println("Got user loc: " + userLongitude + " " + userLatitude);

                        // Calculate and attach distance for frontend filtering/sorting
                        for (DoctorEntity doctor : doctors) {
                            double doctorLat = parseOrDefault(doctor.getLatitude(), 0);
                            double doctorLon = parseOrDefault(doctor.getLongitude(), 0);
                            double distance = IpGeolocation.calculateDistance(userLatitude, userLongitude, doctorLat, doctorLon);
                            doctor.setDistanceKm(distance);
                        }

                        // Sort doctors by distance using Haversine
                        doctors.sort(Comparator.comparingDouble(doctor -> doctor.getDistanceKm() == null ? Double.MAX_VALUE : doctor.getDistanceKm()));
//                        doctors.sort(Comparator.comparingDouble(doctor -> {
//                            double distance = IpGeolocation.calculateDistance(
//                                    userLatitude,
//                                    userLongitude,
//                                    Double.parseDouble(doctor.getLatitude()),
//                                    Double.parseDouble(doctor.getLongitude())
//                            );
//                            System.out.println("Distance for doctor " + doctor.getName() + ": " + distance); // Replace getName() with an appropriate identifier
//                            return distance;
//                        }));

                    }
                }
            }
            // rating based sorting.
            else {
                doctors.sort((doctor1, doctor2) -> {
                    Double rating1 = doctor1.getRatings() != null ? doctor1.getRatings() : 0.0;
                    Double rating2 = doctor2.getRatings() != null ? doctor2.getRatings() : 0.0;
                    return rating2.compareTo(rating1);
                });
            }

            // Apply minimum rating filter if provided
            if (minRating != null) {
                double min = minRating;
                doctors.removeIf(d -> (d.getRatings() == null ? 0.0 : d.getRatings()) < min);
            }

            // Apply limit if provided
            if (limit != null && limit > 0 && doctors.size() > limit) {
                doctors = doctors.subList(0, Math.min(limit, doctors.size()));
            }


            // Ensure a usable image path shape for frontend rendering
            for (DoctorEntity doctor : doctors) {
                if (doctor.getImage() == null) {
                    doctor.setImage("");
                }
            }

            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.ok(getSampleDoctors(ipSorted));
        }
    }

    public ResponseEntity<?> getAllDoctorsByCoordinates(Double latitude, Double longitude) {
        try {
            List<DoctorEntity> doctors = doctorsRepository.findAll();
            if (doctors.isEmpty()) {
                doctors = getSampleDoctors(true);
            }

            for (DoctorEntity doctor : doctors) {
                double doctorLat = parseOrDefault(doctor.getLatitude(), 0);
                double doctorLon = parseOrDefault(doctor.getLongitude(), 0);
                double distance = IpGeolocation.calculateDistance(latitude, longitude, doctorLat, doctorLon);
                doctor.setDistanceKm(distance);
            }

            doctors.sort(Comparator.comparingDouble(doctor -> doctor.getDistanceKm() == null ? Double.MAX_VALUE : doctor.getDistanceKm()));
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            List<DoctorEntity> fallback = getSampleDoctors(true);
            for (DoctorEntity doctor : fallback) {
                double doctorLat = parseOrDefault(doctor.getLatitude(), 0);
                double doctorLon = parseOrDefault(doctor.getLongitude(), 0);
                double distance = IpGeolocation.calculateDistance(latitude, longitude, doctorLat, doctorLon);
                doctor.setDistanceKm(distance);
            }
            fallback.sort(Comparator.comparingDouble(doctor -> doctor.getDistanceKm() == null ? Double.MAX_VALUE : doctor.getDistanceKm()));
            return ResponseEntity.ok(fallback);
        }
    }

    private double parseOrDefault(String value, double defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private List<DoctorEntity> getSampleDoctors(boolean ipSorted) {
        List<DoctorEntity> sample = new ArrayList<>();

        sample.add(createSampleDoctor(
                "Sample-1",
                "Dr. Nusrat Jahan",
                "01900000001",
                "Child Development and Behavioral Pediatrics",
                "Autism, Speech Delay, Early Intervention",
                "Sat-Thu 4:00 PM - 8:00 PM",
                "12",
                4.8,
                "Dhanmondi, Dhaka",
                "90.3742",
                "23.7461"
        ));

        sample.add(createSampleDoctor(
                "Sample-2",
                "Dr. Tanim Rahman",
                "01900000002",
                "Pediatric Neurology and ASD Follow-up",
                "Neurodevelopment, Learning Disorders",
                "Sun-Thu 6:00 PM - 10:00 PM",
                "9",
                4.6,
                "Uttara, Dhaka",
                "90.4001",
                "23.8759"
        ));

        sample.add(createSampleDoctor(
                "Sample-3",
                "Dr. Marium Akter",
                "01900000003",
                "Speech and Communication Therapy",
                "Speech, Social Skills, Parent Coaching",
                "Sat-Wed 10:00 AM - 2:00 PM",
                "7",
                4.7,
                "Chattogram",
                "91.7832",
                "22.3569"
        ));

        if (!ipSorted) {
            sample.sort((doctor1, doctor2) -> doctor2.getRatings().compareTo(doctor1.getRatings()));
        }

        return sample;
    }

    private DoctorEntity createSampleDoctor(
            String id,
            String name,
            String phone,
            String description,
            String specialities,
            String officeHours,
            String experienceCount,
            Double ratings,
            String address,
            String longitude,
            String latitude
    ) {
        DoctorEntity doctor = new DoctorEntity();
        doctor.setId(id);
        doctor.setName(name);
        doctor.setPhone(phone);
        doctor.setDescription(description);
        doctor.setSpecialities(specialities);
        doctor.setOfficeHours(officeHours);
        doctor.setExperienceCount(experienceCount);
        doctor.setRatings(ratings);
        doctor.setAddress(address);
        doctor.setLongitude(longitude);
        doctor.setLatitude(latitude);
        doctor.setImage("");
        doctor.setWebsite("");
        doctor.setGender("N/A");
        return doctor;
    }
}

