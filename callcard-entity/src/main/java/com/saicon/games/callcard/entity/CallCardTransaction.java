package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.Users;
import com.saicon.games.entities.shared.UserGroup;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;

/**
 * CallCard Transaction History Entity.
 * Provides immutable audit trail for all CallCard modifications.
 * Records before/after states in JSON format for complete audit tracking.
 *
 * IMPORTANT: Transaction records are IMMUTABLE - no updates or deletes allowed.
 * Multi-tenant isolation enforced via userGroupId.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
@Entity
@Table(name = "CALL_CARD_TRANSACTION_HISTORY",
        indexes = {
                @javax.persistence.Index(name = "idx_transaction_callcard", columnList = "CALL_CARD_ID"),
                @javax.persistence.Index(name = "idx_transaction_user", columnList = "USER_ID"),
                @javax.persistence.Index(name = "idx_transaction_usergroup", columnList = "USER_GROUP_ID"),
                @javax.persistence.Index(name = "idx_transaction_type", columnList = "TRANSACTION_TYPE"),
                @javax.persistence.Index(name = "idx_transaction_timestamp", columnList = "TIMESTAMP"),
                @javax.persistence.Index(name = "idx_transaction_session", columnList = "SESSION_ID")
        })
@NamedQueries({
        @NamedQuery(
                name = "CallCardTransaction.findByCallCardId",
                query = "SELECT t FROM CallCardTransaction t WHERE t.callCardId = :callCardId AND t.userGroupId = :userGroupId ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "CallCardTransaction.findByUserId",
                query = "SELECT t FROM CallCardTransaction t WHERE t.userId.userId = :userId AND t.userGroupId = :userGroupId AND t.timestamp BETWEEN :dateFrom AND :dateTo ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "CallCardTransaction.findByType",
                query = "SELECT t FROM CallCardTransaction t WHERE t.transactionType = :transactionType AND t.userGroupId = :userGroupId AND t.timestamp BETWEEN :dateFrom AND :dateTo ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "CallCardTransaction.findByUserGroup",
                query = "SELECT t FROM CallCardTransaction t WHERE t.userGroupId = :userGroupId AND t.timestamp BETWEEN :dateFrom AND :dateTo ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "CallCardTransaction.countByCallCard",
                query = "SELECT COUNT(t) FROM CallCardTransaction t WHERE t.callCardId = :callCardId AND t.userGroupId = :userGroupId"
        )
})
public class CallCardTransaction {

    @Id
    @Column(name = "TRANSACTION_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String transactionId;

    /**
     * CallCard ID this transaction refers to.
     * FK to CALL_CARD table (but not enforced to allow history retention after deletion)
     */
    @Column(name = "CALL_CARD_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String callCardId;

    /**
     * Type of transaction (CREATE, UPDATE, DELETE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 50)
    private CallCardTransactionType transactionType;

    /**
     * User who performed the action
     */
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
    @ManyToOne(optional = false)
    private Users userId;

    /**
     * Tenant isolation - User Group ID
     */
    @Column(name = "USER_GROUP_ID", nullable = false)
    private Integer userGroupId;

    /**
     * Transaction timestamp (when the action occurred)
     */
    @Column(name = "TIMESTAMP", nullable = false, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    /**
     * Old value (JSON serialized state before change)
     * NULL for CREATE operations
     */
    @Column(name = "OLD_VALUE", columnDefinition = "nvarchar(max)")
    @Lob
    private String oldValue;

    /**
     * New value (JSON serialized state after change)
     * NULL for DELETE operations
     */
    @Column(name = "NEW_VALUE", columnDefinition = "nvarchar(max)")
    @Lob
    private String newValue;

    /**
     * Human-readable description of the change
     */
    @Column(name = "DESCRIPTION", columnDefinition = "nvarchar(500)")
    private String description;

    /**
     * IP address of the request origin (for audit trail)
     */
    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

    /**
     * Session ID for tracking related actions
     */
    @Column(name = "SESSION_ID", length = 100)
    private String sessionId;

    /**
     * Additional metadata (JSON format for flexible storage)
     */
    @Column(name = "METADATA", columnDefinition = "nvarchar(max)")
    @Lob
    private String metadata;

    // Constructors

    public CallCardTransaction() {
    }

    /**
     * Quick constructor for simple transactions
     */
    public CallCardTransaction(String callCardId, CallCardTransactionType transactionType,
                                Users userId, Integer userGroupId, String description) {
        this.callCardId = callCardId;
        this.transactionType = transactionType;
        this.userId = userId;
        this.userGroupId = userGroupId;
        this.description = description;
        this.timestamp = new Date();
    }

    // Getters and Setters

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCallCardId() {
        return callCardId;
    }

    public void setCallCardId(String callCardId) {
        this.callCardId = callCardId;
    }

    public CallCardTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(CallCardTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Integer getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(Integer userGroupId) {
        this.userGroupId = userGroupId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "CallCardTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", callCardId='" + callCardId + '\'' +
                ", transactionType=" + transactionType +
                ", userId=" + (userId != null ? userId.getUserId() : "null") +
                ", userGroupId=" + userGroupId +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}
