package com.shop.dto;

import java.util.List;

public record SubtotalResult(
        List<InvoiceLine> lines,
        long subtotal
) {
}
