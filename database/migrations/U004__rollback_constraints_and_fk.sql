-- ============================================================================
-- CallCard Microservice - Rollback V004
-- ============================================================================
-- Purpose: Rollback V004__add_constraints_and_fk.sql
-- Drops all foreign keys and check constraints created in V004
-- Author: Talos Maind Platform
-- Date: 2025-12-22
-- Database: Microsoft SQL Server 2008+
-- ============================================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

PRINT 'Starting V004 rollback - dropping foreign keys and constraints...'
GO

-- ============================================================================
-- Drop Foreign Keys (in dependency order)
-- ============================================================================

-- CALL_CARD_REFUSER_INDEX - FKs
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_REFUSER_INDEX_REFUSER'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER_INDEX DROP CONSTRAINT FK_REFUSER_INDEX_REFUSER;
    PRINT 'FK_REFUSER_INDEX_REFUSER dropped';
END
GO

-- CALL_CARD_REFUSER - FKs
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_REFUSER_CALLCARD'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER DROP CONSTRAINT FK_REFUSER_CALLCARD;
    PRINT 'FK_REFUSER_CALLCARD dropped';
END
GO

-- CALL_CARD - FKs
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_CALLCARD_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD DROP CONSTRAINT FK_CALLCARD_TEMPLATE;
    PRINT 'FK_CALLCARD_TEMPLATE dropped';
END
GO

-- CALL_CARD_TEMPLATE_ENTRY - FKs
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_ENTRY_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_ENTRY DROP CONSTRAINT FK_TEMPLATE_ENTRY_TEMPLATE;
    PRINT 'FK_TEMPLATE_ENTRY_TEMPLATE dropped';
END
GO

-- CALL_CARD_TEMPLATE_POS - FKs
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_POS_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_POS DROP CONSTRAINT FK_TEMPLATE_POS_TEMPLATE;
    PRINT 'FK_TEMPLATE_POS_TEMPLATE dropped';
END
GO

-- CALL_CARD_TEMPLATE_USER - FKs
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_USER_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER DROP CONSTRAINT FK_TEMPLATE_USER_TEMPLATE;
    PRINT 'FK_TEMPLATE_USER_TEMPLATE dropped';
END
GO

-- CALL_CARD_TEMPLATE_USER_REFERENCES - FKs
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_USER_REF_TEMPLATEUSER'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER_REFERENCES DROP CONSTRAINT FK_TEMPLATE_USER_REF_TEMPLATEUSER;
    PRINT 'FK_TEMPLATE_USER_REF_TEMPLATEUSER dropped';
END
GO

-- ============================================================================
-- Drop Check Constraints (per table)
-- ============================================================================

-- CALL_CARD_TEMPLATE constraints
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_DATERANGE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE DROP CONSTRAINT CHK_TEMPLATE_DATERANGE;
    PRINT 'CHK_TEMPLATE_DATERANGE dropped';
END
GO

-- CALL_CARD_TEMPLATE_ENTRY constraints
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_ENTRY_ORDERING'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_ENTRY DROP CONSTRAINT CHK_TEMPLATE_ENTRY_ORDERING;
    PRINT 'CHK_TEMPLATE_ENTRY_ORDERING dropped';
END
GO

-- CALL_CARD constraints
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_CALLCARD_DATERANGE'
)
BEGIN
    ALTER TABLE CALL_CARD DROP CONSTRAINT CHK_CALLCARD_DATERANGE;
    PRINT 'CHK_CALLCARD_DATERANGE dropped';
END
GO

-- CALL_CARD_REFUSER constraints
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_DATERANGE'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER DROP CONSTRAINT CHK_REFUSER_DATERANGE;
    PRINT 'CHK_REFUSER_DATERANGE dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_STATUS'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER DROP CONSTRAINT CHK_REFUSER_STATUS;
    PRINT 'CHK_REFUSER_STATUS dropped';
END
GO

-- CALL_CARD_REFUSER_INDEX constraints
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_INDEX_QUANTITY'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER_INDEX DROP CONSTRAINT CHK_REFUSER_INDEX_QUANTITY;
    PRINT 'CHK_REFUSER_INDEX_QUANTITY dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_INDEX_USED_QTY'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER_INDEX DROP CONSTRAINT CHK_REFUSER_INDEX_USED_QTY;
    PRINT 'CHK_REFUSER_INDEX_USED_QTY dropped';
END
GO

-- CALL_CARD_TEMPLATE_USER_REFERENCES constraints
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_USER_REF_TYPE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER_REFERENCES DROP CONSTRAINT CHK_TEMPLATE_USER_REF_TYPE;
    PRINT 'CHK_TEMPLATE_USER_REF_TYPE dropped';
END
GO

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_USER_VALIDATION'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER_REFERENCES DROP CONSTRAINT CHK_TEMPLATE_USER_VALIDATION;
    PRINT 'CHK_TEMPLATE_USER_VALIDATION dropped';
END
GO

-- ============================================================================
-- Rollback Complete
-- ============================================================================
PRINT 'V004 rollback completed successfully';
PRINT 'All foreign keys and check constraints have been dropped';
GO
