package com.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.model.Region;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Invoice(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String orderId,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        LocalDateTime createdAt,

        List<InvoiceLine> lines,
        long subtotal,
        long categoryDiscount,
        String couponCode,
        long couponDiscount,
        long total,
        Region region,

        BigDecimal taxRate,

        long taxAmount,
        long finalTotal,
        boolean stockUpdated
) {
    public Invoice withLines(List<InvoiceLine> lines) {
        return new Invoice(orderId, createdAt, lines, subtotal, categoryDiscount, couponCode,
                couponDiscount, total, region, taxRate, taxAmount, finalTotal, stockUpdated);
    }
}
