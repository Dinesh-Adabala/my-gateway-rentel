package com.ads.mygateway.payment.controller;

import com.razorpay.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;

@RestController
@RequestMapping("/api/payment")
public class RazorpayWebhookController {

    @Value("${razorpay.webhookSecret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public String handleWebhook(HttpServletRequest request,
                                @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            StringBuilder inputBuffer = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    inputBuffer.append(line);
                }
            }
            String payload = inputBuffer.toString();

            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);

            if (isValid) {
                System.out.println("✅ Verified Webhook Payload: " + payload);
                // TODO: Update payment status in DB
                return "success";
            } else {
                System.out.println("❌ Invalid Signature");
                return "invalid";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}