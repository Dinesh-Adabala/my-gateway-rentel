package com.ads.mygateway.util;

import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MailTemplates {

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
}

