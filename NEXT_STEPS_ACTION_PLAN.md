# CallCard Microservice - Next Steps Action Plan

**Current Status**: 40% Complete - Core infrastructure ready, imports need fixing
**Estimated Time to MVP**: 4-5 hours of focused work
**Date**: 2025-12-21

---

## Quick Start: Resume Implementation

### What's Been Done ✅
1. Core utility classes created (GenericDAO, UUIDGenerator, Assert, QueryManagers)
2. All 8 entity files fixed (UUIDGenerator imports corrected)
3. Configuration layer partially fixed (ComponentConfiguration imports updated)

### What's Left ⏳
1. Fix ~200+ import statements across 20+ files
2. Create exception and response wrapper classes
3. Add setter methods to component classes
4. Implement stub methods (or real implementations)
5. Fix POM dependencies
6. Build and test

---

## Action Plan: Complete in 5 Steps

### STEP 1: Create Missing Foundation Classes (45 min)

Create these files to enable compilation:

#### 1.1 Exception Classes (15 min)
```bash
cd /c/Users/dimit/tradetool_middleware/callcard-ws-api/src/main/java/com/saicon/games/callcard/exception
```

**Create `BusinessLayerException.java`**:
```java
package com.saicon.games.callcard.exception;

public class BusinessLayerException extends Exception {
    private String errorCode;

    public BusinessLayerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

**Create `ExceptionTypeTO.java`**:
```java
package com.saicon.games.callcard.exception;

public class ExceptionTypeTO {
    public static final String NONE = "0000";
    public static final String GENERAL_ERROR = "1000";
    public static final String INVALID_INPUT = "1001";
    public static final String NOT_FOUND = "1002";
    public static final String UNAUTHORIZED = "1003";

    private String errorCode;
    private String message;

    public ExceptionTypeTO(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
}
```

#### 1.2 Response Wrapper Classes (20 min)
```bash
cd /c/Users/dimit/tradetool_middleware/callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/response
```

**Create `ResponseStatus.java`** (enum):
```java
package com.saicon.games.callcard.ws.response;

public enum ResponseStatus {
    OK, ERROR, WARNING
}
```

**Create `WSResponse.java`**:
```java
package com.saicon.games.callcard.ws.response;

public class WSResponse {
    private String errorCode;
    private String message;
    private ResponseStatus status;

    public WSResponse(String errorCode, ResponseStatus status) {
        this(errorCode, "", status);
    }

    public WSResponse(String errorCode, String message, ResponseStatus status) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }

    // Getters/setters
}
```

**Create `ResponseListCallCard.java`**:
```java
package com.saicon.games.callcard.ws.response;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import java.util.List;

public class ResponseListCallCard extends WSResponse {
    private List<CallCardDTO> items;
    private int totalCount;

    public ResponseListCallCard(String errorCode, ResponseStatus status, List<CallCardDTO> items, int totalCount) {
        super(errorCode, status);
        this.items = items;
        this.totalCount = totalCount;
    }

    // Getters/setters
}
```

Create similar classes for:
- `ResponseListSimplifiedCallCard.java`
- `ResponseListItemStatistics.java`

#### 1.3 Stub Interfaces (10 min)
```bash
cd /c/Users/dimit/tradetool_middleware/callcard-components/src/main/java/com/saicon/games/callcard/components/external
```

Create stub interfaces that throw UnsupportedOperationException:
- `ISalesOrderManagement.java`
- `IAddressbookManagement.java`
- `IAppSettingsComponent.java`
- `IMetadataComponent.java`
- `IUserMetadataComponent.java`
- `IUserSessionManagement.java`

Template:
```java
package com.saicon.games.callcard.components.external;

public interface ISalesOrderManagement {
    // All methods throw: throw new UnsupportedOperationException("Sales Order integration not yet implemented in microservice");
}
```

---

### STEP 2: Fix All Import Statements (60 min)

Use find-replace to fix package names systematically.

#### 2.1 CallCardManagement.java (20 min)

1. Change package:
   ```java
   package com.saicon.games.callcard.components.impl;
   ```

2. Fix imports (use this mapping):
   ```
   OLD → NEW
   com.saicon.games.entities.common.IGenericDAO → com.saicon.games.callcard.dao.IGenericDAO
   com.saicon.games.components.ErpDynamicQueryManager → com.saicon.games.callcard.components.ErpDynamicQueryManager
   com.saicon.games.components.ErpNativeQueryManager → com.saicon.games.callcard.components.ErpNativeQueryManager
   com.saicon.games.commons.utilities.Assert → com.saicon.games.callcard.util.Assert
   com.saicon.games.commons.exceptions.BusinessLayerException → com.saicon.games.callcard.exception.BusinessLayerException
   com.saicon.games.commons.exceptions.ExceptionTypeTO → com.saicon.games.callcard.exception.ExceptionTypeTO
   com.saicon.callCard.dto.* → com.saicon.games.callcard.ws.dto.*
   com.saicon.games.entities.* → com.saicon.games.callcard.entity.* (for CallCard entities)
   com.saicon.user.entities.Users → com.saicon.games.entities.shared.Users
   com.saicon.generic.entities.ItemTypes → com.saicon.games.entities.shared.ItemTypes
   com.saicon.application.entities.Application → com.saicon.games.entities.shared.Application
   ```

3. Remove or stub these imports:
   - `com.saicon.addressbook.entities.*` → Remove or create stubs
   - `com.saicon.ecommerce.dto.*` → Remove or stub
   - `com.saicon.games.appsettings.dto.*` → Remove or stub
   - All external component interfaces → Use stub interfaces from Step 1.3

4. Add setter methods:
   ```java
   public void setCallCardDao(IGenericDAO<CallCard, String> callCardDao) {
       this.callCardDao = callCardDao;
   }
   // ... repeat for all 13 DAO/manager fields
   ```

#### 2.2 ICallCardManagement.java (5 min)

Fix DTO imports:
```
com.saicon.callCard.dto.* → com.saicon.games.callcard.ws.dto.*
```

#### 2.3 CallCardService.java (10 min)

1. Change package:
   ```java
   package com.saicon.games.callcard.service;
   ```

2. Fix imports:
   ```
   com.saicon.games.core.components.ICallCardManagement → com.saicon.games.callcard.components.ICallCardManagement
   com.saicon.callCard.dto.* → com.saicon.games.callcard.ws.dto.*
   com.saicon.games.ws.commons.* → com.saicon.games.callcard.ws.response.*
   com.saicon.games.commons.exceptions.* → com.saicon.games.callcard.exception.*
   ```

3. Add setters:
   ```java
   public void setCallCardManagement(ICallCardManagement callCardManagement) {
       this.callCardManagement = callCardManagement;
   }

   public void setUserSessionManagement(IUserSessionManagement userSessionManagement) {
       this.userSessionManagement = userSessionManagement;
   }
   ```

#### 2.4 ICallCardService.java (5 min)

Fix imports:
```
com.saicon.callCard.dto.* → com.saicon.games.callcard.ws.dto.*
com.saicon.games.ws.commons.WSResponse → com.saicon.games.callcard.ws.response.WSResponse
com.saicon.games.ws.data.* → com.saicon.games.callcard.ws.response.*
```

#### 2.5 CallCardResources.java (15 min)

1. Change package:
   ```java
   package com.saicon.games.callcard.resources;
   ```

2. Fix imports (same pattern as CallCardService)

3. Remove ITalosResource interface (or create stub)

4. Verify JAX-RS annotations are complete

#### 2.6 All DTO Files (10 DTOs × 1 min = 10 min)

For each DTO in `callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/`:
- Fix any wrong imports
- Ensure package is `com.saicon.games.callcard.ws.dto`
- Verify all referenced classes exist

---

### STEP 3: Fix POM Dependencies (30 min)

#### 3.1 Root pom.xml
Add dependency management:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>2.7.18</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 3.2 callcard-entity/pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>5.6.15.Final</version>
    </dependency>
    <dependency>
        <groupId>javax.persistence</groupId>
        <artifactId>javax.persistence-api</artifactId>
        <version>2.2</version>
    </dependency>
</dependencies>
```

#### 3.3 callcard-components/pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>com.saicon.callcard</groupId>
        <artifactId>callcard-entity</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
    </dependency>
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
</dependencies>
```

#### 3.4 callcard-service/pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>com.saicon.callcard</groupId>
        <artifactId>callcard-components</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.saicon.callcard</groupId>
        <artifactId>callcard-ws-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.apache.cxf</groupId>
        <artifactId>cxf-rt-frontend-jaxws</artifactId>
        <version>3.5.5</version>
    </dependency>
</dependencies>
```

#### 3.5 callcard-ws-api/pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>com.saicon.callcard</groupId>
        <artifactId>callcard-entity</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>javax.jws</groupId>
        <artifactId>javax.jws-api</artifactId>
        <version>1.1</version>
    </dependency>
    <dependency>
        <groupId>javax.ws.rs</groupId>
        <artifactId>javax.ws.rs-api</artifactId>
        <version>2.1.1</version>
    </dependency>
    <dependency>
        <groupId>io.swagger</groupId>
        <artifactId>swagger-annotations</artifactId>
        <version>1.6.9</version>
    </dependency>
</dependencies>
```

#### 3.6 CallCard_Server_WS/pom.xml
```xml
<dependencies>
    <!-- All modules -->
    <dependency>
        <groupId>com.saicon.callcard</groupId>
        <artifactId>callcard-service</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- CXF -->
    <dependency>
        <groupId>org.apache.cxf</groupId>
        <artifactId>cxf-spring-boot-starter-jaxws</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- Jersey -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jersey</artifactId>
    </dependency>

    <!-- SQL Server -->
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
    </dependency>
</dependencies>
```

---

### STEP 4: Build and Debug (45-90 min)

#### 4.1 First Build Attempt
```bash
cd /c/Users/dimit/tradetool_middleware
mvn clean install -DskipTests
```

#### 4.2 Fix Compilation Errors
- Read error messages carefully
- Fix missing imports
- Fix method signature mismatches
- Create any missing stub classes

#### 4.3 Iterative Build
Repeat until clean build:
```bash
mvn clean install -DskipTests
```

#### 4.4 Run Tests (Optional for MVP)
```bash
mvn clean install
```

---

### STEP 5: Start and Verify (20 min)

#### 5.1 Start Microservice
```bash
cd CallCard_Server_WS
mvn spring-boot:run
```

#### 5.2 Verify Endpoints

**WSDL**:
```bash
curl http://localhost:8080/services/CallCardService?wsdl
```

**REST Health** (if configured):
```bash
curl http://localhost:8080/actuator/health
```

**REST API**:
```bash
curl http://localhost:8080/api/callcard/template/{userId}
```

#### 5.3 Test Basic SOAP Call
Use SoapUI or curl to call a simple operation like `getCallCardsFromTemplate`.

---

## Quick Reference: Package Mappings

| Original Package | New Microservice Package |
|------------------|--------------------------|
| `com.saicon.games.entities.common.*` | `com.saicon.games.callcard.dao.*` |
| `com.saicon.games.components.Erp*` | `com.saicon.games.callcard.components.Erp*` |
| `com.saicon.games.commons.utilities.Assert` | `com.saicon.games.callcard.util.Assert` |
| `com.saicon.games.commons.exceptions.*` | `com.saicon.games.callcard.exception.*` |
| `com.saicon.callCard.dto.*` | `com.saicon.games.callcard.ws.dto.*` |
| `com.saicon.games.ws.commons.*` | `com.saicon.games.callcard.ws.response.*` |
| `com.saicon.games.entities.*` (CallCard) | `com.saicon.games.callcard.entity.*` |
| `com.saicon.user.entities.Users` | `com.saicon.games.entities.shared.Users` |
| `com.saicon.generic.entities.ItemTypes` | `com.saicon.games.entities.shared.ItemTypes` |

---

## Troubleshooting

### Common Issues

**Issue**: Missing class errors
- **Solution**: Create stub class in correct package

**Issue**: Method not found
- **Solution**: Check method signature matches interface

**Issue**: Circular dependency
- **Solution**: Review module dependencies in POMs

**Issue**: Spring Boot won't start
- **Solution**: Check application.properties, verify datasource config

**Issue**: CXF endpoint not accessible
- **Solution**: Verify CxfConfiguration, check port 8080 is free

---

## Success Checklist

- [ ] All Java files compile without errors
- [ ] Maven build succeeds: `mvn clean install -DskipTests`
- [ ] Application starts: `mvn spring-boot:run`
- [ ] WSDL accessible at /services/CallCardService?wsdl
- [ ] REST endpoints respond (even if 501 Not Implemented)
- [ ] No exceptions in startup logs

---

## Notes

- **This is MVP** - Focus on getting it to compile and run first
- **Stub liberally** - External dependencies can be implemented later
- **Test incrementally** - Build after each major change
- **Document assumptions** - Note what's stubbed vs implemented

---

## Contact/Support

- Review `PHASE3_IMPLEMENTATION_STATUS.md` for detailed status
- Check `ENTITY_COPY_REPORT.md` for entity migration details
- Refer to `specs/001-callcard-microservice/tasks.md` for full task list

---

**Good luck! You're 40% done - keep going!** 🚀
