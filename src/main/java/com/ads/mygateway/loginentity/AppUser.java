package com.ads.mygateway.loginentity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String address;
    private String countryCode;
    private String phoneNumber;

    // @Column(unique = true, nullable = false)
    private String email;

    private String password; // stored hashed

    // store profile pic filename or URL; here we keep simple string
    private String profilePic;
    private String about;

    private boolean verified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
