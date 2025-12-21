package com.saicon.games.callcard.exception;

/**
 * Business layer exception for call card operations.
 * Mirrors the original gameserver_v3 exception structure.
 */
public class BusinessLayerException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    private ExceptionTypeTO exceptionType;

    public BusinessLayerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.exceptionType = new ExceptionTypeTO(errorCode, message);
    }

    public BusinessLayerException(ExceptionTypeTO exceptionType) {
        super(exceptionType.getMessage());
        this.errorCode = exceptionType.getErrorCode();
        this.exceptionType = exceptionType;
    }

    public BusinessLayerException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ExceptionTypeTO.GENERAL_ERROR;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ExceptionTypeTO getExceptionType() {
        return exceptionType;
    }
}
