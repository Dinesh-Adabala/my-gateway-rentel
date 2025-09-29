package com.ads.mygateway.property.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDTO {
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
    private List<String> additionalCharges;
    private List<String> amenities;
    private String about;
    private String policyAndHouseRules;
    private List<String> images;
    private String emailId;
}
