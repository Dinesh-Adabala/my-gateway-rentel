package com.ads.mygateway.util;

import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MailTemplates {

    private static String loadTemplate(String name) throws IOException {
        return StreamUtils.copyToString(
                MailTemplates.class.getResourceAsStream("/templates/" + name),
                StandardCharsets.UTF_8);
    }
    // Simple method that loads the resource and replaces placeholders
    public static String verificationEmail(String name, String link) {
        try {
            String raw = StreamUtils.copyToString(
                    MailTemplates.class.getResourceAsStream("/templates/verification-email.html"),
                    StandardCharsets.UTF_8);
            return raw.replace("[[name]]", name == null ? "User" : name)
                    .replace("[[link]]", link);
        } catch (IOException e) {
            // fallback to basic html if template fails
            return "<p>Hello " + name + "</p>"
                    + "<p>Click the link to verify: <a href=\"" + link + "\">" + link + "</a></p>";
        }
    }

    public static String guestInquiryCreated(String guestName, String propertyName, String checkin, String checkout) {
        try {
            String html = loadTemplate("guest-inquiry-created.html");
            return html.replace("[[guestName]]", guestName)
                    .replace("[[propertyName]]", propertyName)
                    .replace("[[checkin]]", checkin)
                    .replace("[[checkout]]", checkout);
        } catch (IOException e) {
            return "Hello " + guestName + ", your booking request for " + propertyName + " was received.";
        }
    }

    public static String ownerInquiryNotification(String propertyName, String guestName, String guestEmail,
                                                  String guestPhone, String checkin, String checkout, String guests) {
        try {
            String html = loadTemplate("owner-inquiry-notification.html");
            return html.replace("[[propertyName]]", propertyName)
                    .replace("[[guestName]]", guestName)
                    .replace("[[guestEmail]]", guestEmail)
                    .replace("[[guestPhone]]", guestPhone)
                    .replace("[[checkin]]", checkin)
                    .replace("[[checkout]]", checkout)
                    .replace("[[guests]]", guests);
        } catch (IOException e) {
            return "New inquiry for " + propertyName + " from " + guestName;
        }
    }

    public static String bookingConfirmed(String guestName, String propertyName, String checkin, String checkout) {
        try {
            String html = loadTemplate("booking-confirmed.html");
            return html.replace("[[guestName]]", guestName)
                    .replace("[[propertyName]]", propertyName)
                    .replace("[[checkin]]", checkin)
                    .replace("[[checkout]]", checkout);
        } catch (IOException e) {
            return "Hello " + guestName + ", your booking for " + propertyName + " is confirmed.";
        }
    }
}

