package com.ads.mygateway.captcha.entity;

import lombok.Data;

import java.time.Instant;

@Data
public class CaptchaEntry {
    private final String id;
    private final String text;
    private final byte[] imageBytes; // PNG bytes
    private final Instant expiry;

    public CaptchaEntry(String id, String text, byte[] imageBytes, Instant expiry) {
        this.id = id;
        this.text = text;
        this.imageBytes = imageBytes;
        this.expiry = expiry;
    }
    public boolean isExpired() {
        return Instant.now().isAfter(expiry);
    }
}
