package com.ads.mygateway.inquiry.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponseDTO {
    private Long id;
    private String enquiryId;
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
    private LocalDateTime checkin;
    private LocalDateTime checkout;
    private Integer nights;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}