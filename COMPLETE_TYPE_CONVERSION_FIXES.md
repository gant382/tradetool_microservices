# Complete Type Conversion Fixes for CallCard Microservice

## Overview
This document contains all type conversion fixes needed for the CallCard microservice to compile successfully.

## File 1: Postcode.java (Stub Enhancement)

**File**: `callcard-components/src/main/java/com/saicon/games/callcard/components/external/Postcode.java`

### Add Import
After `package com.saicon.games.callcard.components.external;`, add:

```java
import com.saicon.games.entities.shared.City;
import java.util.ArrayList;
import java.util.List;
```

### Add Field
After line `private String countryCode;`, add:

```java
private List<City> cities;
```

### Add Method
At the end of the class (before closing brace), add:

```java
public List<City> getCities() {
    return cities;
}

public void setCities(List<City> cities) {
    this.cities = cities;
}
```

**Complete Updated Postcode.java**:
```java
package com.saicon.games.callcard.components.external;

import com.saicon.games.entities.shared.City;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub class for Postcode entity from addressbook/location subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full dependencies. The actual implementation resides in the addressbook module.
 */
public class Postcode {
    // Stub implementation - add fields/methods as needed for compilation
    private Integer postcodeId;
    private String postcode;
    private String city;
    private String stateCode;
    private String countryCode;
    private List<City> cities;

    public Integer getPostcodeId() {
        return postcodeId;
    }

    public void setPostcodeId(Integer postcodeId) {
        this.postcodeId = postcodeId;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }
}
```

## File 2: CallCardManagement.java (Type Conversion Fixes)

**File**: `callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java`

### Step 1: Add Missing Imports

After line 46 (`import com.saicon.games.entities.shared.ItemTypes;`), add:
```java
import com.saicon.games.entities.shared.UserAddressbook;
import com.saicon.games.entities.shared.City;
import com.saicon.games.metadata.dto.MetadataDTO;
```

After line 60 (`import java.util.*;`), add:
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

### Step 2: Fix Line 2672 - long to int conversion

**Find** (around line 2672):
```java
public int countCallCards(List<String> callCardIds, List<String> userIds, List<String> refUserIds, List<String> callCardTemplateIds, Date startDate, Boolean active, boolean currentlyActive, boolean isNotCompleted, String gameTypeId) {
    return erpDynamicQueryManager.countCallCards(callCardIds, userIds, refUserIds, callCardTemplateIds, startDate, active, currentlyActive, true, gameTypeId);
}
```

**Replace with**:
```java
public int countCallCards(List<String> callCardIds, List<String> userIds, List<String> refUserIds, List<String> callCardTemplateIds, Date startDate, Boolean active, boolean currentlyActive, boolean isNotCompleted, String gameTypeId) {
    return (int) erpDynamicQueryManager.countCallCards(callCardIds, userIds, refUserIds, callCardTemplateIds, startDate, active, currentlyActive, true, gameTypeId);
}
```

### Step 3: Fix Line 3104 - Object to List<UserAddressbook> cast

**Find** (around line 3104):
```java
Users user = usersDao.getReference(userId);
List<UserAddressbook> userAddressBooks = user.getUserAddress();
```

**Replace with**:
```java
Users user = usersDao.getReference(userId);
@SuppressWarnings("unchecked")
List<UserAddressbook> userAddressBooks = (List<UserAddressbook>) user.getUserAddress();
```

### Step 4: Fix Line 3923 - Object[] to Number conversion

**Find** (around line 3923):
```java
if (results != null && !results.isEmpty()) {
    return ((Number) results.get(0)).longValue();
}
```

**Replace with**:
```java
if (results != null && !results.isEmpty()) {
    Object[] row = results.get(0);
    return ((Number) row[0]).longValue();
}
```

### Step 5: Fix All listCallCardTemplates Calls with null metadataFilter

**Find pattern** (4 occurrences at lines 189, 293, 2828, 2838):
```java
listCallCardTemplates(userGroupId, gameTypeId, null, null,
```

**Replace with**:
```java
listCallCardTemplates(userGroupId, gameTypeId, null, (List<com.saicon.multiplayer.dto.KeyValueDTO>)null,
```

**Specific Fixes**:

#### Line 189:
**Before**:
```java
return erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, callCardTemplateId != null ? Arrays.asList(callCardTemplateId) : null, metadataFilter, true, true, null, 0, -1);
```
**After** (no change needed - metadataFilter is already typed):
```java
return erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, callCardTemplateId != null ? Arrays.asList(callCardTemplateId) : null, metadataFilter, true, true, null, 0, -1);
```

#### Line 293:
**Before**:
```java
List<CallCardTemplate> assignedCallCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, null, true, true, userId, 0, -1);
```
**After**:
```java
List<CallCardTemplate> assignedCallCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, (List<com.saicon.multiplayer.dto.KeyValueDTO>)null, true, true, userId, 0, -1);
```

#### Line 2828:
**Before**:
```java
List<CallCardTemplate> templates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, metadataFilter, true, true, null, 0, -1);
```
**After** (no change needed - metadataFilter is already typed):
```java
List<CallCardTemplate> templates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, metadataFilter, true, true, null, 0, -1);
```

#### Line 2838:
**Before**:
```java
List<CallCardTemplate> callCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, null, true, true, userId, 0, -1);
```
**After**:
```java
List<CallCardTemplate> callCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, (List<com.saicon.multiplayer.dto.KeyValueDTO>)null, true, true, userId, 0, -1);
```

## Summary of All Changes

### Postcode.java
1. Add imports: City, ArrayList, List
2. Add field: `private List<City> cities;`
3. Add methods: `getCities()` and `setCities()`

### CallCardManagement.java
1. Add imports: UserAddressbook, City, MetadataDTO, Collectors
2. Fix line 2672: Cast long to int with `(int)`
3. Fix line 3104: Add @SuppressWarnings and cast Object to List<UserAddressbook>
4. Fix line 3923: Extract Object[] element before casting to Number
5. Fix line 293: Cast null to (List<KeyValueDTO>)null
6. Fix line 2838: Cast null to (List<KeyValueDTO>)null

## Application Order

1. **First**: Fix Postcode.java (it's a dependency)
2. **Second**: Fix CallCardManagement.java imports
3. **Third**: Fix CallCardManagement.java code

## Verification

After applying all fixes, compile with:
```bash
cd C:\Users\dimit\tradetool_middleware
mvn clean compile -pl callcard-components -am
```

## Notes

- A file watcher or IDE may be modifying files automatically
- Consider closing the IDE before applying changes
- The MetadataDTO comes from `com.saicon.games.metadata.dto`, not `com.saicon.games.callcard.ws.dto`
- All casts are safe given the stub implementations and expected data structures
