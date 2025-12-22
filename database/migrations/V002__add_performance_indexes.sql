-- ============================================================================
-- CallCard Microservice - Performance Indexes
-- ============================================================================
-- Purpose: Add performance indexes for common queries on CallCard tables
-- Features: Composite indexes, covering indexes, multi-tenant support
-- Author: Talos Maind Platform
-- Date: 2025-12-22
-- Version: V002
-- Database: Microsoft SQL Server 2008+
-- ============================================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE Indexes
-- ============================================================================

-- Index on USER_GROUP_ID and GAME_TYPE_ID (multi-tenant + game type filtering)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_usergroup_gametype'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_template_usergroup_gametype
        ON CALL_CARD_TEMPLATE(USER_GROUP_ID, GAME_TYPE_ID)
        INCLUDE (NAME, START_DATE, END_DATE, ACTIVE)
    PRINT 'Index idx_template_usergroup_gametype created on CALL_CARD_TEMPLATE';
END
GO

-- Index on date range (validity window queries)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_daterange'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_template_daterange
        ON CALL_CARD_TEMPLATE(START_DATE, END_DATE)
        INCLUDE (USER_GROUP_ID, ACTIVE)
    PRINT 'Index idx_template_daterange created on CALL_CARD_TEMPLATE';
END
GO

-- Index on active status
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_active'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_template_active
        ON CALL_CARD_TEMPLATE(ACTIVE, USER_GROUP_ID)
        WHERE ACTIVE = 1
    PRINT 'Index idx_template_active created on CALL_CARD_TEMPLATE';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_ENTRY Indexes
-- ============================================================================

-- Index on CALL_CARD_TEMPLATE_ID (foreign key lookups)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_entry_template'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_ENTRY')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_template_entry_template
        ON CALL_CARD_TEMPLATE_ENTRY(CALL_CARD_TEMPLATE_ID)
        INCLUDE (ITEM_TYPE_ID, ORDERING, PROPERTIES)
    PRINT 'Index idx_template_entry_template created on CALL_CARD_TEMPLATE_ENTRY';
END
GO

-- Index on ITEM_TYPE_ID (filter by item type)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_entry_itemtype'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_ENTRY')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_template_entry_itemtype
        ON CALL_CARD_TEMPLATE_ENTRY(ITEM_TYPE_ID)
        INCLUDE (CALL_CARD_TEMPLATE_ID, ORDERING)
    PRINT 'Index idx_template_entry_itemtype created on CALL_CARD_TEMPLATE_ENTRY';
END
GO

-- ============================================================================
-- CALL_CARD Indexes
-- ============================================================================

-- Index on CALL_CARD_TEMPLATE_ID (template lookups)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_template'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_callcard_template
        ON CALL_CARD(CALL_CARD_TEMPLATE_ID)
        INCLUDE (USER_ID, ACTIVE, START_DATE, END_DATE)
    PRINT 'Index idx_callcard_template created on CALL_CARD';
END
GO

-- Index on USER_ID (user-specific callcards)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_user'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_callcard_user
        ON CALL_CARD(USER_ID, ACTIVE)
        INCLUDE (START_DATE, END_DATE, CALL_CARD_TEMPLATE_ID)
    PRINT 'Index idx_callcard_user created on CALL_CARD';
END
GO

-- Index on date range (find active callcards by date)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_daterange'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_callcard_daterange
        ON CALL_CARD(START_DATE, END_DATE)
        INCLUDE (USER_ID, ACTIVE, CALL_CARD_TEMPLATE_ID)
    PRINT 'Index idx_callcard_daterange created on CALL_CARD';
END
GO

-- Index on INTERNAL_REF_NO (reference lookups)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_internal_ref'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_callcard_internal_ref
        ON CALL_CARD(INTERNAL_REF_NO)
        WHERE INTERNAL_REF_NO IS NOT NULL
    PRINT 'Index idx_callcard_internal_ref created on CALL_CARD';
END
GO

-- Index on active status
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_active'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_callcard_active
        ON CALL_CARD(ACTIVE, USER_ID)
        WHERE ACTIVE = 1
    PRINT 'Index idx_callcard_active created on CALL_CARD';
END
GO

-- ============================================================================
-- CALL_CARD_REFUSER Indexes
-- ============================================================================

-- Index on CALL_CARD_ID (find all assigned users)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_callcard'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_callcard
        ON CALL_CARD_REFUSER(CALL_CARD_ID, ACTIVE)
        INCLUDE (REF_USER_ID, START_DATE, END_DATE, STATUS)
    PRINT 'Index idx_refuser_callcard created on CALL_CARD_REFUSER';
END
GO

-- Index on REF_USER_ID (find callcards assigned to user)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_refuser'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_refuser
        ON CALL_CARD_REFUSER(REF_USER_ID, ACTIVE)
        INCLUDE (CALL_CARD_ID, START_DATE, END_DATE)
    PRINT 'Index idx_refuser_refuser created on CALL_CARD_REFUSER';
END
GO

-- Index on SOURCE_USER_ID (audit trail - who assigned)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_source'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_source
        ON CALL_CARD_REFUSER(SOURCE_USER_ID)
        INCLUDE (CALL_CARD_ID, REF_USER_ID, CREATED_DATE)
        WHERE SOURCE_USER_ID IS NOT NULL
    PRINT 'Index idx_refuser_source created on CALL_CARD_REFUSER';
END
GO

-- Index on date range (valid assignments)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_daterange'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_daterange
        ON CALL_CARD_REFUSER(START_DATE, END_DATE)
        INCLUDE (CALL_CARD_ID, REF_USER_ID, ACTIVE)
    PRINT 'Index idx_refuser_daterange created on CALL_CARD_REFUSER';
END
GO

-- Index on STATUS
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_status'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_status
        ON CALL_CARD_REFUSER(STATUS, ACTIVE)
        WHERE STATUS IS NOT NULL
    PRINT 'Index idx_refuser_status created on CALL_CARD_REFUSER';
END
GO

-- Index on INTERNAL_REF_NO
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_internal_ref'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_internal_ref
        ON CALL_CARD_REFUSER(INTERNAL_REF_NO)
        WHERE INTERNAL_REF_NO IS NOT NULL
    PRINT 'Index idx_refuser_internal_ref created on CALL_CARD_REFUSER';
END
GO

-- ============================================================================
-- CALL_CARD_REFUSER_INDEX Indexes
-- ============================================================================

-- Index on CALL_CARD_REFUSER_ID (items for reference user)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_index_refuser'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER_INDEX')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_index_refuser
        ON CALL_CARD_REFUSER_INDEX(CALL_CARD_REFUSER_ID)
        INCLUDE (ITEM_TYPE_ID, QUANTITY, USED_QUANTITY, STATUS)
    PRINT 'Index idx_refuser_index_refuser created on CALL_CARD_REFUSER_INDEX';
END
GO

-- Index on ITEM_TYPE_ID (filter by item type)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_index_itemtype'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER_INDEX')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_refuser_index_itemtype
        ON CALL_CARD_REFUSER_INDEX(ITEM_TYPE_ID)
        INCLUDE (CALL_CARD_REFUSER_ID, QUANTITY, USED_QUANTITY)
    PRINT 'Index idx_refuser_index_itemtype created on CALL_CARD_REFUSER_INDEX';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_USER Indexes
-- ============================================================================

-- Index on CALL_CARD_TEMPLATE_ID
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_user_template'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_USER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_template_user_template
        ON CALL_CARD_TEMPLATE_USER(CALL_CARD_TEMPLATE_ID)
        INCLUDE (USER_ID, IS_ACTIVE)
    PRINT 'Index idx_template_user_template created on CALL_CARD_TEMPLATE_USER';
END
GO

-- Index on USER_ID
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_user_user'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_USER')
)
BEGIN
    CREATE NONCLUSTERED INDEX idx_template_user_user
        ON CALL_CARD_TEMPLATE_USER(USER_ID, IS_ACTIVE)
        INCLUDE (CALL_CARD_TEMPLATE_ID)
    PRINT 'Index idx_template_user_user created on CALL_CARD_TEMPLATE_USER';
END
GO

-- ============================================================================
-- CALL_CARD_TRANSACTION_HISTORY Indexes (from V003)
-- ============================================================================
-- Note: These indexes are created in V003__create_transaction_history.sql
-- Listed here for documentation purposes

-- idx_transaction_callcard - ON CALL_CARD_TRANSACTION_HISTORY(CALL_CARD_ID, USER_GROUP_ID)
-- idx_transaction_user - ON CALL_CARD_TRANSACTION_HISTORY(USER_ID, USER_GROUP_ID, TIMESTAMP)
-- idx_transaction_usergroup - ON CALL_CARD_TRANSACTION_HISTORY(USER_GROUP_ID, TIMESTAMP)
-- idx_transaction_type - ON CALL_CARD_TRANSACTION_HISTORY(TRANSACTION_TYPE, USER_GROUP_ID, TIMESTAMP)
-- idx_transaction_timestamp - ON CALL_CARD_TRANSACTION_HISTORY(TIMESTAMP DESC)
-- idx_transaction_session - ON CALL_CARD_TRANSACTION_HISTORY(SESSION_ID) WHERE SESSION_ID IS NOT NULL

-- ============================================================================
-- Verification Script (for testing)
-- ============================================================================

/*
-- List all indexes on CallCard tables:
SELECT
    OBJECT_NAME(i.object_id) AS TableName,
    i.name AS IndexName,
    i.type_desc AS IndexType,
    c.name AS ColumnName
FROM sys.indexes i
JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE OBJECT_NAME(i.object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(i.object_id), i.name;

-- Check index fragmentation:
SELECT
    OBJECT_NAME(ips.object_id) AS TableName,
    i.name AS IndexName,
    ips.avg_fragmentation_in_percent AS FragmentationPercent
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
WHERE OBJECT_NAME(ips.object_id) LIKE 'CALL_CARD%'
    AND ips.avg_fragmentation_in_percent > 0
    AND i.name IS NOT NULL
ORDER BY ips.avg_fragmentation_in_percent DESC;
*/

-- ============================================================================
-- Migration Complete
-- ============================================================================
PRINT 'V002 - Performance indexes created successfully';
PRINT 'Total indexes created for CallCard tables';
GO
