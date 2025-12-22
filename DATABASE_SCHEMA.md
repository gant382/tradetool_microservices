# CallCard Microservice Database Schema Documentation

**Version**: 2.0
**Last Updated**: 2025-12-22
**Project**: Talos Maind - CallCard Microservice
**Database**: Microsoft SQL Server 2008+
**ORM**: Hibernate 5.6.x with JPA 2.2

---

## Table of Contents

1. [Overview](#overview)
2. [Entity Relationship Diagram](#entity-relationship-diagram)
3. [Core CallCard Tables](#core-callcard-tables)
4. [Supporting Tables](#supporting-tables)
5. [Shared Tables (with gameserver_v3)](#shared-tables-with-gameserver_v3)
6. [Indexes and Constraints](#indexes-and-constraints)
7. [Data Types Reference](#data-types-reference)
8. [Sample Queries](#sample-queries)
9. [Audit & Compliance](#audit--compliance)

---

## Overview

The CallCard microservice manages a multi-tenant card-based system with:

- **Templates**: Reusable card templates with configurable entries
- **Card Instances**: User-specific card assignments
- **User References**: Card distribution and assignment tracking
- **Audit Trail**: Immutable transaction history for compliance
- **Multi-Tenancy**: Isolation via USER_GROUPS (tenant)

### Key Characteristics

| Aspect | Details |
|--------|---------|
| **Primary Key Type** | UNIQUEIDENTIFIER (UUID) for CallCard entities |
| **Tenant Isolation** | USER_GROUP_ID for CallCardTransaction |
| **Audit Support** | CALL_CARD_TRANSACTION_HISTORY table with JSON columns |
| **Soft Deletes** | Supported via ACTIVE flag (boolean) |
| **Temporal Data** | Start/End date ranges for validity periods |
| **Relationships** | Cascading references without enforced FK constraints |

---

## Entity Relationship Diagram

### ASCII ER Diagram

```
┌─────────────────────┐
│   USER_GROUPS       │
│  (Tenant Isolation) │
├─────────────────────┤
│ GROUP_ID (PK)       │
│ GROUP_NAME          │
│ DATE_ADDED          │
│ IS_ACTIVE           │
└──────────┬──────────┘
           │
           │ 1:N
           │
           ▼
┌─────────────────────────────────────┐      ┌──────────────────────┐
│   GAME_TYPE                         │◄─┐   │   ITEM_TYPES         │
├─────────────────────────────────────┤  │   ├──────────────────────┤
│ GAME_TYPE_ID (PK)                   │  │   │ ITEM_TYPE_ID (PK)    │
│ NAME                                │  │   │ ITEM_TYPE_DESC       │
│ VERSION                             │  │   └──────────────────────┘
│ MIN/MAX/DEFAULT_PARTICIPANTS        │  │
│ BOT_APPLICABLE                      │  │
└────────────┬────────────────────────┘  │
             │                            │
             │ 1:N                        │
             │                            │
             ▼                            │
┌──────────────────────────────┐        │
│ CALL_CARD_TEMPLATE           │        │
├──────────────────────────────┤        │
│ CALL_CARD_TEMPLATE_ID (PK)   │        │
│ USER_GROUP_ID (FK)           │        │
│ GAME_TYPE_ID (FK) ───────────┘        │
│ START_DATE                   │        │
│ END_DATE                     │        │
│ ACTIVE                       │        │
│ NAME                         │        │
└──────────┬───────────────────┘        │
           │                            │
    ┌──────┴──────┐                    │
    │             │                    │
    │ 1:N         │ 1:N                │
    │             │                    │
    ▼             ▼                    │
┌─────────────────────────────────┐   │
│ CALL_CARD_TEMPLATE_ENTRY        │   │
├─────────────────────────────────┤   │
│ ID (PK)                         │   │
│ CALL_CARD_TEMPLATE_ID (FK)      │   │
│ ITEM_TYPE_ID (FK) ──────────────┼───┘
│ ITEM_ID                         │
│ PROPERTIES (nvarchar(max))      │
│ ORDERING                        │
└─────────────────────────────────┘


         ┌─────────────────────────────────────┐
         │  CALL_CARD_TEMPLATE_POS             │
         ├─────────────────────────────────────┤
         │ CALL_CARD_TEMPLATE_POS_ID (PK)      │
         │ CALL_CARD_TEMPLATE_ID (FK)          │
         │ REF_USER_ID (FK) ──────────┐        │
         │ MANDATORY                  │        │
         │ ACTIVE                     │        │
         │ ORDERING                   │        │
         │ GROUP_ID                   │        │
         └─────────────────────────────────────┘
                                       │
                                       │
         ┌─────────────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│      USERS                   │
├──────────────────────────────┤
│ USER_ID (PK)                 │
│ LOGIN_NAME                   │
│ USERNAME / NICKNAME          │
│ EMAIL                        │
│ MOBILE                       │
│ FIRST_NAME / LAST_NAME       │
│ GROUP_ID (FK → USER_GROUPS)  │
│ IS_GUEST                     │
│ IS_BOT                       │
│ CREATED_ON / LAST_UPDATED    │
└────────────┬─────────────────┘
             │
      ┌──────┴──────┐
      │ 1:N         │ 1:N
      │             │
      ▼             ▼
┌─────────────────────────────────┐
│ CALL_CARD                       │
├─────────────────────────────────┤
│ CALL_CARD_ID (PK)               │
│ CALL_CARD_TEMPLATE_ID (FK)      │
│ USER_ID (FK → USERS)            │
│ START_DATE                      │
│ END_DATE                        │
│ ACTIVE                          │
│ COMMENTS                        │
│ INTERNAL_REF_NO                 │
│ LAST_UPDATED                    │
└──────────┬──────────────────────┘
           │
           │ 1:N
           │
           ▼
┌──────────────────────────────────────┐
│ CALL_CARD_REFUSER                    │
├──────────────────────────────────────┤
│ CALL_CARD_REFUSER_ID (PK)            │
│ CALL_CARD_ID (FK)                    │
│ REF_USER_ID (FK → USERS)             │
│ SOURCE_USER_ID (FK → USERS)          │
│ START_DATE                           │
│ END_DATE                             │
│ ACTIVE                               │
│ COMMENT                              │
│ STATUS                               │
│ REF_NO                               │
│ INTERNAL_REF_NO                      │
│ LAST_UPDATED                         │
└──────────┬───────────────────────────┘
           │
           │ 1:N
           │
           ▼
┌──────────────────────────────────────┐
│ CALL_CARD_REFUSER_INDEX              │
├──────────────────────────────────────┤
│ CALL_CARD_REFUSER_INDEX_ID (PK)      │
│ CALL_CARD_REFUSER_ID (FK)            │
│ ITEM_ID                              │
│ ITEM_TYPE_ID (FK)                    │
│ PROPERTY_ID                          │
│ PROPERTY_VALUE                       │
│ STATUS                               │
│ SUBMIT_DATE                          │
│ AMOUNT (decimal(7,2))                │
│ TYPE                                 │
└──────────────────────────────────────┘


┌──────────────────────────────────────────────┐
│ CALL_CARD_TRANSACTION_HISTORY                │
│ (Immutable Audit Trail - Multi-Tenant)       │
├──────────────────────────────────────────────┤
│ TRANSACTION_ID (PK)                          │
│ CALL_CARD_ID                                 │
│ TRANSACTION_TYPE (ENUM)                      │
│ USER_ID (FK → USERS)                         │
│ USER_GROUP_ID (FK → USER_GROUPS)             │
│ TIMESTAMP                                    │
│ OLD_VALUE (nvarchar(max) - JSON)             │
│ NEW_VALUE (nvarchar(max) - JSON)             │
│ DESCRIPTION                                  │
│ IP_ADDRESS                                   │
│ SESSION_ID                                   │
│ METADATA (nvarchar(max) - JSON)              │
├──────────────────────────────────────────────┤
│ Indexes:                                     │
│   idx_transaction_callcard (CALL_CARD_ID)    │
│   idx_transaction_user (USER_ID)             │
│   idx_transaction_usergroup (USER_GROUP_ID)  │
│   idx_transaction_type (TRANSACTION_TYPE)    │
│   idx_transaction_timestamp (TIMESTAMP)      │
│   idx_transaction_session (SESSION_ID)       │
└──────────────────────────────────────────────┘
```

---

## Core CallCard Tables

### 1. CALL_CARD_TEMPLATE

**Purpose**: Defines reusable card templates with game type and user group scoping.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| CALL_CARD_TEMPLATE_ID | UNIQUEIDENTIFIER | NO | PK | Unique template identifier (UUID) |
| USER_GROUP_ID | INT | NO | FK → USER_GROUPS.GROUP_ID | Multi-tenant isolation (scoped to tenant) |
| GAME_TYPE_ID | UNIQUEIDENTIFIER | NO | FK → GAME_TYPE.GAME_TYPE_ID | Associated game type |
| START_DATE | DATETIME | NO | - | Template validity period start |
| END_DATE | DATETIME | NO | - | Template validity period end |
| ACTIVE | BIT | NO | - | Active/inactive flag (soft delete) |
| NAME | NVARCHAR(MAX) | NO | - | Template human-readable name |

**Relationships**:
- 1:N → CALL_CARD_TEMPLATE_ENTRY (template items)
- 1:N → CALL_CARD_TEMPLATE_POS (position mapping)
- 1:N → CALL_CARD (card instances)

**Key Queries**:
```sql
-- Find active templates for a game type
SELECT * FROM CALL_CARD_TEMPLATE
WHERE GAME_TYPE_ID = @gameTypeId
  AND ACTIVE = 1
  AND GETDATE() BETWEEN START_DATE AND END_DATE;

-- Find templates for a specific user group
SELECT * FROM CALL_CARD_TEMPLATE
WHERE USER_GROUP_ID = @groupId
  AND ACTIVE = 1;
```

---

### 2. CALL_CARD_TEMPLATE_ENTRY

**Purpose**: Defines individual items within a template configuration.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| ID | UNIQUEIDENTIFIER | NO | PK | Unique entry identifier (UUID) |
| CALL_CARD_TEMPLATE_ID | UNIQUEIDENTIFIER | NO | FK | Parent template reference |
| ITEM_ID | UNIQUEIDENTIFIER | YES | - | Reference to item in external system |
| ITEM_TYPE_ID | INT | NO | FK → ITEM_TYPES.ITEM_TYPE_ID | Item classification |
| PROPERTIES | NVARCHAR(MAX) | NO | - | JSON configuration properties |
| ORDERING | INT | NO | - | Display order in template (ascending) |

**Relationships**:
- N:1 → CALL_CARD_TEMPLATE (parent)
- N:1 → ITEM_TYPES (classification)

**Index Strategy**:
- Implicit index on CALL_CARD_TEMPLATE_ID (foreign key)
- ORDERING should be indexed for UI rendering

---

### 3. CALL_CARD_TEMPLATE_POS

**Purpose**: Maps positions (users) to template - defines mandatory roles and ordering.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| CALL_CARD_TEMPLATE_POS_ID | UNIQUEIDENTIFIER | NO | PK | Unique identifier (UUID) |
| CALL_CARD_TEMPLATE_ID | UNIQUEIDENTIFIER | NO | FK | Parent template |
| REF_USER_ID | UNIQUEIDENTIFIER | NO | FK → USERS.USER_ID | User/role reference |
| MANDATORY | BIT | NO | - | Whether this position is required |
| ACTIVE | BIT | NO | - | Whether this position is active |
| ORDERING | INT | NO | - | Position order in template |
| GROUP_ID | INT | YES | - | Optional grouping (e.g., signatories) |

**Relationships**:
- N:1 → CALL_CARD_TEMPLATE (parent)
- N:1 → USERS (position/role holder)

**Key Queries**:
```sql
-- Find mandatory positions for template (ordered)
SELECT * FROM CALL_CARD_TEMPLATE_POS
WHERE CALL_CARD_TEMPLATE_ID = @templateId
  AND MANDATORY = 1
  AND ACTIVE = 1
ORDER BY GROUP_ID DESC, ORDERING ASC;
```

---

### 4. CALL_CARD

**Purpose**: Instance of a card assigned to a specific user with validity dates.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| CALL_CARD_ID | UNIQUEIDENTIFIER | NO | PK | Unique card instance identifier (UUID) |
| CALL_CARD_TEMPLATE_ID | UNIQUEIDENTIFIER | NO | FK | Template this card is based on |
| USER_ID | UNIQUEIDENTIFIER | NO | FK → USERS.USER_ID | Primary card holder |
| START_DATE | DATETIME | NO | - | Card activation date |
| END_DATE | DATETIME | YES | - | Card expiration date (nullable = no expiry) |
| ACTIVE | BIT | NO | - | Active/inactive status (soft delete) |
| COMMENTS | NVARCHAR(100) | YES | - | Administrative notes |
| INTERNAL_REF_NO | NVARCHAR(100) | YES | - | Custom reference number |
| LAST_UPDATED | DATETIME | YES | - | Last modification timestamp |

**Relationships**:
- N:1 → CALL_CARD_TEMPLATE (template)
- N:1 → USERS (primary holder)
- 1:N → CALL_CARD_REFUSER (distributed recipients)
- 1:N → CALL_CARD_TRANSACTION_HISTORY (audit trail)

**Named Queries**:

```sql
-- Filter by user and valid end date
SELECT c FROM CallCard c
WHERE c.userId IN (:userIds)
  AND c.endDate > :endDate;

-- Lookup by user and internal reference
SELECT c FROM CallCard c
WHERE c.userId.userId = :userId
  AND c.internalRefNo = :internalRefNo;
```

**Index Strategy**:
```sql
CREATE INDEX idx_callcard_user ON CALL_CARD(USER_ID);
CREATE INDEX idx_callcard_template ON CALL_CARD(CALL_CARD_TEMPLATE_ID);
CREATE INDEX idx_callcard_validity ON CALL_CARD(START_DATE, END_DATE)
  WHERE ACTIVE = 1;
```

---

### 5. CALL_CARD_REFUSER

**Purpose**: Tracks card distribution - references to recipients and metadata.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| CALL_CARD_REFUSER_ID | UNIQUEIDENTIFIER | NO | PK | Unique reference identifier (UUID) |
| CALL_CARD_ID | UNIQUEIDENTIFIER | NO | FK | Parent card instance |
| REF_USER_ID | UNIQUEIDENTIFIER | NO | FK → USERS.USER_ID | Recipient/referenced user |
| SOURCE_USER_ID | UNIQUEIDENTIFIER | YES | FK → USERS.USER_ID | Issuer/source user (nullable) |
| START_DATE | DATETIME | YES | - | Reference validity start |
| END_DATE | DATETIME | YES | - | Reference validity end |
| ACTIVE | BIT | NO | - | Active/inactive status |
| COMMENT | NVARCHAR(100) | YES | - | Notes about this reference |
| STATUS | INT | YES | - | Status code (vendor-specific) |
| REF_NO | NVARCHAR(100) | YES | - | Reference number |
| INTERNAL_REF_NO | NVARCHAR(100) | YES | - | Internal tracking number |
| LAST_UPDATED | DATETIME | YES | - | Last modification timestamp |

**Relationships**:
- N:1 → CALL_CARD (parent card)
- N:1 → USERS (recipient - REF_USER_ID)
- N:1 → USERS (issuer - SOURCE_USER_ID, optional)
- 1:N → CALL_CARD_REFUSER_INDEX (indexed items)

**Legacy Alias Methods**:
```java
getIssuerUserId()    // → sourceUserId.userId
getRecipientUserId() // → refUserId.userId
getDateCreated()     // → startDate
getDateUpdated()     // → lastUpdated
getItems()           // → CallCardRefUserIndexes
```

**Named Queries**:

```sql
-- Find all references for a card
SELECT u FROM CallCardRefUser u
WHERE u.callCardId.callCardId = :callCardId;

-- Find references by card and end date
SELECT u FROM CallCardRefUser u
WHERE u.callCardId IN (:cardIds)
  AND u.endDate > :date;

-- Lookup by card and recipient
SELECT u FROM CallCardRefUser u
WHERE u.callCardId.callCardId = :cardId
  AND u.refUserId.userId = :userId;

-- Find by internal reference
SELECT u FROM CallCardRefUser u
WHERE u.callCardId.callCardId = :cardId
  AND u.internalRefNo = :refNo;
```

---

### 6. CALL_CARD_REFUSER_INDEX

**Purpose**: Item-level details for each card reference (properties, amounts, etc.).

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| CALL_CARD_REFUSER_INDEX_ID | UNIQUEIDENTIFIER | NO | PK | Unique index identifier (UUID) |
| CALL_CARD_REFUSER_ID | UNIQUEIDENTIFIER | NO | FK | Parent reference |
| ITEM_ID | UNIQUEIDENTIFIER | YES | - | Referenced item identifier |
| ITEM_TYPE_ID | INT | NO | FK → ITEM_TYPES.ITEM_TYPE_ID | Item type classification |
| PROPERTY_ID | NVARCHAR(MAX) | NO | - | Property identifier (JSON key) |
| PROPERTY_VALUE | NVARCHAR(MAX) | NO | - | Property value (JSON value) |
| STATUS | INT | NO | - | Item-level status code |
| SUBMIT_DATE | DATETIME | NO | - | When this item was submitted |
| AMOUNT | DECIMAL(7,2) | YES | - | Associated amount (e.g., credit) |
| TYPE | INT | NO | - | Item type classification |

**Relationships**:
- N:1 → CALL_CARD_REFUSER (parent reference)
- N:1 → ITEM_TYPES (classification)

**SQL Result Set Mapping**:
Entity result mapping defined for raw SQL queries (RefUserIndexMapping)

**Named Queries**:

```sql
-- Get all items for a reference
SELECT u FROM CallCardRefUserIndex u
WHERE u.callCardRefUserId.callCardRefUserId = :refUserId;

-- Get items for multiple references (batch)
SELECT u FROM CallCardRefUserIndex u
WHERE u.callCardRefUserId.callCardRefUserId IN (:refUserIds);

-- Find by submission date range
SELECT u FROM CallCardRefUserIndex u
WHERE u.callCardRefUserId IN (:refUserIds)
  AND u.submitDate > :date;
```

**Index Strategy**:
```sql
CREATE INDEX idx_refuser_index_callcard
  ON CALL_CARD_REFUSER_INDEX(CALL_CARD_REFUSER_ID);
CREATE INDEX idx_refuser_index_submit_date
  ON CALL_CARD_REFUSER_INDEX(SUBMIT_DATE)
  WHERE STATUS = 1;  -- Active items only
```

---

### 7. CALL_CARD_TEMPLATE_USER

**Purpose**: User-specific template assignments (legacy - minimal in modern schema).

**Table Schema**:
- ID (UNIQUEIDENTIFIER, PK)
- CALL_CARD_TEMPLATE_ID (FK)
- USER_ID (FK → USERS)
- Various metadata fields

---

### 8. CALL_CARD_TEMPLATE_USER_REFERENCES

**Purpose**: User reference configurations for templates (legacy).

---

## Supporting Tables

### 9. CALL_CARD_TRANSACTION_HISTORY

**Purpose**: Immutable audit trail for compliance and forensics. Records all modifications to CallCard entities.

**Critical Characteristics**:
- **Immutable**: No UPDATE or DELETE operations allowed
- **Multi-Tenant Isolation**: USER_GROUP_ID enforcement for tenant data segregation
- **Complete Audit Trail**: Before/after states in JSON format
- **Session Tracking**: SESSION_ID for correlating related changes
- **Network Audit**: IP_ADDRESS for security analysis

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| TRANSACTION_ID | UNIQUEIDENTIFIER | NO | PK | Unique transaction identifier (UUID) |
| CALL_CARD_ID | UNIQUEIDENTIFIER | NO | Index | Card being modified (FK-like, not enforced) |
| TRANSACTION_TYPE | NVARCHAR(50) | NO | Index | Transaction type (enum: CREATE, UPDATE, DELETE, etc.) |
| USER_ID | UNIQUEIDENTIFIER | NO | Index + FK | User performing the action |
| USER_GROUP_ID | INT | NO | Index + FK | Tenant isolation key |
| TIMESTAMP | DATETIME | NO | Index | When transaction occurred |
| OLD_VALUE | NVARCHAR(MAX) | YES | LOB | Before state (JSON, NULL for CREATE) |
| NEW_VALUE | NVARCHAR(MAX) | YES | LOB | After state (JSON, NULL for DELETE) |
| DESCRIPTION | NVARCHAR(500) | YES | - | Human-readable change description |
| IP_ADDRESS | NVARCHAR(45) | YES | - | Request origin (IPv4 or IPv6) |
| SESSION_ID | NVARCHAR(100) | YES | Index | Session correlation ID |
| METADATA | NVARCHAR(MAX) | YES | LOB | Flexible metadata (JSON format) |

**Indexes**:

```sql
CREATE INDEX idx_transaction_callcard
  ON CALL_CARD_TRANSACTION_HISTORY(CALL_CARD_ID);

CREATE INDEX idx_transaction_user
  ON CALL_CARD_TRANSACTION_HISTORY(USER_ID);

CREATE INDEX idx_transaction_usergroup
  ON CALL_CARD_TRANSACTION_HISTORY(USER_GROUP_ID);

CREATE INDEX idx_transaction_type
  ON CALL_CARD_TRANSACTION_HISTORY(TRANSACTION_TYPE);

CREATE INDEX idx_transaction_timestamp
  ON CALL_CARD_TRANSACTION_HISTORY(TIMESTAMP)
  WHERE TIMESTAMP >= DATEADD(MONTH, -12, GETDATE());  -- Last 12 months

CREATE INDEX idx_transaction_session
  ON CALL_CARD_TRANSACTION_HISTORY(SESSION_ID)
  WHERE SESSION_ID IS NOT NULL;
```

**Transaction Types** (Enum):

```
CREATE        - CallCard Created
UPDATE        - CallCard Updated (general modification)
DELETE        - CallCard Deleted or Deactivated
ASSIGN        - User Assigned to CallCard
UNASSIGN      - User Unassigned from CallCard
TEMPLATE_CHANGE - Template Modified/Replaced
STATUS_CHANGE - Active Status Changed
DATE_CHANGE   - Start/End Date Modified
COMMENT_CHANGE - Comments Updated
REFERENCE_CHANGE - Internal Reference Number Modified
```

**Named Queries**:

```sql
-- Audit trail for a specific card
SELECT t FROM CallCardTransaction t
WHERE t.callCardId = :callCardId
  AND t.userGroupId = :userGroupId
ORDER BY t.timestamp DESC;

-- User activity report
SELECT t FROM CallCardTransaction t
WHERE t.userId.userId = :userId
  AND t.userGroupId = :userGroupId
  AND t.timestamp BETWEEN :dateFrom AND :dateTo
ORDER BY t.timestamp DESC;

-- Transaction type analysis
SELECT t FROM CallCardTransaction t
WHERE t.transactionType = :type
  AND t.userGroupId = :userGroupId
  AND t.timestamp BETWEEN :dateFrom AND :dateTo
ORDER BY t.timestamp DESC;

-- Multi-tenant audit
SELECT t FROM CallCardTransaction t
WHERE t.userGroupId = :userGroupId
  AND t.timestamp BETWEEN :dateFrom AND :dateTo
ORDER BY t.timestamp DESC;

-- Count transactions for card
SELECT COUNT(t) FROM CallCardTransaction t
WHERE t.callCardId = :callCardId
  AND t.userGroupId = :userGroupId;
```

**JSON Structure Examples**:

Old/New Value (CallCard CREATE):
```json
{
  "callCardId": "550e8400-e29b-41d4-a716-446655440000",
  "callCardTemplateId": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440002",
  "startDate": "2025-12-22T00:00:00Z",
  "endDate": "2026-12-22T00:00:00Z",
  "active": true,
  "comments": "Issued at sign-up",
  "internalRefNo": "CC-2025-12345"
}
```

Metadata (context information):
```json
{
  "source": "api",
  "client_version": "2.1.0",
  "environment": "production",
  "request_id": "req-123456",
  "additional_context": "bulk_import_batch_5"
}
```

---

## Shared Tables (with gameserver_v3)

The CallCard microservice shares several tables with the legacy gameserver_v3 system. These are read-only or minimally updated:

### USERS (Shared)

**Purpose**: Central user management across all systems.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| USER_ID | UNIQUEIDENTIFIER | NO | PK | Unique user identifier (UUID) |
| LOGIN_NAME | NVARCHAR(255) | NO | UNIQUE | Login credential (username) |
| USERNAME | NVARCHAR(255) | YES | - | Display name (nickname) |
| GROUP_ID | UNIQUEIDENTIFIER | NO | FK → USER_GROUPS | User's tenant/organization |
| EXTERNAL_SYSTEM_ID | NVARCHAR(100) | YES | - | Integration ID from other systems |
| EMAIL | NVARCHAR(250) | YES | - | Email address |
| MOBILE | NVARCHAR(100) | YES | - | Phone number |
| FIRST_NAME | NVARCHAR(100) | YES | - | First name |
| LAST_NAME | NVARCHAR(100) | YES | - | Last name |
| IS_GUEST | BIT | NO | - | Guest account flag |
| IS_BOT | BIT | NO | DEFAULT 0 | Bot account flag |
| CREATED_ON | DATETIME | YES | - | Account creation date |
| LAST_UPDATED | DATETIME | YES | - | Last update date |

**Named Queries**:
```sql
SELECT u FROM Users u;
SELECT u FROM Users u WHERE u.userId = :userId;
SELECT u FROM Users u WHERE u.userId IN (:userIds);
SELECT u FROM Users u WHERE u.username = :username;
```

**Access Pattern**: Read-only in CallCard context

---

### USER_GROUPS (Shared)

**Purpose**: Tenant/organization grouping with feature flags.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| GROUP_ID | UNIQUEIDENTIFIER(36) | NO | PK | Unique group identifier (UUID) |
| GROUP_NAME | VARCHAR(50) | NO | - | Organization name |
| DATE_ADDED | DATETIME | NO | - | Group creation date |
| IS_ACTIVE | TINYINT | NO | - | Active status (boolean) |
| IS_FACEBOOK | BIT | NO | - | Facebook integration enabled |
| REQUIRED_REGISTRATION_FIELDS | NVARCHAR(200) | YES | - | Comma-separated field names |
| REGISTRATION_WITH_ACTIVATION | BIT | NO | - | Require email verification |
| SESSION_ELEVATION_REQUIRED | BIT | NO | - | Require session elevation |
| IMPORT_EXTERNAL_SYSTEM_USER_DETAILS_ON_LOGIN | BIT | NO | - | Import user data from SSO |
| CHECK_PASSWORD_COMPLEXITY | BIT | NO | - | Enforce password policy |
| MAX_FAILED_LOGINS | INT | YES | - | Lockout threshold |
| REQUIRES_EMAIL_VERIFICATION | BIT | NO | - | Email verification required |
| REQUIRES_MOBILE_VERIFICATION | BIT | NO | - | Mobile verification required |
| UNIQUE_USER_BY_EMAIL | BIT | NO | - | One account per email |

**Caching**: READ_ONLY cache strategy (Immutable)

**Named Queries**:
```sql
SELECT ug FROM UserGroups ug;
SELECT ug FROM UserGroups ug WHERE ug.groupId = :groupId;
SELECT ug FROM UserGroups ug WHERE ug.groupId IN (:groupIds);
SELECT ug FROM UserGroups ug
WHERE ug.groupId = :groupId AND ug.isActive = true;
```

**Access Pattern**: Read-only reference data

---

### GAME_TYPE (Shared)

**Purpose**: Game classifications and participant constraints.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| GAME_TYPE_ID | UNIQUEIDENTIFIER | NO | PK | Unique game type identifier (UUID) |
| NAME | NVARCHAR(255) | NO | - | Human-readable name |
| DESCRIPTION | NVARCHAR(50) | YES | - | Short description |
| VERSION | NVARCHAR(50) | YES | - | Version string |
| MIN_PARTICIPANTS | INT | NO | - | Minimum participants |
| MAX_PARTICIPANTS | INT | NO | - | Maximum participants |
| DEFAULT_PARTICIPANTS | INT | NO | - | Default participant count |
| BOT_APPLICABLE | BIT | NO | - | Bots can participate |

**Caching**: READ_ONLY cache strategy (Immutable + Cacheable)

**Named Queries**:
```sql
SELECT g FROM GameType g;
SELECT g FROM GameType g WHERE g.gameTypeId IN (:ids);
SELECT g FROM GameType g WHERE g.gameTypeId = :id;
```

**Access Pattern**: Read-only reference data

---

### ITEM_TYPES (Shared)

**Purpose**: Classification system for template and index items.

**Table Schema**:

| Column Name | Data Type | Nullable | Constraints | Purpose |
|-------------|-----------|----------|-------------|---------|
| ITEM_TYPE_ID | INT | NO | PK | Numeric type identifier |
| ITEM_TYPE_DESC | NVARCHAR(255) | NO | - | Type description/name |

**Caching**: READ_ONLY cache strategy (Immutable)

**Named Queries**:
```sql
SELECT i FROM ItemTypes i WHERE i.itemTypeDesc = :description;
SELECT i FROM ItemTypes i;
```

**Access Pattern**: Read-only reference data

---

## Indexes and Constraints

### Primary Key Indexes

```sql
-- CallCard core tables
PRIMARY KEY (CALL_CARD_ID)                    ON CALL_CARD
PRIMARY KEY (CALL_CARD_TEMPLATE_ID)           ON CALL_CARD_TEMPLATE
PRIMARY KEY (ID)                              ON CALL_CARD_TEMPLATE_ENTRY
PRIMARY KEY (CALL_CARD_TEMPLATE_POS_ID)       ON CALL_CARD_TEMPLATE_POS
PRIMARY KEY (CALL_CARD_REFUSER_ID)            ON CALL_CARD_REFUSER
PRIMARY KEY (CALL_CARD_REFUSER_INDEX_ID)      ON CALL_CARD_REFUSER_INDEX
PRIMARY KEY (TRANSACTION_ID)                  ON CALL_CARD_TRANSACTION_HISTORY
```

### Foreign Key Indexes

```sql
-- Implicit indexes on FK columns (automatically created)
FK: CALL_CARD.USER_ID → USERS.USER_ID
FK: CALL_CARD.CALL_CARD_TEMPLATE_ID → CALL_CARD_TEMPLATE.CALL_CARD_TEMPLATE_ID
FK: CALL_CARD_TEMPLATE.USER_GROUP_ID → USER_GROUPS.GROUP_ID
FK: CALL_CARD_TEMPLATE.GAME_TYPE_ID → GAME_TYPE.GAME_TYPE_ID
FK: CALL_CARD_TEMPLATE_ENTRY.CALL_CARD_TEMPLATE_ID → CALL_CARD_TEMPLATE.CALL_CARD_TEMPLATE_ID
FK: CALL_CARD_TEMPLATE_ENTRY.ITEM_TYPE_ID → ITEM_TYPES.ITEM_TYPE_ID
FK: CALL_CARD_TEMPLATE_POS.CALL_CARD_TEMPLATE_ID → CALL_CARD_TEMPLATE.CALL_CARD_TEMPLATE_ID
FK: CALL_CARD_TEMPLATE_POS.REF_USER_ID → USERS.USER_ID
FK: CALL_CARD_REFUSER.CALL_CARD_ID → CALL_CARD.CALL_CARD_ID
FK: CALL_CARD_REFUSER.REF_USER_ID → USERS.USER_ID
FK: CALL_CARD_REFUSER.SOURCE_USER_ID → USERS.USER_ID
FK: CALL_CARD_REFUSER_INDEX.CALL_CARD_REFUSER_ID → CALL_CARD_REFUSER.CALL_CARD_REFUSER_ID
FK: CALL_CARD_REFUSER_INDEX.ITEM_TYPE_ID → ITEM_TYPES.ITEM_TYPE_ID
FK: CALL_CARD_TRANSACTION_HISTORY.USER_ID → USERS.USER_ID
FK: CALL_CARD_TRANSACTION_HISTORY.USER_GROUP_ID → USER_GROUPS.GROUP_ID
```

### Explicit Performance Indexes

```sql
-- Transaction history queries (heavy usage)
CREATE INDEX idx_transaction_callcard
  ON CALL_CARD_TRANSACTION_HISTORY(CALL_CARD_ID);

CREATE INDEX idx_transaction_user
  ON CALL_CARD_TRANSACTION_HISTORY(USER_ID);

CREATE INDEX idx_transaction_usergroup
  ON CALL_CARD_TRANSACTION_HISTORY(USER_GROUP_ID);

CREATE INDEX idx_transaction_type
  ON CALL_CARD_TRANSACTION_HISTORY(TRANSACTION_TYPE);

CREATE INDEX idx_transaction_timestamp
  ON CALL_CARD_TRANSACTION_HISTORY(TIMESTAMP)
  INCLUDE (CALL_CARD_ID, USER_ID)
  WHERE TIMESTAMP >= DATEADD(MONTH, -12, GETDATE());

CREATE INDEX idx_transaction_session
  ON CALL_CARD_TRANSACTION_HISTORY(SESSION_ID)
  WHERE SESSION_ID IS NOT NULL;

-- CallCard validity range queries
CREATE INDEX idx_callcard_validity
  ON CALL_CARD(START_DATE, END_DATE)
  WHERE ACTIVE = 1;

-- Card reference queries
CREATE INDEX idx_refuser_callcard
  ON CALL_CARD_REFUSER(CALL_CARD_ID);

CREATE INDEX idx_refuser_user
  ON CALL_CARD_REFUSER(REF_USER_ID);

CREATE INDEX idx_refuser_index_callcard
  ON CALL_CARD_REFUSER_INDEX(CALL_CARD_REFUSER_ID);

-- Template lookups
CREATE INDEX idx_template_group_game
  ON CALL_CARD_TEMPLATE(USER_GROUP_ID, GAME_TYPE_ID)
  WHERE ACTIVE = 1;
```

### Constraints

```sql
-- NOT NULL constraints (enforced at column level)
CONSTRAINT CHECK CALL_CARD.START_DATE <= CALL_CARD.END_DATE
CONSTRAINT CHECK CALL_CARD_TEMPLATE.START_DATE <= CALL_CARD_TEMPLATE.END_DATE

-- Unique constraints (where applicable)
UNIQUE INDEX uix_users_login_group
  ON USERS(LOGIN_NAME, GROUP_ID);

-- Check constraints for enums
CONSTRAINT chk_transaction_type
  CHECK TRANSACTION_TYPE IN ('CREATE', 'UPDATE', 'DELETE', 'ASSIGN',
                             'UNASSIGN', 'TEMPLATE_CHANGE', 'STATUS_CHANGE',
                             'DATE_CHANGE', 'COMMENT_CHANGE', 'REFERENCE_CHANGE');
```

---

## Data Types Reference

### UUID Handling

**UNIQUEIDENTIFIER** - SQL Server native UUID type:
- 16 bytes (128 bits)
- Generated via custom UUIDGenerator utility
- Used for: CALL_CARD_ID, CALL_CARD_TEMPLATE_ID, TRANSACTION_ID, etc.

```java
// Hibernate custom generator
@GeneratedValue(generator = "system-uuid")
@GenericGenerator(name = "system-uuid",
  strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
private String callCardId;
```

### Temporal Data Types

| Type | Size | Range | Usage |
|------|------|-------|-------|
| DATETIME | 8 bytes | 1753-01-01 to 9999-12-31 | Transaction times, card validity |
| DATETIME2 | 6-8 bytes | 0001-01-01 to 9999-12-31 | Not used (DATETIME sufficient) |

**Storage**: Mapped to `java.util.Date` with `@Temporal(TemporalType.TIMESTAMP)`

### Numeric Data Types

| Type | Storage | Range | Usage |
|------|---------|-------|-------|
| INT | 4 bytes | -2^31 to 2^31-1 | Status codes, types, amounts (whole) |
| DECIMAL(7,2) | 5 bytes | -99999.99 to 99999.99 | Financial amounts |
| BIT | 1 byte | 0 or 1 | Boolean flags (ACTIVE, MANDATORY) |
| TINYINT | 1 byte | 0-255 | Counters, small codes |

### String Data Types

| Type | Max Size | Usage |
|------|----------|-------|
| NVARCHAR(50) | 100 bytes | Names, enums, statuses |
| NVARCHAR(100) | 200 bytes | Short text (comments, ref numbers) |
| NVARCHAR(255) | 510 bytes | Usernames, emails |
| NVARCHAR(500) | 1000 bytes | Descriptions |
| NVARCHAR(MAX) | Up to 2GB | JSON properties, audit values |

**Character Set**: Unicode (NVARCHAR) for international support

### Large Object Types

| Type | Max Size | Usage |
|------|----------|-------|
| NVARCHAR(MAX) | 2GB | JSON serialization (properties, audit trail) |
| Mapped in Hibernate | - | @Lob annotation on OLD_VALUE, NEW_VALUE, METADATA |

---

## Sample Queries

### 1. Get Active Cards for User with Valid Dates

```sql
SELECT
    cc.CALL_CARD_ID,
    cc.INTERNAL_REF_NO,
    cct.NAME as TEMPLATE_NAME,
    gt.NAME as GAME_TYPE,
    cc.START_DATE,
    cc.END_DATE,
    cc.COMMENTS
FROM
    CALL_CARD cc
    INNER JOIN CALL_CARD_TEMPLATE cct ON cc.CALL_CARD_TEMPLATE_ID = cct.CALL_CARD_TEMPLATE_ID
    INNER JOIN GAME_TYPE gt ON cct.GAME_TYPE_ID = gt.GAME_TYPE_ID
WHERE
    cc.USER_ID = @userId
    AND cc.ACTIVE = 1
    AND GETDATE() >= cc.START_DATE
    AND (cc.END_DATE IS NULL OR GETDATE() <= cc.END_DATE)
ORDER BY
    cc.START_DATE DESC;
```

### 2. Get All Recipients of a Card

```sql
SELECT
    cru.CALL_CARD_REFUSER_ID,
    u1.LOGIN_NAME as RECIPIENT,
    u2.LOGIN_NAME as ISSUER,
    cru.REF_NO,
    cru.START_DATE,
    cru.END_DATE,
    cru.STATUS,
    COUNT(crui.CALL_CARD_REFUSER_INDEX_ID) as ITEM_COUNT
FROM
    CALL_CARD_REFUSER cru
    INNER JOIN USERS u1 ON cru.REF_USER_ID = u1.USER_ID
    LEFT JOIN USERS u2 ON cru.SOURCE_USER_ID = u2.USER_ID
    LEFT JOIN CALL_CARD_REFUSER_INDEX crui ON cru.CALL_CARD_REFUSER_ID = crui.CALL_CARD_REFUSER_ID
WHERE
    cru.CALL_CARD_ID = @callCardId
    AND cru.ACTIVE = 1
GROUP BY
    cru.CALL_CARD_REFUSER_ID,
    u1.LOGIN_NAME,
    u2.LOGIN_NAME,
    cru.REF_NO,
    cru.START_DATE,
    cru.END_DATE,
    cru.STATUS
ORDER BY
    cru.START_DATE DESC;
```

### 3. Card Template with All Entries and Positions

```sql
SELECT
    cct.CALL_CARD_TEMPLATE_ID,
    cct.NAME,
    gt.NAME as GAME_TYPE,
    ug.GROUP_NAME,
    cct.START_DATE,
    cct.END_DATE,
    cct.ACTIVE,
    ccte.ORDERING as ENTRY_ORDER,
    ccte.PROPERTIES,
    it1.ITEM_TYPE_DESC,
    cctp.ORDERING as POS_ORDER,
    u.LOGIN_NAME as POSITION_USER,
    cctp.MANDATORY
FROM
    CALL_CARD_TEMPLATE cct
    INNER JOIN GAME_TYPE gt ON cct.GAME_TYPE_ID = gt.GAME_TYPE_ID
    INNER JOIN USER_GROUPS ug ON cct.USER_GROUP_ID = ug.GROUP_ID
    LEFT JOIN CALL_CARD_TEMPLATE_ENTRY ccte ON cct.CALL_CARD_TEMPLATE_ID = ccte.CALL_CARD_TEMPLATE_ID
    LEFT JOIN ITEM_TYPES it1 ON ccte.ITEM_TYPE_ID = it1.ITEM_TYPE_ID
    LEFT JOIN CALL_CARD_TEMPLATE_POS cctp ON cct.CALL_CARD_TEMPLATE_ID = cctp.CALL_CARD_TEMPLATE_ID
    LEFT JOIN USERS u ON cctp.REF_USER_ID = u.USER_ID
WHERE
    cct.CALL_CARD_TEMPLATE_ID = @templateId
ORDER BY
    ccte.ORDERING,
    cctp.ORDERING;
```

### 4. Audit Trail for a Card (Last 30 Days)

```sql
SELECT
    cct.TRANSACTION_ID,
    cct.TRANSACTION_TYPE,
    u.LOGIN_NAME,
    cct.TIMESTAMP,
    cct.DESCRIPTION,
    cct.IP_ADDRESS,
    cct.SESSION_ID,
    cct.OLD_VALUE,
    cct.NEW_VALUE
FROM
    CALL_CARD_TRANSACTION_HISTORY cct
    INNER JOIN USERS u ON cct.USER_ID = u.USER_ID
WHERE
    cct.CALL_CARD_ID = @callCardId
    AND cct.USER_GROUP_ID = @userGroupId
    AND cct.TIMESTAMP >= DATEADD(DAY, -30, GETDATE())
ORDER BY
    cct.TIMESTAMP DESC;
```

### 5. User Activity Report (Date Range)

```sql
SELECT
    u.LOGIN_NAME,
    COUNT(CASE WHEN cct.TRANSACTION_TYPE = 'CREATE' THEN 1 END) as CREATES,
    COUNT(CASE WHEN cct.TRANSACTION_TYPE = 'UPDATE' THEN 1 END) as UPDATES,
    COUNT(CASE WHEN cct.TRANSACTION_TYPE = 'DELETE' THEN 1 END) as DELETES,
    COUNT(CASE WHEN cct.TRANSACTION_TYPE = 'ASSIGN' THEN 1 END) as ASSIGNS,
    COUNT(*) as TOTAL_TRANSACTIONS,
    MIN(cct.TIMESTAMP) as FIRST_ACTION,
    MAX(cct.TIMESTAMP) as LAST_ACTION
FROM
    CALL_CARD_TRANSACTION_HISTORY cct
    INNER JOIN USERS u ON cct.USER_ID = u.USER_ID
WHERE
    cct.USER_GROUP_ID = @userGroupId
    AND cct.TIMESTAMP BETWEEN @dateFrom AND @dateTo
GROUP BY
    u.LOGIN_NAME
ORDER BY
    TOTAL_TRANSACTIONS DESC;
```

### 6. Orphaned Transaction Records (Card Deleted but History Retained)

```sql
SELECT
    cct.CALL_CARD_ID,
    COUNT(*) as TRANSACTION_COUNT,
    MIN(cct.TIMESTAMP) as FIRST_ACTION,
    MAX(cct.TIMESTAMP) as LAST_ACTION
FROM
    CALL_CARD_TRANSACTION_HISTORY cct
WHERE
    cct.CALL_CARD_ID NOT IN (SELECT CALL_CARD_ID FROM CALL_CARD)
    AND cct.USER_GROUP_ID = @userGroupId
GROUP BY
    cct.CALL_CARD_ID
HAVING
    COUNT(*) > 0
ORDER BY
    LAST_ACTION DESC;
```

### 7. Items Submitted for References (Last 7 Days)

```sql
SELECT
    crui.CALL_CARD_REFUSER_INDEX_ID,
    cc.INTERNAL_REF_NO as CARD_REF,
    u.LOGIN_NAME as RECIPIENT,
    it.ITEM_TYPE_DESC,
    crui.PROPERTY_ID,
    crui.PROPERTY_VALUE,
    crui.AMOUNT,
    crui.SUBMIT_DATE,
    crui.STATUS
FROM
    CALL_CARD_REFUSER_INDEX crui
    INNER JOIN CALL_CARD_REFUSER cru ON crui.CALL_CARD_REFUSER_ID = cru.CALL_CARD_REFUSER_ID
    INNER JOIN CALL_CARD cc ON cru.CALL_CARD_ID = cc.CALL_CARD_ID
    INNER JOIN USERS u ON cru.REF_USER_ID = u.USER_ID
    INNER JOIN ITEM_TYPES it ON crui.ITEM_TYPE_ID = it.ITEM_TYPE_ID
WHERE
    crui.SUBMIT_DATE >= DATEADD(DAY, -7, GETDATE())
    AND crui.STATUS = 1
ORDER BY
    crui.SUBMIT_DATE DESC;
```

### 8. Templates by User Group and Game Type

```sql
SELECT
    cct.CALL_CARD_TEMPLATE_ID,
    cct.NAME,
    gt.NAME as GAME_TYPE,
    ug.GROUP_NAME,
    cct.START_DATE,
    cct.END_DATE,
    cct.ACTIVE,
    COUNT(DISTINCT ccte.ID) as ENTRY_COUNT,
    COUNT(DISTINCT cctp.CALL_CARD_TEMPLATE_POS_ID) as POSITION_COUNT,
    COUNT(DISTINCT cc.CALL_CARD_ID) as INSTANCE_COUNT
FROM
    CALL_CARD_TEMPLATE cct
    INNER JOIN GAME_TYPE gt ON cct.GAME_TYPE_ID = gt.GAME_TYPE_ID
    INNER JOIN USER_GROUPS ug ON cct.USER_GROUP_ID = ug.GROUP_ID
    LEFT JOIN CALL_CARD_TEMPLATE_ENTRY ccte ON cct.CALL_CARD_TEMPLATE_ID = ccte.CALL_CARD_TEMPLATE_ID
    LEFT JOIN CALL_CARD_TEMPLATE_POS cctp ON cct.CALL_CARD_TEMPLATE_ID = cctp.CALL_CARD_TEMPLATE_ID
    LEFT JOIN CALL_CARD cc ON cct.CALL_CARD_TEMPLATE_ID = cc.CALL_CARD_TEMPLATE_ID
WHERE
    ug.GROUP_ID = @groupId
GROUP BY
    cct.CALL_CARD_TEMPLATE_ID,
    cct.NAME,
    gt.NAME,
    ug.GROUP_NAME,
    cct.START_DATE,
    cct.END_DATE,
    cct.ACTIVE
ORDER BY
    cct.START_DATE DESC;
```

### 9. Compliance Report: All Changes to Critical Fields

```sql
SELECT
    cct.TRANSACTION_ID,
    cc.INTERNAL_REF_NO,
    cct.TRANSACTION_TYPE,
    u.LOGIN_NAME as MODIFIED_BY,
    cct.TIMESTAMP,
    cct.DESCRIPTION,
    JSON_VALUE(cct.OLD_VALUE, '$.active') as OLD_ACTIVE,
    JSON_VALUE(cct.NEW_VALUE, '$.active') as NEW_ACTIVE,
    JSON_VALUE(cct.OLD_VALUE, '$.endDate') as OLD_END_DATE,
    JSON_VALUE(cct.NEW_VALUE, '$.endDate') as NEW_END_DATE,
    cct.IP_ADDRESS,
    cct.SESSION_ID
FROM
    CALL_CARD_TRANSACTION_HISTORY cct
    INNER JOIN USERS u ON cct.USER_ID = u.USER_ID
    LEFT JOIN CALL_CARD cc ON cct.CALL_CARD_ID = cc.CALL_CARD_ID
WHERE
    cct.USER_GROUP_ID = @userGroupId
    AND cct.TIMESTAMP >= DATEADD(DAY, -90, GETDATE())
    AND cct.TRANSACTION_TYPE IN ('UPDATE', 'DELETE', 'STATUS_CHANGE')
ORDER BY
    cct.TIMESTAMP DESC;
```

### 10. Card Distribution Analysis

```sql
SELECT
    cc.CALL_CARD_ID,
    cc.INTERNAL_REF_NO,
    u_holder.LOGIN_NAME as CARD_HOLDER,
    cct.NAME as TEMPLATE_NAME,
    COUNT(DISTINCT cru.CALL_CARD_REFUSER_ID) as RECIPIENT_COUNT,
    COUNT(DISTINCT crui.CALL_CARD_REFUSER_INDEX_ID) as TOTAL_ITEMS,
    COUNT(DISTINCT CASE WHEN crui.STATUS = 1 THEN crui.CALL_CARD_REFUSER_INDEX_ID END) as SUBMITTED_ITEMS,
    SUM(CASE WHEN crui.AMOUNT IS NOT NULL THEN crui.AMOUNT ELSE 0 END) as TOTAL_VALUE
FROM
    CALL_CARD cc
    INNER JOIN USERS u_holder ON cc.USER_ID = u_holder.USER_ID
    INNER JOIN CALL_CARD_TEMPLATE cct ON cc.CALL_CARD_TEMPLATE_ID = cct.CALL_CARD_TEMPLATE_ID
    LEFT JOIN CALL_CARD_REFUSER cru ON cc.CALL_CARD_ID = cru.CALL_CARD_ID AND cru.ACTIVE = 1
    LEFT JOIN CALL_CARD_REFUSER_INDEX crui ON cru.CALL_CARD_REFUSER_ID = crui.CALL_CARD_REFUSER_ID
WHERE
    cc.ACTIVE = 1
    AND GETDATE() >= cc.START_DATE
    AND (cc.END_DATE IS NULL OR GETDATE() <= cc.END_DATE)
GROUP BY
    cc.CALL_CARD_ID,
    cc.INTERNAL_REF_NO,
    u_holder.LOGIN_NAME,
    cct.NAME
ORDER BY
    RECIPIENT_COUNT DESC;
```

---

## Audit & Compliance

### Immutability Enforcement

The CALL_CARD_TRANSACTION_HISTORY table is **write-once, read-many**:

```java
// Hibernate configuration prevents updates/deletes
@Entity
@Table(name = "CALL_CARD_TRANSACTION_HISTORY", ...)
public class CallCardTransaction {
    // No @Updatable fields - all columns inserted once
    // No cascade DELETE - orphan records retained
}
```

**Database-Level Enforcement** (Optional):

```sql
-- Create trigger to prevent unauthorized modifications
CREATE TRIGGER trig_prevent_transaction_update
ON CALL_CARD_TRANSACTION_HISTORY
INSTEAD OF UPDATE
AS
BEGIN
    RAISERROR('Transaction history is immutable', 16, 1);
    ROLLBACK;
END;

CREATE TRIGGER trig_prevent_transaction_delete
ON CALL_CARD_TRANSACTION_HISTORY
INSTEAD OF DELETE
AS
BEGIN
    RAISERROR('Transaction history cannot be deleted', 16, 1);
    ROLLBACK;
END;
```

### Multi-Tenant Isolation

All audit queries must filter by USER_GROUP_ID:

```sql
-- CORRECT: Tenant-scoped query
SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
WHERE CALL_CARD_ID = @cardId
  AND USER_GROUP_ID = @userGroupId;

-- INCORRECT: Missing tenant isolation
SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
WHERE CALL_CARD_ID = @cardId;
```

### Transaction Type Classification

Each transaction type captures a distinct business event:

| Type | Trigger | Data Captured | Compliance Use |
|------|---------|---------------|-----------------|
| CREATE | Card created | Initial state in NEW_VALUE | Proof of issuance |
| UPDATE | Card modified | Before/after states | Change history |
| DELETE | Card deactivated/deleted | Final state in OLD_VALUE | Deletion audit |
| ASSIGN | User assigned to card | User reference + timestamp | Access control |
| UNASSIGN | User removed from card | Previous holder + timestamp | Access revocation |
| TEMPLATE_CHANGE | Card template modified | Old/new template references | Policy change tracking |
| STATUS_CHANGE | Card activated/inactivated | Active flag toggle | Status history |
| DATE_CHANGE | Validity dates modified | Old/new dates | Period adjustment record |
| COMMENT_CHANGE | Comments updated | Old/new comments | Administrative notes |
| REFERENCE_CHANGE | Internal ref number changed | Old/new reference | Tracking number changes |

### Query Best Practices

1. **Always Include User Group Filter**
   ```sql
   AND cct.USER_GROUP_ID = @currentUserGroupId
   ```

2. **Index on Frequently Queried Columns**
   ```sql
   WHERE TIMESTAMP >= DATEADD(MONTH, -12, GETDATE())
     AND TRANSACTION_TYPE = 'UPDATE'
   ```

3. **Archive Old Records (Optional)**
   ```sql
   -- Move to archive after 5 years
   INSERT INTO CALL_CARD_TRANSACTION_HISTORY_ARCHIVE
   SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
   WHERE TIMESTAMP < DATEADD(YEAR, -5, GETDATE());
   ```

4. **Monitor Transaction Growth**
   ```sql
   SELECT COUNT(*) as TOTAL_TRANSACTIONS,
          MIN(TIMESTAMP) as OLDEST_RECORD,
          MAX(TIMESTAMP) as NEWEST_RECORD,
          DATEDIFF(DAY, MIN(TIMESTAMP), MAX(TIMESTAMP)) as DAYS_SPAN
   FROM CALL_CARD_TRANSACTION_HISTORY;
   ```

---

## Persistence Configuration

### Spring Boot Integration

The schema is managed by Hibernate with Spring Boot auto-configuration:

```xml
<!-- persistence.xml -->
<persistence-unit name="callcard-pu" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <!-- Entities -->
    <class>com.saicon.games.callcard.entity.CallCard</class>
    <class>com.saicon.games.callcard.entity.CallCardTemplate</class>
    <class>com.saicon.games.callcard.entity.CallCardRefUser</class>
    <class>com.saicon.games.callcard.entity.CallCardTransaction</class>
    <!-- ... more entities ... -->

    <properties>
        <property name="hibernate.cache.use_second_level_cache" value="true"/>
        <property name="hibernate.cache.use_query_cache" value="true"/>
        <property name="hibernate.cache.region.factory_class"
                  value="org.hibernate.cache.jcache.JCacheRegionFactory"/>
        <property name="hibernate.generate_statistics" value="true"/>
    </properties>
</persistence-unit>
```

### Application Properties (Spring Boot)

```properties
# Database
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=callcard_db
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.database-platform=org.hibernate.dialect.SQLServer2012Dialect

# Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=\
  org.hibernate.cache.jcache.JCacheRegionFactory

# Connection Pooling (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
```

---

## Change Log

### Version 2.0 (2025-12-22)

- Added comprehensive CALL_CARD_TRANSACTION_HISTORY table with immutable audit trail
- Multi-tenant isolation via USER_GROUP_ID
- Transaction type enumeration with 10 distinct operation types
- JSON-based old_value/new_value/metadata columns for flexible audit storage
- Session tracking and IP address logging
- Named queries for audit trail retrieval
- Performance indexes for common query patterns
- Compliance documentation and sample queries

### Version 1.0 (2016-09-02)

- Initial CallCard entity model
- Core tables: CALL_CARD, CALL_CARD_TEMPLATE, CALL_CARD_REFUSER, etc.
- Template-based card generation
- Position mapping and item classification
- Multi-tenant support via USER_GROUPS

---

## Related Documentation

- **Entity Classes**: `/callcard-entity/src/main/java/com/saicon/games/callcard/entity/`
- **Service Layer**: `/callcard-service/src/main/java/com/saicon/games/callcard/service/`
- **REST API**: `/callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/`
- **Integration Guide**: `MIDDLEWARE_INTEGRATION_INSTRUCTIONS.md`

---

**Document Version**: 2.0
**Last Updated**: 2025-12-22
**Author**: Talos Maind Platform Documentation
**Status**: Active
