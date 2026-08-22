package com.shop.service;

import com.shop.dto.InvoiceLine;
import com.shop.dto.OrderItemRequest;
import com.shop.dto.SubtotalResult;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.model.Product;
import com.shop.repository.ProductRepository;
import com.shop.util.Money;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PricingService {

    private final ProductRepository productRepository;

    public PricingService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public SubtotalResult calculate(List<OrderItemRequest> items) {
        return calculateMerged(mergeAndValidate(items));
    }

    public SubtotalResult calculateMerged(Map<String, Integer> quantities) {
        Map<String, Product> products = productRepository.getAllOrThrow(quantities.keySet());

        List<InvoiceLine> lines = new ArrayList<>();
        long subtotal = Money.ZERO;

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            Product product = products.get(entry.getKey());
            int quantity = entry.getValue();
            long lineTotal = Money.times(product.getPrice(), quantity);

            lines.add(new InvoiceLine(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantity,
                    lineTotal,
                    product.getCategory()));

            subtotal += lineTotal;
        }

        return new SubtotalResult(List.copyOf(lines), subtotal);
    }

    public Map<String, Integer> mergeAndValidate(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_ORDER, "Don hang phai co it nhat 1 san pham.");
        }

        Map<String, Integer> merged = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            if (item.productId() == null || item.productId().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_PRODUCT_ID, "productId khong duoc de trong.");
            }
            if (item.quantity() < 1) {
                throw new BusinessException(
                        ErrorCode.INVALID_QUANTITY,
                        "So luong phai >= 1 (san pham '" + item.productId() + "' dang la " + item.quantity() + ").",
                        Map.of("productId", item.productId(), "quantity", item.quantity()));
            }
            merged.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return merged;
    }
}
