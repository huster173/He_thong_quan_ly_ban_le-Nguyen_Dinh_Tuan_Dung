package com.shop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.Arrays;


public enum Region {

    NOI_THANH("noi_thanh", 8),
    NGOAI_THANH("ngoai_thanh", 10),
    TINH_KHAC("tinh_khac", 12);

    private final String code;
    private final int taxPercent;

    Region(String code, int taxPercent) {
        this.code = code;
        this.taxPercent = taxPercent;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public int getTaxPercent() {
        return taxPercent;
    }

    public BigDecimal getTaxRate() {
        return BigDecimal.valueOf(taxPercent, 2);
    }

    @JsonCreator
    public static Region from(String code) {
        if (code == null || code.isBlank()) {
            return NOI_THANH;
        }
        return Arrays.stream(values())
                .filter(r -> r.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_REGION,
                        "Khu vuc khong hop le: '" + code + "'. Cho phep: noi_thanh, ngoai_thanh, tinh_khac."));
    }
}
