package com.saicon.games.callcard.util;

import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.exception.ExceptionTypeTO;

import java.util.UUID;

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

    public static void isValidUUID(String value, String message) throws BusinessLayerException {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new BusinessLayerException(message, ExceptionTypeTO.GENERAL_ERROR);
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
