package com.shop.service;

import com.shop.model.Region;
import com.shop.util.Money;
import org.springframework.stereotype.Service;

@Service
public class TaxService {

    public long taxFor(long totalAfterDiscount, Region region) {
        return Money.percentOf(totalAfterDiscount, region.getTaxPercent());
    }
}
