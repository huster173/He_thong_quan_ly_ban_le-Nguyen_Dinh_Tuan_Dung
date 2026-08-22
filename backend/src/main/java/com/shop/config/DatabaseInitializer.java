package com.shop.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void initialize() {
        if (tableExists("PRODUCT")) {
            log.info("Schema da ton tai, bo qua buoc khoi tao.");
            return;
        }

        log.info("Chua co schema -- dang tao bang va nap du lieu mau.");
        createSchema();
        seedProducts();
        log.info("Khoi tao database xong.");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_tables WHERE table_name = ?", Integer.class, tableName);
        return count != null && count > 0;
    }

    private void createSchema() {
        List<String> statements = List.of("""
                        CREATE TABLE product (
                            id       VARCHAR2(10)  PRIMARY KEY,
                            name     VARCHAR2(100) NOT NULL,
                            price    NUMBER(15,0)  NOT NULL,
                            category VARCHAR2(40)  NOT NULL,
                            stock    NUMBER(10,0)  NOT NULL
                                CONSTRAINT product_stock_non_negative CHECK (stock >= 0)
                        )
                        """, """
                        CREATE TABLE orders (
                            order_id          VARCHAR2(20)  PRIMARY KEY,
                            created_at        TIMESTAMP     NOT NULL,
                            subtotal          NUMBER(15,0)  NOT NULL,
                            category_discount NUMBER(15,0)  NOT NULL,
                            coupon_code       VARCHAR2(20),
                            coupon_discount   NUMBER(15,0)  NOT NULL,
                            total             NUMBER(15,0)  NOT NULL,
                            region            VARCHAR2(20)  NOT NULL,
                            tax_rate          NUMBER(5,4)   NOT NULL,
                            tax_amount        NUMBER(15,0)  NOT NULL,
                            final_total       NUMBER(15,0)  NOT NULL
                        )
                        """, """
                        CREATE TABLE order_line (
                            order_id   VARCHAR2(20)  NOT NULL REFERENCES orders(order_id),
                            line_no    NUMBER(4,0)   NOT NULL,
                            product_id VARCHAR2(10)  NOT NULL,
                            name       VARCHAR2(100) NOT NULL,
                            category   VARCHAR2(40)  NOT NULL,
                            unit_price NUMBER(15,0)  NOT NULL,
                            quantity   NUMBER(10,0)  NOT NULL,
                            line_total NUMBER(15,0)  NOT NULL,
                            CONSTRAINT order_line_pk PRIMARY KEY (order_id, line_no)
                        )
                        """,
                "CREATE SEQUENCE order_seq START WITH 1 INCREMENT BY 1 NOCACHE");

        statements.forEach(jdbc::execute);
    }

    private void seedProducts() {
        insert("P01", "Ao thun", 150000, "clothing", 20);
        insert("P02", "Quan jean", 450000, "clothing", 10);
        insert("P03", "Tai nghe", 890000, "electronics", 5);
        insert("P04", "Sac du phong", 350000, "electronics", 8);
    }

    private void insert(String id, String name, long price, String category, int stock) {
        jdbc.update("INSERT INTO product (id, name, price, category, stock) VALUES (?, ?, ?, ?, ?)",
                id, name, price, category, stock);
    }
}
