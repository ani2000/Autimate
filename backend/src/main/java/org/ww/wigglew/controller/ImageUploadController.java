package org.ww.wigglew.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.ww.wigglew.models.request.ImageRequestModel;
import org.ww.wigglew.service.ChildService;
import org.ww.wigglew.config.jwt.JWTExtractorService;
import org.ww.wigglew.service.GeminiImageUploaderService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Base64;


@RestController
@RequestMapping("/api/v1/drawing")
public class ImageUploadController {

    @Value("${upload.path:/tmp/uploads}")  // Default path if not specified in application.properties
    private String uploadPath;

    private final ChildService childService;
    private final JWTExtractorService jwtExtractorService;
    private final GeminiImageUploaderService geminiImageUploaderService;

    public ImageUploadController(ChildService childService, JWTExtractorService jwtExtractorService, GeminiImageUploaderService geminiImageUploaderService) {
        this.childService = childService;
        this.jwtExtractorService = jwtExtractorService;
        this.geminiImageUploaderService = geminiImageUploaderService;
    }

    @PostMapping("/compare_drawn_and_reference")
    public ResponseEntity<?> handleImageUpload(@RequestBody ImageRequestModel request,
                                               @RequestHeader("Authorization") String jwtToken) {
        try {
            String childId = childService.getActiveChild(jwtToken);
            if (childId == null || childId.isEmpty()) {
                return ResponseEntity.status(403).body("Invalid or missing token");
            }

            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(uploadPath));

            // Generate random filename
            String randomFilename = UUID.randomUUID().toString() + ".png";
            Path destinationPath = Paths.get(uploadPath, randomFilename);
            System.out.println("_--\n\n --- destionation path : " + destinationPath.toString());

            // Remove base64 header if present
            String base64Image = request.getImage();
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }

            // Decode and save the image
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            Files.write(destinationPath, imageBytes);

            System.out.println("Destination path: " + destinationPath.toString());

            //now call gemini for response
            String feedbackOnImage = geminiImageUploaderService.getImageFeedback(String.valueOf(destinationPath));

            return ResponseEntity.ok().body(feedbackOnImage);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to save image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
