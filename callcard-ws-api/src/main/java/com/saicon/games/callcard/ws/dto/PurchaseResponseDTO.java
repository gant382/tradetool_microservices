package com.saicon.games.callcard.ws.dto;

import java.io.Serializable;

public class PurchaseResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String responseCode;
    private String message;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
