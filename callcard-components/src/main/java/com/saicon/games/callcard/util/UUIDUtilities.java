package com.saicon.games.callcard.util;

import java.util.UUID;

public class UUIDUtilities {
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValidUUID(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
