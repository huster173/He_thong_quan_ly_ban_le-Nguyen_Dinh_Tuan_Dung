package com.shop.dto;

import java.util.List;


public record PromotionResult(
        List<InvoiceLine> lines,
        long subtotal,
        long categoryDiscount,
        String couponCode,
        long couponDiscount,
        long total
) {
}
