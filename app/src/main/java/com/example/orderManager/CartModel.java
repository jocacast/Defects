package com.example.orderManager;

public class CartModel {
    private String productName;
    private long quantity;

    public CartModel(String productName, long quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    public CartModel() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
