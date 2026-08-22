package com.shop.service;

import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.model.Product;
import com.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void deduct(Map<String, Integer> quantities) {
        List<String> productIds = quantities.keySet().stream().sorted().toList();

        for (String productId : productIds) {
            int need = quantities.get(productId);
            if (!productRepository.tryDeduct(productId, need)) {
                Product product = productRepository.getOrThrow(productId);
                throw outOfStock(product, need, product.getStock());
            }
        }
    }

    private BusinessException outOfStock(Product product, int need, int available) {
        return new BusinessException(
                ErrorCode.OUT_OF_STOCK,
                "San pham '" + product.getName() + "' chi con " + available
                        + " trong kho, don can " + need + ".",
                Map.of(
                        "productId", product.getId(),
                        "productName", product.getName(),
                        "requested", need,
                        "available", available));
    }
}
