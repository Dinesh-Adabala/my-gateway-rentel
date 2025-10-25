package com.ads.mygateway.captcha.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CaptchaResult {
    private final String id;
    private final String imageBase64;
    private final Instant expiry;

    public CaptchaResult(String id, String imageBase64, Instant expiry) {
        this.id = id;
        this.imageBase64 = imageBase64;
        this.expiry = expiry;
    }
}
