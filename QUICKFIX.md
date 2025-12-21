# Quick Fix Guide for CallCardManagement.java

## TL;DR - Just Fix It!

```powershell
cd C:\Users\dimit\tradetool_middleware
.\Fix-CallCardManagementImports.ps1
```

That's it! The script will:
- ✅ Create backup
- ✅ Fix all 45 imports
- ✅ Comment out 22 stubs
- ✅ Show summary

## What Gets Fixed

| What | Before | After |
|------|--------|-------|
| Package | `com.saicon.games.core.components.impl` | `com.saicon.games.callcard.components.impl` |
| Entities | `com.saicon.application.entities.*` | `com.saicon.games.entities.shared.*` |
| Exceptions | `com.saicon.games.commons.exceptions.*` | `com.saicon.games.callcard.exception.*` |
| Utils | `com.saicon.games.commons.utilities.*` | `com.saicon.games.callcard.util.*` |
| DAO | `com.saicon.games.entities.common.*` | `com.saicon.games.callcard.dao.*` |
| Components | `com.saicon.games.core.components.*` | `com.saicon.games.callcard.components.*` |
| DTOs | `com.saicon.callCard.dto.*` | `com.saicon.games.callcard.ws.dto.*` |

## After Running

```bash
# Check what changed
git diff callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java

# Try to compile
cd callcard-components && mvn clean compile

# See errors (if any)
mvn compile 2>&1 | grep "error:"
```

## Top Priority Stubs to Implement

1. `Constants` - Used everywhere
2. `EventType` / `EventTO` - Events
3. `MetadataDTO` / `KeyValueDTO` - Metadata
4. `AppSettingsDTO` / `ScopeType` - Settings

## Rollback If Needed

```bash
cd C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl
mv CallCardManagement.java.backup CallCardManagement.java
```

## Files Created

- `Fix-CallCardManagementImports.ps1` - The fix script
- `IMPORT_FIX_SUMMARY.md` - Full documentation
- `CALLCARD_MANAGEMENT_IMPORT_FIXES.md` - Manual fix guide
- `QUICKFIX.md` - This file

## Help

Something wrong? Check:
1. Is file open in IDE? Close it first
2. Permission denied? Run as Administrator
3. Still stuck? See `IMPORT_FIX_SUMMARY.md`
