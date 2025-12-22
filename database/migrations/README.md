# CallCard Microservice - Database Migrations

This directory contains database migration scripts for the CallCard microservice. The CallCard system manages call card templates, assignments, and audit trails for the Talos Maind platform.

## Overview

**Database**: Microsoft SQL Server 2008+
**Shared Database**: `gameserver_v3` (multi-tenant)
**Framework**: Flyway or Liquibase (versioned scripts)
**Language**: T-SQL

## Migration Files

### Forward Migrations (V*)

#### V001__initial_schema_verification.sql
**Purpose**: Create and verify CallCard core tables

**Tables Created**:
- `CALL_CARD_TEMPLATE` - Template definitions for different game types
- `CALL_CARD_TEMPLATE_ENTRY` - Items within a template
- `CALL_CARD_TEMPLATE_POS` - Point-of-sale configurations
- `CALL_CARD` - Individual card instances assigned to users
- `CALL_CARD_REFUSER` - Reference users (assignments/delegation)
- `CALL_CARD_REFUSER_INDEX` - Items allocated to reference users
- `CALL_CARD_TEMPLATE_USER` - User-specific template customizations
- `CALL_CARD_TEMPLATE_USER_REFERENCES` - User reference tracking
- `CALL_CARD_TRANSACTION_TYPE` - Audit transaction type enumeration

**Key Features**:
- Multi-tenant isolation via `USER_GROUP_ID`
- UUID primary keys (UNIQUEIDENTIFIER)
- Date range validity support
- Audit fields (CREATED_DATE, LAST_UPDATED)
- Soft foreign keys to shared schema tables

**Dependencies**:
- `gameserver_v3.USERS` (USER_ID)
- `gameserver_v3.USER_GROUPS` (GROUP_ID)
- `gameserver_v3.GAME_TYPE` (GAME_TYPE_ID)
- `gameserver_v3.ITEM_TYPES` (ITEM_TYPE_ID)

---

#### V002__add_performance_indexes.sql
**Purpose**: Create performance indexes for common queries

**Indexes by Table**:

**CALL_CARD_TEMPLATE**:
- `idx_template_usergroup_gametype` - Multi-tenant + game type filtering
- `idx_template_daterange` - Validity window queries
- `idx_template_active` - Active status filtering

**CALL_CARD_TEMPLATE_ENTRY**:
- `idx_template_entry_template` - Find entries by template
- `idx_template_entry_itemtype` - Filter by item type

**CALL_CARD**:
- `idx_callcard_template` - Template lookups
- `idx_callcard_user` - User-specific cards
- `idx_callcard_daterange` - Find active cards by date
- `idx_callcard_internal_ref` - Reference number lookups
- `idx_callcard_active` - Active status filtering

**CALL_CARD_REFUSER**:
- `idx_refuser_callcard` - Find all assignments to a card
- `idx_refuser_refuser` - Find cards assigned to user
- `idx_refuser_source` - Audit trail (who assigned)
- `idx_refuser_daterange` - Valid date range assignments
- `idx_refuser_status` - Filter by assignment status
- `idx_refuser_internal_ref` - Reference number lookups

**CALL_CARD_REFUSER_INDEX**:
- `idx_refuser_index_refuser` - Items for a reference user
- `idx_refuser_index_itemtype` - Filter by item type

**CALL_CARD_TEMPLATE_USER**:
- `idx_template_user_template` - User modifications of template
- `idx_template_user_user` - Find user's template customizations

**Query Performance Impact**:
- 20-50% faster for common queries (estimated)
- Covering indexes reduce table scans
- Multi-tenant queries optimized for GROUP_ID filtering

---

#### V003__create_transaction_history.sql
**Purpose**: Audit trail for all CallCard modifications

**Table Created**:
- `CALL_CARD_TRANSACTION_HISTORY` - Immutable audit records

**Features**:
- JSON state storage (OLD_VALUE, NEW_VALUE)
- Session tracking (SESSION_ID, IP_ADDRESS)
- Multi-tenant isolation (USER_GROUP_ID)
- 10 transaction types (CREATE, UPDATE, DELETE, ASSIGN, UNASSIGN, etc.)
- Timestamp-based ordering
- Soft foreign keys (allows history after deletion)

**Indexes**:
- Optimized for audit queries (by card, user, date range, type)

---

#### V004__add_constraints_and_fk.sql
**Purpose**: Add referential integrity constraints

**Foreign Keys**:
- `FK_TEMPLATE_ENTRY_TEMPLATE` → CALL_CARD_TEMPLATE (CASCADE)
- `FK_TEMPLATE_POS_TEMPLATE` → CALL_CARD_TEMPLATE (CASCADE)
- `FK_CALLCARD_TEMPLATE` → CALL_CARD_TEMPLATE (RESTRICT)
- `FK_REFUSER_CALLCARD` → CALL_CARD (CASCADE)
- `FK_REFUSER_INDEX_REFUSER` → CALL_CARD_REFUSER (CASCADE)
- `FK_TEMPLATE_USER_TEMPLATE` → CALL_CARD_TEMPLATE (CASCADE)
- `FK_TEMPLATE_USER_REF_TEMPLATEUSER` → CALL_CARD_TEMPLATE_USER (CASCADE)

**Check Constraints**:
- `CHK_TEMPLATE_DATERANGE` - START_DATE ≤ END_DATE
- `CHK_TEMPLATE_ENTRY_ORDERING` - ORDERING ≥ 0
- `CHK_CALLCARD_DATERANGE` - START_DATE ≤ END_DATE (NULL allowed)
- `CHK_REFUSER_DATERANGE` - START_DATE ≤ END_DATE (NULL allowed)
- `CHK_REFUSER_STATUS` - STATUS ≥ 0 (NULL allowed)
- `CHK_REFUSER_INDEX_QUANTITY` - QUANTITY ≥ 0 (NULL allowed)
- `CHK_REFUSER_INDEX_USED_QTY` - USED_QUANTITY ≥ 0 (NULL allowed)
- `CHK_TEMPLATE_USER_REF_TYPE` - REFERENCE_TYPE in allowed values
- `CHK_TEMPLATE_USER_VALIDATION` - VALIDATION_STATUS in allowed values

**Dependencies to Shared Schema**:
- FK constraints to shared schema tables are **commented out**
- Uncomment manually after verifying shared schema exists
- Soft FKs allow audit records to survive deletions

---

### Rollback Migrations (U*)

Rollback scripts follow the reverse migration pattern:

#### U001__rollback_initial_schema.sql
Drops all CallCard tables in dependency order

#### U002__rollback_performance_indexes.sql
Drops all indexes created in V002

#### U004__rollback_constraints_and_fk.sql
Drops all FK and check constraints

**WARNING**: Rollback scripts are **destructive** - they delete data!

---

## Installation & Configuration

### Option 1: Flyway (Recommended)

Flyway is a lightweight, powerful database version control system.

**Setup**:

```bash
# 1. Add Flyway Maven dependency (already in pom.xml if configured)
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>9.22.x</version>
</dependency>

# 2. Configure application.yml
spring:
  flyway:
    locations: classpath:db/migration
    enabled: true
    baselineVersion: 0  # Set baseline if existing DB
    placeholderReplacement: false
```

**Run Migrations**:

```bash
# Automatic on Spring Boot startup (if enabled)
cd CallCard_Server_WS
mvn spring-boot:run

# Manual migration check
mvn flyway:info

# Manual migration execution
mvn flyway:migrate

# Validate migration status
mvn flyway:validate

# Baseline existing database (if not new)
mvn flyway:baseline -Dflyway.baselineVersion=0
```

**Flyway Advantages**:
- Automatic versioning (lexicographic order)
- Spring Boot integration
- Migration history tracked in `flyway_schema_history` table
- Automatic rollback on failure (transaction support)
- Type-safe Java migrations optional

---

### Option 2: Liquibase

Liquibase provides change-log based versioning.

**Setup**:

```bash
# 1. Create changelog XML file
# Location: src/main/resources/db/changelog/db.changelog-master.xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <include file="migrations/V001__initial_schema_verification.sql" relativeToChangelogFile="true"/>
    <include file="migrations/V002__add_performance_indexes.sql" relativeToChangelogFile="true"/>
    <include file="migrations/V004__add_constraints_and_fk.sql" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

**Configure application.yml**:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
```

**Run Migrations**:

```bash
# Automatic on startup
mvn spring-boot:run

# Check status
mvn liquibase:status

# Execute migrations
mvn liquibase:update

# Rollback (if implemented)
mvn liquibase:rollback -Dliquibase.rollback.count=1
```

---

### Option 3: Manual Execution (Development Only)

For development or testing environments:

```bash
# Using SQL Server Management Studio (SSMS)
# 1. Open Query Editor
# 2. Connect to database: gameserver_v3
# 3. Execute scripts in order:
#    - V001__initial_schema_verification.sql
#    - V002__add_performance_indexes.sql
#    - V004__add_constraints_and_fk.sql

# Using sqlcmd (command line)
sqlcmd -S localhost -U sa -P your_password -d gameserver_v3 -i V001__initial_schema_verification.sql
sqlcmd -S localhost -U sa -P your_password -d gameserver_v3 -i V002__add_performance_indexes.sql
sqlcmd -S localhost -U sa -P your_password -d gameserver_v3 -i V004__add_constraints_and_fk.sql

# Using PowerShell (Windows)
Invoke-Sqlcmd -ServerInstance "localhost" -Username "sa" -Password "your_password" `
    -Database "gameserver_v3" -InputFile "V001__initial_schema_verification.sql"
```

---

## Database Connection String

**SQL Server Format**:

```
Server=localhost;Database=gameserver_v3;User Id=sa;Password=your_password;
```

**Spring Boot (application.yml)**:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3;
    username: sa
    password: your_password
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create with migrations
    database-platform: org.hibernate.dialect.SQLServer2012Dialect
```

---

## Verification & Testing

### Verify Migration Success

```sql
-- 1. Check if tables exist
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME LIKE 'CALL_CARD%'
ORDER BY TABLE_NAME;

-- Expected tables:
-- CALL_CARD
-- CALL_CARD_REFUSER
-- CALL_CARD_REFUSER_INDEX
-- CALL_CARD_TEMPLATE
-- CALL_CARD_TEMPLATE_ENTRY
-- CALL_CARD_TEMPLATE_POS
-- CALL_CARD_TEMPLATE_USER
-- CALL_CARD_TEMPLATE_USER_REFERENCES
-- CALL_CARD_TRANSACTION_HISTORY
-- CALL_CARD_TRANSACTION_TYPE

-- 2. Check indexes
SELECT OBJECT_NAME(i.object_id) AS TableName, i.name AS IndexName
FROM sys.indexes i
WHERE OBJECT_NAME(i.object_id) LIKE 'CALL_CARD%'
    AND i.name IS NOT NULL
ORDER BY OBJECT_NAME(i.object_id);

-- 3. Check constraints
SELECT OBJECT_NAME(c.parent_object_id) AS TableName, c.name AS ConstraintName
FROM sys.check_constraints c
WHERE OBJECT_NAME(c.parent_object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(c.parent_object_id);

-- 4. Check foreign keys
SELECT OBJECT_NAME(fk.parent_object_id) AS TableName, fk.name AS ForeignKeyName
FROM sys.foreign_keys fk
WHERE OBJECT_NAME(fk.parent_object_id) LIKE 'CALL_CARD%';

-- 5. Check transaction types populated
SELECT * FROM CALL_CARD_TRANSACTION_TYPE;

-- 6. Check Flyway history (if using Flyway)
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
```

### Data Integrity Tests

```sql
-- 1. Check for orphaned records (no parent template)
SELECT cc.CALL_CARD_ID, cc.CALL_CARD_TEMPLATE_ID
FROM CALL_CARD cc
WHERE cc.CALL_CARD_TEMPLATE_ID NOT IN (
    SELECT CALL_CARD_TEMPLATE_ID FROM CALL_CARD_TEMPLATE
)
AND cc.CALL_CARD_TEMPLATE_ID IS NOT NULL;

-- 2. Check for invalid date ranges
SELECT CALL_CARD_TEMPLATE_ID, START_DATE, END_DATE
FROM CALL_CARD_TEMPLATE
WHERE START_DATE > END_DATE;

SELECT CALL_CARD_ID, START_DATE, END_DATE
FROM CALL_CARD
WHERE END_DATE IS NOT NULL AND START_DATE > END_DATE;

-- 3. Check index usage (SQL Server)
SELECT OBJECT_NAME(i.object_id) AS TableName,
       i.name AS IndexName,
       s.user_updates AS Updates,
       s.user_seeks + s.user_scans + s.user_lookups AS Reads
FROM sys.indexes i
LEFT JOIN sys.dm_db_index_usage_stats s ON i.object_id = s.object_id
    AND i.index_id = s.index_id
WHERE OBJECT_NAME(i.object_id) LIKE 'CALL_CARD%'
ORDER BY OBJECT_NAME(i.object_id);
```

---

## Shared Schema Dependencies

The CallCard microservice shares the `gameserver_v3` database. The following tables are referenced:

| Table | Column | Usage | Status |
|-------|--------|-------|--------|
| USERS | USER_ID | CallCard assignments | FK commented (shared) |
| USER_GROUPS | GROUP_ID | Multi-tenant isolation | FK commented (shared) |
| GAME_TYPE | GAME_TYPE_ID | Template game type | FK commented (shared) |
| ITEM_TYPES | ITEM_TYPE_ID | Template/index item types | FK commented (shared) |

**Important**: Foreign keys to shared schema tables are **commented out** in V004. To enable them:

1. Verify the shared tables exist
2. Uncomment the FK sections in V004__add_constraints_and_fk.sql
3. Re-run the migration or execute the uncommented statements manually

---

## Handling Existing Data

### Baseline Existing Database

If the CallCard tables already exist:

**Using Flyway**:

```bash
mvn flyway:baseline -Dflyway.baselineVersion=1

# This creates flyway_schema_history with version 1 already applied
# Next migration will be V002
```

**Using Liquibase**:

```bash
mvn liquibase:changelogSync

# Marks all existing changes as applied
```

**Manual**:

1. Back up database
2. Drop Flyway/Liquibase schema history table if exists
3. Run migrations in order

---

## Rollback Procedures

### Using Flyway

Flyway doesn't support undo by default. For rollback:

1. **Clean and Reapply**: Drop schema and re-migrate
   ```bash
   mvn flyway:clean          # DESTRUCTIVE!
   mvn flyway:migrate        # Reapply all migrations
   ```

2. **Manual Rollback**: Use U* scripts
   ```bash
   sqlcmd -S localhost -U sa -d gameserver_v3 -i U004__rollback_constraints_and_fk.sql
   sqlcmd -S localhost -U sa -d gameserver_v3 -i U002__rollback_performance_indexes.sql
   sqlcmd -S localhost -U sa -d gameserver_v3 -i U001__rollback_initial_schema.sql
   ```

### Using Liquibase

Liquibase supports native rollback:

```bash
# Rollback last migration
mvn liquibase:rollback -Dliquibase.rollback.count=1

# Rollback to specific tag
mvn liquibase:rollback -Dliquibase.rollback.tag=v1.0

# Preview rollback (dry-run)
mvn liquibase:rollbackSQL -Dliquibase.rollback.count=1
```

### Warning - Data Loss

**All rollback operations are destructive**. Always:

1. Back up production database before rollback
2. Test rollback in development first
3. Document rollback reason and timestamp
4. Notify team of rollback

---

## Performance Tuning

### Index Maintenance

```sql
-- Rebuild fragmented indexes (> 30% fragmentation)
ALTER INDEX idx_callcard_user ON CALL_CARD REBUILD;

-- Reorganize lightly fragmented indexes (10-30%)
ALTER INDEX idx_template_usergroup_gametype ON CALL_CARD_TEMPLATE REORGANIZE;

-- Update index statistics
UPDATE STATISTICS CALL_CARD;
EXEC sp_updatestats;

-- Check index fragmentation
SELECT
    OBJECT_NAME(ips.object_id) AS TableName,
    i.name AS IndexName,
    ips.avg_fragmentation_in_percent AS FragmentationPercent
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
JOIN sys.indexes i ON ips.object_id = i.object_id
    AND ips.index_id = i.index_id
WHERE OBJECT_NAME(ips.object_id) LIKE 'CALL_CARD%'
    AND ips.avg_fragmentation_in_percent > 0
    AND i.name IS NOT NULL;
```

### Query Optimization

Use execution plans to identify missing indexes:

```sql
-- Enable actual execution plan (SSMS: Ctrl+L)
SET STATISTICS IO ON;

-- Sample slow query
SELECT cc.*, ccu.* FROM CALL_CARD cc
JOIN CALL_CARD_REFUSER cr ON cc.CALL_CARD_ID = cr.CALL_CARD_ID
WHERE cc.USER_ID = 123
  AND cc.ACTIVE = 1
  AND cc.START_DATE <= GETDATE()
  AND (cc.END_DATE IS NULL OR cc.END_DATE > GETDATE());

SET STATISTICS IO OFF;
```

### Partitioning (Optional for Large Tables)

For > 1 million audit records:

```sql
-- Partition CALL_CARD_TRANSACTION_HISTORY by date
-- Improve query performance for historical data
CREATE PARTITION FUNCTION pf_transaction_date (DATETIME)
    AS RANGE LEFT FOR VALUES
    ('2024-01-01', '2025-01-01', '2026-01-01');
```

---

## Troubleshooting

### Common Issues

**1. Foreign Key Constraint Error**

```
Msg 547: The INSERT, UPDATE, or DELETE statement conflicted
with a FOREIGN KEY constraint
```

**Solution**: Check that referenced tables exist and IDs are valid:

```sql
-- Verify referenced data exists
SELECT DISTINCT CALL_CARD_TEMPLATE_ID FROM CALL_CARD
WHERE CALL_CARD_TEMPLATE_ID NOT IN (
    SELECT CALL_CARD_TEMPLATE_ID FROM CALL_CARD_TEMPLATE
);

-- Remove orphaned records or insert missing parents
```

---

**2. Migration Not Running**

```
Migration V001 failed: Cannot open database
```

**Solution**: Check connection string and database exists:

```bash
# Test connection with sqlcmd
sqlcmd -S localhost -U sa -P password -Q "SELECT DB_NAME();"

# Verify database exists
sqlcmd -S localhost -U sa -P password -Q "SELECT NAME FROM sys.databases WHERE NAME = 'gameserver_v3';"
```

---

**3. Constraint Violation on Data Insert**

```
Check constraint 'CHK_CALLCARD_DATERANGE' failed
```

**Solution**: Ensure date ranges are valid:

```sql
-- Fix invalid data
UPDATE CALL_CARD SET END_DATE = DATEADD(DAY, 30, START_DATE)
WHERE END_DATE IS NOT NULL AND END_DATE < START_DATE;
```

---

**4. Index Bloat (Migration Takes Too Long)**

**Solution**: Disable indexes during bulk inserts, rebuild after:

```sql
-- Disable indexes temporarily
ALTER INDEX idx_refuser_callcard ON CALL_CARD_REFUSER DISABLE;

-- ... bulk insert operations ...

-- Rebuild indexes
ALTER INDEX idx_refuser_callcard ON CALL_CARD_REFUSER REBUILD;
```

---

## Monitoring & Maintenance

### Regular Maintenance Schedule

| Task | Frequency | Command |
|------|-----------|---------|
| Check fragmentation | Weekly | See verification section |
| Rebuild fragmented indexes | Monthly | `ALTER INDEX ... REBUILD` |
| Update statistics | Weekly | `EXEC sp_updatestats` |
| Audit trail cleanup | Quarterly | Delete records > 1 year old |
| Backup database | Daily | `BACKUP DATABASE...` |

### Sample Maintenance Script

```sql
-- Run weekly to optimize CallCard tables
BEGIN TRANSACTION;

-- Update statistics
UPDATE STATISTICS CALL_CARD;
UPDATE STATISTICS CALL_CARD_REFUSER;
UPDATE STATISTICS CALL_CARD_TEMPLATE;
UPDATE STATISTICS CALL_CARD_TRANSACTION_HISTORY;

-- Rebuild heavily fragmented indexes
DECLARE @TableName NVARCHAR(128);
DECLARE @IndexName NVARCHAR(128);

DECLARE index_cursor CURSOR FOR
    SELECT OBJECT_NAME(i.object_id), i.name
    FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
    JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
    WHERE OBJECT_NAME(ips.object_id) LIKE 'CALL_CARD%'
        AND ips.avg_fragmentation_in_percent > 30
        AND i.name IS NOT NULL;

OPEN index_cursor;
FETCH NEXT FROM index_cursor INTO @TableName, @IndexName;

WHILE @@FETCH_STATUS = 0
BEGIN
    EXEC('ALTER INDEX ' + @IndexName + ' ON ' + @TableName + ' REBUILD');
    PRINT 'Rebuilt index: ' + @IndexName + ' on table: ' + @TableName;
    FETCH NEXT FROM index_cursor INTO @TableName, @IndexName;
END;

CLOSE index_cursor;
DEALLOCATE index_cursor;

COMMIT TRANSACTION;
PRINT 'Maintenance completed successfully';
```

---

## Additional Resources

### Documentation Files

- `CLAUDE.md` - Project context and architecture
- `callcard-entity/` - JPA entity definitions
- `callcard-components/` - Data access components
- `callcard-service/` - Business logic
- `callcard-ws-api/` - Web service interfaces

### Related Microservices

- **GameServer V3**: Core gaming platform (shared database)
- **TradeTool Middleware**: Consolidated middleware platform
- **Talos Maind**: Enterprise AI chatbot platform

### References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [SQL Server T-SQL Reference](https://docs.microsoft.com/en-us/sql/t-sql/language-reference)
- [Hibernate JPA Configuration](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html)

---

## Support & Contact

For issues or questions about database migrations:

- **Architecture**: Refer to project CLAUDE.md and entity classes
- **SQL Issues**: Check SQL Server error logs and Flyway/Liquibase logs
- **Application Issues**: Review Spring Boot application logs

---

## Changelog

### Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-12-22 | Initial migration scripts (V001-V004) |
| - | - | Rollback scripts (U001-U002, U004) |
| - | - | Comprehensive documentation |

---

**Last Updated**: 2025-12-22
**Maintained by**: Talos Maind Platform Team
