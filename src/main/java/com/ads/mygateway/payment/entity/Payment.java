package com.ads.mygateway.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String planName;
    private int amount; // in INR
    private String status; // PENDING, SUCCESS, FAILED
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private String referenceId;

    private String customerEmail;
    private String customerContact;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

