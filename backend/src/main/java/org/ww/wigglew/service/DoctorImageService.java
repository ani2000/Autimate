package org.ww.wigglew.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Loads and provides access to the built-in doctor avatar images shipped with the application.
 * <p>
 * The images are expected to live under <code>classpath:/static/doctors/female</code> and
 * <code>classpath:/static/doctors/male</code>. The service exposes a deterministic mapping
 * from a gender + seed (e.g. doctor name or id) to one of the available images.
 */
@Service
@Slf4j
public class DoctorImageService {

    private static final String FEMALE_IMAGE_PATTERN = "classpath:/static/doctors/female/*.*";
    private static final String MALE_IMAGE_PATTERN = "classpath:/static/doctors/male/*.*";

    private final List<String> femaleImages = new ArrayList<>();
    private final List<String> maleImages = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadImages("female", FEMALE_IMAGE_PATTERN, femaleImages);
        loadImages("male", MALE_IMAGE_PATTERN, maleImages);

        // Sort by numeric filename (1.jpeg, 2.jpeg, ...). Ensures deterministic assignment.
        sortImageList(femaleImages);
        sortImageList(maleImages);

        if (femaleImages.isEmpty()) {
            femaleImages.add("/doctors/female/1.jpeg");
        }
        if (maleImages.isEmpty()) {
            maleImages.add("/doctors/male/1.jpeg");
        }

        log.info("DoctorImageService loaded {} female images and {} male images", femaleImages.size(), maleImages.size());
    }

    private void loadImages(String gender, String pattern, List<String> target) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pattern);
            for (Resource r : resources) {
                String filename = r.getFilename();
                if (filename != null && !filename.isBlank()) {
                    target.add(String.format("/doctors/%s/%s", gender, filename));
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load {} images from pattern {}: {}", gender, pattern, e.getMessage());
        }
    }

    /**
     * Returns a deterministic image url for the given gender and seed (e.g. doctor id or name).
     */
    public String getImageForGender(String gender, String seed) {
        List<String> pool = getPool(gender);
        if (pool.isEmpty()) {
            return "";
        }
        int idx = Math.floorMod(Objects.hashCode(seed), pool.size());
        return pool.get(idx);
    }

    public List<String> getImagesForGender(String gender) {
        return new ArrayList<>(getPool(gender));
    }

    public String getNextAvailableImage(String gender, java.util.Set<String> usedImages) {
        List<String> pool = getPool(gender);
        if (pool.isEmpty()) return "";

        for (String candidate : pool) {
            if (!usedImages.contains(candidate)) {
                return candidate;
            }
        }

        // If we run out of unused images, fall back to deterministic choice.
        int idx = Math.floorMod(Objects.hashCode(gender), pool.size());
        return pool.get(idx);
    }

    private void sortImageList(List<String> list) {
        list.sort((a, b) -> {
            try {
                int na = Integer.parseInt(a.replaceAll(".*\\/(\\d+).*", "$1"));
                int nb = Integer.parseInt(b.replaceAll(".*\\/(\\d+).*", "$1"));
                return Integer.compare(na, nb);
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });
    }

    private List<String> getPool(String gender) {
        if (gender == null) {
            return femaleImages;
        }
        String normalized = gender.trim().toLowerCase();
        if (normalized.contains("female")) {
            return femaleImages;
        }
        if (normalized.contains("male")) {
            return maleImages;
        }
        return femaleImages;
    }
}
