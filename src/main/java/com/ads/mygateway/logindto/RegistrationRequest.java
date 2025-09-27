package com.ads.mygateway.logindto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    @NotBlank
    private String firstName;
    @NotBlank private String lastName;
    private String address;
    private String countryCode;
    private String phoneNumber;

    public String getEmail() {
        return email;
    }

    @Email
    @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
    // profilePic can be a base64 string or filename — for simplicity accept filename
    private String profilePic;
}

