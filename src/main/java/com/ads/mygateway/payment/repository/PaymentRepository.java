package com.ads.mygateway.payment.repository;

import com.ads.mygateway.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

