package com.shop.service;

import com.shop.dto.InvoiceLine;
import com.shop.dto.PromotionResult;
import com.shop.dto.SubtotalResult;
import com.shop.promotion.CouponContext;
import com.shop.promotion.CouponStrategy;
import com.shop.util.Money;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionService {

    static final int CATEGORY_QUANTITY_THRESHOLD = 3;

    static final int CATEGORY_DISCOUNT_PERCENT = 10;

    private final CouponContext couponContext;

    public PromotionService(CouponContext couponContext) {
        this.couponContext = couponContext;
    }

    public PromotionResult apply(SubtotalResult base, String couponCode) {
        boolean declined = couponContext.isNone(couponCode);
        CouponStrategy requested = declined ? null : couponContext.parse(couponCode);

        long categoryDiscount = Money.ZERO;

        for (CategoryBucket bucket : groupByCategory(base.lines()).values()) {
            if (bucket.quantity < CATEGORY_QUANTITY_THRESHOLD) {
                continue;
            }
            categoryDiscount += Money.percentOf(bucket.amount, CATEGORY_DISCOUNT_PERCENT);
        }

        long afterCategory = Money.atLeastZero(base.subtotal() - categoryDiscount);

        CouponStrategy coupon = requested;
        if (coupon == null && !declined) {
            coupon = couponContext.firstApplicable(afterCategory);
        }

        long couponDiscount = Money.ZERO;
        String appliedCode = null;
        if (coupon != null && coupon.isApplicable(afterCategory)) {
            couponDiscount = coupon.discount(afterCategory);
            appliedCode = coupon.code();
        }

        long total = Money.atLeastZero(afterCategory - couponDiscount);

        return new PromotionResult(
                base.lines(),
                base.subtotal(),
                categoryDiscount,
                appliedCode,
                couponDiscount,
                total);
    }

    private Map<String, CategoryBucket> groupByCategory(List<InvoiceLine> lines) {
        Map<String, CategoryBucket> buckets = new LinkedHashMap<>();
        for (InvoiceLine line : lines) {
            CategoryBucket bucket = buckets.computeIfAbsent(line.category(), k -> new CategoryBucket());
            bucket.quantity += line.quantity();
            bucket.amount += line.lineTotal();
        }
        return buckets;
    }

    private static final class CategoryBucket {
        private int quantity;
        private long amount = Money.ZERO;
    }
}
