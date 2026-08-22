package com.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderItemRequest(
        @NotBlank(message = "productId khong duoc de trong")
        String productId,

        @Min(value = 1, message = "quantity phai >= 1")
        int quantity
) {
}
