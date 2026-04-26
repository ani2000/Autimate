package org.ww.wigglew.service.aex;


import org.springframework.stereotype.Service;
import org.ww.wigglew.entity.aex.ASDExEntity;
import org.ww.wigglew.repo.aex.ASDExRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ASDExDBService {

    private final ASDExRepository asdExRepository;

    public ASDExDBService(ASDExRepository asdExRepository) {
        this.asdExRepository = asdExRepository;
    }

    public void deleteASDExEntity(String id) {
        asdExRepository.deleteById(id);
    }

    public List<HashMap<String, Object>> getDocumentsByUsername(String username) {
        List<ASDExEntity> documents = asdExRepository.findByUsername(username);
        return documents.stream()
                .map(this::documentToMap)
                .collect(Collectors.toList());
    }

    public HashMap<String, Object> getDocumentByRequestID(String requestID) {
        Optional<ASDExEntity> document = asdExRepository.findByRequestID(requestID);
        return documentToMap(document);
    }

    private HashMap<String, Object> documentToMap(ASDExEntity document) {
        HashMap<String, Object> map = new HashMap<>();
        if (document != null) {
            map.put("id", document.getId());
            map.put("username", document.getUsername());
            map.put("requestID", document.getRequestID());
            map.put("testDate", document.getTestDate());
            map.put("q10_res", document.getQ10());
            map.put("video_res", document.getVid_res());
            map.put("video_confidence", document.getVid_confid());
            map.put("suggested_therapies", document.getSuggested_therapies());
            map.put("suggested_games", document.getSuggested_games());
        }
        return map;
    }

    private HashMap<String, Object> documentToMap(Optional<ASDExEntity> document) {
        return document.map(this::documentToMap).orElse(new HashMap<>());
    }
}
