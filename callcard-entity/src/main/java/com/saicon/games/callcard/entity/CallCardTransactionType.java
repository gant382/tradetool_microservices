package com.saicon.games.callcard.entity;

/**
 * Transaction types for CallCard audit history tracking.
 * Records all modifications to CallCard entities for compliance and audit purposes.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public enum CallCardTransactionType {

    /**
     * CallCard entity created
     */
    CREATE("CallCard Created", "New CallCard entity was created in the system"),

    /**
     * CallCard entity updated (general modification)
     */
    UPDATE("CallCard Updated", "CallCard entity was modified"),

    /**
     * CallCard entity deleted or deactivated
     */
    DELETE("CallCard Deleted", "CallCard entity was deleted or deactivated"),

    /**
     * User assigned to CallCard
     */
    ASSIGN("User Assigned", "User was assigned to the CallCard"),

    /**
     * User unassigned from CallCard
     */
    UNASSIGN("User Unassigned", "User was unassigned from the CallCard"),

    /**
     * CallCard template changed
     */
    TEMPLATE_CHANGE("Template Changed", "CallCard template was modified or replaced"),

    /**
     * CallCard status changed (active/inactive)
     */
    STATUS_CHANGE("Status Changed", "CallCard active status was changed"),

    /**
     * CallCard dates modified (start/end date)
     */
    DATE_CHANGE("Date Modified", "CallCard start or end date was modified"),

    /**
     * CallCard comments modified
     */
    COMMENT_CHANGE("Comment Modified", "CallCard comments were updated"),

    /**
     * CallCard internal reference number changed
     */
    REFERENCE_CHANGE("Reference Changed", "CallCard internal reference number was modified");

    private final String displayName;
    private final String description;

    CallCardTransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get transaction type by name (case-insensitive)
     *
     * @param name Transaction type name
     * @return CallCardTransactionType or null if not found
     */
    public static CallCardTransactionType fromString(String name) {
        if (name == null) {
            return null;
        }
        for (CallCardTransactionType type : CallCardTransactionType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
