package org.ww.wigglew.entity.aex;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "asd_ex")
public class ASDExEntity {
    @Id
    private String id;
    private String username;
    private String requestID;
    private LocalDateTime testDate; // Ensure to convert from your long timestamp
    private String q10;
    private String vid_res;
    private String vid_confid;
    private String suggested_therapies;
    private String suggested_games;
}
