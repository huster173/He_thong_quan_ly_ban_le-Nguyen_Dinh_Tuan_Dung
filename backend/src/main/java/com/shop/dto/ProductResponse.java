package com.shop.dto;

import com.shop.model.Product;

public record ProductResponse(
        String id,
        String name,
        long price,
        String category,
        int stock
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getCategory(), p.getStock());
    }
}
