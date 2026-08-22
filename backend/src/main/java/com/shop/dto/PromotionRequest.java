package com.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;


public record PromotionRequest(
        @NotEmpty(message = "Don hang phai co it nhat 1 san pham")
        @Valid
        List<OrderItemRequest> items,

        String couponCode
) {
}
