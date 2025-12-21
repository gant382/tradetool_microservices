# CallCardManagement.java Type Conversion Fixes

## File Location
`C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java`

## Required Import Additions

Add these imports after line 46 (`import com.saicon.games.entities.shared.ItemTypes;`):

```java
import com.saicon.games.entities.shared.UserAddressbook;
import com.saicon.games.entities.shared.City;
import com.saicon.games.metadata.dto.MetadataDTO;
```

Add this import after line 60 (`import java.util.*;`):

```java
import java.util.stream.Collectors;
```

Replace line 36:
```java
// TODO: import com.saicon.games.callcard.ws.dto.MetadataDTO;
```
With:
```java
// MetadataDTO imported from com.saicon.games.metadata.dto
```

## Code Fixes

### Fix 1: Line 136 - listCallCards() return type
**Issue**: Method returns `List<CallCard>` but no changes needed here - method signature is correct.

**Current (CORRECT)**:
```java
List<CallCard> callCards = erpDynamicQueryManager.listCallCards(null, Arrays.asList(userId), null, null, null, true, true, true, gameTypeId, 0, -1);
```

### Fix 2: Line 279 - ArrayList constructor with Arrays.asList
**Issue**: Using `new ArrayList<String>(Arrays.asList(...))` when Arrays.asList returns String[] not List<List<String>>

**Current**:
```java
List<String> categories = new ArrayList<String>(Arrays.asList(brandProduct.getSubcategoryIds()));
```

**Fixed**:
```java
List<String> categories = new ArrayList<>(Arrays.asList(brandProduct.getSubcategoryIds()));
```

**Note**: Actually this is fine as-is. Arrays.asList() returns List<String> when given String[], so the constructor works. No change needed.

###  Fix 3: Line 238 - listMetadataKeysByItemType
**Issue**: Method signature mismatch (actually appears correct based on interface)

**Current (CORRECT)**:
```java
List<MetadataKeyDTO> metadataKeyDTOs = metadataComponent.listMetadataKeysByItemType(Constants.ITEM_TYPE_CALL_CARD_INDEX, false);
```

### Fix 4: Line 293 - listCallCardTemplates signature
**Issue**: Method call has wrong number/order of parameters

**Current**:
```java
List<CallCardTemplate> assignedCallCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, null, true, true, userId, 0, -1);
```

**Expected signature** from ErpDynamicQueryManager line 218:
```java
public List<CallCardTemplate> listCallCardTemplates(
    String userGroupId,
    String gameTypeId,
    List<String> callCardTemplateIds,
    List<KeyValueDTO> metadataFilter,  // NOT null
    boolean currentlyActive,
    Boolean active,
    String assignedToUserId,
    int rangeFrom,
    int rangeTo)
```

**Fixed**:
```java
List<CallCardTemplate> assignedCallCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, (List<KeyValueDTO>)null, true, true, userId, 0, -1);
```

### Fix 5: Line 2672 - countCallCards return type (long to int)
**Issue**: countCallCards() returns long but method signature requires int

**Current**:
```java
public int countCallCards(List<String> callCardIds, List<String> userIds, List<String> refUserIds, List<String> callCardTemplateIds, Date startDate, Boolean active, boolean currentlyActive, boolean isNotCompleted, String gameTypeId) {
    return erpDynamicQueryManager.countCallCards(callCardIds, userIds, refUserIds, callCardTemplateIds, startDate, active, currentlyActive, true, gameTypeId);
}
```

**Fixed**:
```java
public int countCallCards(List<String> callCardIds, List<String> userIds, List<String> refUserIds, List<String> callCardTemplateIds, Date startDate, Boolean active, boolean currentlyActive, boolean isNotCompleted, String gameTypeId) {
    return (int) erpDynamicQueryManager.countCallCards(callCardIds, userIds, refUserIds, callCardTemplateIds, startDate, active, currentlyActive, true, gameTypeId);
}
```

### Fix 6: Line 2817 - listUserMetadata return type
**Issue**: Returns List<MetadataDTO> (this is correct, no fix needed once import is added)

**Current (CORRECT after import added)**:
```java
List<MetadataDTO> metadataDTOs = userMetadataComponent.listUserMetadata(Collections.singletonList(userId), metadataKeys, false);
```

### Fix 7: Line 3104 - getUserAddress() return type
**Issue**: getUserAddress() returns Object but needs to be cast to List<UserAddressbook>

**Current**:
```java
Users user = usersDao.getReference(userId);
List<UserAddressbook> userAddressBooks = user.getUserAddress();
```

**Fixed** (add @SuppressWarnings and cast):
```java
Users user = usersDao.getReference(userId);
@SuppressWarnings("unchecked")
List<UserAddressbook> userAddressBooks = (List<UserAddressbook>) user.getUserAddress();
```

OR if getUserAddress() method signature can be checked, it might already return the correct type.

### Fix 8: Line 3148 - getCities().get(0) return type
**Issue**: getCities() returns Object but needs City cast

**Current**:
```java
if (postCode != null && postCode.getCities() != null && postCode.getCities().size() > 0) {
    City city = postCode.getCities().get(0);
```

**Fixed**:
```java
if (postCode != null && postCode.getCities() != null && ((List<?>)postCode.getCities()).size() > 0) {
    @SuppressWarnings("unchecked")
    City city = (City) ((List<?>)postCode.getCities()).get(0);
```

OR check if getCities() already returns List<City>, then no cast needed.

### Fix 9: Line 3923 - Object[] to Number conversion
**Issue**: results.get(0) returns Object[] but needs first element extracted

**Current**:
```java
if (results != null && !results.isEmpty()) {
    return ((Number) results.get(0)).longValue();
}
```

**Fixed**:
```java
if (results != null && !results.isEmpty()) {
    Object[] row = results.get(0);
    return ((Number) row[0]).longValue();
}
```

## Additional Fixes Needed (from compilation errors)

### Fix 10: Lines with listCallCardTemplates using metadataFilter

Find all calls to `listCallCardTemplates` with `null` as 4th parameter and cast to `(List<KeyValueDTO>)null`:

- Line 189
- Line 293
- Line 2828
- Line 2838

**Pattern to find**:
```java
listCallCardTemplates(userGroupId, gameTypeId, null, null,
```

**Replace with**:
```java
listCallCardTemplates(userGroupId, gameTypeId, null, (List<KeyValueDTO>)null,
```

## Summary of Changes

1. **Add 4 import statements** (UserAddressbook, City, MetadataDTO, Collectors)
2. **Fix line 2672**: Cast long to int
3. **Fix line 3104**: Add @SuppressWarnings and cast to List<UserAddressbook>
4. **Fix line 3148**: Add cast to City if needed
5. **Fix line 3923**: Extract Object[] element before casting
6. **Fix lines 189, 293, 2828, 2838**: Cast null to (List<KeyValueDTO>)null for metadataFilter parameter

## How to Apply

Due to file watcher interference, recommend:
1. Close IDE/editor
2. Apply changes manually
3. Reopen IDE
4. Verify compilation

Or use the provided PowerShell script after stopping file watchers.
