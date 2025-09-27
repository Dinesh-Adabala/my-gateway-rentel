package com.ads.mygateway.property.dto;

import lombok.Data;

import java.util.List;

@Data
public class PropertyRequest {
    private String title;
    private String country;
    private String state;
    private String city;
    private String address;

    private int bedrooms;
    private int bathrooms;
    private int sqft;
    private double price;
    private int yearBuilt;

    private List<String> amenities;
    private String description;
    private List<String> images;
}
