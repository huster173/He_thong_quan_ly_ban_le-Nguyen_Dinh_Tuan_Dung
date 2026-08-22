package com.shop.model;

public class Product {

    private final String id;
    private final String name;
    private final long price;
    private final String category;
    private final int stock;

    public Product(String id, String name, long price, String category, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public int getStock() {
        return stock;
    }
}
