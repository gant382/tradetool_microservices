package com.saicon.games.callcard.ws.data;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import com.saicon.games.callcard.ws.response.WSResponse;

import java.util.List;

/**
 * Response wrapper for CallCard list operations
 */
public class ResponseListCallCard extends WSResponse {
    private List<CallCardDTO> records;
    private int totalRecords;

    public ResponseListCallCard() {
    }

    public ResponseListCallCard(List<CallCardDTO> records, int totalRecords) {
        this.setRecords(records);
        this.setTotalRecords(totalRecords);
    }

    public ResponseListCallCard(String result, ResponseStatus status, List<CallCardDTO> records, int totalRecords) {
        super(result, status);
        this.setRecords(records);
        this.setTotalRecords(totalRecords);
    }

    public ResponseListCallCard(int errorNumber, String result, ResponseStatus status, List<CallCardDTO> records, int totalRecords) {
        super(errorNumber, result, status);
        this.setRecords(records);
        this.setTotalRecords(totalRecords);
    }

    public List<CallCardDTO> getRecords() {
        return records;
    }

    public void setRecords(List<CallCardDTO> records) {
        this.records = records;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
}
