package com.ads.mygateway.logindto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String countryCode;
    private String phoneNumber;
    private String address;
    private String profilePic;
    private String about;
    private boolean verified;
}
