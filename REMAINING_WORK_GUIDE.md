# CallCard Components - Remaining Work Guide

**Quick Reference for Completing the Last 56 Errors**

---

## Current Status
- **Errors Remaining:** 56
- **Percentage Complete:** 82.6%
- **Estimated Time:** 4-6 hours
- **Files to Edit:** Primarily `CallCardManagement.java` and `ICallCardManagement.java`

---

## Error Categories & Fixes

### Category 1: Interface Signature Mismatches (Priority 1)
**Count:** ~8 errors
**Time:** 30 minutes

#### Files to Edit
- `src/main/java/com/saicon/games/callcard/components/ICallCardManagement.java`

#### Methods Needing `throws BusinessLayerException`

```java
// Find these methods and add throws clause:

void submitTransactions(String userId, String userGroupId, String gameTypeId,
                       String applicationId, String indirectUserId,
                       CallCardDTO callCardDTO); // ADD: throws BusinessLayerException

CallCardDTO getNewOrPendingCallCard(String userId, String userGroupId,
                                   String gameTypeId, String applicationId,
                                   String callCardId,
                                   List<String> filterProperties); // ADD: throws BusinessLayerException

void addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId,
                                  String applicationId, String userId,
                                  SimplifiedCallCardDTO callCard); // ADD: throws BusinessLayerException
```

#### Command to Verify
```bash
cd /c/Users/dimit/tradetool_middleware/callcard-components
mvn clean compile -DskipTests 2>&1 | grep "cannot implement" | wc -l
# Should be 0 after fix
```

---

### Category 2: List to Map Conversions (Priority 1)
**Count:** 6 errors
**Lines:** Around 2150-2160
**Time:** 1 hour

#### Problem Pattern
```java
// CURRENT (BROKEN)
List<Object> result = query.getResultList();
Map<Integer, List<SolrBrandProductDTO>> productMap = result; // ERROR
```

#### Fix Pattern
```java
// FIXED
List<Object> result = query.getResultList();
Map<Integer, List<SolrBrandProductDTO>> productMap = new HashMap<>();

for (Object obj : result) {
    if (obj instanceof Object[]) {
        Object[] row = (Object[]) obj;
        Integer key = (Integer) row[0];
        SolrBrandProductDTO dto = convertToDTO(row); // Helper method

        productMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dto);
    }
}
```

#### Helper Method to Add
```java
private SolrBrandProductDTO convertToDTO(Object[] row) {
    SolrBrandProductDTO dto = new SolrBrandProductDTO();
    if (row.length > 0) dto.setId((Integer) row[0]);
    if (row.length > 1) dto.setName((String) row[1]);
    // Add remaining fields as needed
    return dto;
}
```

---

### Category 3: Double Dereferencing (Priority 1)
**Count:** 6 errors
**Lines:** Around 2331, others
**Time:** 30 minutes

#### Problem Pattern
```java
// CURRENT (BROKEN)
double value = someMethod();
if (value != null) { // ERROR: primitives can't be null
    // ...
}
```

#### Fix Pattern (Option 1: Use Wrapper)
```java
// FIXED
Double value = someMethod(); // Change return type to Double
if (value != null && value != 0.0) {
    // ...
}
```

#### Fix Pattern (Option 2: Remove Null Check)
```java
// FIXED - if null is impossible
double value = someMethod();
if (value != 0.0) { // Don't check for null
    // ...
}
```

#### Fix Pattern (Option 3: Use Optional)
```java
// FIXED - modern approach
Optional<Double> value = Optional.ofNullable(someMethod());
if (value.isPresent() && value.get() != 0.0) {
    // ...
}
```

---

### Category 4: SalesOrder Type Conversions (Priority 2)
**Count:** 4 errors
**Lines:** Around 3200-3300
**Time:** 1 hour

#### Problem Pattern
```java
// CURRENT (BROKEN)
Object result = query.getSingleResult();
SalesOrder order = result; // ERROR: Object can't be cast implicitly
```

#### Fix Pattern
```java
// FIXED
Object result = query.getSingleResult();
SalesOrder order = null;
if (result != null) {
    if (result instanceof SalesOrder) {
        order = (SalesOrder) result;
    } else if (result instanceof Object[]) {
        // Handle tuple result
        Object[] row = (Object[]) result;
        order = convertToSalesOrder(row);
    }
}
```

---

### Category 5: DTO Constructor Errors (Priority 2)
**Count:** 4 errors
**Time:** 1 hour

#### Problem: SalesOrderDTO Constructor
```java
// CURRENT (BROKEN) - passing null where primitive expected
new SalesOrderDTO(
    null,          // ERROR: can't pass null for primitive
    "orderId",
    "status",
    // ...
);
```

#### Fix: Use Proper Default Values
```java
// FIXED
new SalesOrderDTO(
    0,             // Use 0 for int
    "orderId",
    "status",
    // ... or create builder pattern
);
```

#### Better Fix: Add Builder Pattern
```java
// In SalesOrderDTO class:
public static Builder builder() {
    return new Builder();
}

public static class Builder {
    private int id = 0;
    private String orderId = "";
    // ... other fields with defaults

    public Builder id(Integer id) {
        this.id = id != null ? id : 0;
        return this;
    }

    public SalesOrderDTO build() {
        return new SalesOrderDTO(id, orderId, ...);
    }
}

// Usage:
SalesOrderDTO dto = SalesOrderDTO.builder()
    .id(someId)
    .orderId("ORD123")
    .build();
```

---

### Category 6: String to Int Conversions (Priority 2)
**Count:** 2 errors
**Line:** ~3168
**Time:** 15 minutes

#### Problem Pattern
```java
// CURRENT (BROKEN)
String stateStr = "123";
int stateId = stateStr; // ERROR: incompatible types
```

#### Fix Pattern
```java
// FIXED
String stateStr = "123";
int stateId = Integer.parseInt(stateStr);

// Or with error handling:
int stateId = 0;
try {
    stateId = Integer.parseInt(stateStr);
} catch (NumberFormatException e) {
    LOGGER.warn("Invalid state ID: {}", stateStr);
    stateId = 0; // default
}
```

---

### Category 7: Integer to ItemTypes Conversion (Priority 2)
**Count:** 2 errors
**Line:** ~3314
**Time:** 30 minutes

#### Problem Pattern
```java
// CURRENT (BROKEN)
Integer typeId = 1;
ItemTypes itemType = typeId; // ERROR: can't convert Integer to enum
```

#### Fix Pattern (Option 1: valueOf)
```java
// FIXED - if ItemTypes has valueOf method
Integer typeId = 1;
ItemTypes itemType = ItemTypes.valueOf(typeId);
```

#### Fix Pattern (Option 2: fromId)
```java
// FIXED - if ItemTypes has custom fromId method
Integer typeId = 1;
ItemTypes itemType = ItemTypes.fromId(typeId);

// If method doesn't exist, add to ItemTypes enum:
public enum ItemTypes {
    TYPE_A(1),
    TYPE_B(2);

    private final int id;

    ItemTypes(int id) {
        this.id = id;
    }

    public static ItemTypes fromId(Integer id) {
        if (id == null) return null;
        for (ItemTypes type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}
```

---

### Category 8: Symbol Resolution Errors (Priority 3)
**Count:** 8 errors
**Time:** 1 hour

#### Common Issues

**Missing Import:**
```java
// Add missing import
import com.saicon.games.external.SomeClass;
```

**Method Not Found:**
```java
// Check method signature in interface
// Verify correct class being called
// Check if method exists in current version
```

**Variable Not Defined:**
```java
// Declare variable before use
// Check scope
// Verify spelling
```

---

## Systematic Fix Process

### Step 1: Backup Current File
```bash
cd /c/Users/dimit/tradetool_middleware/callcard-components
cp src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java \
   src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java.pre-final-fixes
```

### Step 2: Fix Category by Category
```bash
# 1. Fix interface signatures (30 min)
# Edit ICallCardManagement.java
mvn clean compile -DskipTests 2>&1 | grep ERROR | wc -l

# 2. Fix type conversions (2 hours)
# Edit CallCardManagement.java - add type conversions
mvn clean compile -DskipTests 2>&1 | grep ERROR | wc -l

# 3. Fix null handling (30 min)
# Edit CallCardManagement.java - change double to Double
mvn clean compile -DskipTests 2>&1 | grep ERROR | wc -l

# 4. Fix remaining (1 hour)
# Edit CallCardManagement.java - fix constructors, conversions
mvn clean compile -DskipTests 2>&1 | grep ERROR | wc -l
```

### Step 3: Verify BUILD SUCCESS
```bash
cd /c/Users/dimit/tradetool_middleware/callcard-components
mvn clean compile -DskipTests

# Look for:
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX s
```

---

## Testing After Fixes

### Unit Tests
```bash
# Run tests after BUILD SUCCESS
mvn test

# Check coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Test with dependent modules
cd ../callcard-service
mvn clean compile -DskipTests
```

---

## Common Pitfalls to Avoid

### 1. Don't Over-Cast
```java
// BAD
Object obj = getValue();
String str = (String) ((Object) ((String) obj));

// GOOD
Object obj = getValue();
String str = obj instanceof String ? (String) obj : null;
```

### 2. Handle Nulls Properly
```java
// BAD
if (value != null && value.getId() != null && value.getId() > 0) // too many checks

// GOOD
if (value != null && value.getId() != null) {
    int id = value.getId();
    if (id > 0) {
        // ...
    }
}
```

### 3. Use Appropriate Types
```java
// BAD - mixing primitives and wrappers
Double d = 0.0; // unnecessary boxing

// GOOD
double d = 0.0; // use primitive when null not needed
Double d = null; // use wrapper when null IS needed
```

---

## Quick Commands Reference

### Check Error Count
```bash
cd /c/Users/dimit/tradetool_middleware/callcard-components
mvn clean compile -DskipTests 2>&1 | grep "^\[ERROR\]" | grep "\.java:\[" | wc -l
```

### List All Errors
```bash
mvn clean compile -DskipTests 2>&1 | grep "^\[ERROR\]" | grep "\.java:\["
```

### Check Specific Line
```bash
sed -n '2331p' src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java
```

### Build All Modules
```bash
cd /c/Users/dimit/tradetool_middleware
for module in callcard-entity callcard-ws-api callcard-components callcard-service; do
    echo "Building $module..."
    cd $module && mvn clean install -DskipTests && cd ..
done
```

---

## Success Criteria

- [ ] 0 compilation errors in callcard-components
- [ ] `mvn clean compile` shows BUILD SUCCESS
- [ ] callcard-service builds successfully
- [ ] All modules installed to Maven repository
- [ ] Integration tests pass

---

## After BUILD SUCCESS

### 1. Install Modules
```bash
cd /c/Users/dimit/tradetool_middleware
cd callcard-entity && mvn install -DskipTests
cd ../callcard-ws-api && mvn install -DskipTests
cd ../callcard-components && mvn install -DskipTests
cd ../callcard-service && mvn install -DskipTests
```

### 2. Run Tests
```bash
cd /c/Users/dimit/tradetool_middleware
mvn test
```

### 3. Create Git Commit
```bash
git add .
git commit -m "Phase 3 COMPLETE: CallCard Microservice Build SUCCESS"
git tag -a v1.0.0 -m "CallCard Microservice v1.0.0"
```

### 4. Generate Final Report
- Update PHASE_3_FINAL_COMPLETION_REPORT.md
- Include all metrics and achievements
- Document any known issues
- Provide deployment instructions

---

**Good Luck! You're 82.6% there!**

---

*Last Updated: 2025-12-22*
*Estimated Completion: 4-6 hours*
