package com.saicon.games.callcard.ws.data;

import com.saicon.games.callcard.ws.dto.TemplateUsageDTO;
import com.saicon.games.callcard.ws.response.WSResponse;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Response wrapper for template usage list.
 */
public class ResponseListTemplateUsage extends WSResponse {
    private static final long serialVersionUID = 1L;

    private List<TemplateUsageDTO> items;
    private int totalCount;

    public ResponseListTemplateUsage() {
        super();
        this.items = new ArrayList<>();
    }

    public ResponseListTemplateUsage(String errorCode, ResponseStatus status,
                                     List<TemplateUsageDTO> items, int totalCount) {
        super(errorCode, status);
        this.items = items != null ? items : new ArrayList<>();
        this.totalCount = totalCount;
    }

    public List<TemplateUsageDTO> getItems() { return items; }
    public void setItems(List<TemplateUsageDTO> items) { this.items = items; }

    // Alias for backwards compatibility
    public List<TemplateUsageDTO> getRecords() { return items; }
    public void setRecords(List<TemplateUsageDTO> records) { this.items = records; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getTotalRecords() { return totalCount; }  // Alias
    public void setTotalRecords(int totalRecords) { this.totalCount = totalRecords; }  // Alias
}
