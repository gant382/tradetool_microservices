# PowerShell script to add throws BusinessLayerException to method signatures

$filePath = "C:/Users/dimit/tradetool_middleware/callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java"

# Read the entire file
$content = Get-Content $filePath -Raw -Encoding UTF8

# Define all the replacements
$replacements = @(
    @{
        Old = 'private List<CallCardTemplate> getCallCardTemplateByMetadataKeys(String userId, String userGroupId, String gameTypeId, String callCardTemplateId, List<String> metadataKeys) {'
        New = 'private List<CallCardTemplate> getCallCardTemplateByMetadataKeys(String userId, String userGroupId, String gameTypeId, String callCardTemplateId, List<String> metadataKeys) throws BusinessLayerException {'
    },
    @{
        Old = 'public CallCardDTO getNewOrPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId, String callCardId, List<String> filterProperties) {'
        New = 'public CallCardDTO getNewOrPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId, String callCardId, List<String> filterProperties) throws BusinessLayerException {'
    },
    @{
        Old = 'public void addCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<CallCardGroupDTO> groups) {'
        New = 'public void addCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<CallCardGroupDTO> groups) throws BusinessLayerException {'
    },
    @{
        Old = 'public void createOrUpdateSimplifiedCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<SimplifiedCallCardRefUserDTO> refUserIds) {'
        New = 'public void createOrUpdateSimplifiedCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<SimplifiedCallCardRefUserDTO> refUserIds) throws BusinessLayerException {'
    },
    @{
        Old = 'public void addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, SimplifiedCallCardDTO callCard) {'
        New = 'public void addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, SimplifiedCallCardDTO callCard) throws BusinessLayerException {'
    },
    @{
        Old = 'private CallCardTemplate getCallCardTemplateByMetadataProperty(String userGroupId, String gameTypeId, String userId) {'
        New = 'private CallCardTemplate getCallCardTemplateByMetadataProperty(String userGroupId, String gameTypeId, String userId) throws BusinessLayerException {'
    },
    @{
        Old = 'private CallCardTemplate getCallCardTemplate(String userGroupId, String gameTypeId, String applicationId, String userId) {'
        New = 'private CallCardTemplate getCallCardTemplate(String userGroupId, String gameTypeId, String applicationId, String userId) throws BusinessLayerException {'
    },
    @{
        Old = 'public void submitTransactions(String userId, String userGroupId, String gameTypeId, String applicationId, String indirectUserId, CallCardDTO callCardDTO) {'
        New = 'public void submitTransactions(String userId, String userGroupId, String gameTypeId, String applicationId, String indirectUserId, CallCardDTO callCardDTO) throws BusinessLayerException {'
    },
    @{
        Old = 'private void createOrUpdateAdditionalRefUserInfo(String userId, List<KeyValueDTO> additionalRefUserInfo) {'
        New = 'private void createOrUpdateAdditionalRefUserInfo(String userId, List<KeyValueDTO> additionalRefUserInfo) throws BusinessLayerException {'
    },
    @{
        Old = 'public CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) {'
        New = 'public CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'
    },
    @{
        Old = 'public TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) {'
        New = 'public TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'
    }
)

# Apply all replacements
$modified = $false
foreach ($replacement in $replacements) {
    if ($content -match [regex]::Escape($replacement.Old)) {
        $content = $content -replace [regex]::Escape($replacement.Old), $replacement.New
        $modified = $true
        Write-Host "Applied: $($replacement.Old.Substring(0, [Math]::Min(50, $replacement.Old.Length)))..."
    } else {
        Write-Host "NOT FOUND: $($replacement.Old.Substring(0, [Math]::Min(50, $replacement.Old.Length)))..."
    }
}

if ($modified) {
    # Save the file
    [System.IO.File]::WriteAllText($filePath, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "`nFile updated successfully!"
} else {
    Write-Host "`nNo changes made - patterns not found."
}
