package com.saicon.games.callcard.ws.response;

import com.saicon.games.callcard.ws.dto.CallCardStatsDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Response wrapper for call card statistics.
 */
public class ResponseListItemStatistics extends WSResponse {
    private static final long serialVersionUID = 1L;

    private List<CallCardStatsDTO> items;
    private int totalCount;

    public ResponseListItemStatistics() {
        super();
        this.items = new ArrayList<>();
        this.totalCount = 0;
    }

    public ResponseListItemStatistics(String errorCode, ResponseStatus status, 
                                      List<CallCardStatsDTO> items, int totalCount) {
        super(errorCode, status);
        this.items = items != null ? items : new ArrayList<>();
        this.totalCount = totalCount;
    }

    public static ResponseListItemStatistics success(List<CallCardStatsDTO> items) {
        return new ResponseListItemStatistics("0000", ResponseStatus.OK, items, 
                                               items != null ? items.size() : 0);
    }

    public List<CallCardStatsDTO> getItems() {
        return items;
    }

    public void setItems(List<CallCardStatsDTO> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
