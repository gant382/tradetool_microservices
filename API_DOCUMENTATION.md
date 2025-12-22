# CallCard Microservice API Documentation

**Version:** 1.0
**Last Updated:** 2025-12-22
**Service:** CallCard Microservice (Talos Maind Platform)
**Protocol:** SOAP (Apache CXF 3.5.x) with REST support
**Compression:** GZIP + FastInfoset binary XML
**Multi-Tenancy:** Yes (userGroupId-based isolation)

---

## Table of Contents

1. [Overview](#overview)
2. [Service Endpoints](#service-endpoints)
3. [WSDL Locations](#wsdl-locations)
4. [Authentication & Authorization](#authentication--authorization)
5. [Request/Response DTOs](#requestresponse-dtos)
6. [API Operations](#api-operations)
7. [Error Handling](#error-handling)
8. [Example Requests/Responses](#example-requestsresponses)
9. [Performance & Optimization](#performance--optimization)
10. [Troubleshooting](#troubleshooting)

---

## Overview

The CallCard Microservice provides a comprehensive SOAP/REST API for managing call cards, tracking statistics, and maintaining transaction history. The service is built on Spring Boot 2.7.x with Apache CXF 3.5.x and supports:

- **Multi-tenant isolation** via userGroupId
- **Bulk operations** with single-query efficiency
- **Transaction auditing** with complete change history
- **Statistical analysis** for usage reporting
- **Mobile optimization** with 60-90% smaller payload sizes
- **Binary compression** via FastInfoset and GZIP

### Key Features

- Create, update, retrieve, and manage call cards
- Simplified API for mobile clients (V2)
- Transaction history with full audit trail
- Statistical reporting on templates and user engagement
- Multi-criteria advanced search capabilities
- Pagination support for large datasets

---

## Service Endpoints

### Base Service URL

```
http://<host>:<port>/callcard-ws/cxf/
```

### SOAP Web Services

Four main SOAP services are exposed via CXF:

| Service | Address | Namespace | WSDL |
|---------|---------|-----------|------|
| **CallCardService** | `/CallCardService` | `http://ws.callCard.saicon.com/` | `?wsdl` |
| **CallCardStatisticsService** | `/CallCardStatisticsService` | `http://ws.callcard.statistics.saicon.com/` | `?wsdl` |
| **CallCardTransactionService** | `/CallCardTransactionService` | `http://ws.callcard.transaction.saicon.com/` | `?wsdl` |
| **SimplifiedCallCardService** | `/SimplifiedCallCardService` | `http://ws.callcard.saicon.com/v2/` | `?wsdl` |

### Complete SOAP Endpoint URLs

```
# CallCardService
http://localhost:8080/callcard-ws/cxf/CallCardService?wsdl

# CallCardStatisticsService
http://localhost:8080/callcard-ws/cxf/CallCardStatisticsService?wsdl

# CallCardTransactionService
http://localhost:8080/callcard-ws/cxf/CallCardTransactionService?wsdl

# SimplifiedCallCardService (Mobile optimized V2)
http://localhost:8080/callcard-ws/cxf/SimplifiedCallCardService?wsdl
```

---

## WSDL Locations

Each service exposes its WSDL at the endpoint URL with `?wsdl` query parameter:

```bash
# Get service WSDL
curl http://localhost:8080/callcard-ws/cxf/CallCardService?wsdl

# Get schema XSD
curl http://localhost:8080/callcard-ws/cxf/CallCardService?xsd=1
```

### Generating Client Code from WSDL

Using Apache CXF wsimport or Maven cxf-codegen-plugin:

```bash
# Generate Java client stubs
wsimport -keep http://localhost:8080/callcard-ws/cxf/CallCardService?wsdl

# Or via Maven pom.xml (cxf-codegen-plugin)
mvn clean generate-sources
```

---

## Authentication & Authorization

### JWT Authentication

All SOAP endpoints require JWT Bearer token authentication in HTTP headers:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Header>
        <Authorization>Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</Authorization>
    </soap:Header>
    <soap:Body>
        <!-- Request payload -->
    </soap:Body>
</soap:Envelope>
```

### Required Claims in JWT

```json
{
    "sub": "user123",
    "userGroupId": 1,
    "role": "ROLE_ADMIN",
    "exp": 1234567890
}
```

### Multi-Tenancy Isolation

**Critical:** All requests must include `userGroupId` parameter:
- Enforced at API level for security
- Prevents cross-tenant data access
- Required for all statistics and transaction queries

### Required User Permissions

| Operation | Required Role |
|-----------|---------------|
| Create CallCard | `ROLE_ADMIN`, `ROLE_MANAGER` |
| Update CallCard | `ROLE_ADMIN`, `ROLE_MANAGER`, Owner |
| Read CallCard | `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_USER` |
| Delete CallCard | `ROLE_ADMIN` |
| View Statistics | `ROLE_ADMIN`, `ROLE_MANAGER` |
| View Transactions | `ROLE_ADMIN` |
| Submit CallCard | `ROLE_ADMIN`, `ROLE_MANAGER` |

---

## Request/Response DTOs

### Core Data Transfer Objects

#### CallCardDTO

Complete call card representation:

```java
{
    "callCardId": "cc-550e8400-e29b-41d4-a716-446655440000",
    "startDate": "2025-12-01T00:00:00Z",
    "endDate": "2025-12-31T23:59:59Z",
    "groupIds": [
        {
            "groupId": "g1",
            "actionItems": [...]
        }
    ],
    "submitted": false,
    "comments": "Q4 2025 call card",
    "lastUpdated": "2025-12-20T15:30:00Z",
    "callCardTemplateId": "template-123",
    "internalRefNo": "REF-2025-001"
}
```

**Fields:**
- `callCardId` (String): Unique identifier (UUID)
- `startDate` (Date): Valid from date (ISO 8601)
- `endDate` (Date): Valid to date (ISO 8601)
- `groupIds` (List): Call card groups with action items
- `submitted` (Boolean): Submission status
- `comments` (String): User comments (optional)
- `lastUpdated` (Date): Last modification timestamp
- `callCardTemplateId` (String): Associated template ID
- `internalRefNo` (String): Internal reference number (optional)

#### SimplifiedCallCardDTO

Lightweight representation for mobile clients:

```java
{
    "callCardId": "cc-550e8400-e29b-41d4-a716-446655440000",
    "dateCreated": "2025-12-01T10:00:00Z",
    "dateUpdated": "2025-12-20T15:30:00Z",
    "refUserIds": [
        {
            "userId": "user-123",
            "userName": "John Doe"
        }
    ],
    "submitted": false,
    "dateUploaded": "2025-12-20T15:35:00Z"
}
```

**Fields:**
- `callCardId` (String): Unique identifier
- `dateCreated` (Date): Creation timestamp
- `dateUpdated` (Date): Last update timestamp
- `refUserIds` (List): Referenced users
- `submitted` (Boolean): Submission status
- `dateUploaded` (Date): Upload timestamp (optional)

#### SimplifiedCallCardV2DTO

Ultra-optimized for mobile (60-90% smaller):

```java
{
    "id": "cc-550e8400-e29b-41d4-a716-446655440000",
    "created": "2025-12-01T10:00:00Z",
    "updated": "2025-12-20T15:30:00Z",
    "users": ["user-123", "user-456"],
    "status": "active",
    "submitted": false
}
```

#### CallCardTransactionDTO

Audit trail entry for each modification:

```java
{
    "transactionId": "txn-123456",
    "callCardId": "cc-550e8400-e29b-41d4-a716-446655440000",
    "transactionType": "UPDATE",
    "userId": 101,
    "userName": "John Doe",
    "userGroupId": 1,
    "timestamp": "2025-12-20T15:30:00Z",
    "oldValue": "{\"submitted\": false}",
    "newValue": "{\"submitted\": true}",
    "description": "User submitted call card",
    "ipAddress": "192.168.1.100",
    "sessionId": "sess-abc123",
    "metadata": "{\"userAgent\": \"Mozilla/5.0\"}"
}
```

**Fields:**
- `transactionId` (String): Unique transaction ID
- `callCardId` (String): Associated call card
- `transactionType` (String): CREATE, UPDATE, DELETE, SUBMIT, etc.
- `userId` (Integer): User who made change
- `userName` (String): User display name
- `userGroupId` (Integer): Tenant ID
- `timestamp` (Date): When change occurred
- `oldValue` (String): JSON of previous state
- `newValue` (String): JSON of new state
- `description` (String): Human-readable description
- `ipAddress` (String): Source IP address
- `sessionId` (String): User session ID
- `metadata` (String): Additional JSON metadata

#### TransactionSearchCriteriaDTO

Advanced search filter object:

```java
{
    "callCardId": "cc-550e8400-e29b-41d4-a716-446655440000",
    "userId": 101,
    "userGroupId": 1,
    "transactionType": "UPDATE",
    "dateFrom": "2025-12-01T00:00:00Z",
    "dateTo": "2025-12-31T23:59:59Z",
    "sessionId": "sess-abc123",
    "ipAddress": "192.168.1.0/24",
    "pageNumber": 0,
    "pageSize": 50,
    "sortBy": "timestamp",
    "sortDirection": "DESC"
}
```

### Response Wrappers

#### WSResponse

Base response for single operations:

```java
{
    "errorCode": "0000",
    "errorNumber": 0,
    "result": "Operation completed successfully",
    "message": "Operation completed successfully",
    "status": "OK"
}
```

**Status Values:** `OK`, `ERROR`, `WARNING`, `PARTIAL`

#### ResponseListCallCard

List response with pagination metadata:

```java
{
    "errorCode": "0000",
    "status": "OK",
    "records": [
        { /* CallCardDTO */ },
        { /* CallCardDTO */ }
    ],
    "totalRecords": 42
}
```

#### TransactionListResponseDTO

Paginated transaction history:

```java
{
    "errorCode": "0000",
    "status": "OK",
    "transactions": [
        { /* CallCardTransactionDTO */ },
        { /* CallCardTransactionDTO */ }
    ],
    "totalRecords": 156,
    "pageNumber": 1,
    "pageSize": 50,
    "totalPages": 4
}
```

#### CallCardBulkResponseDTO

Mobile-optimized bulk response with metrics:

```java
{
    "callCards": [
        { /* SimplifiedCallCardV2DTO */ },
        { /* SimplifiedCallCardV2DTO */ }
    ],
    "totalCount": 250,
    "page": 1,
    "pageSize": 25,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false,
    "errors": [],
    "queryTime": "2025-12-20T15:30:00.123Z",
    "executionTimeMs": 45
}
```

#### ResponseCallCardStats

Statistics summary:

```java
{
    "totalCallCards": 1200,
    "activeCallCards": 950,
    "submittedCallCards": 850,
    "totalUsers": 245,
    "totalTemplates": 18,
    "averageCardsPerUser": 4.9,
    "averageUsersPerCard": 3.2,
    "averageCompletionTime": 3.5,
    "activeUsers": 189,
    "status": "OK"
}
```

---

## API Operations

### 1. CallCardService

Main service for call card management.

#### 1.1 listSimplifiedCallCards

Retrieve simplified call cards with date and range filtering.

**SOAP Operation:** `listSimplifiedCallCards`
**HTTP Method:** POST
**Request Format:** SOAP 1.2 with XML-based parameters

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| callCardUserId | String | No | Filter by specific call card user |
| fromUserId | String | No | Filter from user ID range |
| toUserId | String | No | Filter to user ID range |
| dateFrom | Date | No | Start date (ISO 8601) |
| dateTo | Date | No | End date (ISO 8601) |
| rangeFrom | int | No | Numeric range start |
| rangeTo | int | No | Numeric range end |

**Response:** `ResponseListSimplifiedCallCard`

**Example Request (SOAP):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callCard.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer YOUR_JWT_TOKEN</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:listSimplifiedCallCards>
            <callCardUserId>user-123</callCardUserId>
            <dateFrom>2025-12-01T00:00:00Z</dateFrom>
            <dateTo>2025-12-31T23:59:59Z</dateTo>
        </ws:listSimplifiedCallCards>
    </soapenv:Body>
</soapenv:Envelope>
```

**Example Response:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:listSimplifiedCallCardsResponse xmlns:ns2="http://ws.callCard.saicon.com/">
            <return>
                <errorCode>0000</errorCode>
                <status>OK</status>
                <records>
                    <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                    <dateCreated>2025-12-01T10:00:00Z</dateCreated>
                    <submitted>false</submitted>
                </records>
                <totalRecords>5</totalRecords>
            </return>
        </ns2:listSimplifiedCallCardsResponse>
    </soap:Body>
</soap:Envelope>
```

#### 1.2 getCallCardStatistics

Retrieve statistics for call cards by property and types.

**SOAP Operation:** `getCallCardStatistics`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |
| propertyId | String | No | Property ID filter |
| types | List<Integer> | No | Type IDs to include |
| dateFrom | Date | No | Statistics start date |
| dateTo | Date | No | Statistics end date |

**Response:** `ResponseListItemStatistics`

#### 1.3 submitTransactions

Submit a call card transaction with associated metadata.

**SOAP Operation:** `submitTransactions`
**Idempotency:** Not idempotent (creates new record each call)

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | Submitting user ID |
| userGroupId | String | Yes | Multi-tenant identifier |
| gameTypeId | String | No | Game/application type |
| applicationId | String | No | Application identifier |
| indirectUserId | String | No | Indirect user reference |
| callCardDTO | CallCardDTO | Yes | Call card to submit |

**Response:** `WSResponse` (status OK/ERROR)

**Example Request (SOAP):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callCard.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer YOUR_JWT_TOKEN</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:submitTransactions>
            <userId>user-123</userId>
            <userGroupId>org-456</userGroupId>
            <gameTypeId>game-001</gameTypeId>
            <applicationId>app-789</applicationId>
            <callCardDTO>
                <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                <callCardTemplateId>template-123</callCardTemplateId>
                <submitted>true</submitted>
                <startDate>2025-12-01T00:00:00Z</startDate>
                <endDate>2025-12-31T23:59:59Z</endDate>
            </callCardDTO>
        </ws:submitTransactions>
    </soapenv:Body>
</soapenv:Envelope>
```

**Example Response:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:submitTransactionsResponse xmlns:ns2="http://ws.callCard.saicon.com/">
            <return>
                <errorCode>0000</errorCode>
                <status>OK</status>
                <result>Call card submitted successfully</result>
            </return>
        </ns2:submitTransactionsResponse>
    </soap:Body>
</soap:Envelope>
```

#### 1.4 getPendingCallCard

Retrieve pending (not submitted) call cards.

**SOAP Operation:** `getPendingCallCard`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |
| userGroupId | String | Yes | Multi-tenant identifier |
| gameTypeId | String | Yes | Game type |
| applicationId | String | Yes | Application ID |

**Response:** `ResponseListCallCard`

#### 1.5 getNewOrPendingCallCard

Retrieve new or pending call cards with optional filtering.

**SOAP Operation:** `getNewOrPendingCallCard`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |
| userGroupId | String | Yes | Multi-tenant identifier |
| gameTypeId | String | Yes | Game type |
| applicationId | String | Yes | Application ID |
| callCardId | String | No | Specific call card ID |
| filterProperties | List<String> | No | Property filter list |

**Response:** `ResponseListCallCard`

#### 1.6 getCallCardsFromTemplate

Retrieve call cards created from specific template.

**SOAP Operation:** `getCallCardsFromTemplate`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |
| userGroupId | String | Yes | Multi-tenant identifier |
| gameTypeId | String | Yes | Game type |
| applicationId | String | Yes | Application ID |

**Response:** `ResponseListCallCard`

#### 1.7 listPendingCallCard

List all pending call cards for user/game combination.

**SOAP Operation:** `listPendingCallCard`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |
| userGroupId | String | Yes | Multi-tenant identifier |
| gameTypeId | String | Yes | Game type |

**Response:** `ResponseListCallCard`

#### 1.8 addCallCardRecords

Add multiple call card records in bulk.

**SOAP Operation:** `addCallCardRecords`
**Batch Size:** Recommended max 100 records per request

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Multi-tenant identifier |
| gameTypeId | String | Yes | Game type |
| applicationId | String | Yes | Application ID |
| userId | String | Yes | User ID |
| callCard | List<CallCardDTO> | Yes | Cards to add (max 100) |

**Response:** `ResponseListCallCard`

**Example Request (SOAP):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callCard.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer YOUR_JWT_TOKEN</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:addCallCardRecords>
            <userGroupId>org-456</userGroupId>
            <gameTypeId>game-001</gameTypeId>
            <applicationId>app-789</applicationId>
            <userId>user-123</userId>
            <callCard>
                <callCardId>cc-001</callCardId>
                <callCardTemplateId>template-123</callCardTemplateId>
            </callCard>
            <callCard>
                <callCardId>cc-002</callCardId>
                <callCardTemplateId>template-456</callCardTemplateId>
            </callCard>
        </ws:addCallCardRecords>
    </soapenv:Body>
</soapenv:Envelope>
```

#### 1.9 addOrUpdateSimplifiedCallCard

Create or update a simplified call card (upsert).

**SOAP Operation:** `addOrUpdateSimplifiedCallCard`
**Idempotency:** Yes (safe to retry)

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Multi-tenant identifier |
| gameTypeId | String | Yes | Game type |
| applicationId | String | Yes | Application ID |
| userId | String | Yes | User ID |
| callCard | SimplifiedCallCardDTO | Yes | Call card data |

**Response:** `WSResponse`

---

### 2. CallCardStatisticsService

Service for statistical analysis and reporting.

#### 2.1 getCallCardStatistics

Get overall statistics for organization's call cards.

**SOAP Operation:** `getCallCardStatistics`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Multi-tenant identifier |
| dateFrom | Date | No | Start date for statistics |
| dateTo | Date | No | End date for statistics |

**Response:** `ResponseCallCardStats`

**Example Response:**
```json
{
    "totalCallCards": 1200,
    "activeCallCards": 950,
    "submittedCallCards": 850,
    "totalUsers": 245,
    "totalTemplates": 18,
    "averageCardsPerUser": 4.9,
    "averageUsersPerCard": 3.2,
    "averageCompletionTime": 3.5,
    "activeUsers": 189,
    "status": "OK",
    "errorCode": "0000"
}
```

#### 2.2 getTemplateUsageStats

Get statistics for specific template usage.

**SOAP Operation:** `getTemplateUsageStats`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| templateId | String | Yes | Template ID |
| userGroupId | String | Yes | Multi-tenant identifier |
| dateFrom | Date | No | Start date |
| dateTo | Date | No | End date |

**Response:** `ResponseListTemplateUsage`

#### 2.3 getUserEngagementStats

Get engagement metrics for specific user.

**SOAP Operation:** `getUserEngagementStats`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |
| userGroupId | String | Yes | Multi-tenant identifier |
| dateFrom | Date | No | Start date |
| dateTo | Date | No | End date |

**Response:** `ResponseListUserEngagement`

#### 2.4 getTopTemplates

Get top N templates by usage count.

**SOAP Operation:** `getTopTemplates`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Multi-tenant identifier |
| limit | Integer | No | Number of templates (default: 10) |
| dateFrom | Date | No | Start date |
| dateTo | Date | No | End date |

**Response:** `ResponseListTemplateUsage` (sorted by usage)

**Example Use Case:** Dashboard widget showing popular templates

#### 2.5 getActiveUsersCount

Get count of active users in date range.

**SOAP Operation:** `getActiveUsersCount`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Multi-tenant identifier |
| dateFrom | Date | No | Start date |
| dateTo | Date | No | End date |

**Response:** `ResponseCallCardStats` (activeUsers field populated)

#### 2.6 getAllUserEngagementStats

Get engagement for all users in organization.

**SOAP Operation:** `getAllUserEngagementStats`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Multi-tenant identifier |
| dateFrom | Date | No | Start date |
| dateTo | Date | No | End date |
| limit | Integer | No | Max results (default: 100) |

**Response:** `ResponseListUserEngagement` (paginated)

#### 2.7 getAllTemplateUsageStats

Get usage statistics for all templates.

**SOAP Operation:** `getAllTemplateUsageStats`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Multi-tenant identifier |
| dateFrom | Date | No | Start date |
| dateTo | Date | No | End date |

**Response:** `ResponseListTemplateUsage` (all templates)

---

### 3. CallCardTransactionService

Service for audit trail and transaction history.

#### 3.1 getTransactionHistory

Get complete transaction history for a call card.

**SOAP Operation:** `getTransactionHistory`
**Ordering:** Newest first (descending timestamp)

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| callCardId | String | Yes | Call card ID |
| userGroupId | Integer | Yes | Multi-tenant identifier |

**Response:** `TransactionListResponseDTO`

**Example Use Case:** Audit trail view showing all changes to a call card

#### 3.2 getTransactionHistoryPaginated

Get paginated transaction history.

**SOAP Operation:** `getTransactionHistoryPaginated`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| callCardId | String | Yes | Call card ID |
| userGroupId | Integer | Yes | Multi-tenant identifier |
| pageNumber | Integer | Yes | Page number (0-based) |
| pageSize | Integer | Yes | Records per page |

**Response:** `TransactionListResponseDTO`

**Example Request (SOAP):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callcard.transaction.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer YOUR_JWT_TOKEN</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:getTransactionHistoryPaginated>
            <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
            <userGroupId>1</userGroupId>
            <pageNumber>0</pageNumber>
            <pageSize>50</pageSize>
        </ws:getTransactionHistoryPaginated>
    </soapenv:Body>
</soapenv:Envelope>
```

**Example Response:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:getTransactionHistoryPaginatedResponse xmlns:ns2="http://ws.callcard.transaction.saicon.com/">
            <return>
                <errorCode>0000</errorCode>
                <status>OK</status>
                <transactions>
                    <transactionId>txn-1</transactionId>
                    <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                    <transactionType>UPDATE</transactionType>
                    <userId>101</userId>
                    <userName>John Doe</userName>
                    <timestamp>2025-12-20T15:30:00Z</timestamp>
                    <description>User submitted call card</description>
                </transactions>
                <totalRecords>156</totalRecords>
                <pageNumber>1</pageNumber>
                <pageSize>50</pageSize>
                <totalPages>4</totalPages>
            </return>
        </ns2:getTransactionHistoryPaginatedResponse>
    </soap:Body>
</soap:Envelope>
```

#### 3.3 getTransactionsByUser

Get all transactions by specific user.

**SOAP Operation:** `getTransactionsByUser`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | Integer | Yes | User ID |
| userGroupId | Integer | Yes | Multi-tenant identifier |
| dateFrom | Date | Yes | Start date |
| dateTo | Date | Yes | End date |

**Response:** `TransactionListResponseDTO`

#### 3.4 getTransactionsByUserPaginated

Paginated transactions by user.

**SOAP Operation:** `getTransactionsByUserPaginated`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | Integer | Yes | User ID |
| userGroupId | Integer | Yes | Multi-tenant identifier |
| dateFrom | Date | Yes | Start date |
| dateTo | Date | Yes | End date |
| pageNumber | Integer | Yes | Page number |
| pageSize | Integer | Yes | Records per page |

**Response:** `TransactionListResponseDTO`

#### 3.5 getTransactionsByType

Get all transactions of specific type.

**SOAP Operation:** `getTransactionsByType`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| transactionType | String | Yes | CREATE, UPDATE, DELETE, SUBMIT, etc. |
| userGroupId | Integer | Yes | Multi-tenant identifier |
| dateFrom | Date | Yes | Start date |
| dateTo | Date | Yes | End date |

**Response:** `TransactionListResponseDTO`

#### 3.6 getTransactionsByTypePaginated

Paginated transactions by type.

**SOAP Operation:** `getTransactionsByTypePaginated`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| transactionType | String | Yes | Transaction type |
| userGroupId | Integer | Yes | Multi-tenant identifier |
| dateFrom | Date | Yes | Start date |
| dateTo | Date | Yes | End date |
| pageNumber | Integer | Yes | Page number |
| pageSize | Integer | Yes | Records per page |

**Response:** `TransactionListResponseDTO`

#### 3.7 searchTransactions

Advanced multi-criteria transaction search.

**SOAP Operation:** `searchTransactions`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| criteria | TransactionSearchCriteriaDTO | Yes | Search criteria object |

**Criteria Fields:**
- `callCardId`: Filter by call card
- `userId`: Filter by user who made change
- `userGroupId`: Tenant ID (required)
- `transactionType`: Transaction type filter
- `dateFrom`/`dateTo`: Date range
- `sessionId`: User session filter
- `ipAddress`: Source IP filter
- `pageNumber`: Page for results
- `pageSize`: Results per page
- `sortBy`: Sort field (timestamp, transactionType, userId, etc.)
- `sortDirection`: ASC or DESC

**Response:** `TransactionListResponseDTO`

**Example Request (SOAP):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callcard.transaction.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer YOUR_JWT_TOKEN</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:searchTransactions>
            <criteria>
                <userGroupId>1</userGroupId>
                <userId>101</userId>
                <transactionType>UPDATE</transactionType>
                <dateFrom>2025-12-01T00:00:00Z</dateFrom>
                <dateTo>2025-12-31T23:59:59Z</dateTo>
                <pageNumber>0</pageNumber>
                <pageSize>50</pageSize>
                <sortBy>timestamp</sortBy>
                <sortDirection>DESC</sortDirection>
            </criteria>
        </ws:searchTransactions>
    </soapenv:Body>
</soapenv:Envelope>
```

#### 3.8 getTransactionCount

Get total transaction count for a call card.

**SOAP Operation:** `getTransactionCount`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| callCardId | String | Yes | Call card ID |
| userGroupId | Integer | Yes | Multi-tenant identifier |

**Response:** Long (count)

#### 3.9 getTransactionById

Get single transaction by ID.

**SOAP Operation:** `getTransactionById`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| transactionId | String | Yes | Transaction ID |
| userGroupId | Integer | Yes | Multi-tenant identifier |

**Response:** `CallCardTransactionDTO`

#### 3.10 getTransactionTypes

Get list of all available transaction types.

**SOAP Operation:** `getTransactionTypes`

**Request Parameters:** None

**Response:** List<String>

**Possible Values:** CREATE, UPDATE, DELETE, SUBMIT, REVERT, APPROVE, REJECT, etc.

#### 3.11 getRecentTransactions

Get recent transactions across all call cards.

**SOAP Operation:** `getRecentTransactions`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | Integer | Yes | Multi-tenant identifier |
| limit | Integer | No | Max records (default: 100) |

**Response:** `TransactionListResponseDTO`

**Example Use Case:** Admin dashboard showing recent activity

#### 3.12 getTransactionsBySession

Get all transactions in a specific user session.

**SOAP Operation:** `getTransactionsBySession`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| sessionId | String | Yes | User session ID |
| userGroupId | Integer | Yes | Multi-tenant identifier |

**Response:** `TransactionListResponseDTO`

---

### 4. SimplifiedCallCardService (Mobile Optimized V2)

Service optimized for mobile clients with 60-90% smaller payloads.

**Namespace:** `http://ws.callcard.saicon.com/v2/`

#### 4.1 getSimplifiedCallCard

Get single simplified call card by ID.

**SOAP Operation:** `getSimplifiedCallCard`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| callCardId | String | Yes | Call card ID |

**Response:** `SimplifiedCallCardV2DTO`

**Payload Size:** ~500 bytes (vs ~3KB for full CallCardDTO)

#### 4.2 getSimplifiedCallCardList

Get paginated simplified call cards with optional filters.

**SOAP Operation:** `getSimplifiedCallCardList`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | No | Filter by user ID |
| userGroupId | String | No | Filter by organization |
| templateId | String | No | Filter by template |
| status | String | No | Filter: active/inactive |
| submitted | Boolean | No | Filter by submitted flag |
| page | int | Yes | Page number (1-based) |
| pageSize | int | Yes | Records per page |

**Response:** `CallCardBulkResponseDTO`

**Example Use Case:** Mobile app displaying user's call card list

#### 4.3 getCallCardSummaries

Get ultra-minimal summaries for list views.

**SOAP Operation:** `getCallCardSummaries`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userGroupId | String | Yes | Organization ID |
| page | int | Yes | Page number (1-based) |
| pageSize | int | Yes | Records per page |

**Response:** List<CallCardSummaryDTO>

**Payload Size:** ~200 bytes per record

#### 4.4 bulkGetSimplifiedCallCards

Fetch multiple call cards by IDs in single query.

**SOAP Operation:** `bulkGetSimplifiedCallCards`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| callCardIds | List<String> | Yes | List of call card IDs (max 100) |

**Response:** `CallCardBulkResponseDTO`

**Performance Benefit:** Single database query vs N queries

**Example Request (SOAP):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callcard.saicon.com/v2/">
    <soapenv:Header>
        <Authorization>Bearer YOUR_JWT_TOKEN</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:bulkGetSimplifiedCallCards>
            <callCardIds>cc-001</callCardIds>
            <callCardIds>cc-002</callCardIds>
            <callCardIds>cc-003</callCardIds>
        </ws:bulkGetSimplifiedCallCards>
    </soapenv:Body>
</soapenv:Envelope>
```

#### 4.5 getSimplifiedCallCardsByTemplate

Get call cards for specific template.

**SOAP Operation:** `getSimplifiedCallCardsByTemplate`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| templateId | String | Yes | Template ID |
| includeInactive | boolean | No | Include inactive cards (default: false) |
| page | int | Yes | Page number (1-based) |
| pageSize | int | Yes | Records per page |

**Response:** `CallCardBulkResponseDTO`

#### 4.6 getSimplifiedCallCardsByUser

Get call cards for specific user.

**SOAP Operation:** `getSimplifiedCallCardsByUser`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |
| includeInactive | boolean | No | Include inactive cards (default: false) |
| page | int | Yes | Page number (1-based) |
| pageSize | int | Yes | Records per page |

**Response:** `CallCardBulkResponseDTO`

#### 4.7 searchCallCardSummaries

Search call cards with minimal payload.

**SOAP Operation:** `searchCallCardSummaries`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| searchTerm | String | Yes | Search term (name, ref no, etc.) |
| userGroupId | String | Yes | Organization ID |
| page | int | Yes | Page number (1-based) |
| pageSize | int | Yes | Records per page |

**Response:** List<CallCardSummaryDTO>

**Example Use Case:** Mobile search functionality with instant results

---

## Error Handling

### Error Response Format

All SOAP operations return errors in consistent format:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <soap:Fault>
            <faultcode>soap:Server</faultcode>
            <faultstring>Error Code: 5001 - Call card not found</faultstring>
        </soap:Fault>
    </soap:Body>
</soap:Envelope>
```

### Error Response DTO

WSResponse with error details:

```json
{
    "errorCode": "5001",
    "errorNumber": 5001,
    "status": "ERROR",
    "message": "Call card not found",
    "result": "Call card not found"
}
```

### Standard Error Codes

| Error Code | HTTP Status | Meaning | Retry | Action |
|-----------|------------|---------|-------|--------|
| 0000 | 200 OK | Success | No | Process response |
| 4001 | 400 Bad Request | Invalid parameters | No | Fix request |
| 4002 | 400 Bad Request | Missing required field | No | Add field |
| 4003 | 400 Bad Request | Invalid date format | No | Use ISO 8601 |
| 4011 | 401 Unauthorized | Missing JWT token | No | Provide JWT |
| 4012 | 401 Unauthorized | Invalid/expired JWT | No | Refresh token |
| 4031 | 403 Forbidden | Insufficient permissions | No | Request access |
| 4032 | 403 Forbidden | Tenant isolation violation | No | Check userGroupId |
| 5001 | 404 Not Found | Call card not found | No | Verify ID |
| 5002 | 404 Not Found | Template not found | No | Verify ID |
| 5003 | 404 Not Found | Transaction not found | No | Verify ID |
| 5004 | 404 Not Found | User not found | No | Verify ID |
| 5011 | 409 Conflict | Call card already exists | No | Use update operation |
| 5012 | 409 Conflict | Optimistic lock error | Yes | Retry with new version |
| 5021 | 422 Unprocessable | Business rule violation | No | Fix data |
| 5022 | 422 Unprocessable | Invalid state transition | No | Check current state |
| 5031 | 500 Internal Error | Database error | Yes | Retry later |
| 5032 | 500 Internal Error | Service unavailable | Yes | Retry with backoff |
| 5033 | 503 Service Unavailable | Server overloaded | Yes | Retry with backoff |

### Exception Type Enumeration

```java
public enum ExceptionTypeTO {
    GENERIC(0),
    VALIDATION_ERROR(1),
    BUSINESS_ERROR(2),
    DATA_NOT_FOUND(3),
    DUPLICATE_RECORD(4),
    UNAUTHORIZED(5),
    FORBIDDEN(6),
    DATABASE_ERROR(7),
    INTEGRATION_ERROR(8),
    TIMEOUT_ERROR(9);
}
```

### Recommended Retry Strategy

```
Immediate Retry (no backoff):
- 4001-4003: Fix request and retry immediately
- 4031-4032: Request access and retry
- 5001-5005: Verify ID and retry

Exponential Backoff (2^n seconds, max 300s):
- 5012: Optimistic lock - retry 1-5 times
- 5031-5033: Server errors - retry 1-10 times

No Retry:
- All 4xxx errors except retryable
- All 5021-5022 errors
```

---

## Example Requests/Responses

### Scenario 1: Create Call Card

**Goal:** Create a new call card for Q4 2025

**SOAP Request:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callCard.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:addCallCardRecords>
            <userGroupId>org-789</userGroupId>
            <gameTypeId>sales</gameTypeId>
            <applicationId>app-mobile</applicationId>
            <userId>user-123</userId>
            <callCard>
                <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                <callCardTemplateId>template-q4-2025</callCardTemplateId>
                <startDate>2025-10-01T00:00:00Z</startDate>
                <endDate>2025-12-31T23:59:59Z</endDate>
                <submitted>false</submitted>
                <comments>Q4 2025 performance targets</comments>
            </callCard>
        </ws:addCallCardRecords>
    </soapenv:Body>
</soapenv:Envelope>
```

**SOAP Response:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:addCallCardRecordsResponse xmlns:ns2="http://ws.callCard.saicon.com/">
            <return>
                <errorCode>0000</errorCode>
                <errorNumber>0</errorNumber>
                <result>Call cards added successfully</result>
                <status>OK</status>
                <records>
                    <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                    <callCardTemplateId>template-q4-2025</callCardTemplateId>
                    <startDate>2025-10-01T00:00:00Z</startDate>
                    <endDate>2025-12-31T23:59:59Z</endDate>
                    <submitted>false</submitted>
                    <lastUpdated>2025-12-20T15:30:00Z</lastUpdated>
                </records>
                <totalRecords>1</totalRecords>
            </return>
        </ns2:addCallCardRecordsResponse>
    </soap:Body>
</soap:Envelope>
```

### Scenario 2: Submit Call Card

**Goal:** Submit a completed call card

**SOAP Request:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callCard.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:submitTransactions>
            <userId>user-123</userId>
            <userGroupId>org-789</userGroupId>
            <gameTypeId>sales</gameTypeId>
            <applicationId>app-mobile</applicationId>
            <callCardDTO>
                <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                <callCardTemplateId>template-q4-2025</callCardTemplateId>
                <submitted>true</submitted>
            </callCardDTO>
        </ws:submitTransactions>
    </soapenv:Body>
</soapenv:Envelope>
```

**SOAP Response (Success):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:submitTransactionsResponse xmlns:ns2="http://ws.callCard.saicon.com/">
            <return>
                <errorCode>0000</errorCode>
                <status>OK</status>
                <result>Call card submitted successfully</result>
            </return>
        </ns2:submitTransactionsResponse>
    </soap:Body>
</soap:Envelope>
```

**SOAP Response (Error - Already Submitted):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <soap:Fault>
            <faultcode>soap:Server</faultcode>
            <faultstring>Error Code: 5022 - Call card already submitted</faultstring>
        </soap:Fault>
    </soap:Body>
</soap:Envelope>
```

### Scenario 3: Get Transaction History

**Goal:** Audit all changes to a call card

**SOAP Request:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callcard.transaction.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:getTransactionHistoryPaginated>
            <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
            <userGroupId>1</userGroupId>
            <pageNumber>0</pageNumber>
            <pageSize>10</pageSize>
        </ws:getTransactionHistoryPaginated>
    </soapenv:Body>
</soapenv:Envelope>
```

**SOAP Response:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:getTransactionHistoryPaginatedResponse xmlns:ns2="http://ws.callcard.transaction.saicon.com/">
            <return>
                <errorCode>0000</errorCode>
                <status>OK</status>
                <transactions>
                    <transactionId>txn-1001</transactionId>
                    <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                    <transactionType>SUBMIT</transactionType>
                    <userId>101</userId>
                    <userName>John Doe</userName>
                    <userGroupId>1</userGroupId>
                    <timestamp>2025-12-20T16:45:30Z</timestamp>
                    <description>Call card submitted for review</description>
                    <ipAddress>192.168.1.100</ipAddress>
                    <sessionId>sess-abc123def456</sessionId>
                </transactions>
                <transactions>
                    <transactionId>txn-1000</transactionId>
                    <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                    <transactionType>UPDATE</transactionType>
                    <userId>101</userId>
                    <userName>John Doe</userName>
                    <userGroupId>1</userGroupId>
                    <timestamp>2025-12-20T15:30:00Z</timestamp>
                    <oldValue>{"submitted": false}</oldValue>
                    <newValue>{"submitted": true}</newValue>
                    <description>Updated submission status</description>
                    <ipAddress>192.168.1.100</ipAddress>
                    <sessionId>sess-abc123def456</sessionId>
                </transactions>
                <transactions>
                    <transactionId>txn-999</transactionId>
                    <callCardId>cc-550e8400-e29b-41d4-a716-446655440000</callCardId>
                    <transactionType>CREATE</transactionType>
                    <userId>101</userId>
                    <userName>John Doe</userName>
                    <userGroupId>1</userGroupId>
                    <timestamp>2025-12-01T10:00:00Z</timestamp>
                    <description>Call card created</description>
                    <ipAddress>192.168.1.100</ipAddress>
                    <sessionId>sess-abc123def456</sessionId>
                </transactions>
                <totalRecords>3</totalRecords>
                <pageNumber>1</pageNumber>
                <pageSize>10</pageSize>
                <totalPages>1</totalPages>
            </return>
        </ns2:getTransactionHistoryPaginatedResponse>
    </soap:Body>
</soap:Envelope>
```

### Scenario 4: Get Statistics Report

**Goal:** Get Q4 statistics for organization

**SOAP Request:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callcard.statistics.saicon.com/">
    <soapenv:Header>
        <Authorization>Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:getCallCardStatistics>
            <userGroupId>org-789</userGroupId>
            <dateFrom>2025-10-01T00:00:00Z</dateFrom>
            <dateTo>2025-12-31T23:59:59Z</dateTo>
        </ws:getCallCardStatistics>
    </soapenv:Body>
</soapenv:Envelope>
```

**SOAP Response:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:getCallCardStatisticsResponse xmlns:ns2="http://ws.callcard.statistics.saicon.com/">
            <return>
                <errorCode>0000</errorCode>
                <status>OK</status>
                <totalCallCards>1200</totalCallCards>
                <activeCallCards>950</activeCallCards>
                <submittedCallCards>850</submittedCallCards>
                <totalUsers>245</totalUsers>
                <totalTemplates>18</totalTemplates>
                <averageCardsPerUser>4.9</averageCardsPerUser>
                <averageUsersPerCard>3.2</averageUsersPerCard>
                <averageCompletionTime>3.5</averageCompletionTime>
                <activeUsers>189</activeUsers>
            </return>
        </ns2:getCallCardStatisticsResponse>
    </soap:Body>
</soap:Envelope>
```

### Scenario 5: Mobile Client - Bulk Fetch

**Goal:** Mobile app loading user's call cards

**SOAP Request (V2 - Mobile Optimized):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                   xmlns:ws="http://ws.callcard.saicon.com/v2/">
    <soapenv:Header>
        <Authorization>Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</Authorization>
    </soapenv:Header>
    <soapenv:Body>
        <ws:getSimplifiedCallCardList>
            <userId>user-123</userId>
            <userGroupId>org-789</userGroupId>
            <page>1</page>
            <pageSize>25</pageSize>
        </ws:getSimplifiedCallCardList>
    </soapenv:Body>
</soapenv:Envelope>
```

**SOAP Response (Compressed with GZIP):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:getSimplifiedCallCardListResponse xmlns:ns2="http://ws.callcard.saicon.com/v2/">
            <return>
                <callCards>
                    <id>cc-001</id>
                    <created>2025-12-01T10:00:00Z</created>
                    <updated>2025-12-20T15:30:00Z</updated>
                    <status>active</status>
                    <submitted>false</submitted>
                </callCards>
                <callCards>
                    <id>cc-002</id>
                    <created>2025-11-15T14:20:00Z</created>
                    <updated>2025-12-10T09:15:00Z</updated>
                    <status>active</status>
                    <submitted>true</submitted>
                </callCards>
                <totalCount>42</totalCount>
                <page>1</page>
                <pageSize>25</pageSize>
                <totalPages>2</totalPages>
                <hasNext>true</hasNext>
                <hasPrevious>false</hasPrevious>
                <executionTimeMs>12</executionTimeMs>
            </return>
        </ns2:getSimplifiedCallCardListResponse>
    </soap:Body>
</soap:Envelope>
```

---

## Performance & Optimization

### Compression Strategy

All services use two-tier compression:

1. **FastInfoset** - Binary XML encoding (40-60% size reduction)
2. **GZIP** - Stream compression (additional 40-70% reduction for text-heavy payloads)

**Total Benefit:** 60-90% smaller payloads vs text XML

**Enable in Client:**
```java
// Apache CXF client example
SOAPService service = new SOAPService();
BindingProvider bp = (BindingProvider) service.getCallCardServicePort();
HTTPConduit conduit = (HTTPConduit) bp.getConduit();
HTTPClientPolicy policy = new HTTPClientPolicy();
policy.setAutoRedirect(true);
policy.setAllowChunking(true);
policy.setConnectionTimeout(30000);
conduit.setClient(policy);
```

### Pagination Best Practices

**Recommended Page Sizes:**

| Client Type | Page Size | Rationale |
|-------------|-----------|-----------|
| Mobile (4G) | 10-25 | Reduce latency, faster UI |
| Mobile (LTE) | 25-50 | Balance between speed and trips |
| Desktop/Web | 50-100 | Reduce server load |
| Bulk Processing | 100-500 | Minimize round trips |

**Pagination Example:**
```java
// Mobile client loading first page
CallCardBulkResponseDTO response = service.getSimplifiedCallCardList(
    userId, userGroupId, null, null, null,
    1,  // page (1-based)
    25  // pageSize
);

// Check if more data available
if (response.isHasNext()) {
    // Load next page when needed
    CallCardBulkResponseDTO page2 = service.getSimplifiedCallCardList(
        userId, userGroupId, null, null, null,
        2, 25
    );
}
```

### Query Performance Tips

| Operation | Performance | Notes |
|-----------|-------------|-------|
| getSimplifiedCallCard | < 10ms | Single ID lookup, cached |
| getSimplifiedCallCardList | 10-50ms | Indexed on userId, pagination fast |
| bulkGetSimplifiedCallCards | 20-100ms | Single query, max 100 IDs recommended |
| getTransactionHistory | 50-200ms | Indexed on callCardId, pagination helps |
| getCallCardStatistics | 100-500ms | Aggregation query, limit date range |
| searchTransactions | 200-1000ms | Multiple index scans, use filters |

### Caching Recommendations

```xml
<!-- Cache Headers (if REST endpoint used) -->
Cache-Control: public, max-age=300
ETag: W/"550e8400-e29b-41d4-a716-446655440000"

<!-- Client-side caching strategy -->
- getSimplifiedCallCard: Cache 5 minutes (rarely changes)
- getCallCardStatistics: Cache 15 minutes (batch job dependent)
- getTransactionHistory: Cache 1 minute (audit requirements)
- searchTransactions: No caching (user-initiated)
```

### Bulk Operation Guidelines

**Batch Sizes:**

- `addCallCardRecords`: Max 100 per batch (larger batches increase latency)
- `bulkGetSimplifiedCallCards`: Max 100 IDs per batch
- `searchTransactions` pagination: Max 500 per page

**Retry Batches Individually:**
```java
List<CallCardDTO> batch1 = records.subList(0, 100);
List<CallCardDTO> batch2 = records.subList(100, 200);

ResponseListCallCard result1 = service.addCallCardRecords(..., batch1);
if (result1.getStatus() != ResponseStatus.OK) {
    // Retry batch1 individually
    for (CallCardDTO record : batch1) {
        service.addCallCardRecords(..., List.of(record));
    }
}
```

---

## Troubleshooting

### Common Issues

#### Issue: JWT Token Expired

**Symptom:** `Error Code: 4012 - Invalid/expired JWT`

**Solution:**
```bash
# Refresh JWT token
curl -X POST http://localhost:8080/auth/refresh \
  -H "Authorization: Bearer old_token"

# Use new token in Authorization header
```

#### Issue: Multi-Tenant Isolation Error

**Symptom:** `Error Code: 4032 - Tenant isolation violation`

**Cause:** userGroupId in request doesn't match JWT claim

**Solution:**
```java
// Verify JWT contains correct userGroupId
// JWT should have: userGroupId: 1
// Request userGroupId parameter should be: "1"
// Ensure String/Integer type matching

String userGroupId = String.valueOf(jwtClaims.getIntegerClaim("userGroupId"));
```

#### Issue: Call Card Not Found After Creation

**Symptom:** `Error Code: 5001 - Call card not found`

**Cause:** Asynchronous replication delay (eventual consistency)

**Solution:**
```java
// Add retry with backoff
CallCardDTO created = service.addCallCardRecords(...).getRecords().get(0);
Thread.sleep(100); // Brief delay for replication

// Retry fetch
SimplifiedCallCardV2DTO card = service.getSimplifiedCallCard(
    created.getCallCardId()
);
```

#### Issue: Slow Statistics Queries

**Symptom:** `getCallCardStatistics` taking > 2 seconds

**Cause:** Querying 5+ years of data without index

**Solution:**
```java
// Limit date range to last 12 months
LocalDateTime now = LocalDateTime.now();
LocalDateTime oneYearAgo = now.minusYears(1);

ResponseCallCardStats stats = service.getCallCardStatistics(
    userGroupId,
    java.sql.Timestamp.valueOf(oneYearAgo),
    java.sql.Timestamp.valueOf(now)
);
```

#### Issue: Transaction Search Returning Empty

**Symptom:** `searchTransactions` returns 0 results even though data exists

**Cause:** Date range too narrow or timezone mismatch

**Solution:**
```java
// Use ISO 8601 UTC timestamps
// Wrong: "2025-12-20 15:30:00"
// Correct: "2025-12-20T15:30:00Z"

TransactionSearchCriteriaDTO criteria = new TransactionSearchCriteriaDTO();
criteria.setDateFrom(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    .parse("2025-12-01T00:00:00Z"));
criteria.setDateTo(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    .parse("2025-12-31T23:59:59Z"));
```

#### Issue: GZIP Decompression Errors

**Symptom:** `javax.xml.stream.XMLStreamException: Unexpected exception`

**Cause:** Client not handling GZIP encoding

**Solution:**
```java
// Ensure HTTP client auto-handles Content-Encoding: gzip
HTTPConduit conduit = (HTTPConduit) bindingProvider.getConduit();
HTTPClientPolicy policy = new HTTPClientPolicy();
policy.setAutoRedirect(true);
conduit.setClient(policy);

// Or use AutoCloseable with HttpClient
try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
    // GZIP automatically handled
}
```

### Debug Mode

**Enable detailed logging:**

```xml
<!-- logback.xml -->
<logger name="com.saicon.games.callcard.ws" level="DEBUG"/>
<logger name="org.apache.cxf" level="DEBUG"/>
<logger name="org.apache.http.wire" level="TRACE"/>
```

**SOAP Message Logging:**
```java
// Enable CXF logging
System.setProperty("org.apache.cxf.Logger",
    "org.apache.cxf.common.logging.Slf4jLogger");
org.apache.cxf.common.logging.LogUtils.setLoggerClass(
    org.apache.cxf.common.logging.Slf4jLogger.class
);
```

**Capture Raw SOAP:**
```bash
# Using tcpdump
tcpdump -i eth0 -A -s 0 'tcp port 8080'

# Using mitmproxy
mitmproxy -p 8081 --mode transparent http://localhost:8080/callcard-ws
```

### Health Check

**Service Health Endpoint:**
```bash
curl http://localhost:8080/callcard-ws/health

# Response:
{
    "status": "UP",
    "components": {
        "database": {"status": "UP"},
        "cxf": {"status": "UP"}
    }
}
```

**WSDL Validation:**
```bash
# Fetch and validate WSDL
curl http://localhost:8080/callcard-ws/cxf/CallCardService?wsdl | xmllint --noout -

# Load test
ab -n 100 -c 10 http://localhost:8080/callcard-ws/cxf/CallCardService?wsdl
```

---

## Support & Contact

For API issues or questions:
- **Technical Support:** tech-support@talosmaind.com
- **Integration Help:** integration@talosmaind.com
- **Bug Reports:** bugs@talosmaind.com

---

**Document Version:** 1.0
**Last Updated:** 2025-12-22
**Next Review:** 2026-03-22
**Status:** Active
