package com.saicon.games.callcard.ws.data;

import com.saicon.games.callcard.ws.dto.UserEngagementDTO;
import com.saicon.games.callcard.ws.response.WSResponse;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Response wrapper for user engagement list.
 */
public class ResponseListUserEngagement extends WSResponse {
    private static final long serialVersionUID = 1L;

    private List<UserEngagementDTO> items;
    private int totalCount;

    public ResponseListUserEngagement() {
        super();
        this.items = new ArrayList<>();
    }

    public ResponseListUserEngagement(String errorCode, ResponseStatus status,
                                      List<UserEngagementDTO> items, int totalCount) {
        super(errorCode, status);
        this.items = items != null ? items : new ArrayList<>();
        this.totalCount = totalCount;
    }

    public List<UserEngagementDTO> getItems() { return items; }
    public void setItems(List<UserEngagementDTO> items) { this.items = items; }

    // Alias for backwards compatibility
    public List<UserEngagementDTO> getRecords() { return items; }
    public void setRecords(List<UserEngagementDTO> records) { this.items = records; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getTotalRecords() { return totalCount; }  // Alias
    public void setTotalRecords(int totalRecords) { this.totalCount = totalRecords; }  // Alias
}
