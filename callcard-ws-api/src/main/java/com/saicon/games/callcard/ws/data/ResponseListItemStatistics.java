package com.saicon.games.callcard.ws.data;

import com.saicon.games.callcard.ws.dto.ItemStatisticsDTO;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import com.saicon.games.callcard.ws.response.WSResponse;

import java.util.List;

/**
 * Response wrapper for ItemStatistics list operations
 */
public class ResponseListItemStatistics extends WSResponse {
    private List<ItemStatisticsDTO> records;
    private int totalNumOfRecords = 0;

    public ResponseListItemStatistics() {
    }

    public ResponseListItemStatistics(List<ItemStatisticsDTO> records, int totalNumOfRecords) {
        this.records = records;
        this.totalNumOfRecords = totalNumOfRecords;
    }

    public ResponseListItemStatistics(String result, ResponseStatus status, List<ItemStatisticsDTO> records, int totalNumOfRecords) {
        super(result, status);
        this.records = records;
        this.totalNumOfRecords = totalNumOfRecords;
    }

    public ResponseListItemStatistics(int errorNumber, String result, ResponseStatus status, List<ItemStatisticsDTO> records, int totalNumOfRecords) {
        super(errorNumber, result, status);
        this.records = records;
        this.totalNumOfRecords = totalNumOfRecords;
    }

    public ResponseListItemStatistics(int errorNumber, String result, ResponseStatus status) {
        super(errorNumber, result, status);
    }

    public ResponseListItemStatistics(int errorNumber, String result, ResponseStatus status, int totalNumOfRecords) {
        super(errorNumber, result, status);
        this.totalNumOfRecords = totalNumOfRecords;
    }

    public List<ItemStatisticsDTO> getRecords() {
        return records;
    }

    public void setRecords(List<ItemStatisticsDTO> records) {
        this.records = records;
    }

    public int getTotalNumOfRecords() {
        return totalNumOfRecords;
    }

    public void setTotalNumOfRecords(int totalNumOfRecords) {
        this.totalNumOfRecords = totalNumOfRecords;
    }
}
