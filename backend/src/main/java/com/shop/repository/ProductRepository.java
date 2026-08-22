package com.shop.repository;

import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.model.Product;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private static final RowMapper<Product> MAPPER = (rs, rowNum) -> new Product(
            rs.getString("id"),
            rs.getString("name"),
            rs.getLong("price"),
            rs.getString("category"),
            rs.getInt("stock"));

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.query("SELECT id, name, price, category, stock FROM product ORDER BY id", MAPPER);
    }

    public Product getOrThrow(String productId) {
        try {
            return jdbc.queryForObject(
                    "SELECT id, name, price, category, stock FROM product WHERE id = ?", MAPPER, productId);
        } catch (EmptyResultDataAccessException e) {
            throw notFound(productId);
        }
    }

    public Map<String, Product> getAllOrThrow(Collection<String> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", Collections.nCopies(productIds.size(), "?"));
        Map<String, Product> byId = jdbc.query(
                        "SELECT id, name, price, category, stock FROM product WHERE id IN (" + placeholders + ")",
                        MAPPER, productIds.toArray())
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (String productId : productIds) {
            if (!byId.containsKey(productId)) {
                throw notFound(productId);
            }
        }
        return byId;
    }

    public boolean tryDeduct(String productId, int quantity) {
        int rowsAffected = jdbc.update(
                "UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?",
                quantity, productId, quantity);
        return rowsAffected == 1;
    }

    private BusinessException notFound(String productId) {
        return new BusinessException(
                ErrorCode.PRODUCT_NOT_FOUND,
                "Khong tim thay san pham: '" + productId + "'.",
                Map.of("productId", productId));
    }
}
