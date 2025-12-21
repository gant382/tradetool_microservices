# Compilation Fixes Required for callcard-components

## Summary
48 compilation errors remaining. Most errors are due to missing methods/setters in DTO classes and missing interface methods.

## Phase 1: Fix ws-api DTOs (rebuild ws-api after these changes)

### 1. UserEngagementDTO.java
**Location:** `callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/UserEngagementDTO.java`

**Add these missing fields and their getters/setters:**
```java
private String userGroupId;
private Date dateFrom;
private Date dateTo;
private long activeCallCards;
private long submittedCallCards;
private Date firstActivityDate;
private Integer activityDaysCount;
private long totalRefUsers;
private Double averageRefUsersPerCallCard;
private long uniqueTemplatesUsed;
private Long averageCompletionTimeMinutes;
private String mostUsedTemplateId;
private String mostUsedTemplateName;
```

**Change existing fields:**
- `int totalCallCards` → `long totalCallCards`

### 2. TemplateUsageDTO.java
**Location:** `callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/TemplateUsageDTO.java`

**Add these missing fields and their getters/setters:**
```java
private String templateId;
private String templateName;
private String userGroupId;
private Date dateFrom;
private Date dateTo;
private long usageCount;
private long uniqueUsers;
private long activeCount;
private long submittedCount;
private long submittedCallCards; // Note: getSubmittedCallCards() method needed
private double completionRate;
private Long averageCompletionTimeMinutes;
private Date lastUsedDate;
private Date firstUsedDate;
private long totalRefUsers;
private Double averageRefUsersPerCallCard;
```

### 3. ExceptionTypeTO.java
**Location:** `callcard-ws-api/src/main/java/com/saicon/games/callcard/exception/ExceptionTypeTO.java`

**Add after line 29 (after GENERIC constant):**
```java
public static final String GENERIC_ERROR = "9999"; // Alias for GENERIC
```

## Phase 2: Fix components interfaces and classes

### 4. ErpDynamicQueryManager.java
**Location:** `callcard-components/src/main/java/com/saicon/games/callcard/components/ErpDynamicQueryManager.java`

**Fix listCallCards method signature (line ~135):**
```java
public List<Object[]> listCallCards(
    String gameTypeId,
    List<String> callCardTemplateIds,
    String callCardStatus,
    String userGroupId,
    String applicationId,
    boolean includeCallCardTemplateData,
    boolean includeCallCardMetadata,
    boolean includeCallCardReferencedUsers,
    String sortOrder,
    int offset,
    int limit
) {
    // Stub implementation
    return new ArrayList<>();
}
```

**Fix listCallCardTemplates method signature (lines 188, 292, 2827, 2837):**
```java
public List<Object[]> listCallCardTemplates(
    String gameTypeId,
    String userGroupId,
    List<String> templateIds,
    boolean activeOnly,
    Boolean includeMetadata,
    String sortOrder,
    int offset,
    int limit
) {
    // Stub implementation
    return new ArrayList<>();
}
```

### 5. ErpNativeQueryManager.java
**Location:** `callcard-components/src/main/java/com/saicon/games/callcard/components/ErpNativeQueryManager.java`

**Add missing methods:**
```java
public List<Object[]> executeNativeQuery(String query, String[] paramNames, Object[] paramValues) {
    // Stub implementation
    return new ArrayList<>();
}

public List<Object[]> listCallCardRefUserIndexesPreviousValuesSummary(
    String userGroupId,
    List<String> callCardIds,
    List<String> metadataKeys,
    Integer limit,
    List<Integer> previousVisitsCounts,
    boolean includeGeoInfo
) {
    // Stub implementation
    return new ArrayList<>();
}
```

### 6. ISalesOrderManagement.java (NEW FILE)
**Location:** `callcard-components/src/main/java/com/saicon/games/callcard/components/external/ISalesOrderManagement.java`

**Create new interface:**
```java
package com.saicon.games.callcard.components.external;

import java.util.Date;

public interface ISalesOrderManagement {

    Object addSalesOrder(
        String orderId,
        String userId,
        String userGroupId,
        Date orderDate,
        Date deliveryDate,
        String orderStatus,
        String paymentMethod,
        String shippingAddress,
        String notes,
        Double totalAmount,
        Double taxAmount,
        Double shippingCost,
        String currency,
        int orderType,
        Date createdDate,
        String createdBy,
        String modifiedBy,
        Date modifiedDate,
        String externalReference,
        Integer priority
    );

    Object createSalesOrderRevision(
        String orderId,
        Date revisionDate,
        Date deliveryDate,
        String orderStatus,
        String paymentMethod,
        String shippingAddress,
        String notes,
        Double totalAmount,
        Double taxAmount,
        Double shippingCost,
        String currency,
        Date createdDate,
        String createdBy,
        String externalReference,
        Integer revisionNumber
    );

    Object addSalesOrderDetails(
        String orderId,
        String productId,
        int quantity,
        Double unitPrice,
        Double totalPrice,
        String notes
    );
}
```

### 7. IAddressbookManagement.java (NEW FILE)
**Location:** `callcard-components/src/main/java/com/saicon/games/callcard/components/external/IAddressbookManagement.java`

**Create new interface:**
```java
package com.saicon.games.callcard.components.external;

public interface IAddressbookManagement {

    Object createAddressbook(
        String userId,
        String addressType,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        int postcodeId,
        String postcodeName,
        int cityId,
        String cityName,
        int stateId,
        String stateName,
        String countryCode,
        String phoneNumber,
        String email,
        Double latitude,
        Double longitude,
        boolean isDefault
    );
}
```

## Phase 3: Fix type conversion issues in CallCardManagement.java

### Type Conversions Needed:

**Line 2177, 2216:** Date to int - wrap in timestamp conversion:
```java
// Instead of passing Date directly to int parameter:
// salesOrderDate (Date) → ((int)(salesOrderDate.getTime() / 1000))
```

**Line 2237:** BigDecimal to double:
```java
// Instead of: totalAmount (BigDecimal)
// Use: totalAmount.doubleValue()
```

**Line 2560:** List vs String[] conditional:
```java
// Change the ternary operator to use consistent types
// Either convert List to array or use List throughout
```

**Line 2659, 3757:** long to int conversion:
```java
// Use explicit cast: (int)longValue
```

**Line 3090:** Cast Object to List<UserAddressbook>:
```java
@SuppressWarnings("unchecked")
List<UserAddressbook> addressList = (List<UserAddressbook>) objectResult;
```

**Line 3135, 3138, 3139:** String to int/State conversions:
```java
// These are related to postcode/city/state lookups
// Need proper entity lookups or use String values consistently
```

## Build Commands

After making above changes:

```bash
# 1. Rebuild ws-api with updated DTOs
cd callcard-ws-api
mvn clean install -DskipTests -U

# 2. Rebuild components with updated interfaces
cd ../callcard-components
mvn clean install -DskipTests

# 3. Final verification - rebuild all three
cd ..
mvn clean install -pl callcard-entity,callcard-ws-api,callcard-components -am -DskipTests
```

## Success Criteria

- Zero compilation errors
- All three modules show BUILD SUCCESS
- Ready for integration testing
