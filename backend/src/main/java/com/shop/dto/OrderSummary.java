package com.shop.dto;

import java.time.LocalDateTime;

public record OrderSummary(
        String orderId,
        LocalDateTime createdAt,
        int itemCount,
        String region,
        String couponCode,
        long finalTotal
) {
}
