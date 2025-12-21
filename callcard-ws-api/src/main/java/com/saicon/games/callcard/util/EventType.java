package com.saicon.games.callcard.util;

/**
 * Event types for CallCard system events.
 */
public enum EventType {
    CREATED(1),
    UPDATED(2),
    DELETED(3),
    ACTIVATED(4),
    DEACTIVATED(5),
    ASSIGNED(6),
    UNASSIGNED(7),
    COMPLETED(8),
    CANCELLED(9),
    CALL_CARD_DOWNLOADED(1000),
    CALL_CARD_UPLOADED(1001),
    CALL_CARD_STATISTICS(1002),
    NO_DISTINCT_CALL_CARD_TEMPLATE(1003),
    CALL_CARD_INDIRECT_ACTION(1004);

    private final int value;

    EventType(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static EventType fromInt(int value) {
        for (EventType type : EventType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EventType value: " + value);
    }
}
