package com.shop.promotion.strategy;

import com.shop.promotion.CouponStrategy;

import com.shop.util.Money;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(2)
public class Sale10PercentCoupon implements CouponStrategy {

    static final int PERCENT = 10;

    @Override
    public String code() {
        return "SALE10PT";
    }

    @Override
    public boolean isApplicable(long base) {
        return true;
    }

    @Override
    public long discount(long base) {
        return Money.percentOf(base, PERCENT);
    }
}
