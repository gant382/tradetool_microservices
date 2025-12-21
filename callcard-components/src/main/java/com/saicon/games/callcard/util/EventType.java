package com.saicon.games.callcard.util;

public enum EventType {
    CREATE(1), UPDATE(2), DELETE(3), SUBMIT(4), COMPLETE(5), CANCEL(6),
    CALL_CARD_STATISTICS(7), NO_DISTINCT_CALL_CARD_TEMPLATE(8),
    CALL_CARD_UPLOADED(9), CALL_CARD_INDIRECT_ACTION(10), CALL_CARD_DOWNLOADED(11);

    private final int value;

    EventType(int value) {
        this.value = value;
    }

    public int toInt() {
        return this.value;
    }
}
