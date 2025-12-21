# CallCard Transaction History - Quick Reference Guide

**Quick reference for developers integrating transaction history functionality.**

---

## Quick Start

### 1. Record a Transaction (Manual)

```java
// In your CallCardManagement class
@Autowired
private ICallCardTransactionManagement transactionManagement;

// When creating a CallCard
CallCard newCallCard = createCallCardEntity(...);
transactionManagement.recordCreate(
    newCallCard,
    currentUserId,
    request.getRemoteAddr(),
    session.getId()
);

// When updating a CallCard
CallCard oldCallCard = loadExisting(callCardId);
CallCard updatedCallCard = updateCallCardEntity(...);
transactionManagement.recordUpdate(
    oldCallCard,
    updatedCallCard,
    currentUserId,
    request.getRemoteAddr(),
    session.getId()
);

// When deleting a CallCard
CallCard callCard = loadExisting(callCardId);
transactionManagement.recordDelete(
    callCard,
    currentUserId,
    request.getRemoteAddr(),
    session.getId()
);
```

---

## REST API Quick Reference

### Get Transaction History
```bash
GET /callcard/transactions/callcard/{callCardId}?page=0&size=20
Header: X-Talos-User-Group-Id: 1
```

### Get Transactions by User
```bash
GET /callcard/transactions/user/{userId}?dateFrom=2025-01-01&dateTo=2025-12-31
Header: X-Talos-User-Group-Id: 1
```

### Advanced Search
```bash
POST /callcard/transactions/search
Header: X-Talos-User-Group-Id: 1
Body: {
  "callCardId": "abc-123",
  "transactionType": "UPDATE",
  "dateFrom": "2025-01-01",
  "dateTo": "2025-12-31",
  "pageNumber": 0,
  "pageSize": 50
}
```

### Get Recent Activity
```bash
GET /callcard/transactions/recent?limit=50
Header: X-Talos-User-Group-Id: 1
```

---

## SOAP Service Quick Reference

### WSDL Location
```
http://your-server/cxf/CallCardTransactionService?wsdl
```

### Example Call
```xml
<soapenv:Envelope>
  <soapenv:Body>
    <ns:getTransactionHistory>
      <ns:callCardId>abc-123</ns:callCardId>
      <ns:userGroupId>1</ns:userGroupId>
    </ns:getTransactionHistory>
  </soapenv:Body>
</soapenv:Envelope>
```

---

## Transaction Types

| Type | When to Use |
|------|-------------|
| `CREATE` | New CallCard created |
| `UPDATE` | General modification |
| `DELETE` | CallCard deleted/deactivated |
| `ASSIGN` | User assigned to CallCard |
| `UNASSIGN` | User unassigned |
| `TEMPLATE_CHANGE` | Template modified |
| `STATUS_CHANGE` | Active status changed |
| `DATE_CHANGE` | Start/end date changed |
| `COMMENT_CHANGE` | Comments updated |
| `REFERENCE_CHANGE` | Internal ref number changed |

---

## Database Queries

### Get all transactions for a CallCard
```sql
SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
WHERE CALL_CARD_ID = 'abc-123'
AND USER_GROUP_ID = 1
ORDER BY TIMESTAMP DESC;
```

### Get recent transactions
```sql
SELECT TOP 100 * FROM CALL_CARD_TRANSACTION_HISTORY
WHERE USER_GROUP_ID = 1
ORDER BY TIMESTAMP DESC;
```

### Get transactions by user
```sql
SELECT * FROM CALL_CARD_TRANSACTION_HISTORY
WHERE USER_ID = 456
AND USER_GROUP_ID = 1
AND TIMESTAMP BETWEEN '2025-01-01' AND '2025-12-31'
ORDER BY TIMESTAMP DESC;
```

---

## Spring Configuration

### Bean Definition
```java
@Bean
public ICallCardTransactionManagement transactionManagement() {
    CallCardTransactionManagement management = new CallCardTransactionManagement();
    management.setTransactionDAO(transactionDAO());
    return management;
}

@Bean
public CallCardTransactionService callCardTransactionService() {
    CallCardTransactionService service = new CallCardTransactionService();
    service.setTransactionManagement(transactionManagement());
    return service;
}
```

### CXF Endpoint
```java
@Bean
public Endpoint transactionServiceEndpoint() {
    EndpointImpl endpoint = new EndpointImpl(bus(), callCardTransactionService());
    endpoint.publish("/CallCardTransactionService");
    return endpoint;
}
```

### Jersey Resource
```java
@Configuration
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(CallCardTransactionResources.class);
    }
}
```

---

## JSON Serialization Example

### Before State (oldValue)
```json
{
  "callCardId": "abc-123",
  "userId": 456,
  "templateId": "tpl-old",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "active": true,
  "comments": "Original comment",
  "lastUpdated": "2025-01-01T10:00:00Z"
}
```

### After State (newValue)
```json
{
  "callCardId": "abc-123",
  "userId": 456,
  "templateId": "tpl-new",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "active": true,
  "comments": "Updated comment",
  "lastUpdated": "2025-01-15T14:30:00Z"
}
```

---

## Common Queries

### Count transactions for a CallCard
```java
Long count = transactionService.getTransactionCount(callCardId, userGroupId);
```

### Get transaction types
```java
List<String> types = transactionService.getTransactionTypes();
// Returns: ["CREATE", "UPDATE", "DELETE", ...]
```

### Search with criteria
```java
TransactionSearchCriteriaDTO criteria = new TransactionSearchCriteriaDTO(userGroupId);
criteria.setCallCardId("abc-123");
criteria.setTransactionType("UPDATE");
criteria.setDateFrom(startDate);
criteria.setDateTo(endDate);
criteria.setPageNumber(0);
criteria.setPageSize(50);

TransactionListResponseDTO response = transactionService.searchTransactions(criteria);
```

---

## Error Handling

### Service Layer
```java
try {
    transactionManagement.recordCreate(callCard, userId, ipAddress, sessionId);
} catch (BusinessLayerException e) {
    LOGGER.error("Failed to record transaction", e);
    // Don't fail the main operation
}
```

### REST API
```java
@ExceptionHandler(BusinessLayerException.class)
public Response handleBusinessException(BusinessLayerException e) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity("{\"error\": \"" + e.getMessage() + "\"}")
        .build();
}
```

---

## Performance Tips

### 1. Use Pagination
```java
// Good: Paginated query
transactionService.getTransactionHistoryPaginated(callCardId, userGroupId, 0, 50);

// Bad: Load all transactions
transactionService.getTransactionHistory(callCardId, userGroupId); // May return thousands
```

### 2. Use Specific Queries
```java
// Good: Query by type and date range
transactionService.getTransactionsByType("UPDATE", userGroupId, dateFrom, dateTo);

// Bad: Load all and filter in memory
List<CallCardTransactionDTO> all = // load all
List<CallCardTransactionDTO> filtered = all.stream()
    .filter(t -> "UPDATE".equals(t.getTransactionType()))
    .collect(Collectors.toList());
```

### 3. Index-Friendly Queries
```sql
-- Good: Uses index
WHERE USER_GROUP_ID = 1 AND CALL_CARD_ID = 'abc-123' ORDER BY TIMESTAMP DESC

-- Bad: Can't use index efficiently
WHERE DESCRIPTION LIKE '%updated%'
```

---

## Testing

### Unit Test Example
```java
@Test
public void testRecordCreate() {
    CallCard callCard = createTestCallCard();

    CallCardTransaction transaction = transactionManagement.recordCreate(
        callCard,
        userId,
        "192.168.1.100",
        "session-123"
    );

    assertNotNull(transaction.getTransactionId());
    assertEquals(CallCardTransactionType.CREATE, transaction.getTransactionType());
    assertNull(transaction.getOldValue());
    assertNotNull(transaction.getNewValue());
}
```

### Integration Test Example
```java
@Test
public void testTransactionRecordedOnCreate() {
    // Create CallCard
    CallCard callCard = callCardManagement.createCallCard(...);

    // Verify transaction recorded
    List<CallCardTransaction> transactions =
        transactionManagement.findByCallCardId(callCard.getCallCardId(), userGroupId, 0, 10);

    assertEquals(1, transactions.size());
    assertEquals(CallCardTransactionType.CREATE, transactions.get(0).getTransactionType());
}
```

---

## Troubleshooting

### No transactions recorded
1. Check if `ICallCardTransactionManagement` is injected
2. Verify recordCreate/Update/Delete methods are called
3. Check database table exists
4. Verify persistence.xml includes CallCardTransaction entity

### Transactions not found in queries
1. Verify correct `userGroupId` is passed
2. Check date range includes transaction timestamp
3. Ensure transaction type is spelled correctly
4. Verify indexes exist on database

### Performance issues
1. Check indexes are created: `EXEC sp_helpindex 'CALL_CARD_TRANSACTION_HISTORY'`
2. Use pagination for large result sets
3. Avoid LIKE queries on large text fields
4. Consider archival strategy for old transactions

---

## File Locations

| Component | Path |
|-----------|------|
| Entity | `callcard-entity/src/main/java/com/saicon/games/callcard/entity/CallCardTransaction.java` |
| Enum | `callcard-entity/src/main/java/com/saicon/games/callcard/entity/CallCardTransactionType.java` |
| DTO | `callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/CallCardTransactionDTO.java` |
| SOAP Interface | `callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/ICallCardTransactionService.java` |
| SOAP Implementation | `callcard-service/src/main/java/com/saicon/games/callcard/service/CallCardTransactionService.java` |
| REST Resources | `callcard-ws-api/src/main/java/com/saicon/games/callcard/resources/CallCardTransactionResources.java` |
| Business Interface | `callcard-components/src/main/java/com/saicon/games/callcard/components/ICallCardTransactionManagement.java` |
| DDL Script | `CallCard_Server_WS/src/main/resources/db/migration/V003__create_transaction_history.sql` |

---

## API Documentation

### Swagger UI
```
http://your-server/swagger-ui.html
Filter: callcard-transactions
```

### SOAP WSDL
```
http://your-server/cxf/CallCardTransactionService?wsdl
```

---

## Common Patterns

### Pattern 1: Record with Error Handling
```java
try {
    CallCard callCard = performBusinessOperation();
    transactionManagement.recordCreate(callCard, userId, ipAddress, sessionId);
    return callCard;
} catch (BusinessLayerException e) {
    LOGGER.error("Business operation failed", e);
    throw e;
} catch (Exception e) {
    LOGGER.error("Transaction recording failed", e);
    // Continue - don't fail business operation due to audit logging failure
    return callCard;
}
```

### Pattern 2: Capture Before State
```java
// Load existing before making changes
CallCard oldCallCard = callCardDAO.find(callCardId);
CallCard oldState = cloneCallCard(oldCallCard); // Deep copy

// Perform update
updateCallCardProperties(oldCallCard, updateRequest);
CallCard updatedCallCard = callCardDAO.save(oldCallCard);

// Record transaction with both states
transactionManagement.recordUpdate(oldState, updatedCallCard, userId, ipAddress, sessionId);
```

### Pattern 3: Bulk Operations
```java
List<CallCardTransaction> transactions = new ArrayList<>();
for (CallCard callCard : callCards) {
    CallCardTransaction transaction = transactionManagement.recordCreate(
        callCard, userId, ipAddress, sessionId
    );
    transactions.add(transaction);
}
// Transactions are automatically persisted
```

---

## Security Considerations

### 1. Multi-Tenant Isolation
Always pass and validate `userGroupId`:
```java
if (userGroupId == null) {
    throw new SecurityException("UserGroupId required for multi-tenant isolation");
}
```

### 2. IP Address Capture
```java
String ipAddress = request.getRemoteAddr();
// Handle proxies
String forwarded = request.getHeader("X-Forwarded-For");
if (forwarded != null) {
    ipAddress = forwarded.split(",")[0].trim();
}
```

### 3. Sensitive Data
Don't log sensitive data in transactions:
```java
// Bad: Logs sensitive fields
transaction.setMetadata("{\"password\":\"secret123\"}");

// Good: Exclude sensitive fields
transaction.setMetadata("{\"fieldsChanged\":[\"email\",\"phone\"]}");
```

---

## Support

For questions or issues:
1. Check detailed documentation: `PHASE5_TRANSACTION_HISTORY_REPORT.md`
2. Review implementation summary: `PHASE5_IMPLEMENTATION_SUMMARY.md`
3. Contact: tech@talosmaind.com

---

**Last Updated:** 2025-12-21
**Version:** 1.0
**Status:** Production Ready

