package com.saicon.games.callcard.components;

import com.saicon.games.callcard.entity.CallCard;
import com.saicon.games.callcard.entity.CallCardTransaction;
import com.saicon.games.callcard.entity.CallCardTransactionType;
import com.saicon.games.callcard.ws.dto.TransactionSearchCriteriaDTO;
import com.saicon.games.callcard.exception.BusinessLayerException;

import java.util.Date;
import java.util.List;

/**
 * CallCard Transaction Management Interface.
 * Provides business logic for transaction history and audit trail.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public interface ICallCardTransactionManagement {

    /**
     * Record a new transaction (audit entry).
     *
     * @param callCardId CallCard ID
     * @param transactionType Type of transaction
     * @param userId User who performed the action
     * @param userGroupId Tenant ID
     * @param oldValue JSON serialized old state (null for CREATE)
     * @param newValue JSON serialized new state (null for DELETE)
     * @param description Human-readable description
     * @param ipAddress Request IP address
     * @param sessionId Session ID
     * @return Created transaction
     * @throws BusinessLayerException if recording fails
     */
    CallCardTransaction recordTransaction(
            String callCardId,
            CallCardTransactionType transactionType,
            Integer userId,
            Integer userGroupId,
            String oldValue,
            String newValue,
            String description,
            String ipAddress,
            String sessionId
    ) throws BusinessLayerException;

    /**
     * Record a CREATE transaction.
     *
     * @param callCard CallCard entity that was created
     * @param userId User who created it
     * @param ipAddress Request IP
     * @param sessionId Session ID
     * @return Created transaction
     * @throws BusinessLayerException if recording fails
     */
    CallCardTransaction recordCreate(CallCard callCard, Integer userId, String ipAddress, String sessionId)
            throws BusinessLayerException;

    /**
     * Record an UPDATE transaction.
     *
     * @param oldCallCard Old state
     * @param newCallCard New state
     * @param userId User who updated
     * @param ipAddress Request IP
     * @param sessionId Session ID
     * @return Created transaction
     * @throws BusinessLayerException if recording fails
     */
    CallCardTransaction recordUpdate(CallCard oldCallCard, CallCard newCallCard, Integer userId,
                                      String ipAddress, String sessionId)
            throws BusinessLayerException;

    /**
     * Record a DELETE transaction.
     *
     * @param callCard CallCard entity that was deleted
     * @param userId User who deleted
     * @param ipAddress Request IP
     * @param sessionId Session ID
     * @return Created transaction
     * @throws BusinessLayerException if recording fails
     */
    CallCardTransaction recordDelete(CallCard callCard, Integer userId, String ipAddress, String sessionId)
            throws BusinessLayerException;

    /**
     * Find transactions by CallCard ID.
     *
     * @param callCardId CallCard ID
     * @param userGroupId Tenant ID
     * @param pageNumber Page number (0-based)
     * @param pageSize Records per page
     * @return List of transactions
     * @throws BusinessLayerException if query fails
     */
    List<CallCardTransaction> findByCallCardId(String callCardId, Integer userGroupId,
                                                Integer pageNumber, Integer pageSize)
            throws BusinessLayerException;

    /**
     * Count transactions by CallCard ID.
     *
     * @param callCardId CallCard ID
     * @param userGroupId Tenant ID
     * @return Transaction count
     * @throws BusinessLayerException if query fails
     */
    Long countByCallCardId(String callCardId, Integer userGroupId) throws BusinessLayerException;

    /**
     * Find transactions by user ID.
     *
     * @param userId User ID
     * @param userGroupId Tenant ID
     * @param dateFrom Start date
     * @param dateTo End date
     * @param pageNumber Page number
     * @param pageSize Records per page
     * @return List of transactions
     * @throws BusinessLayerException if query fails
     */
    List<CallCardTransaction> findByUserId(Integer userId, Integer userGroupId, Date dateFrom, Date dateTo,
                                            Integer pageNumber, Integer pageSize)
            throws BusinessLayerException;

    /**
     * Count transactions by user ID.
     *
     * @param userId User ID
     * @param userGroupId Tenant ID
     * @param dateFrom Start date
     * @param dateTo End date
     * @return Transaction count
     * @throws BusinessLayerException if query fails
     */
    Long countByUserId(Integer userId, Integer userGroupId, Date dateFrom, Date dateTo)
            throws BusinessLayerException;

    /**
     * Find transactions by type.
     *
     * @param transactionType Transaction type
     * @param userGroupId Tenant ID
     * @param dateFrom Start date
     * @param dateTo End date
     * @param pageNumber Page number
     * @param pageSize Records per page
     * @return List of transactions
     * @throws BusinessLayerException if query fails
     */
    List<CallCardTransaction> findByType(CallCardTransactionType transactionType, Integer userGroupId,
                                          Date dateFrom, Date dateTo, Integer pageNumber, Integer pageSize)
            throws BusinessLayerException;

    /**
     * Count transactions by type.
     *
     * @param transactionType Transaction type
     * @param userGroupId Tenant ID
     * @param dateFrom Start date
     * @param dateTo End date
     * @return Transaction count
     * @throws BusinessLayerException if query fails
     */
    Long countByType(CallCardTransactionType transactionType, Integer userGroupId, Date dateFrom, Date dateTo)
            throws BusinessLayerException;

    /**
     * Advanced transaction search.
     *
     * @param criteria Search criteria
     * @return List of transactions
     * @throws BusinessLayerException if query fails
     */
    List<CallCardTransaction> searchTransactions(TransactionSearchCriteriaDTO criteria)
            throws BusinessLayerException;

    /**
     * Count search results.
     *
     * @param criteria Search criteria
     * @return Transaction count
     * @throws BusinessLayerException if query fails
     */
    Long countSearchResults(TransactionSearchCriteriaDTO criteria) throws BusinessLayerException;

    /**
     * Find transaction by ID.
     *
     * @param transactionId Transaction ID
     * @param userGroupId Tenant ID
     * @return Transaction or null
     * @throws BusinessLayerException if query fails
     */
    CallCardTransaction findById(String transactionId, Integer userGroupId) throws BusinessLayerException;

    /**
     * Find recent transactions.
     *
     * @param userGroupId Tenant ID
     * @param limit Max records
     * @return List of recent transactions
     * @throws BusinessLayerException if query fails
     */
    List<CallCardTransaction> findRecent(Integer userGroupId, Integer limit) throws BusinessLayerException;

    /**
     * Find transactions by session ID.
     *
     * @param sessionId Session ID
     * @param userGroupId Tenant ID
     * @return List of transactions
     * @throws BusinessLayerException if query fails
     */
    List<CallCardTransaction> findBySessionId(String sessionId, Integer userGroupId) throws BusinessLayerException;

    /**
     * Serialize CallCard entity to JSON for storage in transaction history.
     *
     * @param callCard CallCard entity
     * @return JSON string
     */
    String serializeCallCard(CallCard callCard);

    /**
     * Detect what changed between two CallCard states.
     *
     * @param oldCallCard Old state
     * @param newCallCard New state
     * @return Description of changes
     */
    String detectChanges(CallCard oldCallCard, CallCard newCallCard);
}
