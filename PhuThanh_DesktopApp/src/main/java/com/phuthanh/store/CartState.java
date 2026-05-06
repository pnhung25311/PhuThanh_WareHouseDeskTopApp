package com.phuthanh.store;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CartState {

    private static final CartState instance = new CartState();

    private final IntegerProperty cartCount = new SimpleIntegerProperty(0);

    private CartState() {}

    public static CartState getInstance() {
        return instance;
    }

    public IntegerProperty cartCountProperty() {
        return cartCount;
    }

    public int getCartCount() {
        return cartCount.get();
    }

    public void setCartCount(int value) {
        cartCount.set(value);
    }

    public void add(int quantity) {
        cartCount.set(cartCount.get() + quantity);
    }

    public void clear() {
        cartCount.set(0);
    }
}