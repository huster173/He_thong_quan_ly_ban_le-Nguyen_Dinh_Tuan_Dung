package com.shop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record InvoiceLine(
        String productId,
        String name,
        long unitPrice,
        int quantity,
        long lineTotal,

        @JsonIgnore
        String category
) {
}
