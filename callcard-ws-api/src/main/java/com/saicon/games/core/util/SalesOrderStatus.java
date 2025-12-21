package com.saicon.games.core.util;

/**
 * SalesOrderStatus - Enumeration for sales order status values
 * Represents different states of a sales order in the lifecycle
 */
public enum SalesOrderStatus {
    PENDING(0, "Pending"),
    COMPLETED(1, "Completed"),
    CANCELLED(2, "Cancelled"),
    REFUNDED(3, "Refunded"),
    SUBMITTED(4, "Submitted");

    private final int code;
    private final String displayName;

    SalesOrderStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get enum value by code
     */
    public static SalesOrderStatus fromCode(int code) {
        for (SalesOrderStatus status : SalesOrderStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }

    /**
     * Get enum value by name
     */
    public static SalesOrderStatus fromName(String name) {
        for (SalesOrderStatus status : SalesOrderStatus.values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return PENDING;
    }
}
