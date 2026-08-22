package com.shop.util;

public final class Money {

    public static final long ZERO = 0L;

    private Money() {
    }

    public static long times(long amount, int quantity) {
        return Math.multiplyExact(amount, (long) quantity);
    }

    public static long percentOf(long amount, int percent) {
        long scaled = Math.multiplyExact(amount, (long) percent);
        long sign = scaled < 0 ? -1 : 1;
        return sign * ((Math.abs(scaled) + 50) / 100);
    }

    public static long atLeastZero(long amount) {
        return Math.max(0L, amount);
    }
}
