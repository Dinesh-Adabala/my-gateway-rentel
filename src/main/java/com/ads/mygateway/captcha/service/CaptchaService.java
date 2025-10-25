package com.ads.mygateway.captcha.service;

import com.ads.mygateway.captcha.dto.CaptchaResult;
import com.ads.mygateway.captcha.entity.CaptchaEntry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Base64;

@Service
public class CaptchaService {

    // in-memory store (replace with Redis for distributed apps)
    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    // configuration (tweak as needed)
    private final Duration ttl = Duration.ofMinutes(5);    // captcha lifetime
    private final int width = 220;
    private final int height = 60;
    private final int defaultLength = 6;
    private final int defaultNoiseLines = 6;

    private final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // avoid ambiguous chars

    public CaptchaService() {}

    /**
     * Create and store a new captcha. Returns id + base64 image (data:image/png;base64,...).
     */
    public CaptchaResult createCaptcha() {
        return createCaptcha(defaultLength, defaultNoiseLines);
    }

    /**
     * Create captcha with custom length/noise (for adaptive difficulty).
     */
    public CaptchaResult createCaptcha(int length, int noiseLines) {
        String id = UUID.randomUUID().toString();
        String text = randomText(length);
        byte[] imageBytes = renderPng(text, noiseLines);
        Instant expiry = Instant.now().plus(ttl);

        CaptchaEntry entry = new CaptchaEntry(id, text, imageBytes, expiry);
        store.put(id, entry);

        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        return new CaptchaResult(id, base64, expiry);
    }

    /**
     * Regenerate a new captcha for the same id (keeps same id). Useful for "refresh".
     * Returns new base64 image and updates expiry/text.
     */
    public CaptchaResult regenerate(String id) {
        // If id doesn't exist or expired, create new id (fallback)
        if (id == null || !store.containsKey(id) || store.get(id).isExpired()) {
            return createCaptcha();
        }
        String text = randomText(defaultLength);
        byte[] imageBytes = renderPng(text, defaultNoiseLines);
        Instant expiry = Instant.now().plus(ttl);
        CaptchaEntry entry = new CaptchaEntry(id, text, imageBytes, expiry);
        store.put(id, entry);
        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        return new CaptchaResult(id, base64, expiry);
    }

    /**
     * Validate captcha answer. On success the entry is removed to avoid reuse.
     * Returns true if valid; false otherwise.
     */
    public boolean validate(String id, String answer) {
        if (id == null || answer == null) return false;
        CaptchaEntry entry = store.get(id);
        if (entry == null) return false;
        if (entry.isExpired()) {
            store.remove(id);
            return false;
        }
        boolean ok = entry.getText().equalsIgnoreCase(answer.trim());
        if (ok) store.remove(id); // delete after successful validation
        return ok;
    }

    /**
     * Remove a captcha (manual delete).
     */
    public void delete(String id) {
        if (id != null) store.remove(id);
    }

    /**
     * Return raw image bytes for GET /{id}.png (or null if not found/expired).
     */
    public byte[] getImageBytes(String id) {
        CaptchaEntry entry = store.get(id);
        if (entry == null) return null;
        if (entry.isExpired()) {
            store.remove(id);
            return null;
        }
        return entry.getImageBytes();
    }

    // cleanup task to remove expired captchas
    @Scheduled(fixedDelay = 60_000) // every minute
    public void cleanup() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> e.getValue().getExpiry().isBefore(now));
    }

    // ---- helpers ----

    private String randomText(int n) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private byte[] renderPng(String text, int noiseLines) {
        try {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();

            // smooth rendering
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            ThreadLocalRandom rnd = ThreadLocalRandom.current();

            // background noise - colored ellipses
            for (int i = 0; i < 12; i++) {
                g.setColor(new Color(rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200), 30));
                int rx = rnd.nextInt(width);
                int ry = rnd.nextInt(height);
                int rw = rnd.nextInt(10, 60);
                int rh = rnd.nextInt(10, 60);
                g.fillOval(rx - rw/2, ry - rh/2, rw, rh);
            }

            // draw random lines (noise)
            for (int i = 0; i < noiseLines; i++) {
                g.setColor(new Color(rnd.nextInt(120), rnd.nextInt(120), rnd.nextInt(120), 120));
                int x1 = rnd.nextInt(width);
                int y1 = rnd.nextInt(height);
                int x2 = rnd.nextInt(width);
                int y2 = rnd.nextInt(height);
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(x1, y1, x2, y2);
            }

            // font & text rendering with per-char rotation
            Font font = new Font("Verdana", Font.BOLD, Math.max(30, height - 20));
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();

            int x = 20;
            for (int i = 0; i < text.length(); i++) {
                String ch = text.substring(i, i + 1);
                // random color not too light
                g.setColor(new Color(rnd.nextInt(40, 120), rnd.nextInt(40, 120), rnd.nextInt(40, 120)));
                // small rotation
                double angle = (rnd.nextDouble() - 0.5) * 0.5; // -0.25..0.25 rad
                AffineTransform orig = g.getTransform();
                int charWidth = fm.charWidth(ch.charAt(0));
                int y = (height + fm.getAscent()) / 2 - 6;
                g.rotate(angle, x + charWidth / 2.0, y - fm.getAscent()/2.0);
                g.drawString(ch, x, y);
                g.setTransform(orig);
                x += charWidth + 6;
            }

            // more noise dots
            for (int i = 0; i < 60; i++) {
                g.setColor(new Color(rnd.nextInt(150), rnd.nextInt(150), rnd.nextInt(150), 160));
                int px = rnd.nextInt(width);
                int py = rnd.nextInt(height);
                g.fillRect(px, py, 1, 1);
            }

            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to render captcha", ex);
        }
    }
}
