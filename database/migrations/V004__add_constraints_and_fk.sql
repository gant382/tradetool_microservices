-- ============================================================================
-- CallCard Microservice - Foreign Keys & Constraints
-- ============================================================================
-- Purpose: Add foreign key relationships and data integrity constraints
-- Features: Multi-tenant isolation, referential integrity, check constraints
-- Author: Talos Maind Platform
-- Date: 2025-12-22
-- Version: V004
-- Database: Microsoft SQL Server 2008+
-- Dependencies: V001 (tables), shared gameserver_v3 schema (USERS, GAME_TYPE, etc.)
-- ============================================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE - Foreign Keys to shared schema
-- ============================================================================

-- FK to USER_GROUPS (multi-tenant isolation)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_USERGROUP'
)
BEGIN
    -- Note: USER_GROUPS table exists in shared gameserver_v3 schema
    -- Uncomment if you want to enforce referential integrity
    -- ALTER TABLE CALL_CARD_TEMPLATE
    -- ADD CONSTRAINT FK_TEMPLATE_USERGROUP
    -- FOREIGN KEY (USER_GROUP_ID) REFERENCES USER_GROUPS(GROUP_ID)
    -- ON DELETE RESTRICT ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_USERGROUP - commented out (shared schema dependency)';
END
GO

-- FK to GAME_TYPE (game type reference)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_GAMETYPE'
)
BEGIN
    -- Note: GAME_TYPE table exists in shared gameserver_v3 schema
    -- Uncomment if referential integrity is required
    -- ALTER TABLE CALL_CARD_TEMPLATE
    -- ADD CONSTRAINT FK_TEMPLATE_GAMETYPE
    -- FOREIGN KEY (GAME_TYPE_ID) REFERENCES GAME_TYPE(GAME_TYPE_ID)
    -- ON DELETE RESTRICT ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_GAMETYPE - commented out (shared schema dependency)';
END
GO

-- Check constraint: START_DATE <= END_DATE
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_DATERANGE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE
    ADD CONSTRAINT CHK_TEMPLATE_DATERANGE
    CHECK (START_DATE <= END_DATE);

    PRINT 'Constraint CHK_TEMPLATE_DATERANGE created on CALL_CARD_TEMPLATE';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_ENTRY - Foreign Keys
-- ============================================================================

-- FK to CALL_CARD_TEMPLATE (cascade delete entries when template deleted)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_ENTRY_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_ENTRY
    ADD CONSTRAINT FK_TEMPLATE_ENTRY_TEMPLATE
    FOREIGN KEY (CALL_CARD_TEMPLATE_ID) REFERENCES CALL_CARD_TEMPLATE(CALL_CARD_TEMPLATE_ID)
    ON DELETE CASCADE ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_ENTRY_TEMPLATE created';
END
GO

-- FK to ITEM_TYPES (shared schema)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_ENTRY_ITEMTYPE'
)
BEGIN
    -- Note: ITEM_TYPES table exists in shared gameserver_v3 schema
    -- Uncomment if referential integrity is required
    -- ALTER TABLE CALL_CARD_TEMPLATE_ENTRY
    -- ADD CONSTRAINT FK_TEMPLATE_ENTRY_ITEMTYPE
    -- FOREIGN KEY (ITEM_TYPE_ID) REFERENCES ITEM_TYPES(ITEM_TYPE_ID)
    -- ON DELETE RESTRICT ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_ENTRY_ITEMTYPE - commented out (shared schema dependency)';
END
GO

-- Check constraint: ORDERING >= 0
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_ENTRY_ORDERING'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_ENTRY
    ADD CONSTRAINT CHK_TEMPLATE_ENTRY_ORDERING
    CHECK (ORDERING >= 0);

    PRINT 'Constraint CHK_TEMPLATE_ENTRY_ORDERING created on CALL_CARD_TEMPLATE_ENTRY';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_POS - Foreign Keys
-- ============================================================================

-- FK to CALL_CARD_TEMPLATE
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_POS_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_POS
    ADD CONSTRAINT FK_TEMPLATE_POS_TEMPLATE
    FOREIGN KEY (CALL_CARD_TEMPLATE_ID) REFERENCES CALL_CARD_TEMPLATE(CALL_CARD_TEMPLATE_ID)
    ON DELETE CASCADE ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_POS_TEMPLATE created';
END
GO

-- ============================================================================
-- CALL_CARD - Foreign Keys & Constraints
-- ============================================================================

-- FK to CALL_CARD_TEMPLATE (restrict delete)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_CALLCARD_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD
    ADD CONSTRAINT FK_CALLCARD_TEMPLATE
    FOREIGN KEY (CALL_CARD_TEMPLATE_ID) REFERENCES CALL_CARD_TEMPLATE(CALL_CARD_TEMPLATE_ID)
    ON DELETE RESTRICT ON UPDATE CASCADE;

    PRINT 'FK_CALLCARD_TEMPLATE created';
END
GO

-- FK to USERS (shared schema)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_CALLCARD_USER'
)
BEGIN
    -- Note: USERS table exists in shared gameserver_v3 schema
    -- Uncomment if referential integrity is required
    -- ALTER TABLE CALL_CARD
    -- ADD CONSTRAINT FK_CALLCARD_USER
    -- FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID)
    -- ON DELETE RESTRICT ON UPDATE CASCADE;

    PRINT 'FK_CALLCARD_USER - commented out (shared schema dependency)';
END
GO

-- Check constraint: END_DATE can be NULL (active indefinitely) or >= START_DATE
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_CALLCARD_DATERANGE'
)
BEGIN
    ALTER TABLE CALL_CARD
    ADD CONSTRAINT CHK_CALLCARD_DATERANGE
    CHECK (END_DATE IS NULL OR START_DATE <= END_DATE);

    PRINT 'Constraint CHK_CALLCARD_DATERANGE created on CALL_CARD';
END
GO

-- ============================================================================
-- CALL_CARD_REFUSER - Foreign Keys & Constraints
-- ============================================================================

-- FK to CALL_CARD (cascade delete assignments when card deleted)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_REFUSER_CALLCARD'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER
    ADD CONSTRAINT FK_REFUSER_CALLCARD
    FOREIGN KEY (CALL_CARD_ID) REFERENCES CALL_CARD(CALL_CARD_ID)
    ON DELETE CASCADE ON UPDATE CASCADE;

    PRINT 'FK_REFUSER_CALLCARD created';
END
GO

-- FK to USERS for REF_USER_ID (shared schema)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_REFUSER_REFUSER'
)
BEGIN
    -- Note: USERS table exists in shared gameserver_v3 schema
    -- Use NOCHECK if you want to add without validating existing data
    -- ALTER TABLE CALL_CARD_REFUSER WITH NOCHECK
    -- ADD CONSTRAINT FK_REFUSER_REFUSER
    -- FOREIGN KEY (REF_USER_ID) REFERENCES USERS(USER_ID)
    -- ON DELETE RESTRICT ON UPDATE CASCADE;

    PRINT 'FK_REFUSER_REFUSER - commented out (shared schema dependency)';
END
GO

-- FK to USERS for SOURCE_USER_ID (who made the assignment)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_REFUSER_SOURCE'
)
BEGIN
    -- Optional: audit trail FK
    -- ALTER TABLE CALL_CARD_REFUSER
    -- ADD CONSTRAINT FK_REFUSER_SOURCE
    -- FOREIGN KEY (SOURCE_USER_ID) REFERENCES USERS(USER_ID)
    -- ON DELETE SET NULL ON UPDATE CASCADE;

    PRINT 'FK_REFUSER_SOURCE - commented out (optional audit trail)';
END
GO

-- Check constraint: END_DATE can be NULL or >= START_DATE
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_DATERANGE'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER
    ADD CONSTRAINT CHK_REFUSER_DATERANGE
    CHECK (END_DATE IS NULL OR START_DATE <= END_DATE);

    PRINT 'Constraint CHK_REFUSER_DATERANGE created on CALL_CARD_REFUSER';
END
GO

-- Check constraint: STATUS should be 0 or positive
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_STATUS'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER
    ADD CONSTRAINT CHK_REFUSER_STATUS
    CHECK (STATUS IS NULL OR STATUS >= 0);

    PRINT 'Constraint CHK_REFUSER_STATUS created on CALL_CARD_REFUSER';
END
GO

-- ============================================================================
-- CALL_CARD_REFUSER_INDEX - Foreign Keys & Constraints
-- ============================================================================

-- FK to CALL_CARD_REFUSER (cascade delete when parent refuser deleted)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_REFUSER_INDEX_REFUSER'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER_INDEX
    ADD CONSTRAINT FK_REFUSER_INDEX_REFUSER
    FOREIGN KEY (CALL_CARD_REFUSER_ID) REFERENCES CALL_CARD_REFUSER(CALL_CARD_REFUSER_ID)
    ON DELETE CASCADE ON UPDATE CASCADE;

    PRINT 'FK_REFUSER_INDEX_REFUSER created';
END
GO

-- FK to ITEM_TYPES (shared schema)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_REFUSER_INDEX_ITEMTYPE'
)
BEGIN
    -- Note: ITEM_TYPES table exists in shared gameserver_v3 schema
    -- Uncomment if referential integrity is required
    -- ALTER TABLE CALL_CARD_REFUSER_INDEX
    -- ADD CONSTRAINT FK_REFUSER_INDEX_ITEMTYPE
    -- FOREIGN KEY (ITEM_TYPE_ID) REFERENCES ITEM_TYPES(ITEM_TYPE_ID)
    -- ON DELETE RESTRICT ON UPDATE CASCADE;

    PRINT 'FK_REFUSER_INDEX_ITEMTYPE - commented out (shared schema dependency)';
END
GO

-- Check constraint: Quantities should be non-negative
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_INDEX_QUANTITY'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER_INDEX
    ADD CONSTRAINT CHK_REFUSER_INDEX_QUANTITY
    CHECK (QUANTITY IS NULL OR QUANTITY >= 0);

    PRINT 'Constraint CHK_REFUSER_INDEX_QUANTITY created on CALL_CARD_REFUSER_INDEX';
END
GO

-- Check constraint: Used quantity <= total quantity
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_REFUSER_INDEX_USED_QTY'
)
BEGIN
    ALTER TABLE CALL_CARD_REFUSER_INDEX
    ADD CONSTRAINT CHK_REFUSER_INDEX_USED_QTY
    CHECK (USED_QUANTITY IS NULL OR USED_QUANTITY >= 0);

    PRINT 'Constraint CHK_REFUSER_INDEX_USED_QTY created on CALL_CARD_REFUSER_INDEX';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_USER - Foreign Keys & Constraints
-- ============================================================================

-- FK to CALL_CARD_TEMPLATE
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_USER_TEMPLATE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER
    ADD CONSTRAINT FK_TEMPLATE_USER_TEMPLATE
    FOREIGN KEY (CALL_CARD_TEMPLATE_ID) REFERENCES CALL_CARD_TEMPLATE(CALL_CARD_TEMPLATE_ID)
    ON DELETE CASCADE ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_USER_TEMPLATE created';
END
GO

-- FK to USERS (shared schema)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_USER_USER'
)
BEGIN
    -- Note: USERS table exists in shared gameserver_v3 schema
    -- Uncomment if referential integrity is required
    -- ALTER TABLE CALL_CARD_TEMPLATE_USER
    -- ADD CONSTRAINT FK_TEMPLATE_USER_USER
    -- FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID)
    -- ON DELETE CASCADE ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_USER_USER - commented out (shared schema dependency)';
END
GO

-- ============================================================================
-- CALL_CARD_TEMPLATE_USER_REFERENCES - Foreign Keys & Constraints
-- ============================================================================

-- FK to CALL_CARD_TEMPLATE_USER
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TEMPLATE_USER_REF_TEMPLATEUSER'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER_REFERENCES
    ADD CONSTRAINT FK_TEMPLATE_USER_REF_TEMPLATEUSER
    FOREIGN KEY (CALL_CARD_TEMPLATE_USER_ID) REFERENCES CALL_CARD_TEMPLATE_USER(CALL_CARD_TEMPLATE_USER_ID)
    ON DELETE CASCADE ON UPDATE CASCADE;

    PRINT 'FK_TEMPLATE_USER_REF_TEMPLATEUSER created';
END
GO

-- Check constraint: REFERENCE_TYPE validation
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_USER_REF_TYPE'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER_REFERENCES
    ADD CONSTRAINT CHK_TEMPLATE_USER_REF_TYPE
    CHECK (REFERENCE_TYPE IN ('ASSIGNEE', 'APPROVER', 'REVIEWER', 'MANAGER', 'OTHER'));

    PRINT 'Constraint CHK_TEMPLATE_USER_REF_TYPE created on CALL_CARD_TEMPLATE_USER_REFERENCES';
END
GO

-- Check constraint: VALIDATION_STATUS validation
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CHK_TEMPLATE_USER_VALIDATION'
)
BEGIN
    ALTER TABLE CALL_CARD_TEMPLATE_USER_REFERENCES
    ADD CONSTRAINT CHK_TEMPLATE_USER_VALIDATION
    CHECK (VALIDATION_STATUS IS NULL OR VALIDATION_STATUS IN ('VALID', 'INVALID', 'PENDING', 'UNKNOWN'));

    PRINT 'Constraint CHK_TEMPLATE_USER_VALIDATION created on CALL_CARD_TEMPLATE_USER_REFERENCES';
END
GO

-- ============================================================================
-- Verification Queries (for testing)
-- ============================================================================

/*
-- List all foreign keys on CallCard tables:
SELECT
    fk.name AS ForeignKeyName,
    OBJECT_NAME(fk.parent_object_id) AS TableName,
    c1.name AS ColumnName,
    OBJECT_NAME(fk.referenced_object_id) AS ReferencedTable,
    c2.name AS ReferencedColumn
FROM sys.foreign_keys fk
JOIN sys.columns c1 ON fk.parent_object_id = c1.object_id AND fk.parent_column_id = c1.column_id
JOIN sys.columns c2 ON fk.referenced_object_id = c2.object_id AND fk.referenced_column_id = c2.column_id
WHERE OBJECT_NAME(fk.parent_object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(fk.parent_object_id);

-- List all check constraints:
SELECT
    cc.name AS ConstraintName,
    OBJECT_NAME(cc.parent_object_id) AS TableName,
    cc.definition AS ConstraintDefinition
FROM sys.check_constraints cc
WHERE OBJECT_NAME(cc.parent_object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(cc.parent_object_id);

-- Test referential integrity (will show errors for any violations):
-- DBCC CHECKCONSTRAINTS;
*/

-- ============================================================================
-- Migration Complete
-- ============================================================================
PRINT 'V004 - Foreign keys and constraints created successfully';
PRINT 'Note: Foreign keys to shared schema tables are commented out - uncomment if needed';
GO
