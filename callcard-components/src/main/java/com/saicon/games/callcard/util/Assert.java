package com.saicon.games.callcard.util;

import java.util.UUID;

/**
 * Simple assertion utility for validating method arguments.
 * Simplified version for CallCard microservice.
 */
public final class Assert {

    private Assert() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void notNull(Object o) {
        notNull(o, "Argument must not be null");
    }

    public static void notNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNullOrEmpty(String s, String message) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean testValue, String message) {
        if (!testValue) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the provided string is a valid UUID format.
     * @param uuid the UUID string to validate
     * @param message the error message if validation fails
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static void isValidUUID(String uuid, String message) {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(message, e);
        }
    }
}
