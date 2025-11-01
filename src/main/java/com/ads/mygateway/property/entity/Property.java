package com.ads.mygateway.property.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String state;
    private String country;
    private Integer guests;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer kitchens;
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
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "property_ical_urls",
            joinColumns = @JoinColumn(name = "property_id")
    )
    @MapKeyColumn(name = "source")   // KEY column in the join table
    @Column(name = "url", columnDefinition = "text") // VALUE column
    private Map<String, String> icalUrls = new HashMap<>();
    private String latitude;
    private String longitude;
}
