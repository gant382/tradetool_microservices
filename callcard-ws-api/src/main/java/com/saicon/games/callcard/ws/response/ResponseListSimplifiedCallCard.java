package com.saicon.games.callcard.ws.response;

import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Response wrapper for list of SimplifiedCallCard DTOs.
 */
public class ResponseListSimplifiedCallCard extends WSResponse {
    private static final long serialVersionUID = 1L;

    private List<SimplifiedCallCardDTO> items;
    private int totalCount;

    public ResponseListSimplifiedCallCard() {
        super();
        this.items = new ArrayList<>();
        this.totalCount = 0;
    }

    public ResponseListSimplifiedCallCard(String errorCode, ResponseStatus status, 
                                          List<SimplifiedCallCardDTO> items, int totalCount) {
        super(errorCode, status);
        this.items = items != null ? items : new ArrayList<>();
        this.totalCount = totalCount;
    }

    public static ResponseListSimplifiedCallCard success(List<SimplifiedCallCardDTO> items) {
        return new ResponseListSimplifiedCallCard("0000", ResponseStatus.OK, items, 
                                                   items != null ? items.size() : 0);
    }

    public List<SimplifiedCallCardDTO> getItems() {
        return items;
    }

    public void setItems(List<SimplifiedCallCardDTO> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
