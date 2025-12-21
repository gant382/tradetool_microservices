package com.saicon.games.callcard.exception;

import java.io.Serializable;

/**
 * Exception type transfer object for standardized error codes.
 * Mirrors the original gameserver_v3 structure.
 */
public class ExceptionTypeTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Standard error codes
    public static final String NONE = "0000";
    public static final String GENERAL_ERROR = "1000";
    public static final String INVALID_INPUT = "1001";
    public static final String NOT_FOUND = "1002";
    public static final String UNAUTHORIZED = "1003";
    public static final String CONCURRENT_MODIFICATION = "1004";
    public static final String VALIDATION_ERROR = "1005";
    public static final String SESSION_EXPIRED = "1006";
    public static final String DUPLICATE_ENTRY = "1007";
    public static final String USER_SESSION_ID_NOT_VALID = "1008";

    private String errorCode;
    private String message;

    public ExceptionTypeTO() {
        this.errorCode = NONE;
        this.message = "";
    }

    public ExceptionTypeTO(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Convert error number to error code string
     * @param errorNumber numeric error code
     * @return error code as string
     */
    public static String valueOf(int errorNumber) {
        return String.valueOf(errorNumber);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
