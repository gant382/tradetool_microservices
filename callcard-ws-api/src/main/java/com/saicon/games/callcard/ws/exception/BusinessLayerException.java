package com.saicon.games.commons.exceptions;

/**
 * The business layer exception.
 * <p/>
 * User: antony
 */
public class BusinessLayerException extends RuntimeException {

    private ExceptionTypeTO exceptionType;

    public BusinessLayerException() {
        exceptionType = ExceptionTypeTO.GENERIC;
    }

    public BusinessLayerException(String message) {
        super(message);
        exceptionType = ExceptionTypeTO.GENERIC;
    }

    public BusinessLayerException(String message, Throwable cause) {
        super(message, cause);
        exceptionType = ExceptionTypeTO.GENERIC;
    }

    public BusinessLayerException(Throwable cause) {
        super(cause);
        exceptionType = ExceptionTypeTO.GENERIC;
    }

    public BusinessLayerException(Throwable cause, ExceptionTypeTO exceptionType) {
        super(cause);
        this.exceptionType = exceptionType;
    }

    public BusinessLayerException(String message, Throwable cause, ExceptionTypeTO exceptionType) {
        super(message, cause);
        this.exceptionType = exceptionType;
    }

    public BusinessLayerException(String message, ExceptionTypeTO exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public BusinessLayerException(ExceptionTypeTO exceptionType) {
        this.exceptionType = exceptionType;
    }

    public ExceptionTypeTO getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(ExceptionTypeTO exceptionType) {
        this.exceptionType = exceptionType;
    }

    public int getErrorCode() {
        return null != exceptionType ? exceptionType.getErrorCode() : ExceptionTypeTO.GENERIC.getErrorCode();
    }
}
