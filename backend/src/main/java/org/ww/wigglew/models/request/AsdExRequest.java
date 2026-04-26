package org.ww.wigglew.models.request;


import java.util.List;


public class AsdExRequest {
    private List<Integer> arrq10;
    private String video_name;
    private String secret_token;
    private String childId;

    // Getters and Setters
    public List<Integer> getArrq10() {
        return arrq10;
    }

    public void setArrq10(List<Integer> arrq10) {
        this.arrq10 = arrq10;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public String getSecret_token() {
        return secret_token;
    }

    public void setSecret_token(String secret_token) {
        this.secret_token = secret_token;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }
}
