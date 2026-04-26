package org.ww.wigglew.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.ww.wigglew.entity.doctor.DoctorEntity;
import org.ww.wigglew.repo.DoctorsRepository;
import org.ww.wigglew.service.DoctorImageService;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.migration", name = "update-doctor-images", havingValue = "true", matchIfMissing = false)
public class DoctorImageMigration implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DoctorImageMigration.class);
    private final DoctorsRepository doctorsRepository;
    private final DoctorImageService doctorImageService;

    public DoctorImageMigration(DoctorsRepository doctorsRepository, DoctorImageService doctorImageService) {
        this.doctorsRepository = doctorsRepository;
        this.doctorImageService = doctorImageService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting doctor image migration (app.migration.update-doctor-images=true)");
        List<DoctorEntity> all = doctorsRepository.findAll();
        if (all == null || all.isEmpty()) {
            log.info("No doctors found, skipping migration.");
            return;
        }

        // Assign a unique image to each doctor, grouped by gender.
        List<DoctorEntity> femaleDocs = new ArrayList<>();
        List<DoctorEntity> maleDocs = new ArrayList<>();
        for (DoctorEntity d : all) {
            if (d.getGender() != null && d.getGender().toLowerCase().contains("female")) {
                femaleDocs.add(d);
            } else if (d.getGender() != null && d.getGender().toLowerCase().contains("male")) {
                maleDocs.add(d);
            } else {
                // Default to female pool if gender isn't specified.
                femaleDocs.add(d);
            }
        }

        int updated = 0;
        updated += assignUniqueImagesPerGender(femaleDocs, "female");
        updated += assignUniqueImagesPerGender(maleDocs, "male");

        if (updated > 0) {
            doctorsRepository.saveAll(all);
            log.info("Doctor image migration completed. Updated {} records.", updated);
        } else {
            log.info("Doctor image migration completed. No changes required.");
        }
    }

    private int assignUniqueImagesPerGender(List<DoctorEntity> docs, String gender) {
        if (docs.isEmpty()) return 0;

        List<String> images = doctorImageService.getImagesForGender(gender);
        if (images.isEmpty()) return 0;

        // Stable ordering ensures migration is repeatable.
        docs.sort((a, b) -> {
            String aKey = a.getId() != null ? a.getId() : a.getName();
            String bKey = b.getId() != null ? b.getId() : b.getName();
            if (aKey == null) aKey = "";
            if (bKey == null) bKey = "";
            return aKey.compareTo(bKey);
        });

        int updated = 0;
        for (int i = 0; i < docs.size(); i++) {
            DoctorEntity doc = docs.get(i);
            String chosen;
            if (i < images.size()) {
                chosen = images.get(i);
            } else {
                // If there are more doctors than images, reuse deterministically (warn once)
                chosen = images.get(i % images.size());
                if (i == images.size()) {
                    log.warn("More doctors ({}) than available {} images ({}); images will be reused.", docs.size(), gender, images.size());
                }
            }
            if (!chosen.equals(doc.getImage())) {
                doc.setImage(chosen);
                updated++;
            }
        }
        return updated;
    }
}
