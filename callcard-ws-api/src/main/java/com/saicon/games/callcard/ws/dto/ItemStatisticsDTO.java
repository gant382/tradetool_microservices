package com.saicon.games.callcard.ws.dto;

import java.io.Serializable;

public class ItemStatisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemId;
    private int count;
    private double total;

    // getters and setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
