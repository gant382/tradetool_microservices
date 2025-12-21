package com.saicon.games.callcard.ws;

import com.saicon.games.callcard.ws.dto.CallCardTransactionDTO;
import com.saicon.games.callcard.ws.dto.TransactionListResponseDTO;
import com.saicon.games.callcard.ws.dto.TransactionSearchCriteriaDTO;
import org.apache.cxf.annotations.FastInfoset;
import org.apache.cxf.annotations.GZIP;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Date;
import java.util.List;

/**
 * CallCard Transaction History Service Interface.
 * Provides audit trail and transaction history operations for CallCard modifications.
 *
 * SOAP Web Service with FastInfoset and GZIP compression support.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
@WebService(
        targetNamespace = "http://ws.callcard.transaction.saicon.com/",
        name = "CallCardTransactionService"
)
@FastInfoset
@GZIP
public interface ICallCardTransactionService {

    /**
     * Get complete transaction history for a specific CallCard.
     * Returns all modifications in chronological order (newest first).
     *
     * @param callCardId CallCard ID to get history for
     * @param userGroupId Tenant ID for multi-tenant isolation
     * @return List of all transactions for the CallCard
     */
    @WebMethod(operationName = "getTransactionHistory")
    TransactionListResponseDTO getTransactionHistory(
            @WebParam(name = "callCardId") String callCardId,
            @WebParam(name = "userGroupId") Integer userGroupId
    );

    /**
     * Get paginated transaction history for a CallCard.
     *
     * @param callCardId CallCard ID
     * @param userGroupId Tenant ID
     * @param pageNumber Page number (0-based)
     * @param pageSize Records per page
     * @return Paginated transaction list
     */
    @WebMethod(operationName = "getTransactionHistoryPaginated")
    TransactionListResponseDTO getTransactionHistoryPaginated(
            @WebParam(name = "callCardId") String callCardId,
            @WebParam(name = "userGroupId") Integer userGroupId,
            @WebParam(name = "pageNumber") Integer pageNumber,
            @WebParam(name = "pageSize") Integer pageSize
    );

    /**
     * Get all transactions performed by a specific user within a date range.
     *
     * @param userId User ID who performed the actions
     * @param userGroupId Tenant ID
     * @param dateFrom Start date (inclusive)
     * @param dateTo End date (inclusive)
     * @return List of transactions by the user
     */
    @WebMethod(operationName = "getTransactionsByUser")
    TransactionListResponseDTO getTransactionsByUser(
            @WebParam(name = "userId") Integer userId,
            @WebParam(name = "userGroupId") Integer userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );

    /**
     * Get paginated transactions by user.
     *
     * @param userId User ID
     * @param userGroupId Tenant ID
     * @param dateFrom Start date
     * @param dateTo End date
     * @param pageNumber Page number
     * @param pageSize Records per page
     * @return Paginated transaction list
     */
    @WebMethod(operationName = "getTransactionsByUserPaginated")
    TransactionListResponseDTO getTransactionsByUserPaginated(
            @WebParam(name = "userId") Integer userId,
            @WebParam(name = "userGroupId") Integer userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo,
            @WebParam(name = "pageNumber") Integer pageNumber,
            @WebParam(name = "pageSize") Integer pageSize
    );

    /**
     * Get all transactions of a specific type within a date range.
     *
     * @param transactionType Type of transaction (CREATE, UPDATE, DELETE, etc.)
     * @param userGroupId Tenant ID
     * @param dateFrom Start date
     * @param dateTo End date
     * @return List of transactions of the specified type
     */
    @WebMethod(operationName = "getTransactionsByType")
    TransactionListResponseDTO getTransactionsByType(
            @WebParam(name = "transactionType") String transactionType,
            @WebParam(name = "userGroupId") Integer userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );

    /**
     * Get paginated transactions by type.
     *
     * @param transactionType Transaction type
     * @param userGroupId Tenant ID
     * @param dateFrom Start date
     * @param dateTo End date
     * @param pageNumber Page number
     * @param pageSize Records per page
     * @return Paginated transaction list
     */
    @WebMethod(operationName = "getTransactionsByTypePaginated")
    TransactionListResponseDTO getTransactionsByTypePaginated(
            @WebParam(name = "transactionType") String transactionType,
            @WebParam(name = "userGroupId") Integer userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo,
            @WebParam(name = "pageNumber") Integer pageNumber,
            @WebParam(name = "pageSize") Integer pageSize
    );

    /**
     * Advanced transaction search with multiple criteria.
     * Supports filtering by CallCard, user, type, date range, session, IP, etc.
     *
     * @param criteria Search criteria object
     * @return Paginated transaction list matching criteria
     */
    @WebMethod(operationName = "searchTransactions")
    TransactionListResponseDTO searchTransactions(
            @WebParam(name = "criteria") TransactionSearchCriteriaDTO criteria
    );

    /**
     * Get transaction count for a CallCard.
     *
     * @param callCardId CallCard ID
     * @param userGroupId Tenant ID
     * @return Number of transactions
     */
    @WebMethod(operationName = "getTransactionCount")
    Long getTransactionCount(
            @WebParam(name = "callCardId") String callCardId,
            @WebParam(name = "userGroupId") Integer userGroupId
    );

    /**
     * Get a single transaction by ID.
     *
     * @param transactionId Transaction ID
     * @param userGroupId Tenant ID (for authorization)
     * @return Transaction details
     */
    @WebMethod(operationName = "getTransactionById")
    CallCardTransactionDTO getTransactionById(
            @WebParam(name = "transactionId") String transactionId,
            @WebParam(name = "userGroupId") Integer userGroupId
    );

    /**
     * Get all available transaction types.
     *
     * @return List of transaction type names
     */
    @WebMethod(operationName = "getTransactionTypes")
    List<String> getTransactionTypes();

    /**
     * Get recent transactions across all CallCards for a tenant.
     * Useful for audit dashboard showing recent activity.
     *
     * @param userGroupId Tenant ID
     * @param limit Maximum number of records (default 100)
     * @return Recent transactions
     */
    @WebMethod(operationName = "getRecentTransactions")
    TransactionListResponseDTO getRecentTransactions(
            @WebParam(name = "userGroupId") Integer userGroupId,
            @WebParam(name = "limit") Integer limit
    );

    /**
     * Get transactions by session ID.
     * Useful for tracking all changes made in a single user session.
     *
     * @param sessionId Session ID
     * @param userGroupId Tenant ID
     * @return All transactions in the session
     */
    @WebMethod(operationName = "getTransactionsBySession")
    TransactionListResponseDTO getTransactionsBySession(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "userGroupId") Integer userGroupId
    );
}
