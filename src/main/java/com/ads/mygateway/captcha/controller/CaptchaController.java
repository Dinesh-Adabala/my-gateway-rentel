package com.ads.mygateway.captcha.controller;

import com.ads.mygateway.captcha.dto.CaptchaResult;
import com.ads.mygateway.captcha.service.CaptchaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private final CaptchaService service;

    public CaptchaController(CaptchaService service) {
        this.service = service;
    }

    /**
     * Create new captcha -> returns JSON { id, imageBase64, expiry }
     */
    @GetMapping("/create-captcha")
    public ResponseEntity<CaptchaResult> create() {
        CaptchaResult res = service.createCaptcha();
        return ResponseEntity.ok(res);
    }

    /**
     * Direct image endpoint for <img src="/api/captcha/{id}.png">.
     * Returns 404 if not found/expired.
     */
    @GetMapping(value = "/{id}.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image(@PathVariable String id) {
        byte[] bytes = service.getImageBytes(id);
        if (bytes == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate").body(bytes);
    }

    /**
     * Validate captcha. On success captcha is deleted.
     * Request: { "captchaId":"...", "answer":"ABC123" }
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, String> body) {
        String id = body.get("captchaId");
        String answer = body.get("answer");
        boolean ok = service.validate(id, answer);
        if (ok) {
            return ResponseEntity.ok(Map.of("valid", true, "message", "Captcha valid"));
        } else {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Invalid or expired captcha"));
        }
    }

    /**
     * Regenerate captcha for same id (refresh) - returns new imageBase64
     * Request: { "captchaId":"..." }
     * If id expired/not found a new id is returned.
     */
    @PostMapping("/regenerate")
    public ResponseEntity<CaptchaResult> regenerate(@RequestBody Map<String, String> body) {
        String id = body.get("captchaId");
        CaptchaResult res = service.regenerate(id);
        return ResponseEntity.ok(res);
    }

    /**
     * Delete captcha manually (optional)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
