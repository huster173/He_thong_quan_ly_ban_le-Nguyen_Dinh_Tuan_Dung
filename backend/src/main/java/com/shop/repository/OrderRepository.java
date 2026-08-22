package com.shop.repository;

import com.shop.dto.Invoice;
import com.shop.dto.InvoiceLine;
import com.shop.dto.OrderSummary;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.model.Region;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {

    private static final RowMapper<OrderSummary> SUMMARY_MAPPER = (rs, rowNum) -> new OrderSummary(
            rs.getString("order_id"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getInt("item_count"),
            rs.getString("region"),
            rs.getString("coupon_code"),
            rs.getLong("final_total"));

    private static final RowMapper<Invoice> HEADER_MAPPER = (rs, rowNum) -> new Invoice(
            rs.getString("order_id"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            List.of(),
            rs.getLong("subtotal"),
            rs.getLong("category_discount"),
            rs.getString("coupon_code"),
            rs.getLong("coupon_discount"),
            rs.getLong("total"),
            Region.from(rs.getString("region")),
            rs.getBigDecimal("tax_rate"),
            rs.getLong("tax_amount"),
            rs.getLong("final_total"),
            true);

    private static final RowMapper<InvoiceLine> LINE_MAPPER = (rs, rowNum) -> new InvoiceLine(
            rs.getString("product_id"),
            rs.getString("name"),
            rs.getLong("unit_price"),
            rs.getInt("quantity"),
            rs.getLong("line_total"),
            rs.getString("category"));

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String nextOrderId() {
        Long next = jdbc.queryForObject("SELECT order_seq.NEXTVAL FROM dual", Long.class);
        return String.format("DH-%06d", next);
    }

    public void save(Invoice invoice) {
        jdbc.update("""
                        INSERT INTO orders (order_id, created_at, subtotal, category_discount,
                                            coupon_code, coupon_discount, total,
                                            region, tax_rate, tax_amount, final_total)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                invoice.orderId(),
                Timestamp.valueOf(invoice.createdAt()),
                invoice.subtotal(),
                invoice.categoryDiscount(),
                invoice.couponCode(),
                invoice.couponDiscount(),
                invoice.total(),
                invoice.region().getCode(),
                invoice.taxRate(),
                invoice.taxAmount(),
                invoice.finalTotal());

        List<InvoiceLine> lines = invoice.lines();
        for (int i = 0; i < lines.size(); i++) {
            InvoiceLine line = lines.get(i);
            jdbc.update("""
                            INSERT INTO order_line (order_id, line_no, product_id, name, category,
                                                    unit_price, quantity, line_total)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    invoice.orderId(), i + 1, line.productId(), line.name(), line.category(),
                    line.unitPrice(), line.quantity(), line.lineTotal());
        }
    }

    public long countAll() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        return total == null ? 0 : total;
    }

    public List<OrderSummary> findPage(int offset, int limit) {
        return jdbc.query("""
                        SELECT o.order_id, o.created_at, o.region, o.coupon_code, o.final_total,
                               (SELECT NVL(SUM(l.quantity), 0) FROM order_line l
                                 WHERE l.order_id = o.order_id) AS item_count
                          FROM orders o
                         ORDER BY o.created_at DESC, o.order_id DESC
                         OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                        """, SUMMARY_MAPPER, offset, limit);
    }

    public Invoice getOrThrow(String orderId) {
        List<Invoice> found = jdbc.query("""
                SELECT order_id, created_at, subtotal, category_discount, coupon_code,
                       coupon_discount, total, region, tax_rate, tax_amount, final_total
                  FROM orders WHERE order_id = ?
                """, HEADER_MAPPER, orderId);

        if (found.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.ORDER_NOT_FOUND,
                    "Khong tim thay don hang: '" + orderId + "'.",
                    Map.of("orderId", orderId));
        }
        return found.get(0).withLines(findLines(orderId));
    }

    private List<InvoiceLine> findLines(String orderId) {
        return jdbc.query("""
                SELECT product_id, name, category, unit_price, quantity, line_total
                  FROM order_line WHERE order_id = ? ORDER BY line_no
                """, LINE_MAPPER, orderId);
    }
}
