-- ============================================================================
-- CallCard Microservice - Rollback V002
-- ============================================================================
-- Purpose: Rollback V002__add_performance_indexes.sql
-- Drops all performance indexes created in V002
-- Author: Talos Maind Platform
-- Date: 2025-12-22
-- Database: Microsoft SQL Server 2008+
-- ============================================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

PRINT 'Starting V002 rollback - dropping performance indexes...'
GO

-- ============================================================================
-- Drop CALL_CARD_TEMPLATE Indexes
-- ============================================================================

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_usergroup_gametype'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE')
)
BEGIN
    DROP INDEX idx_template_usergroup_gametype ON CALL_CARD_TEMPLATE;
    PRINT 'Index idx_template_usergroup_gametype dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_daterange'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE')
)
BEGIN
    DROP INDEX idx_template_daterange ON CALL_CARD_TEMPLATE;
    PRINT 'Index idx_template_daterange dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_active'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE')
)
BEGIN
    DROP INDEX idx_template_active ON CALL_CARD_TEMPLATE;
    PRINT 'Index idx_template_active dropped';
END
GO

-- ============================================================================
-- Drop CALL_CARD_TEMPLATE_ENTRY Indexes
-- ============================================================================

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_entry_template'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_ENTRY')
)
BEGIN
    DROP INDEX idx_template_entry_template ON CALL_CARD_TEMPLATE_ENTRY;
    PRINT 'Index idx_template_entry_template dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_entry_itemtype'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_ENTRY')
)
BEGIN
    DROP INDEX idx_template_entry_itemtype ON CALL_CARD_TEMPLATE_ENTRY;
    PRINT 'Index idx_template_entry_itemtype dropped';
END
GO

-- ============================================================================
-- Drop CALL_CARD Indexes
-- ============================================================================

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_template'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    DROP INDEX idx_callcard_template ON CALL_CARD;
    PRINT 'Index idx_callcard_template dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_user'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    DROP INDEX idx_callcard_user ON CALL_CARD;
    PRINT 'Index idx_callcard_user dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_daterange'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    DROP INDEX idx_callcard_daterange ON CALL_CARD;
    PRINT 'Index idx_callcard_daterange dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_internal_ref'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    DROP INDEX idx_callcard_internal_ref ON CALL_CARD;
    PRINT 'Index idx_callcard_internal_ref dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_callcard_active'
    AND object_id = OBJECT_ID('CALL_CARD')
)
BEGIN
    DROP INDEX idx_callcard_active ON CALL_CARD;
    PRINT 'Index idx_callcard_active dropped';
END
GO

-- ============================================================================
-- Drop CALL_CARD_REFUSER Indexes
-- ============================================================================

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_callcard'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    DROP INDEX idx_refuser_callcard ON CALL_CARD_REFUSER;
    PRINT 'Index idx_refuser_callcard dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_refuser'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    DROP INDEX idx_refuser_refuser ON CALL_CARD_REFUSER;
    PRINT 'Index idx_refuser_refuser dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_source'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    DROP INDEX idx_refuser_source ON CALL_CARD_REFUSER;
    PRINT 'Index idx_refuser_source dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_daterange'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    DROP INDEX idx_refuser_daterange ON CALL_CARD_REFUSER;
    PRINT 'Index idx_refuser_daterange dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_status'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    DROP INDEX idx_refuser_status ON CALL_CARD_REFUSER;
    PRINT 'Index idx_refuser_status dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_internal_ref'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER')
)
BEGIN
    DROP INDEX idx_refuser_internal_ref ON CALL_CARD_REFUSER;
    PRINT 'Index idx_refuser_internal_ref dropped';
END
GO

-- ============================================================================
-- Drop CALL_CARD_REFUSER_INDEX Indexes
-- ============================================================================

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_index_refuser'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER_INDEX')
)
BEGIN
    DROP INDEX idx_refuser_index_refuser ON CALL_CARD_REFUSER_INDEX;
    PRINT 'Index idx_refuser_index_refuser dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_refuser_index_itemtype'
    AND object_id = OBJECT_ID('CALL_CARD_REFUSER_INDEX')
)
BEGIN
    DROP INDEX idx_refuser_index_itemtype ON CALL_CARD_REFUSER_INDEX;
    PRINT 'Index idx_refuser_index_itemtype dropped';
END
GO

-- ============================================================================
-- Drop CALL_CARD_TEMPLATE_USER Indexes
-- ============================================================================

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_user_template'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_USER')
)
BEGIN
    DROP INDEX idx_template_user_template ON CALL_CARD_TEMPLATE_USER;
    PRINT 'Index idx_template_user_template dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'idx_template_user_user'
    AND object_id = OBJECT_ID('CALL_CARD_TEMPLATE_USER')
)
BEGIN
    DROP INDEX idx_template_user_user ON CALL_CARD_TEMPLATE_USER;
    PRINT 'Index idx_template_user_user dropped';
END
GO

-- ============================================================================
-- Rollback Complete
-- ============================================================================
PRINT 'V002 rollback completed successfully';
PRINT 'All performance indexes have been dropped';
GO
