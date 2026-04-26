package org.ww.wigglew.controller.aex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.ww.wigglew.config.jwt.JWTExtractorService;
import org.ww.wigglew.entity.aex.ASDExEntity;
import org.ww.wigglew.entity.aex.QuestionExamEntity;
import org.ww.wigglew.models.request.AsdExRequest;
import org.ww.wigglew.models.request.TherapySuggestionPromptStringRequest;
import org.ww.wigglew.repo.aex.ASDExRepository;
import org.ww.wigglew.service.ChildService;
import org.ww.wigglew.service.TherapySuggestionAPIService;
import org.ww.wigglew.service.aex.ASDExDBService;
import org.ww.wigglew.service.aex.ASDExServerlessInvokeService;
import org.ww.wigglew.service.aex.AutismExQ10Service;
import org.ww.wigglew.service.aex.PseudoAssessmentService;
import org.ww.wigglew.service.BucketStorageService;

@RestController
@RequestMapping("/api/v1/aex")
public class AutismExController {
    @Autowired
    AutismExQ10Service autismExQ10Service;

    @Autowired
    BucketStorageService bucketStorageService;

    @Autowired
    ASDExRepository asdExRepository;

    @Autowired
    ASDExDBService asdExDBService;

    @Autowired
    ASDExServerlessInvokeService asdExServerlessInvokeService;

    @Autowired
    PseudoAssessmentService pseudoAssessmentService;

    @Autowired
    TherapySuggestionAPIService therapySuggestionAPIService;

    @Autowired
    private JWTExtractorService jwtExtractorService;

    @Autowired
    private ChildService childService;

    @Value("${SERVERLESS_ML_VIDEO_URL:https://hossen1907012--autism-video-analysis-fn-main.modal.run}")
    private String serverlessBaseUrl;

    @Value("${SERVERLESS_SECRET_TOKEN:}")
    private String serverlessSecretToken;

    @Value("${app.aex.pseudo-mode:true}")
    private boolean pseudoMode;

    private static final Logger logger = LoggerFactory.getLogger(AutismExController.class);

    @PostMapping("/invoke")
    public ResponseEntity<?> invokeServerless(@RequestBody AsdExRequest asdExRequest, @RequestHeader("Authorization") String jwtToken) {
        try {
            String childId = childService.getActiveChild(jwtToken);

            asdExRequest.setChildId(childId);
            asdExRequest.setSecret_token(serverlessSecretToken);

            if (pseudoMode) {
                ASDExEntity saved = pseudoAssessmentService.generateAndSave(asdExRequest);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Pseudo assessment generated successfully");
                response.put("mode", "pseudo");
                response.put("requestID", saved.getRequestID());
                return ResponseEntity.ok(response);
            }

            String serverlessUrl = serverlessBaseUrl;

            String err = asdExServerlessInvokeService.invokeServerless(serverlessUrl, asdExRequest);

            if (err == null) {
                return ResponseEntity.ok("Invocation successful");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invocation failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


    @PostMapping("/therapy_suggestion")
    public ResponseEntity<?> getTherapySuggestion(@RequestBody TherapySuggestionPromptStringRequest suggestionPromptStr, @RequestHeader("Authorization") String jwtToken){
        try {
            String childId = childService.getActiveChild(jwtToken);
            suggestionPromptStr.setChildId(childId);

            //request gpt.


            String err = null; // dummy. remove must.
            if (err == null) {
                return ResponseEntity.ok("Invocation successful");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invocation failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    /**
     * @return Pre-signed url to access digitalocean s3
     */
    @GetMapping("/url/presigned")
    public String fetchPreSignedUrl() throws Exception {
        return bucketStorageService.getPreSignedUrl("mp4");
    }


    @GetMapping("/lists")
    public ResponseEntity<?> getAllTest(@RequestHeader("Authorization") String jwtToken) {
        logger.info("Received GET /lists request");
        try {
            String childId = childService.getActiveChild(jwtToken);
            logger.debug("Child ID: {}", childId);

            if (childId != null) {
                List<ASDExEntity> tests = asdExRepository.findByUsername(childId);
                logger.info("Found {} tests for child ID {}", tests.size(), childId);
                return ResponseEntity.ok(tests);
            } else {
                logger.warn("No child found for JWT token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No child found");
            }
        } catch (Exception e) {
            logger.error("Exception in /lists endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestHeader("Authorization") String jwtToken) {
        try {
            String childId = childService.getActiveChild(jwtToken);
            if (childId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active child selected");
            }

            List<ASDExEntity> rows = asdExRepository.findByUsername(childId);
            rows.sort(Comparator.comparing(ASDExEntity::getTestDate, Comparator.nullsLast(Comparator.naturalOrder())));

            List<Map<String, Object>> mapped = new ArrayList<>();
            for (ASDExEntity row : rows) {
                double videoConfidence = 0.0;
                try {
                    videoConfidence = Double.parseDouble(row.getVid_confid());
                } catch (Exception ignored) { }
                int score = (int) Math.round(Math.max(0, Math.min(1, videoConfidence)) * 100);

                Map<String, Object> item = new HashMap<>();
                item.put("id", row.getId());
                item.put("requestID", row.getRequestID());
                item.put("createdAt", row.getTestDate());
                item.put("score", score);
                item.put("result", score >= 65 ? "At Risk" : score >= 40 ? "Moderate" : "Low Risk");
                item.put("q10", row.getQ10());
                item.put("video_confidence", row.getVid_confid());
                mapped.add(item);
            }

            return ResponseEntity.ok(mapped);
        } catch (Exception e) {
            logger.error("Exception in /history endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    /**
     * @return Return a single test's result. Necessary for Video Invoking.
     */
    @GetMapping("/res/{requestID}")
    public ResponseEntity<?> getDocumentByRequestID(@PathVariable String requestID) {
        logger.info("Received GET /result/{} request", requestID);
        try {
            HashMap<String, Object> document = asdExDBService.getDocumentByRequestID(requestID);
            if (document != null && !document.isEmpty()) {
                logger.info("Found document for requestID: {}", requestID);
                return ResponseEntity.ok(document);
            } else {
                logger.warn("No document found for requestID: {}", requestID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No document found");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while fetching document for requestID: {}", requestID, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the document");
        }
    }
}
