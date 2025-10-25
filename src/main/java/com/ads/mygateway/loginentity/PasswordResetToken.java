package com.ads.mygateway.loginentity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    // store numeric OTP as string (e.g. "123456")
    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiryAt;

    private LocalDateTime createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(String email, String otp, LocalDateTime expiryAt) {
        this.email = email;
        this.otp = otp;
        this.expiryAt = expiryAt;
        this.createdAt = LocalDateTime.now();
    }

}
