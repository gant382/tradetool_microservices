package com.saicon.games.commons.utilities;

import java.util.UUID;

/**
 * Utility class for UUID operations - extracted from gameserver_v3
 */
public class UUIDUtilities {

    private UUIDUtilities() {
        // Private constructor for utility class
    }

    /**
     * Check if a string is a valid UUID
     */
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

    /**
     * Convert string to UUID, return null if invalid
     */
    public static UUID toUUID(String uuidString) {
        if (!isValidUUID(uuidString)) {
            return null;
        }
        return UUID.fromString(uuidString);
    }

    /**
     * Generate a new random UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Compare two UUID strings
     */
    public static int compareUUIDs(String uuid1, String uuid2) {
        if (uuid1 == null && uuid2 == null) {
            return 0;
        }
        if (uuid1 == null) {
            return -1;
        }
        if (uuid2 == null) {
            return 1;
        }
        return uuid1.compareTo(uuid2);
    }
}
