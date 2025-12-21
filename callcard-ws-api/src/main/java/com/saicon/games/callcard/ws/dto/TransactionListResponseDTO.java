package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for paginated transaction list response.
 * Contains transactions and pagination metadata.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public class TransactionListResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private List<CallCardTransactionDTO> transactions;

    @DTOParam(2)
    private Long totalRecords;

    @DTOParam(3)
    private Integer currentPage;

    @DTOParam(4)
    private Integer pageSize;

    @DTOParam(5)
    private Integer totalPages;

    @DTOParam(6)
    private Boolean hasNext;

    @DTOParam(7)
    private Boolean hasPrevious;

    // Constructors

    public TransactionListResponseDTO() {
    }

    public TransactionListResponseDTO(List<CallCardTransactionDTO> transactions, Long totalRecords,
                                       Integer currentPage, Integer pageSize) {
        this.transactions = transactions;
        this.totalRecords = totalRecords;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        this.hasNext = currentPage < (totalPages - 1);
        this.hasPrevious = currentPage > 0;
    }

    // Getters and Setters

    public List<CallCardTransactionDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<CallCardTransactionDTO> transactions) {
        this.transactions = transactions;
    }

    public Long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "TransactionListResponseDTO{" +
                "totalRecords=" + totalRecords +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", transactionCount=" + (transactions != null ? transactions.size() : 0) +
                '}';
    }
}
