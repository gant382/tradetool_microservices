package com.saicon.games.callcard.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saicon.games.callcard.util.DTOParam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * CallCard Bulk Response DTO - For efficient bulk operations
 *
 * Features:
 * - Single query result container
 * - Pagination metadata
 * - Error handling for partial failures
 * - Total count for UI pagination
 * - GZIP compression recommended
 *
 * Use cases:
 * - Bulk fetch by IDs
 * - Paginated list queries
 * - Template-based queries
 * - User group queries
 */
@XmlRootElement(name = "CallCardBulkResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallCardBulkResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private List<SimplifiedCallCardDTO> callCards;

    @DTOParam(2)
    private int totalCount;

    @DTOParam(3)
    private int page;

    @DTOParam(4)
    private int pageSize;

    @DTOParam(5)
    private int totalPages;

    @DTOParam(6)
    private boolean hasNext;

    @DTOParam(7)
    private boolean hasPrevious;

    @DTOParam(8)
    private List<String> errors;

    @DTOParam(9)
    private String queryTime; // ISO 8601 timestamp

    @DTOParam(10)
    private long executionTimeMs;

    public CallCardBulkResponseDTO() {
        this.callCards = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public CallCardBulkResponseDTO(List<SimplifiedCallCardDTO> callCards, int totalCount,
                                   int page, int pageSize) {
        this.callCards = callCards != null ? callCards : new ArrayList<>();
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages(totalCount, pageSize);
        this.hasNext = page < this.totalPages;
        this.hasPrevious = page > 1;
        this.errors = new ArrayList<>();
    }

    private int calculateTotalPages(int totalCount, int pageSize) {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    // Getters and Setters
    public List<SimplifiedCallCardDTO> getCallCards() {
        return callCards;
    }

    public void setCallCards(List<SimplifiedCallCardDTO> callCards) {
        this.callCards = callCards;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.totalPages = calculateTotalPages(totalCount, this.pageSize);
        updateNavigationFlags();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
        updateNavigationFlags();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages(this.totalCount, pageSize);
        updateNavigationFlags();
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    public String getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(String queryTime) {
        this.queryTime = queryTime;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    private void updateNavigationFlags() {
        this.hasNext = this.page < this.totalPages;
        this.hasPrevious = this.page > 1;
    }

    @Override
    public String toString() {
        return "CallCardBulkResponseDTO{" +
                "totalCount=" + totalCount +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", callCardsSize=" + (callCards != null ? callCards.size() : 0) +
                ", hasErrors=" + (errors != null && !errors.isEmpty()) +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}
