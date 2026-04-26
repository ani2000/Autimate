package org.ww.wigglew.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/verification")
public class IdentityVerificationController {

    @PostMapping("/nid/pseudo")
    public ResponseEntity<Map<String, Object>> verifyNidPseudo(@RequestBody Map<String, String> request) {
        String nid = safe(request.get("nid"));
        String dob = safe(request.get("dob"));
        String fullName = safe(request.get("fullName")).toLowerCase();

        Map<String, String> record1 = Map.of(
                "nid", "1994123412345",
                "dob", "1994-07-12",
                "fullName", "anindya saha"
        );

        Map<String, String> record2 = Map.of(
                "nid", "2001567812345",
                "dob", "2001-02-19",
                "fullName", "muhtasir imran"
        );

        boolean exactMatch = matches(record1, nid, dob, fullName) || matches(record2, nid, dob, fullName);
        boolean partialMatch =
                (record1.get("nid").equals(nid) && record1.get("dob").equals(dob)) ||
                (record2.get("nid").equals(nid) && record2.get("dob").equals(dob));

        Map<String, Object> response = new HashMap<>();
        response.put("mode", "pseudo");

        if (exactMatch) {
            response.put("verified", true);
            response.put("status", "VERIFIED");
            response.put("confidence", 0.98);
            response.put("message", "NID verification successful (pseudo sample record).");
        } else if (partialMatch) {
            response.put("verified", true);
            response.put("status", "VERIFIED_WITH_NAME_WARNING");
            response.put("confidence", 0.78);
            response.put("message", "NID and DOB matched sample record, but name differs.");
        } else if (!nid.isEmpty() && !dob.isEmpty() && !fullName.isEmpty()) {
            // Demo/pseudo mode: accept any filled-in NID data as verified
            response.put("verified", true);
            response.put("status", "DEMO_VERIFIED");
            response.put("confidence", 0.50);
            response.put("message", "NID accepted in demo/pseudo mode.");
        } else {
            response.put("verified", false);
            response.put("status", "INCOMPLETE");
            response.put("confidence", 0.0);
            response.put("message", "Please fill in all NID fields.");
        }

        return ResponseEntity.ok(response);
    }

    private boolean matches(Map<String, String> record, String nid, String dob, String fullName) {
        return record.get("nid").equals(nid)
                && record.get("dob").equals(dob)
                && record.get("fullName").equals(fullName);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
