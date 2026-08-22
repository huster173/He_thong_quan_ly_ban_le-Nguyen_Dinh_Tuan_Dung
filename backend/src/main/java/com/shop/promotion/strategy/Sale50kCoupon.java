package com.shop.promotion.strategy;

import com.shop.promotion.CouponStrategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Sale50kCoupon implements CouponStrategy {

    static final long MIN_SUBTOTAL = 300_000L;
    static final long AMOUNT = 50_000L;

    @Override
    public String code() {
        return "SALE50K";
    }

    @Override
    public boolean isApplicable(long base) {
        return base >= MIN_SUBTOTAL;
    }

    @Override
    public long discount(long base) {
        return AMOUNT;
    }
}
