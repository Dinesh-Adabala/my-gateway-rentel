package com.ads.mygateway.payment.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    private String orderId;
    private String key;
    private int amount;
    private String currency;
    private String planName;
}
