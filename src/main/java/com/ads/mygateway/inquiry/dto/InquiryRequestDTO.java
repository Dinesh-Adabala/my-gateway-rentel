package com.ads.mygateway.inquiry.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryRequestDTO {
    private String propertyId;
    private String propertyName;
    private String propertyImages;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private Integer guests;
    private Double totalAmount;
    private String message;
    private String checkin;   // yyyy-MM-ddTHH:mm:ss
    private String checkout;  // yyyy-MM-ddTHH:mm:ss
    private Integer nights;
}