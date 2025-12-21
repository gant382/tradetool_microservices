package com.saicon.games.callcard.ws.response;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Response wrapper for list of CallCard DTOs.
 */
public class ResponseListCallCard extends WSResponse {
    private static final long serialVersionUID = 1L;

    private List<CallCardDTO> items;
    private int totalCount;

    public ResponseListCallCard() {
        super();
        this.items = new ArrayList<>();
        this.totalCount = 0;
    }

    public ResponseListCallCard(String errorCode, ResponseStatus status, List<CallCardDTO> items, int totalCount) {
        super(errorCode, status);
        this.items = items != null ? items : new ArrayList<>();
        this.totalCount = totalCount;
    }

    public static ResponseListCallCard success(List<CallCardDTO> items) {
        return new ResponseListCallCard("0000", ResponseStatus.OK, items, items != null ? items.size() : 0);
    }

    public List<CallCardDTO> getItems() {
        return items;
    }

    public void setItems(List<CallCardDTO> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
