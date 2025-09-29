package com.ads.mygateway.logindto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditProfileRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String address;
    private String countryCode;
    private String phoneNumber;
    private String profilePic;
    private String about;
}
