package com.ads.mygateway.property.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PropertySearchRequest {
    private String name;
    private LocalDateTime checkin;
    private LocalDateTime checkout;

}
