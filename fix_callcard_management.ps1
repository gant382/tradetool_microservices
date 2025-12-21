# PowerShell script to fix CallCardManagement.java type conversion issues

$filePath = "C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java"

# Read the file content
$content = Get-Content $filePath -Raw

# 1. Add missing imports after line with "import com.saicon.games.entities.shared.ItemTypes;"
$content = $content -replace '(import com\.saicon\.games\.entities\.shared\.ItemTypes;)', '$1
import com.saicon.games.entities.shared.UserAddressbook;
import com.saicon.games.entities.shared.City;
import com.saicon.games.metadata.dto.MetadataDTO;'

# 2. Add Collectors import after java.util.*;
$content = $content -replace '(import java\.util\.\*;)', '$1
import java.util.stream.Collectors;'

# 3. Replace the TODO comment for MetadataDTO
$content = $content -replace '// TODO: import com\.saicon\.games\.callcard\.ws\.dto\.MetadataDTO;', '// MetadataDTO imported from com.saicon.games.metadata.dto'

# Save the file
$content | Set-Content $filePath -NoNewline

Write-Host "Imports added successfully!"
Write-Host "File updated: $filePath"
