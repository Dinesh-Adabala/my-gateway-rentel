package com.ads.mygateway.property.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String propertyId;
    private String propertyName;
    private String location;
    private int guests;
    private int bedrooms;
    private int bathrooms;
    private int kitchens;
    private String ratePeriodStart;
    private String ratePeriodEnd;
    private Double minRate;
    private Double nightlyRate;
    private Double weekendRate;
    private Double weeklyRate;
    private Double monthlyRate;

    @ElementCollection
    private List<String> additionalCharges;  // Eg: ["Cleaning Fee", "Service Fee"]

    @ElementCollection
    private List<String> amenities;  // Eg: ["Shower", "Swimming Pool"]

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(columnDefinition = "TEXT")
    private String policyAndHouseRules;

    @ElementCollection
    private List<String> images;  // store URLs of images (S3 links or paths)
    private String emailId;
    private List<String> icalUrls;
}
