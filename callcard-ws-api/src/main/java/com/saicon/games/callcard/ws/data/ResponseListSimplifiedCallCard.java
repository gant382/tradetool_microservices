package com.saicon.games.callcard.ws.data;

import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import com.saicon.games.callcard.ws.response.WSResponse;

import java.util.List;

/**
 * Response wrapper for SimplifiedCallCard list operations
 */
public class ResponseListSimplifiedCallCard extends WSResponse {
    private List<SimplifiedCallCardDTO> records;
    private int totalRecords;

    public ResponseListSimplifiedCallCard() {
    }

    public ResponseListSimplifiedCallCard(List<SimplifiedCallCardDTO> records, int totalRecords) {
        this.setRecords(records);
        this.setTotalRecords(totalRecords);
    }

    public ResponseListSimplifiedCallCard(String result, ResponseStatus status, List<SimplifiedCallCardDTO> records, int totalRecords) {
        super(result, status);
        this.setRecords(records);
        this.setTotalRecords(totalRecords);
    }

    public ResponseListSimplifiedCallCard(int errorNumber, String result, ResponseStatus status, List<SimplifiedCallCardDTO> records, int totalRecords) {
        super(errorNumber, result, status);
        this.setRecords(records);
        this.setTotalRecords(totalRecords);
    }

    public List<SimplifiedCallCardDTO> getRecords() {
        return records;
    }

    public void setRecords(List<SimplifiedCallCardDTO> records) {
        this.records = records;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
}
