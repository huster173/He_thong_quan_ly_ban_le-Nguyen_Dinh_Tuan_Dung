package com.shop.promotion;

public interface CouponStrategy {

    String code();

    boolean isApplicable(long base);

    long discount(long base);
}
