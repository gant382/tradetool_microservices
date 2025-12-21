package com.saicon.games.callcard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saicon.games.callcard.components.ICallCardTransactionManagement;
import com.saicon.games.callcard.entity.CallCardTransaction;
import com.saicon.games.callcard.entity.CallCardTransactionType;
import com.saicon.games.callcard.ws.ICallCardTransactionService;
import com.saicon.games.callcard.ws.dto.CallCardTransactionDTO;
import com.saicon.games.callcard.ws.dto.TransactionListResponseDTO;
import com.saicon.games.callcard.ws.dto.TransactionSearchCriteriaDTO;
import com.saicon.games.callcard.exception.BusinessLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CallCard Transaction Service Implementation.
 * Implements SOAP web service for transaction history and audit trail operations.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
@WebService(
        targetNamespace = "http://ws.callcard.transaction.saicon.com/",
        portName = "CallCardTransactionServicePort",
        endpointInterface = "com.saicon.games.callcard.ws.ICallCardTransactionService",
        serviceName = "CallCardTransactionService"
)
public class CallCardTransactionService implements ICallCardTransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallCardTransactionService.class);
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 500;

    private ICallCardTransactionManagement transactionManagement;

    // Dependency injection via Spring
    public void setTransactionManagement(ICallCardTransactionManagement transactionManagement) {
        this.transactionManagement = transactionManagement;
    }

    @Override
    public TransactionListResponseDTO getTransactionHistory(String callCardId, Integer userGroupId) {
        return getTransactionHistoryPaginated(callCardId, userGroupId, 0, DEFAULT_PAGE_SIZE);
    }

    @Override
    public TransactionListResponseDTO getTransactionHistoryPaginated(String callCardId, Integer userGroupId,
                                                                      Integer pageNumber, Integer pageSize) {
        try {
            LOGGER.debug("Getting transaction history for CallCard: {}, UserGroup: {}, Page: {}, Size: {}",
                    callCardId, userGroupId, pageNumber, pageSize);

            validatePagination(pageNumber, pageSize);
            validateTenantId(userGroupId);

            List<CallCardTransaction> transactions = transactionManagement.findByCallCardId(
                    callCardId, userGroupId, pageNumber, pageSize);

            Long totalCount = transactionManagement.countByCallCardId(callCardId, userGroupId);

            List<CallCardTransactionDTO> dtos = transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new TransactionListResponseDTO(dtos, totalCount, pageNumber, pageSize);

        } catch (BusinessLayerException e) {
            LOGGER.error("Business error getting transaction history: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error getting transaction history", e);
            return createErrorResponse("Failed to retrieve transaction history");
        }
    }

    @Override
    public TransactionListResponseDTO getTransactionsByUser(Integer userId, Integer userGroupId,
                                                             Date dateFrom, Date dateTo) {
        return getTransactionsByUserPaginated(userId, userGroupId, dateFrom, dateTo, 0, DEFAULT_PAGE_SIZE);
    }

    @Override
    public TransactionListResponseDTO getTransactionsByUserPaginated(Integer userId, Integer userGroupId,
                                                                      Date dateFrom, Date dateTo,
                                                                      Integer pageNumber, Integer pageSize) {
        try {
            LOGGER.debug("Getting transactions by user: {}, UserGroup: {}, DateRange: {} to {}",
                    userId, userGroupId, dateFrom, dateTo);

            validatePagination(pageNumber, pageSize);
            validateTenantId(userGroupId);
            validateDateRange(dateFrom, dateTo);

            List<CallCardTransaction> transactions = transactionManagement.findByUserId(
                    userId, userGroupId, dateFrom, dateTo, pageNumber, pageSize);

            Long totalCount = transactionManagement.countByUserId(userId, userGroupId, dateFrom, dateTo);

            List<CallCardTransactionDTO> dtos = transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new TransactionListResponseDTO(dtos, totalCount, pageNumber, pageSize);

        } catch (BusinessLayerException e) {
            LOGGER.error("Business error getting transactions by user: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error getting transactions by user", e);
            return createErrorResponse("Failed to retrieve transactions by user");
        }
    }

    @Override
    public TransactionListResponseDTO getTransactionsByType(String transactionType, Integer userGroupId,
                                                             Date dateFrom, Date dateTo) {
        return getTransactionsByTypePaginated(transactionType, userGroupId, dateFrom, dateTo, 0, DEFAULT_PAGE_SIZE);
    }

    @Override
    public TransactionListResponseDTO getTransactionsByTypePaginated(String transactionType, Integer userGroupId,
                                                                      Date dateFrom, Date dateTo,
                                                                      Integer pageNumber, Integer pageSize) {
        try {
            LOGGER.debug("Getting transactions by type: {}, UserGroup: {}, DateRange: {} to {}",
                    transactionType, userGroupId, dateFrom, dateTo);

            validatePagination(pageNumber, pageSize);
            validateTenantId(userGroupId);
            validateDateRange(dateFrom, dateTo);

            CallCardTransactionType type = CallCardTransactionType.fromString(transactionType);
            if (type == null) {
                LOGGER.warn("Invalid transaction type: {}", transactionType);
                return createErrorResponse("Invalid transaction type: " + transactionType);
            }

            List<CallCardTransaction> transactions = transactionManagement.findByType(
                    type, userGroupId, dateFrom, dateTo, pageNumber, pageSize);

            Long totalCount = transactionManagement.countByType(type, userGroupId, dateFrom, dateTo);

            List<CallCardTransactionDTO> dtos = transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new TransactionListResponseDTO(dtos, totalCount, pageNumber, pageSize);

        } catch (BusinessLayerException e) {
            LOGGER.error("Business error getting transactions by type: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error getting transactions by type", e);
            return createErrorResponse("Failed to retrieve transactions by type");
        }
    }

    @Override
    public TransactionListResponseDTO searchTransactions(TransactionSearchCriteriaDTO criteria) {
        try {
            LOGGER.debug("Searching transactions with criteria: {}", criteria);

            if (criteria == null || criteria.getUserGroupId() == null) {
                return createErrorResponse("Search criteria and userGroupId are required");
            }

            validatePagination(criteria.getPageNumber(), criteria.getPageSize());

            List<CallCardTransaction> transactions = transactionManagement.searchTransactions(criteria);
            Long totalCount = transactionManagement.countSearchResults(criteria);

            List<CallCardTransactionDTO> dtos = transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new TransactionListResponseDTO(dtos, totalCount,
                    criteria.getPageNumber(), criteria.getPageSize());

        } catch (BusinessLayerException e) {
            LOGGER.error("Business error searching transactions: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error searching transactions", e);
            return createErrorResponse("Failed to search transactions");
        }
    }

    @Override
    public Long getTransactionCount(String callCardId, Integer userGroupId) {
        try {
            validateTenantId(userGroupId);
            return transactionManagement.countByCallCardId(callCardId, userGroupId);
        } catch (Exception e) {
            LOGGER.error("Error getting transaction count", e);
            return 0L;
        }
    }

    @Override
    public CallCardTransactionDTO getTransactionById(String transactionId, Integer userGroupId) {
        try {
            validateTenantId(userGroupId);

            CallCardTransaction transaction = transactionManagement.findById(transactionId, userGroupId);
            if (transaction == null) {
                LOGGER.warn("Transaction not found: {}", transactionId);
                return null;
            }

            return convertToDTO(transaction);

        } catch (Exception e) {
            LOGGER.error("Error getting transaction by ID", e);
            return null;
        }
    }

    @Override
    public List<String> getTransactionTypes() {
        return Arrays.stream(CallCardTransactionType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionListResponseDTO getRecentTransactions(Integer userGroupId, Integer limit) {
        try {
            validateTenantId(userGroupId);

            int safeLimit = (limit != null && limit > 0) ? Math.min(limit, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

            List<CallCardTransaction> transactions = transactionManagement.findRecent(userGroupId, safeLimit);

            List<CallCardTransactionDTO> dtos = transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new TransactionListResponseDTO(dtos, (long) dtos.size(), 0, safeLimit);

        } catch (Exception e) {
            LOGGER.error("Error getting recent transactions", e);
            return createErrorResponse("Failed to retrieve recent transactions");
        }
    }

    @Override
    public TransactionListResponseDTO getTransactionsBySession(String sessionId, Integer userGroupId) {
        try {
            validateTenantId(userGroupId);

            List<CallCardTransaction> transactions = transactionManagement.findBySessionId(sessionId, userGroupId);

            List<CallCardTransactionDTO> dtos = transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new TransactionListResponseDTO(dtos, (long) dtos.size(), 0, dtos.size());

        } catch (Exception e) {
            LOGGER.error("Error getting transactions by session", e);
            return createErrorResponse("Failed to retrieve transactions by session");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Convert entity to DTO
     */
    private CallCardTransactionDTO convertToDTO(CallCardTransaction transaction) {
        CallCardTransactionDTO dto = new CallCardTransactionDTO();

        dto.setTransactionId(transaction.getTransactionId());
        dto.setCallCardId(transaction.getCallCardId());
        dto.setTransactionType(transaction.getTransactionType() != null ?
                transaction.getTransactionType().name() : null);
        dto.setUserId(transaction.getUserId() != null ? transaction.getUserId().getUserId() : null);
        dto.setUserName(transaction.getUserId() != null ? transaction.getUserId().getUsername() : null);
        dto.setUserGroupId(transaction.getUserGroupId());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setOldValue(transaction.getOldValue());
        dto.setNewValue(transaction.getNewValue());
        dto.setDescription(transaction.getDescription());
        dto.setIpAddress(transaction.getIpAddress());
        dto.setSessionId(transaction.getSessionId());
        dto.setMetadata(transaction.getMetadata());

        return dto;
    }

    /**
     * Validate pagination parameters
     */
    private void validatePagination(Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    /**
     * Validate tenant ID
     */
    private void validateTenantId(Integer userGroupId) {
        if (userGroupId == null) {
            throw new IllegalArgumentException("UserGroupId is required for multi-tenant isolation");
        }
    }

    /**
     * Validate date range
     */
    private void validateDateRange(Date dateFrom, Date dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("Date range (dateFrom and dateTo) is required");
        }
        if (dateFrom.after(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be before or equal to dateTo");
        }
    }

    /**
     * Create error response
     */
    private TransactionListResponseDTO createErrorResponse(String message) {
        TransactionListResponseDTO response = new TransactionListResponseDTO();
        response.setTransactions(new ArrayList<>());
        response.setTotalRecords(0L);
        response.setCurrentPage(0);
        response.setPageSize(0);
        response.setTotalPages(0);
        return response;
    }
}
