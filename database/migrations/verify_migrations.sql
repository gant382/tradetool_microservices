-- ============================================================================
-- CallCard Database Migrations - Verification Script
-- ============================================================================
-- Purpose: Verify all migrations have been applied correctly
-- Run this script after migrations to validate schema
-- Author: Talos Maind Platform
-- Date: 2025-12-22
-- ============================================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

PRINT '========================================';
PRINT 'CallCard Migration Verification Report';
PRINT '========================================';
PRINT '';

-- ============================================================================
-- 1. VERIFY TABLES EXIST (V001)
-- ============================================================================

PRINT '1. VERIFYING TABLES (V001)...';
PRINT '';

DECLARE @TableCount INT;
SELECT @TableCount = COUNT(*)
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME LIKE 'CALL_CARD%';

PRINT 'Expected: 9 tables | Found: ' + CAST(@TableCount AS NVARCHAR(10));

IF @TableCount = 9
    PRINT '✓ PASS: All tables exist'
ELSE
    PRINT '✗ FAIL: Missing ' + CAST((9 - @TableCount) AS NVARCHAR(10)) + ' tables'

PRINT '';
PRINT 'Detailed Table List:';
SELECT
    ROW_NUMBER() OVER (ORDER BY TABLE_NAME) AS [#],
    TABLE_NAME AS [Table Name],
    'OK' AS [Status]
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME LIKE 'CALL_CARD%'
ORDER BY TABLE_NAME;

PRINT '';

-- ============================================================================
-- 2. VERIFY TABLE STRUCTURE (Column Count)
-- ============================================================================

PRINT '2. VERIFYING TABLE STRUCTURE...';
PRINT '';

SELECT
    OBJECT_NAME(c.object_id) AS [Table Name],
    COUNT(*) AS [Column Count]
FROM sys.columns c
WHERE OBJECT_NAME(c.object_id) LIKE 'CALL_CARD%'
GROUP BY OBJECT_NAME(c.object_id)
ORDER BY [Table Name];

PRINT '';

-- ============================================================================
-- 3. VERIFY PRIMARY KEYS
-- ============================================================================

PRINT '3. VERIFYING PRIMARY KEYS...';
PRINT '';

DECLARE @PKCount INT;
SELECT @PKCount = COUNT(*)
FROM sys.key_constraints
WHERE type = 'PK'
    AND OBJECT_NAME(parent_object_id) LIKE 'CALL_CARD%';

PRINT 'Primary Keys Found: ' + CAST(@PKCount AS NVARCHAR(10));

SELECT
    OBJECT_NAME(k.parent_object_id) AS [Table Name],
    k.name AS [Primary Key]
FROM sys.key_constraints k
WHERE k.type = 'PK'
    AND OBJECT_NAME(k.parent_object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(k.parent_object_id);

PRINT '';

-- ============================================================================
-- 4. VERIFY INDEXES (V002)
-- ============================================================================

PRINT '4. VERIFYING INDEXES (V002)...';
PRINT '';

DECLARE @IndexCount INT;
SELECT @IndexCount = COUNT(*)
FROM sys.indexes
WHERE OBJECT_NAME(object_id) LIKE 'CALL_CARD%'
    AND name IS NOT NULL
    AND name NOT LIKE 'PK%';

PRINT 'Expected: ~26 indexes | Found: ' + CAST(@IndexCount AS NVARCHAR(10));

IF @IndexCount >= 20
    PRINT '✓ PASS: Performance indexes exist'
ELSE
    PRINT '⚠ WARNING: Only ' + CAST(@IndexCount AS NVARCHAR(10)) + ' indexes found'

PRINT '';
PRINT 'Indexes by Table:';
SELECT
    OBJECT_NAME(i.object_id) AS [Table Name],
    COUNT(*) AS [Index Count]
FROM sys.indexes i
WHERE OBJECT_NAME(i.object_id) LIKE 'CALL_CARD%'
    AND i.name IS NOT NULL
    AND i.name NOT LIKE 'PK%'
GROUP BY OBJECT_NAME(i.object_id)
ORDER BY [Table Name];

PRINT '';

-- ============================================================================
-- 5. VERIFY CONSTRAINTS (V004)
-- ============================================================================

PRINT '5. VERIFYING CONSTRAINTS (V004)...';
PRINT '';

DECLARE @FKCount INT;
SELECT @FKCount = COUNT(*)
FROM sys.foreign_keys
WHERE OBJECT_NAME(parent_object_id) LIKE 'CALL_CARD%';

PRINT 'Foreign Keys Found: ' + CAST(@FKCount AS NVARCHAR(10));

SELECT
    OBJECT_NAME(fk.parent_object_id) AS [Table Name],
    fk.name AS [Foreign Key],
    OBJECT_NAME(fk.referenced_object_id) AS [References Table]
FROM sys.foreign_keys fk
WHERE OBJECT_NAME(fk.parent_object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(fk.parent_object_id);

PRINT '';

DECLARE @CheckCount INT;
SELECT @CheckCount = COUNT(*)
FROM sys.check_constraints
WHERE OBJECT_NAME(parent_object_id) LIKE 'CALL_CARD%';

PRINT 'Check Constraints Found: ' + CAST(@CheckCount AS NVARCHAR(10));

SELECT
    OBJECT_NAME(cc.parent_object_id) AS [Table Name],
    cc.name AS [Constraint Name],
    cc.definition AS [Definition]
FROM sys.check_constraints cc
WHERE OBJECT_NAME(cc.parent_object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(cc.parent_object_id);

PRINT '';

-- ============================================================================
-- 6. VERIFY TRANSACTION TYPES (V001)
-- ============================================================================

PRINT '6. VERIFYING TRANSACTION TYPES...';
PRINT '';

DECLARE @TransactionTypeCount INT;
SELECT @TransactionTypeCount = COUNT(*)
FROM CALL_CARD_TRANSACTION_TYPE;

PRINT 'Expected: 10 types | Found: ' + CAST(@TransactionTypeCount AS NVARCHAR(10));

IF @TransactionTypeCount = 10
    PRINT '✓ PASS: All transaction types populated'
ELSE
    PRINT '⚠ WARNING: Only ' + CAST(@TransactionTypeCount AS NVARCHAR(10)) + ' types found'

PRINT '';
SELECT * FROM CALL_CARD_TRANSACTION_TYPE ORDER BY TRANSACTION_TYPE_ID;

PRINT '';

-- ============================================================================
-- 7. VERIFY FLYWAY HISTORY (if using Flyway)
-- ============================================================================

PRINT '7. CHECKING FLYWAY MIGRATION HISTORY...';
PRINT '';

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'flyway_schema_history')
BEGIN
    PRINT 'Flyway History Found:';
    SELECT
        installed_rank AS [Rank],
        version AS [Version],
        description AS [Description],
        type AS [Type],
        installed_on AS [Installed],
        execution_time AS [Duration (ms)],
        success AS [Success]
    FROM flyway_schema_history
    ORDER BY installed_rank;

    PRINT '';
    PRINT 'Last Migration: ' + (SELECT CAST(MAX(installed_on) AS NVARCHAR(20)) FROM flyway_schema_history);
END
ELSE
BEGIN
    PRINT 'ℹ INFO: Flyway history table not found (manual migration or not using Flyway)';
END

PRINT '';

-- ============================================================================
-- 8. DATA VALIDATION CHECKS
-- ============================================================================

PRINT '8. DATA VALIDATION CHECKS...';
PRINT '';

-- Check for orphaned records
DECLARE @OrphanedCallCards INT;
SELECT @OrphanedCallCards = COUNT(*)
FROM CALL_CARD cc
WHERE cc.CALL_CARD_TEMPLATE_ID NOT IN (
    SELECT CALL_CARD_TEMPLATE_ID FROM CALL_CARD_TEMPLATE
);

IF @OrphanedCallCards > 0
    PRINT '⚠ WARNING: ' + CAST(@OrphanedCallCards AS NVARCHAR(10)) + ' orphaned CALL_CARD records (FK not enforced?)'
ELSE
    PRINT '✓ PASS: No orphaned CALL_CARD records'

-- Check for invalid date ranges in CALL_CARD_TEMPLATE
DECLARE @InvalidTemplateRanges INT;
SELECT @InvalidTemplateRanges = COUNT(*)
FROM CALL_CARD_TEMPLATE
WHERE START_DATE > END_DATE;

IF @InvalidTemplateRanges > 0
    PRINT '✗ FAIL: ' + CAST(@InvalidTemplateRanges AS NVARCHAR(10)) + ' CALL_CARD_TEMPLATE records with invalid date ranges'
ELSE
    PRINT '✓ PASS: All CALL_CARD_TEMPLATE date ranges valid'

-- Check for invalid date ranges in CALL_CARD
DECLARE @InvalidCardRanges INT;
SELECT @InvalidCardRanges = COUNT(*)
FROM CALL_CARD
WHERE END_DATE IS NOT NULL AND START_DATE > END_DATE;

IF @InvalidCardRanges > 0
    PRINT '✗ FAIL: ' + CAST(@InvalidCardRanges AS NVARCHAR(10)) + ' CALL_CARD records with invalid date ranges'
ELSE
    PRINT '✓ PASS: All CALL_CARD date ranges valid'

PRINT '';

-- ============================================================================
-- 9. INDEX STATISTICS
-- ============================================================================

PRINT '9. INDEX STATISTICS (Query Performance)...';
PRINT '';

SELECT TOP 10
    OBJECT_NAME(i.object_id) AS [Table],
    i.name AS [Index],
    ips.avg_fragmentation_in_percent AS [Fragmentation %],
    ips.page_count AS [Pages],
    CASE
        WHEN ips.avg_fragmentation_in_percent < 10 THEN 'Good'
        WHEN ips.avg_fragmentation_in_percent < 30 THEN 'Fair (reorganize)'
        ELSE 'Poor (rebuild)'
    END AS [Action]
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
WHERE OBJECT_NAME(ips.object_id) LIKE 'CALL_CARD%'
    AND ips.index_id > 0
    AND ips.avg_fragmentation_in_percent > 0
ORDER BY ips.avg_fragmentation_in_percent DESC;

PRINT '';

-- ============================================================================
-- 10. SUMMARY REPORT
-- ============================================================================

PRINT '========================================';
PRINT 'MIGRATION SUMMARY';
PRINT '========================================';
PRINT '';

PRINT 'Verification Status:';
PRINT '  Tables: ' + CASE WHEN @TableCount = 9 THEN '✓ PASS' ELSE '✗ FAIL' END;
PRINT '  Indexes: ' + CASE WHEN @IndexCount >= 20 THEN '✓ PASS' ELSE '⚠ PARTIAL' END;
PRINT '  Foreign Keys: ' + CAST(@FKCount AS NVARCHAR(10)) + ' (verify if correct)';
PRINT '  Check Constraints: ' + CAST(@CheckCount AS NVARCHAR(10)) + ' (verify if correct)';
PRINT '  Transaction Types: ' + CASE WHEN @TransactionTypeCount = 10 THEN '✓ PASS' ELSE '✗ FAIL' END;
PRINT '  Data Integrity: ' + CASE WHEN @OrphanedCallCards = 0 AND @InvalidTemplateRanges = 0 AND @InvalidCardRanges = 0 THEN '✓ PASS' ELSE '✗ ISSUES FOUND' END;

PRINT '';
PRINT 'Next Steps:';
PRINT '  1. Review any warnings or failures above';
PRINT '  2. Check application logs for migration success messages';
PRINT '  3. Test CallCard API endpoints';
PRINT '  4. Run performance benchmarks if data already exists';
PRINT '  5. Monitor query performance with indexes';

PRINT '';
PRINT 'Maintenance Recommended:';
IF @IndexCount > 0 AND EXISTS (
    SELECT 1 FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
    WHERE ips.avg_fragmentation_in_percent > 30
)
    PRINT '  - Run index rebuild job (fragmentation > 30%)';
ELSE
    PRINT '  - Index fragmentation acceptable';

PRINT '';
PRINT '========================================';
PRINT 'Verification Complete: ' + CAST(GETDATE() AS NVARCHAR(20));
PRINT '========================================';
GO
