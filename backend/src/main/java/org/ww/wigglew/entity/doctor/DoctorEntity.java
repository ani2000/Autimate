package org.ww.wigglew.entity.doctor;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Transient;

@Data
@Document(collection = "doctor")
public class DoctorEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String phone;

    private String name;
    private String description;
    private String experienceCount; //example: 19 years
    private String gender;
    private Double ratings;
    private String specialities;
    private String image; //url
    private String officeHours;
    private String website;

    private String longitude;
    private String latitude;
    private String address;

    @Transient
    private Double distanceKm;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}