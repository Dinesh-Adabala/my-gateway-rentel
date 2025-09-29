package com.ads.mygateway.payment.controller;

import com.ads.mygateway.payment.service.PaymentService;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create/{planId}")
    public ResponseEntity<String> createPayment(@PathVariable int planId) throws Exception {
        String planName;
        int amount;

        if (planId == 1) {
            planName = "Silver";
            amount = 1;
        } else if (planId == 2) {
            planName = "Gold";
            amount = 2;
        } else {
            planName = "Platinum";
            amount = 3;
        }

        String url = paymentService.createPaymentLink(planName, amount,
                "customer@example.com", "9876543210");

        return ResponseEntity.ok(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam Map<String, String> params) {
        try {
            String paymentId = params.get("razorpay_payment_id");
            String orderId = params.get("razorpay_payment_link_id");
            String signature = params.get("razorpay_signature");
            JSONObject json = new JSONObject(params);
            boolean isValid = Utils.verifyPaymentSignature(json, keySecret);

            if (isValid) {
                paymentService.updatePaymentSuccess(paymentId, orderId, signature);
                return ResponseEntity.ok("Payment Successful ✅ : " + paymentId);
            } else {
                paymentService.updatePaymentFailure(orderId);
                return ResponseEntity.badRequest().body("Payment verification failed ❌");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying payment: " + e.getMessage());

        }
    }
}