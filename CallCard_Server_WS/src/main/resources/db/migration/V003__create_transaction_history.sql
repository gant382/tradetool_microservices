-- ============================================================================
-- CallCard Transaction History Table
-- ============================================================================
-- Purpose: Audit trail for all CallCard modifications
-- Features: Immutable records, JSON state storage, multi-tenant isolation
-- Author: Talos Maind Platform
-- Date: 2025-12-21
-- Version: V003
-- ============================================================================

-- Create CallCard Transaction History Table
CREATE TABLE CALL_CARD_TRANSACTION_HISTORY (
    TRANSACTION_ID          UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    CALL_CARD_ID           UNIQUEIDENTIFIER    NOT NULL,
    TRANSACTION_TYPE       NVARCHAR(50)        NOT NULL,
    USER_ID                INT                 NOT NULL,
    USER_GROUP_ID          INT                 NOT NULL,
    TIMESTAMP              DATETIME            NOT NULL DEFAULT GETDATE(),
    OLD_VALUE              NVARCHAR(MAX)       NULL,
    NEW_VALUE              NVARCHAR(MAX)       NULL,
    DESCRIPTION            NVARCHAR(500)       NULL,
    IP_ADDRESS             NVARCHAR(45)        NULL,
    SESSION_ID             NVARCHAR(100)       NULL,
    METADATA               NVARCHAR(MAX)       NULL,

    -- Primary Key
    CONSTRAINT PK_CALL_CARD_TRANSACTION_HISTORY PRIMARY KEY (TRANSACTION_ID)
);

-- ============================================================================
-- Indexes for Performance
-- ============================================================================

-- Index on CallCard ID (most common query - get history for specific CallCard)
CREATE NONCLUSTERED INDEX idx_transaction_callcard
    ON CALL_CARD_TRANSACTION_HISTORY(CALL_CARD_ID, USER_GROUP_ID)
    INCLUDE (TRANSACTION_TYPE, TIMESTAMP, DESCRIPTION);

-- Index on User ID (audit queries by user)
CREATE NONCLUSTERED INDEX idx_transaction_user
    ON CALL_CARD_TRANSACTION_HISTORY(USER_ID, USER_GROUP_ID, TIMESTAMP);

-- Index on User Group (tenant isolation)
CREATE NONCLUSTERED INDEX idx_transaction_usergroup
    ON CALL_CARD_TRANSACTION_HISTORY(USER_GROUP_ID, TIMESTAMP);

-- Index on Transaction Type (filter by operation type)
CREATE NONCLUSTERED INDEX idx_transaction_type
    ON CALL_CARD_TRANSACTION_HISTORY(TRANSACTION_TYPE, USER_GROUP_ID, TIMESTAMP);

-- Index on Timestamp (date range queries)
CREATE NONCLUSTERED INDEX idx_transaction_timestamp
    ON CALL_CARD_TRANSACTION_HISTORY(TIMESTAMP DESC);

-- Index on Session ID (track related operations)
CREATE NONCLUSTERED INDEX idx_transaction_session
    ON CALL_CARD_TRANSACTION_HISTORY(SESSION_ID)
    WHERE SESSION_ID IS NOT NULL;

-- ============================================================================
-- Transaction Type Constraint
-- ============================================================================
-- Ensure only valid transaction types are stored
ALTER TABLE CALL_CARD_TRANSACTION_HISTORY
    ADD CONSTRAINT CHK_TRANSACTION_TYPE
    CHECK (TRANSACTION_TYPE IN (
        'CREATE',
        'UPDATE',
        'DELETE',
        'ASSIGN',
        'UNASSIGN',
        'TEMPLATE_CHANGE',
        'STATUS_CHANGE',
        'DATE_CHANGE',
        'COMMENT_CHANGE',
        'REFERENCE_CHANGE'
    ));

-- ============================================================================
-- Foreign Key to Users Table (Optional - depends on your schema)
-- ============================================================================
-- Uncomment if Users table exists and you want referential integrity
-- Note: For audit trail, it's often better to NOT enforce FK
-- so transaction records remain even if user is deleted
--
-- ALTER TABLE CALL_CARD_TRANSACTION_HISTORY
--     ADD CONSTRAINT FK_TRANSACTION_USER
--     FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID);

-- ============================================================================
-- Comments/Extended Properties (SQL Server)
-- ============================================================================
EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'Immutable audit trail for all CallCard modifications. Records before/after states in JSON format.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD_TRANSACTION_HISTORY';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'Unique transaction identifier (UUID)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD_TRANSACTION_HISTORY',
    @level2type = N'COLUMN', @level2name = N'TRANSACTION_ID';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'CallCard ID this transaction refers to (soft FK to allow history retention after deletion)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD_TRANSACTION_HISTORY',
    @level2type = N'COLUMN', @level2name = N'CALL_CARD_ID';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'JSON serialized state before change (NULL for CREATE operations)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD_TRANSACTION_HISTORY',
    @level2type = N'COLUMN', @level2name = N'OLD_VALUE';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'JSON serialized state after change (NULL for DELETE operations)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD_TRANSACTION_HISTORY',
    @level2type = N'COLUMN', @level2name = N'NEW_VALUE';

-- ============================================================================
-- Sample Queries for Testing
-- ============================================================================

-- Get all transactions for a specific CallCard
-- SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
-- WHERE CALL_CARD_ID = 'your-callcard-id'
-- AND USER_GROUP_ID = 1
-- ORDER BY TIMESTAMP DESC;

-- Get all transactions by a user within date range
-- SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
-- WHERE USER_ID = 123
-- AND USER_GROUP_ID = 1
-- AND TIMESTAMP BETWEEN '2025-01-01' AND '2025-12-31'
-- ORDER BY TIMESTAMP DESC;

-- Get all transactions of a specific type
-- SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
-- WHERE TRANSACTION_TYPE = 'UPDATE'
-- AND USER_GROUP_ID = 1
-- ORDER BY TIMESTAMP DESC;

-- Count transactions per type
-- SELECT TRANSACTION_TYPE, COUNT(*) as TransactionCount
-- FROM CALL_CARD_TRANSACTION_HISTORY
-- WHERE USER_GROUP_ID = 1
-- GROUP BY TRANSACTION_TYPE
-- ORDER BY TransactionCount DESC;

-- ============================================================================
-- Migration Complete
-- ============================================================================
PRINT 'Transaction History table created successfully';
