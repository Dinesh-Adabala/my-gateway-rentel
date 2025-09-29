package com.ads.mygateway.payment.service;

import com.ads.mygateway.payment.entity.Payment;
import com.ads.mygateway.payment.repository.PaymentRepository;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    private RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    public void init() throws Exception {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    public String createPaymentLink(String planName, int amount, String email, String contact) throws Exception {
        JSONObject request = new JSONObject();
        request.put("amount", amount * 100);
        request.put("currency", "INR");
        request.put("description", "Payment for plan: " + planName);

        String referenceId = UUID.randomUUID().toString();
        request.put("reference_id", referenceId);

        JSONObject customer = new JSONObject();
        customer.put("name", "Test User");
        customer.put("email", email);
        customer.put("contact", contact);
        request.put("customer", customer);

        JSONObject notify = new JSONObject();
        notify.put("sms", true);
        notify.put("email", true);
        request.put("notify", notify);

        request.put("callback_url", "http://localhost:8080/api/payment/callback");
        request.put("callback_method", "get");

        PaymentLink paymentLink = razorpayClient.paymentLink.create(request);

        // Save payment in DB with PENDING status
        Payment payment = Payment.builder().planName(planName).amount(amount).status("PENDING").razorpayOrderId(paymentLink.get("id")) // Razorpay's payment link id
                .referenceId(referenceId).customerEmail(email).customerContact(contact).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        paymentRepository.save(payment);

        return paymentLink.get("short_url");
    }

    public void updatePaymentSuccess(String razorpayPaymentId, String orderId, String signature) {
        Payment payment = paymentRepository.findAll().stream().filter(p -> p.getRazorpayOrderId().equals(orderId)).findFirst().orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));

        payment.setStatus("SUCCESS");
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(signature);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
    }

    public void updatePaymentFailure(String orderId) {
        Payment payment = paymentRepository.findAll().stream().filter(p -> p.getRazorpayOrderId().equals(orderId)).findFirst().orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));

        payment.setStatus("FAILED");
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
    }
}
