package com.shop.dto;

import com.shop.model.Region;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;


public record OrderRequest(
        @NotEmpty(message = "Don hang phai co it nhat 1 san pham")
        @Valid
        List<OrderItemRequest> items,

        String couponCode,

        Region region
) {
    public Region regionOrDefault() {
        return region == null ? Region.NOI_THANH : region;
    }
}
