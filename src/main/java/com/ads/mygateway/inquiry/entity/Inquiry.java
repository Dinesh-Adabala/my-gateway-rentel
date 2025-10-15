package com.ads.mygateway.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
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