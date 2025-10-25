package com.ads.mygateway.property.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationSuggestionDTO {
    private String location;
    private String state;
    private String country;

}
