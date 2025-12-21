package com.saicon.games.callcard.util;

import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.exception.ExceptionTypeTO;
import com.saicon.games.commons.utilities.UUIDUtilities;

/**
 * Assertion utility for input validation
 */
public class Assert {

    public static void notNull(Object object, String message) throws BusinessLayerException {
        if (object == null) {
            throw new BusinessLayerException(message, ExceptionTypeTO.GENERAL_ERROR);
        }
    }

    public static void notNullOrEmpty(String value, String message) throws BusinessLayerException {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessLayerException(message, ExceptionTypeTO.GENERAL_ERROR);
        }
    }

    /**
     * Validates that a UUID string value is not null/empty and is a valid UUID format.
     * Throws BusinessLayerException with fieldName-prefixed message if validation fails.
     *
     * @param value the UUID string to validate
     * @param fieldName the field name for error message context
     * @throws BusinessLayerException if value is null, empty, or not a valid UUID
     */
    public static void isValidUUID(String value, String fieldName) throws BusinessLayerException {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessLayerException(fieldName + " cannot be null or empty", ExceptionTypeTO.GENERAL_ERROR);
        }
        if (!UUIDUtilities.isValidUUID(value)) {
            throw new BusinessLayerException(fieldName + " must be a valid UUID", ExceptionTypeTO.GENERAL_ERROR);
        }
    }

    public static void isTrue(boolean condition, String message) throws BusinessLayerException {
        if (!condition) {
            throw new BusinessLayerException(message, ExceptionTypeTO.GENERAL_ERROR);
        }
    }

    public static void isFalse(boolean condition, String message) throws BusinessLayerException {
        if (condition) {
            throw new BusinessLayerException(message, ExceptionTypeTO.GENERAL_ERROR);
        }
    }
}
