# CallCard Statistics API - Quick Reference Guide

## Base URLs

- **SOAP Endpoint:** `http://localhost:8080/cxf/CallCardStatisticsService`
- **SOAP WSDL:** `http://localhost:8080/cxf/CallCardStatisticsService?wsdl`
- **REST Base:** `http://localhost:8080/rest/callcard/statistics`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (if enabled)

## REST API Quick Reference

### Common Headers
```
X-Talos-User-Group-Id: {userGroupId}  (Required for all endpoints)
Accept: application/json
```

### Common Query Parameters
```
dateFrom: ISO 8601 date (optional) - e.g., 2025-01-01T00:00:00
dateTo: ISO 8601 date (optional) - e.g., 2025-12-31T23:59:59
```

---

## 1. Get Overall Statistics

**GET** `/overall`

**Description:** Get comprehensive CallCard statistics for the entire user group

**Query Params:**
- `dateFrom` (optional) - Start date
- `dateTo` (optional) - End date

**Response:** CallCardStatsDTO
```json
{
  "userGroupId": "uuid",
  "dateFrom": "2025-01-01T00:00:00",
  "dateTo": "2025-12-31T23:59:59",
  "totalCallCards": 1250,
  "activeCallCards": 320,
  "submittedCallCards": 930,
  "totalUsers": 85,
  "totalTemplates": 12,
  "totalRefUsers": 4500,
  "averageUsersPerCallCard": 3.6,
  "averageCallCardsPerUser": 14.7,
  "averageCompletionTimeMinutes": 45
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/rest/callcard/statistics/overall?dateFrom=2025-01-01&dateTo=2025-12-31" \
  -H "X-Talos-User-Group-Id: 12345678-1234-1234-1234-123456789012" \
  -H "Accept: application/json"
```

---

## 2. Get Template Usage Statistics

**GET** `/template/{templateId}`

**Description:** Get usage statistics for a specific CallCard template

**Path Params:**
- `templateId` (required) - Template UUID

**Query Params:**
- `dateFrom` (optional) - Start date
- `dateTo` (optional) - End date

**Response:** TemplateUsageDTO
```json
{
  "templateId": "uuid",
  "templateName": "Monthly Sales Report",
  "userGroupId": "uuid",
  "usageCount": 245,
  "uniqueUsers": 42,
  "activeCount": 68,
  "submittedCount": 177,
  "completionRate": 72.24,
  "averageCompletionTimeMinutes": 38,
  "lastUsedDate": "2025-12-20T14:30:00",
  "firstUsedDate": "2025-01-15T09:00:00",
  "totalRefUsers": 892,
  "averageRefUsersPerCallCard": 3.64
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/rest/callcard/statistics/template/TEMPLATE-UUID" \
  -H "X-Talos-User-Group-Id: 12345678-1234-1234-1234-123456789012" \
  -H "Accept: application/json"
```

---

## 3. Get User Engagement Statistics

**GET** `/user/{userId}`

**Description:** Get engagement statistics for a specific user

**Path Params:**
- `userId` (required) - User UUID

**Query Params:**
- `dateFrom` (optional) - Start date
- `dateTo` (optional) - End date

**Response:** UserEngagementDTO
```json
{
  "userId": "uuid",
  "userName": "john.doe",
  "userGroupId": "uuid",
  "totalCallCards": 38,
  "activeCallCards": 9,
  "submittedCallCards": 29,
  "completionRate": 76.32,
  "totalRefUsers": 142,
  "averageRefUsersPerCallCard": 3.74,
  "uniqueTemplatesUsed": 7,
  "mostUsedTemplateId": "uuid",
  "mostUsedTemplateName": "Monthly Sales Report",
  "averageCompletionTimeMinutes": 42,
  "lastActivityDate": "2025-12-20T16:45:00",
  "firstActivityDate": "2025-01-10T08:30:00",
  "activityDaysCount": 156
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/rest/callcard/statistics/user/USER-UUID" \
  -H "X-Talos-User-Group-Id: 12345678-1234-1234-1234-123456789012" \
  -H "Accept: application/json"
```

---

## 4. Get Top Templates

**GET** `/templates/top`

**Description:** Get N most used templates, ranked by usage count

**Query Params:**
- `limit` (optional, default: 10) - Maximum number of templates
- `dateFrom` (optional) - Start date
- `dateTo` (optional) - End date

**Response:** Array of TemplateUsageDTO
**Response Header:** `X-Talos-Item-Count: {count}`

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/rest/callcard/statistics/templates/top?limit=5" \
  -H "X-Talos-User-Group-Id: 12345678-1234-1234-1234-123456789012" \
  -H "Accept: application/json"
```

---

## 5. Get Active Users Count

**GET** `/users/active/count`

**Description:** Get count of unique users who created or modified CallCards

**Query Params:**
- `dateFrom` (optional) - Start date
- `dateTo` (optional) - End date

**Response:**
```json
{
  "activeUsersCount": 85
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/rest/callcard/statistics/users/active/count?dateFrom=2025-01-01" \
  -H "X-Talos-User-Group-Id: 12345678-1234-1234-1234-123456789012" \
  -H "Accept: application/json"
```

---

## 6. Get All User Engagement Statistics

**GET** `/users/engagement`

**Description:** Get engagement statistics for all users in the user group

**Query Params:**
- `limit` (optional, default: 100) - Maximum number of users
- `dateFrom` (optional) - Start date
- `dateTo` (optional) - End date

**Response:** Array of UserEngagementDTO
**Response Header:** `X-Talos-Item-Count: {count}`

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/rest/callcard/statistics/users/engagement?limit=10" \
  -H "X-Talos-User-Group-Id: 12345678-1234-1234-1234-123456789012" \
  -H "Accept: application/json"
```

---

## 7. Get All Template Usage Statistics

**GET** `/templates/usage`

**Description:** Get usage statistics for all templates in the user group

**Query Params:**
- `dateFrom` (optional) - Start date
- `dateTo` (optional) - End date

**Response:** Array of TemplateUsageDTO
**Response Header:** `X-Talos-Item-Count: {count}`

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/rest/callcard/statistics/templates/usage" \
  -H "X-Talos-User-Group-Id: 12345678-1234-1234-1234-123456789012" \
  -H "Accept: application/json"
```

---

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful, data returned |
| 204 | No Content - Request successful, but no data found |
| 400 | Bad Request - Invalid parameters (e.g., invalid UUID) |
| 404 | Not Found - Resource not found (e.g., template, user) |
| 500 | Internal Server Error - Unexpected server error |

---

## Error Response Format

```json
{
  "errorNumber": "1001",
  "message": "Invalid userGroupId: must be a valid UUID",
  "status": "ERROR"
}
```

---

## Common Use Cases

### 1. Dashboard Overview
```bash
# Get overall stats for current month
curl -X GET "http://localhost:8080/rest/callcard/statistics/overall?dateFrom=2025-12-01" \
  -H "X-Talos-User-Group-Id: {uuid}"
```

### 2. Template Performance Report
```bash
# Get top 10 templates for Q4
curl -X GET "http://localhost:8080/rest/callcard/statistics/templates/top?limit=10&dateFrom=2025-10-01&dateTo=2025-12-31" \
  -H "X-Talos-User-Group-Id: {uuid}"
```

### 3. User Activity Report
```bash
# Get most active users this year
curl -X GET "http://localhost:8080/rest/callcard/statistics/users/engagement?limit=20&dateFrom=2025-01-01" \
  -H "X-Talos-User-Group-Id: {uuid}"
```

### 4. Template Deep Dive
```bash
# Get detailed stats for specific template
curl -X GET "http://localhost:8080/rest/callcard/statistics/template/{templateId}" \
  -H "X-Talos-User-Group-Id: {uuid}"
```

### 5. User Performance Review
```bash
# Get engagement stats for specific user
curl -X GET "http://localhost:8080/rest/callcard/statistics/user/{userId}" \
  -H "X-Talos-User-Group-Id: {uuid}"
```

---

## SOAP Operations

For SOAP-based integrations, use the following operations:

**WSDL:** `http://localhost:8080/cxf/CallCardStatisticsService?wsdl`

**Operations:**
1. `getCallCardStatistics`
2. `getTemplateUsageStats`
3. `getUserEngagementStats`
4. `getTopTemplates`
5. `getActiveUsersCount`
6. `getAllUserEngagementStats`
7. `getAllTemplateUsageStats`

**Sample SOAP Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ws="http://ws.callcard.statistics.saicon.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:getCallCardStatistics>
         <userGroupId>12345678-1234-1234-1234-123456789012</userGroupId>
         <dateFrom>2025-01-01T00:00:00</dateFrom>
         <dateTo>2025-12-31T23:59:59</dateTo>
      </ws:getCallCardStatistics>
   </soapenv:Body>
</soapenv:Envelope>
```

---

## JavaScript/TypeScript Integration

```typescript
// Using Fetch API
const getOverallStatistics = async (userGroupId: string, dateFrom?: string, dateTo?: string) => {
  const params = new URLSearchParams();
  if (dateFrom) params.append('dateFrom', dateFrom);
  if (dateTo) params.append('dateTo', dateTo);

  const response = await fetch(
    `http://localhost:8080/rest/callcard/statistics/overall?${params}`,
    {
      headers: {
        'X-Talos-User-Group-Id': userGroupId,
        'Accept': 'application/json'
      }
    }
  );

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
  }

  return await response.json();
};

// Using Axios
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/rest/callcard/statistics',
  headers: {
    'Accept': 'application/json'
  }
});

const getTemplateStats = async (userGroupId: string, templateId: string) => {
  const response = await api.get(`/template/${templateId}`, {
    headers: {
      'X-Talos-User-Group-Id': userGroupId
    }
  });

  return response.data;
};
```

---

## Python Integration

```python
import requests
from typing import Optional, Dict, Any

class CallCardStatisticsClient:
    def __init__(self, base_url: str, user_group_id: str):
        self.base_url = base_url
        self.user_group_id = user_group_id
        self.headers = {
            'X-Talos-User-Group-Id': user_group_id,
            'Accept': 'application/json'
        }

    def get_overall_statistics(
        self,
        date_from: Optional[str] = None,
        date_to: Optional[str] = None
    ) -> Dict[str, Any]:
        params = {}
        if date_from:
            params['dateFrom'] = date_from
        if date_to:
            params['dateTo'] = date_to

        response = requests.get(
            f'{self.base_url}/overall',
            headers=self.headers,
            params=params
        )
        response.raise_for_status()
        return response.json()

    def get_top_templates(
        self,
        limit: int = 10,
        date_from: Optional[str] = None,
        date_to: Optional[str] = None
    ) -> list:
        params = {'limit': limit}
        if date_from:
            params['dateFrom'] = date_from
        if date_to:
            params['dateTo'] = date_to

        response = requests.get(
            f'{self.base_url}/templates/top',
            headers=self.headers,
            params=params
        )
        response.raise_for_status()
        return response.json()

# Usage
client = CallCardStatisticsClient(
    'http://localhost:8080/rest/callcard/statistics',
    '12345678-1234-1234-1234-123456789012'
)

stats = client.get_overall_statistics(date_from='2025-01-01')
top_templates = client.get_top_templates(limit=5)
```

---

## Postman Collection

Import this JSON into Postman for easy testing:

```json
{
  "info": {
    "name": "CallCard Statistics API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/rest/callcard/statistics"
    },
    {
      "key": "userGroupId",
      "value": "12345678-1234-1234-1234-123456789012"
    }
  ],
  "item": [
    {
      "name": "Get Overall Statistics",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "X-Talos-User-Group-Id",
            "value": "{{userGroupId}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/overall?dateFrom=2025-01-01&dateTo=2025-12-31",
          "host": ["{{baseUrl}}"],
          "path": ["overall"],
          "query": [
            {"key": "dateFrom", "value": "2025-01-01"},
            {"key": "dateTo", "value": "2025-12-31"}
          ]
        }
      }
    }
  ]
}
```

---

**Last Updated:** December 21, 2025
**Version:** 1.0
**API Status:** Production Ready
