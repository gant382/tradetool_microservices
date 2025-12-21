package com.saicon.games.callcard.ws.response;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Base web service response wrapper.
 * Mirrors the original gameserver_v3 response structure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WSResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private int errorNumber;
    private String errorCode;
    private String result;  // Same as message, but using different name for backwards compatibility
    private String message;
    private ResponseStatus status;

    public WSResponse() {
        this.errorCode = "0000";
        this.errorNumber = 0;
        this.status = ResponseStatus.OK;
    }

    public WSResponse(String errorCodeOrResult, ResponseStatus status) {
        if (status == ResponseStatus.ERROR) {
            this.errorCode = errorCodeOrResult;
            this.result = "";
        } else {
            this.errorCode = "0000";
            this.result = errorCodeOrResult;
        }
        this.errorNumber = 0;
        this.status = status;
    }

    public WSResponse(int errorNumber, String result, ResponseStatus status) {
        this.errorNumber = errorNumber;
        this.errorCode = String.valueOf(errorNumber);
        this.result = result;
        this.message = result;
        this.status = status;
    }

    public WSResponse(String errorCode, String message, ResponseStatus status) {
        this.errorCode = errorCode;
        this.errorNumber = 0;
        this.message = message;
        this.result = message;
        this.status = status;
    }

    public static WSResponse success() {
        return new WSResponse("0000", ResponseStatus.OK);
    }

    public static WSResponse error(String errorCode, String message) {
        return new WSResponse(errorCode, message, ResponseStatus.ERROR);
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public void setErrorNumber(int errorNumber) {
        this.errorNumber = errorNumber;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
        this.message = result;  // Keep in sync
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        this.result = message;  // Keep in sync
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}
