-- ============================================================================
-- CallCard Microservice - Rollback V001
-- ============================================================================
-- Purpose: Rollback V001__initial_schema_verification.sql
-- Drops all CallCard core tables created in V001
-- Author: Talos Maind Platform
-- Date: 2025-12-22
-- Database: Microsoft SQL Server 2008+
-- WARNING: This will delete all data in CallCard tables!
-- ============================================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

PRINT 'Starting V001 rollback - dropping CallCard tables...'
GO

-- Drop in reverse dependency order

-- Drop CALL_CARD_TRANSACTION_TYPE first (no dependencies within CallCard)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TRANSACTION_TYPE')
BEGIN
    DROP TABLE CALL_CARD_TRANSACTION_TYPE;
    PRINT 'Table CALL_CARD_TRANSACTION_TYPE dropped';
END
GO

-- Drop CALL_CARD_TEMPLATE_USER_REFERENCES (depends on CALL_CARD_TEMPLATE_USER)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_USER_REFERENCES')
BEGIN
    DROP TABLE CALL_CARD_TEMPLATE_USER_REFERENCES;
    PRINT 'Table CALL_CARD_TEMPLATE_USER_REFERENCES dropped';
END
GO

-- Drop CALL_CARD_TEMPLATE_USER
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_USER')
BEGIN
    DROP TABLE CALL_CARD_TEMPLATE_USER;
    PRINT 'Table CALL_CARD_TEMPLATE_USER dropped';
END
GO

-- Drop CALL_CARD_REFUSER_INDEX (depends on CALL_CARD_REFUSER)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_REFUSER_INDEX')
BEGIN
    DROP TABLE CALL_CARD_REFUSER_INDEX;
    PRINT 'Table CALL_CARD_REFUSER_INDEX dropped';
END
GO

-- Drop CALL_CARD_REFUSER (depends on CALL_CARD)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_REFUSER')
BEGIN
    DROP TABLE CALL_CARD_REFUSER;
    PRINT 'Table CALL_CARD_REFUSER dropped';
END
GO

-- Drop CALL_CARD (depends on CALL_CARD_TEMPLATE)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD')
BEGIN
    DROP TABLE CALL_CARD;
    PRINT 'Table CALL_CARD dropped';
END
GO

-- Drop CALL_CARD_TEMPLATE_POS (depends on CALL_CARD_TEMPLATE)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_POS')
BEGIN
    DROP TABLE CALL_CARD_TEMPLATE_POS;
    PRINT 'Table CALL_CARD_TEMPLATE_POS dropped';
END
GO

-- Drop CALL_CARD_TEMPLATE_ENTRY (depends on CALL_CARD_TEMPLATE)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_ENTRY')
BEGIN
    DROP TABLE CALL_CARD_TEMPLATE_ENTRY;
    PRINT 'Table CALL_CARD_TEMPLATE_ENTRY dropped';
END
GO

-- Drop CALL_CARD_TEMPLATE (base table)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE')
BEGIN
    DROP TABLE CALL_CARD_TEMPLATE;
    PRINT 'Table CALL_CARD_TEMPLATE dropped';
END
GO

-- ============================================================================
-- Rollback Complete
-- ============================================================================
PRINT 'V001 rollback completed successfully';
PRINT 'All CallCard tables have been dropped';
PRINT 'WARNING: All data has been deleted!';
GO
