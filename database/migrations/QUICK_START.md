# CallCard Database Migrations - Quick Start Guide

## 30-Second Setup

### For Spring Boot Application (Recommended)

1. **Ensure Flyway dependency in pom.xml**:
   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
       <version>9.22.3</version>
   </dependency>
   ```

2. **Configure application.yml**:
   ```yaml
   spring:
     flyway:
      locations: classpath:db/migration
      enabled: true
   ```

3. **Place migration files** in `CallCard_Server_WS/src/main/resources/db/migration/`

4. **Run Spring Boot**:
   ```bash
   cd CallCard_Server_WS
   mvn spring-boot:run
   # Migrations run automatically on startup
   ```

### For Existing Database (Manual)

If tables already exist, use Flyway baseline:

```bash
mvn flyway:baseline -Dflyway.baselineVersion=0
```

---

## File Structure

```
database/migrations/
├── README.md                           # Full documentation
├── QUICK_START.md                      # This file
├── V001__initial_schema_verification.sql   # Create tables
├── V002__add_performance_indexes.sql       # Add indexes
├── V004__add_constraints_and_fk.sql        # Add FK + constraints
├── U001__rollback_initial_schema.sql       # Undo V001
├── U002__rollback_performance_indexes.sql  # Undo V002
└── U004__rollback_constraints_and_fk.sql   # Undo V004
```

---

## What Gets Created

### 9 Core Tables

| Table | Purpose | Records |
|-------|---------|---------|
| CALL_CARD_TEMPLATE | Template definitions | ~1-10 |
| CALL_CARD_TEMPLATE_ENTRY | Items in template | ~10-100 |
| CALL_CARD_TEMPLATE_POS | POS configs | ~0-50 |
| CALL_CARD | Individual cards assigned to users | ~1000-10000 |
| CALL_CARD_REFUSER | Users assigned to cards | ~5000-50000 |
| CALL_CARD_REFUSER_INDEX | Items allocated to users | ~10000-100000 |
| CALL_CARD_TEMPLATE_USER | User template customizations | ~100-1000 |
| CALL_CARD_TEMPLATE_USER_REFERENCES | User references in templates | ~100-1000 |
| CALL_CARD_TRANSACTION_HISTORY | Audit trail (all changes) | ~100000+ |

### 26 Performance Indexes

- 3 on CALL_CARD_TEMPLATE
- 2 on CALL_CARD_TEMPLATE_ENTRY
- 5 on CALL_CARD
- 7 on CALL_CARD_REFUSER
- 2 on CALL_CARD_REFUSER_INDEX
- 2 on CALL_CARD_TEMPLATE_USER
- (Additional indexes in V003__create_transaction_history.sql)

### Constraints & Foreign Keys

- 8 Foreign Keys (with CASCADE deletes where appropriate)
- 9 Check Constraints (date ranges, quantities, enum values)

---

## Common Tasks

### Check Migration Status

**Using Flyway**:
```bash
mvn flyway:info
```

**Using SQL**:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
```

### Validate Tables Exist

```sql
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME LIKE 'CALL_CARD%' ORDER BY TABLE_NAME;
```

### Verify Indexes

```sql
SELECT OBJECT_NAME(i.object_id) AS TableName, i.name AS IndexName
FROM sys.indexes i
WHERE OBJECT_NAME(i.object_id) LIKE 'CALL_CARD%'
    AND i.name IS NOT NULL
ORDER BY OBJECT_NAME(i.object_id);
```

### Check Constraints

```sql
SELECT OBJECT_NAME(c.parent_object_id) AS TableName, c.name
FROM sys.check_constraints c
WHERE OBJECT_NAME(c.parent_object_id) LIKE 'CALL_CARD%';
```

---

## Troubleshooting

### "Table already exists"
Solution: Use `flyway:baseline` or modify IF NOT EXISTS checks

### "Cannot find database"
Solution: Check connection string in application.yml:
```yaml
spring.datasource.url: jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3
```

### "Foreign key constraint failed"
Solution: Uncomment FK constraints in V004 ONLY after verifying shared schema tables exist

### Slow migrations
Solution: Disable indexes during migration (handled automatically by IF NOT EXISTS)

---

## Next Steps

1. ✅ Copy migration files to `CallCard_Server_WS/src/main/resources/db/migration/`
2. ✅ Configure Flyway in application.yml
3. ✅ Run Spring Boot application
4. ✅ Verify tables in database
5. ✅ Check CloudCard application logs for success

---

## Reference

**Full Documentation**: See `README.md` for:
- Detailed migration descriptions
- Flyway vs Liquibase setup
- Performance tuning
- Monitoring & maintenance
- Rollback procedures

**Database Connection**:
- Host: localhost
- Database: gameserver_v3
- Driver: SQL Server JDBC

---

**Last Updated**: 2025-12-22
