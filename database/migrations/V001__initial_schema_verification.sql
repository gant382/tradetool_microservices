-- ============================================================================
-- CallCard Microservice - Initial Schema Verification
-- ============================================================================
-- Purpose: Verify and create CallCard core tables (shared database with gameserver_v3)
-- Features: UUID primary keys, multi-tenant support, audit fields
-- Author: Talos Maind Platform
-- Date: 2025-12-22
-- Version: V001
-- Database: Microsoft SQL Server 2008+
-- ============================================================================

-- Set ANSI_NULLS and QUOTED_IDENTIFIER
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE Table
-- ============================================================================
-- Purpose: Define CallCard templates for different game types and user groups
-- Features: Date range validity, multi-tenant isolation (USER_GROUP_ID)
-- Dependencies: USERS and GAME_TYPE tables from shared gameserver_v3 schema

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE')
BEGIN
    CREATE TABLE CALL_CARD_TEMPLATE (
        CALL_CARD_TEMPLATE_ID   UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        USER_GROUP_ID           INT                 NOT NULL,
        GAME_TYPE_ID            INT                 NOT NULL,
        NAME                    NVARCHAR(255)       NOT NULL,
        START_DATE              DATETIME            NOT NULL,
        END_DATE                DATETIME            NOT NULL,
        ACTIVE                  BIT                 NOT NULL DEFAULT 1,
        CREATED_DATE            DATETIME            NOT NULL DEFAULT GETDATE(),
        LAST_UPDATED            DATETIME            NULL,

        -- Primary Key
        CONSTRAINT PK_CALL_CARD_TEMPLATE PRIMARY KEY (CALL_CARD_TEMPLATE_ID)
    );

    PRINT 'CALL_CARD_TEMPLATE table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_TEMPLATE table already exists';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_ENTRY Table
-- ============================================================================
-- Purpose: Define items/entries within a CallCard template
-- Features: Item type support, property storage (JSON), ordering

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_ENTRY')
BEGIN
    CREATE TABLE CALL_CARD_TEMPLATE_ENTRY (
        ID                      UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        CALL_CARD_TEMPLATE_ID   UNIQUEIDENTIFIER    NOT NULL,
        ITEM_ID                 UNIQUEIDENTIFIER    NULL,
        ITEM_TYPE_ID            INT                 NOT NULL,
        PROPERTIES              NVARCHAR(MAX)       NOT NULL,
        ORDERING                INT                 NOT NULL DEFAULT 0,

        -- Primary Key
        CONSTRAINT PK_CALL_CARD_TEMPLATE_ENTRY PRIMARY KEY (ID)
    );

    PRINT 'CALL_CARD_TEMPLATE_ENTRY table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_TEMPLATE_ENTRY table already exists';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_POS Table
-- ============================================================================
-- Purpose: Point-of-sale (POS) configuration for CallCard templates
-- Features: POS-specific settings, location mapping

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_POS')
BEGIN
    CREATE TABLE CALL_CARD_TEMPLATE_POS (
        CALL_CARD_TEMPLATE_POS_ID  UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        CALL_CARD_TEMPLATE_ID      UNIQUEIDENTIFIER    NOT NULL,
        POS_ID                     UNIQUEIDENTIFIER    NULL,
        CONFIGURATION              NVARCHAR(MAX)       NULL,
        CREATED_DATE               DATETIME            NOT NULL DEFAULT GETDATE(),

        -- Primary Key
        CONSTRAINT PK_CALL_CARD_TEMPLATE_POS PRIMARY KEY (CALL_CARD_TEMPLATE_POS_ID)
    );

    PRINT 'CALL_CARD_TEMPLATE_POS table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_TEMPLATE_POS table already exists';
END
GO

-- ============================================================================
-- CALL_CARD Table
-- ============================================================================
-- Purpose: Individual CallCard instances assigned to users
-- Features: Active date range, multi-tenant support, audit trail
-- Dependencies: CALL_CARD_TEMPLATE, USERS tables

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD')
BEGIN
    CREATE TABLE CALL_CARD (
        CALL_CARD_ID            UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        CALL_CARD_TEMPLATE_ID   UNIQUEIDENTIFIER    NOT NULL,
        USER_ID                 INT                 NOT NULL,
        START_DATE              DATETIME            NOT NULL,
        END_DATE                DATETIME            NULL,
        ACTIVE                  BIT                 NOT NULL DEFAULT 1,
        COMMENTS                NVARCHAR(255)       NULL,
        INTERNAL_REF_NO         NVARCHAR(100)       NULL,
        CREATED_DATE            DATETIME            NOT NULL DEFAULT GETDATE(),
        LAST_UPDATED            DATETIME            NULL,

        -- Primary Key
        CONSTRAINT PK_CALL_CARD PRIMARY KEY (CALL_CARD_ID)
    );

    PRINT 'CALL_CARD table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD table already exists';
END
GO

-- ============================================================================
-- CALL_CARD_REFUSER Table
-- ============================================================================
-- Purpose: Reference users within a CallCard (assignments/delegation)
-- Features: Date-based validity, assignment tracking, audit fields
-- Dependencies: CALL_CARD, USERS tables

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_REFUSER')
BEGIN
    CREATE TABLE CALL_CARD_REFUSER (
        CALL_CARD_REFUSER_ID    UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        CALL_CARD_ID            UNIQUEIDENTIFIER    NOT NULL,
        REF_USER_ID             INT                 NOT NULL,
        SOURCE_USER_ID          INT                 NULL,
        START_DATE              DATETIME            NULL,
        END_DATE                DATETIME            NULL,
        ACTIVE                  BIT                 NOT NULL DEFAULT 1,
        STATUS                  INT                 NULL,
        COMMENT                 NVARCHAR(255)       NULL,
        REF_NO                  NVARCHAR(100)       NULL,
        INTERNAL_REF_NO         NVARCHAR(100)       NULL,
        CREATED_DATE            DATETIME            NOT NULL DEFAULT GETDATE(),
        LAST_UPDATED            DATETIME            NULL,

        -- Primary Key
        CONSTRAINT PK_CALL_CARD_REFUSER PRIMARY KEY (CALL_CARD_REFUSER_ID)
    );

    PRINT 'CALL_CARD_REFUSER table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_REFUSER table already exists';
END
GO

-- ============================================================================
-- CALL_CARD_REFUSER_INDEX Table
-- ============================================================================
-- Purpose: Index/items for reference users within CallCard
-- Features: Item-level tracking, quantity/allocation management

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_REFUSER_INDEX')
BEGIN
    CREATE TABLE CALL_CARD_REFUSER_INDEX (
        CALL_CARD_REFUSER_INDEX_ID  UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        CALL_CARD_REFUSER_ID        UNIQUEIDENTIFIER    NOT NULL,
        ITEM_TYPE_ID                INT                 NOT NULL,
        QUANTITY                    INT                 NULL,
        USED_QUANTITY               INT                 NULL DEFAULT 0,
        STATUS                      INT                 NULL,
        CREATED_DATE                DATETIME            NOT NULL DEFAULT GETDATE(),

        -- Primary Key
        CONSTRAINT PK_CALL_CARD_REFUSER_INDEX PRIMARY KEY (CALL_CARD_REFUSER_INDEX_ID)
    );

    PRINT 'CALL_CARD_REFUSER_INDEX table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_REFUSER_INDEX table already exists';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_USER Table
-- ============================================================================
-- Purpose: Track template modifications/versions per user
-- Features: User-specific template customizations, audit trail

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_USER')
BEGIN
    CREATE TABLE CALL_CARD_TEMPLATE_USER (
        CALL_CARD_TEMPLATE_USER_ID  UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        CALL_CARD_TEMPLATE_ID       UNIQUEIDENTIFIER    NOT NULL,
        USER_ID                     INT                 NOT NULL,
        MODIFIED_CONFIGURATION      NVARCHAR(MAX)       NULL,
        IS_ACTIVE                   BIT                 NOT NULL DEFAULT 1,
        CREATED_DATE                DATETIME            NOT NULL DEFAULT GETDATE(),
        LAST_UPDATED                DATETIME            NULL,

        -- Primary Key
        CONSTRAINT PK_CALL_CARD_TEMPLATE_USER PRIMARY KEY (CALL_CARD_TEMPLATE_USER_ID)
    );

    PRINT 'CALL_CARD_TEMPLATE_USER table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_TEMPLATE_USER table already exists';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_USER_REFERENCES Table
-- ============================================================================
-- Purpose: Track user references and relationships in templates
-- Features: Reference tracking, validation status

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TEMPLATE_USER_REFERENCES')
BEGIN
    CREATE TABLE CALL_CARD_TEMPLATE_USER_REFERENCES (
        REFERENCE_ID                UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
        CALL_CARD_TEMPLATE_USER_ID  UNIQUEIDENTIFIER    NOT NULL,
        REFERENCED_USER_ID          INT                 NOT NULL,
        REFERENCE_TYPE              NVARCHAR(50)        NOT NULL,
        VALIDATION_STATUS           NVARCHAR(50)        NULL,
        CREATED_DATE                DATETIME            NOT NULL DEFAULT GETDATE(),

        -- Primary Key
        CONSTRAINT PK_CALL_CARD_TEMPLATE_USER_REFERENCES PRIMARY KEY (REFERENCE_ID)
    );

    PRINT 'CALL_CARD_TEMPLATE_USER_REFERENCES table created successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_TEMPLATE_USER_REFERENCES table already exists';
END
GO

-- ============================================================================
-- CALL_CARD_TRANSACTION_TYPE Table
-- ============================================================================
-- Purpose: Define valid transaction types for audit trail
-- Features: Type enumeration, descriptions

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALL_CARD_TRANSACTION_TYPE')
BEGIN
    CREATE TABLE CALL_CARD_TRANSACTION_TYPE (
        TRANSACTION_TYPE_ID     INT                 NOT NULL IDENTITY(1,1) PRIMARY KEY,
        TRANSACTION_TYPE_NAME   NVARCHAR(50)        NOT NULL UNIQUE,
        DESCRIPTION             NVARCHAR(255)       NULL,
        IS_ACTIVE               BIT                 NOT NULL DEFAULT 1
    );

    -- Insert default transaction types
    INSERT INTO CALL_CARD_TRANSACTION_TYPE (TRANSACTION_TYPE_NAME, DESCRIPTION)
    VALUES
        ('CREATE', 'CallCard creation'),
        ('UPDATE', 'CallCard modification'),
        ('DELETE', 'CallCard deletion'),
        ('ASSIGN', 'User assignment to CallCard'),
        ('UNASSIGN', 'User removal from CallCard'),
        ('TEMPLATE_CHANGE', 'Template change'),
        ('STATUS_CHANGE', 'Status change'),
        ('DATE_CHANGE', 'Date range modification'),
        ('COMMENT_CHANGE', 'Comment modification'),
        ('REFERENCE_CHANGE', 'Reference modification');

    PRINT 'CALL_CARD_TRANSACTION_TYPE table created and populated successfully';
END
ELSE
BEGIN
    PRINT 'CALL_CARD_TRANSACTION_TYPE table already exists';
END
GO

-- ============================================================================
-- Extended Properties (SQL Server Metadata)
-- ============================================================================

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'CallCard templates define the structure and items for callcards in the system',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD_TEMPLATE';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'Individual CallCard instances assigned to users with date range validity',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'Reference users assigned within a CallCard (delegation/assignment tracking)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'CALL_CARD_REFUSER';

-- ============================================================================
-- Verification Queries (for testing)
-- ============================================================================

/*
-- Verify all CallCard tables exist:
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME LIKE 'CALL_CARD%'
ORDER BY TABLE_NAME;

-- Check CALL_CARD_TEMPLATE structure:
EXEC sp_columns @table_name = 'CALL_CARD_TEMPLATE';

-- Verify transaction types populated:
SELECT * FROM CALL_CARD_TRANSACTION_TYPE;
*/

-- ============================================================================
-- Migration Complete
-- ============================================================================
PRINT 'V001 - Initial schema verification completed successfully';
PRINT 'All CallCard core tables verified/created';
GO
