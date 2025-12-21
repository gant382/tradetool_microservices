package com.saicon.games.callcard.ws.dto;

import java.io.Serializable;

public class ItemStatisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemId;
    private int totalCount;
    private int activeCount;
    private Integer rank;
    private int viewCount;
    private boolean isActive;
    private int status;
    private int count;
    private double total;

    // Constructors
    public ItemStatisticsDTO() {
    }

    public ItemStatisticsDTO(String itemId, int totalCount, int activeCount, Integer rank, int viewCount, boolean isActive, int status) {
        this.itemId = itemId;
        this.totalCount = totalCount;
        this.activeCount = activeCount;
        this.rank = rank;
        this.viewCount = viewCount;
        this.isActive = isActive;
        this.status = status;
    }

    // getters and setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
