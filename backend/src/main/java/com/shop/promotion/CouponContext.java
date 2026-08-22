package com.shop.promotion;

import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CouponContext {

    public static final String NONE = "NONE";

    private final List<CouponStrategy> strategies;

    public CouponContext(List<CouponStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public boolean isNone(String code) {
        return code != null && NONE.equalsIgnoreCase(code.trim());
    }

    public CouponStrategy firstApplicable(long base) {
        return strategies.stream()
                .filter(strategy -> strategy.isApplicable(base))
                .findFirst()
                .orElse(null);
    }

    public CouponStrategy parse(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalized = code.trim().toUpperCase();
        return strategies.stream()
                .filter(strategy -> strategy.code().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_COUPON,
                        "Ma giam gia khong ton tai: '" + code + "'. Cho phep: " + availableCodes() + "."));
    }

    public String availableCodes() {
        return strategies.stream().map(CouponStrategy::code).collect(Collectors.joining(", "));
    }
}
