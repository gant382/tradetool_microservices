# CallCard Microservice - Postman Collection

**Created:** December 27, 2025
**Version:** 1.0.0

This directory contains a complete Postman collection and environment files for testing the CallCard microservice API.

---

## 📦 Files Included

### Collection
- **CallCard_Microservice_API.postman_collection.json** - Complete API collection with all endpoints

### Environments
- **CallCard_Environments.postman_environment.json** - EC2 Internal (Direct to port 8080)
- **CallCard_EC2_Nginx.postman_environment.json** - EC2 via Nginx (Port 80)
- **CallCard_Public_DNS.postman_environment.json** - Public DNS (HTTPS) - *for when external gateway is configured*

---

## 🚀 Quick Start

### 1. Import into Postman

**Option A: Import Collection & Environments Separately**
1. Open Postman
2. Click **Import** button (top left)
3. Drag and drop **CallCard_Microservice_API.postman_collection.json**
4. Click **Import** again
5. Drag and drop all 3 environment JSON files

**Option B: Import All at Once**
1. Open Postman
2. Click **Import** button
3. Select all 4 JSON files at once
4. Click **Import**

### 2. Select Environment

1. In the top-right corner of Postman, click the environment dropdown
2. Select the appropriate environment:
   - **CallCard - EC2 Internal** - For testing directly on EC2 (port 8080)
   - **CallCard - EC2 via Nginx** - For testing via Nginx proxy (port 80)
   - **CallCard - Public DNS (HTTPS)** - For testing via public URL (when configured)

### 3. Configure JWT Token (For Protected Endpoints)

1. Click the **Environment quick look** (eye icon) in top-right
2. Click **Edit** next to your selected environment
3. Find the `jwt_token` variable
4. Paste your JWT token in the **Current Value** field
5. Click **Save**

**Note:** Public endpoints (health, info, Swagger, WSDL) don't require authentication.

### 4. Test the API

Start with the health check to verify connectivity:
1. Navigate to: **Health & Monitoring** → **Health Check**
2. Click **Send**
3. You should receive a 200 OK response with status "UP"

---

## 📋 Collection Structure

The collection is organized into 7 main folders:

### 1. Health & Monitoring
Public and protected endpoints for service health and metrics:
- **Health Check** (Public) - `/actuator/health`
- **Service Info** (Public) - `/actuator/info`
- **Actuator Endpoints List** (Protected) - `/actuator`
- **Metrics** (Protected) - `/actuator/metrics`
- **Prometheus Metrics** (Protected) - `/actuator/prometheus`

### 2. REST API - CallCards
CRUD operations for call cards:
- **List All CallCards** (GET) - `/rest/callcards`
- **Get CallCard by ID** (GET) - `/rest/callcards/{id}`
- **Create CallCard** (POST) - `/rest/callcards`
- **Update CallCard** (PUT) - `/rest/callcards/{id}`
- **Delete CallCard** (DELETE) - `/rest/callcards/{id}`
- **Search CallCards** (GET) - `/rest/callcards?status=ACTIVE&userId=1`

### 3. REST API - Statistics
Statistics and analytics endpoints:
- **Get Statistics** (GET) - `/rest/statistics`
- **Get Statistics by User** (GET) - `/rest/statistics/user/{userId}`
- **Get Statistics by Date Range** (GET) - `/rest/statistics?startDate=...&endDate=...`

### 4. REST API - Transactions
Transaction history and management:
- **Get Transaction History** (GET) - `/rest/transactions`
- **Get Transactions by CallCard** (GET) - `/rest/transactions/callcard/{id}`
- **Create Transaction** (POST) - `/rest/transactions`

### 5. REST API - Simplified
Optimized endpoints for mobile/low bandwidth:
- **Get Simplified CallCards** (GET) - `/rest/simplified`
- **Get Simplified CallCard by ID** (GET) - `/rest/simplified/{id}`

### 6. SOAP Services
Apache CXF SOAP web services:
- **CallCardService - WSDL** (GET) - `/cxf/CallCardService?wsdl`
- **CallCardService - SOAP Request** (POST) - `/cxf/CallCardService`
- **CallCardStatisticsService - WSDL** (GET) - `/cxf/CallCardStatisticsService?wsdl`
- **SimplifiedCallCardService - WSDL** (GET) - `/cxf/SimplifiedCallCardService?wsdl`
- **CallCardTransactionService - WSDL** (GET) - `/cxf/CallCardTransactionService?wsdl`
- **CXF Services List** (GET) - `/cxf/services`

### 7. API Documentation
Interactive documentation:
- **Swagger UI** (GET) - `/swagger-ui.html`
- **Swagger UI Index** (GET) - `/swagger-ui/index.html`
- **OpenAPI Docs** (GET) - `/api-docs`

---

## 🔐 Authentication

### Public Endpoints (No Authentication Required)
- `/actuator/health`
- `/actuator/info`
- `/swagger-ui.html`
- `/swagger-ui/index.html`
- `/api-docs`
- `/cxf/services`
- `/cxf/{ServiceName}?wsdl` (WSDL definitions)

### Protected Endpoints (JWT Required)
- All `/rest/*` endpoints (CallCards, Statistics, Transactions, Simplified)
- All SOAP service operations (POST requests to `/cxf/{ServiceName}`)
- All `/actuator/*` endpoints except health and info

### How to Get a JWT Token

**Option 1: Use an existing authentication service**
If you have an authentication service that issues JWT tokens, obtain a token from there.

**Option 2: Generate a test token**
Contact your backend team for a test JWT token.

**Option 3: Create your own (for development)**
If you have access to the JWT secret, you can generate a token using tools like:
- jwt.io
- JWT libraries in your preferred language

---

## 🌍 Environment Variables

Each environment file contains these variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `base_url` | Base URL for the API | `http://172.17.165.60:8080/callcard` |
| `jwt_token` | JWT authentication token | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| `callcard_id` | Sample call card ID for testing | `1` |
| `user_id` | Sample user ID for testing | `1` |

### Environment URLs

| Environment | Base URL | Use Case |
|-------------|----------|----------|
| **EC2 Internal** | `http://172.17.165.60:8080/callcard` | Direct access to microservice on EC2 |
| **EC2 via Nginx** | `http://172.17.165.60/callcard` | Access via Nginx reverse proxy |
| **Public DNS** | `https://talosmaind.saicongames.com/callcard` | Public access via HTTPS (when configured) |

---

## 📝 Example Requests

### 1. Health Check (No Auth)
```http
GET http://172.17.165.60:8080/callcard/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "callCard": {
      "status": "UP",
      "details": {
        "service": "callcard-microservice",
        "version": "1.0.0",
        "status": "Running",
        "database": "Connected"
      }
    },
    "database": {
      "status": "UP",
      "details": {
        "database": "Microsoft SQL Server",
        "version": "15.00.4375"
      }
    }
  }
}
```

### 2. List CallCards (With Auth)
```http
GET http://172.17.165.60:8080/callcard/rest/callcards
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (if authorized):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "cardNumber": "CC-2025-001",
    "initialAmount": 100.00,
    "currentBalance": 75.00,
    "status": "ACTIVE",
    "expiryDate": "2026-12-31T23:59:59"
  }
]
```

**Expected Response (if not authorized):**
```json
{
  "path": "/callcard/rest/callcards",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "timestamp": 1766842581226,
  "status": 401
}
```

### 3. Create CallCard (With Auth)
```http
POST http://172.17.165.60:8080/callcard/rest/callcards
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "userId": 1,
  "cardNumber": "CC-2025-002",
  "initialAmount": 100.00,
  "currentBalance": 100.00,
  "status": "ACTIVE",
  "expiryDate": "2026-12-31T23:59:59"
}
```

### 4. SOAP Request (With Auth)
```http
POST http://172.17.165.60:8080/callcard/cxf/CallCardService
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: text/xml
SOAPAction: getCallCard

<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:call="http://ws.callcard.games.saicon.com/">
  <soap:Header/>
  <soap:Body>
    <call:getCallCard>
      <callCardId>1</callCardId>
    </call:getCallCard>
  </soap:Body>
</soap:Envelope>
```

---

## 🧪 Testing Workflow

### Step 1: Verify Service is Running
1. Select **EC2 Internal** or **EC2 via Nginx** environment
2. Run **Health Check** request
3. Verify response status is "UP"

### Step 2: Test Public Endpoints
1. Run **Service Info** request
2. Open **Swagger UI** in browser (copy URL from request)
3. Get **CXF Services List** to see available SOAP services

### Step 3: Configure JWT Token
1. Obtain a JWT token from your authentication service
2. Add token to environment variable
3. Test a protected endpoint (e.g., **List All CallCards**)

### Step 4: Test CRUD Operations
1. **Create CallCard** - Create a new call card
2. **Get CallCard by ID** - Retrieve the created call card
3. **Update CallCard** - Modify the call card
4. **Search CallCards** - Search with filters
5. **Delete CallCard** - Remove the call card

### Step 5: Test Statistics & Transactions
1. **Get Statistics** - View overall statistics
2. **Get Statistics by User** - User-specific stats
3. **Get Transaction History** - View transaction log
4. **Create Transaction** - Add a transaction

### Step 6: Test SOAP Services
1. **Get WSDL** - Download service definition
2. **Send SOAP Request** - Test SOAP operation
3. View response in Postman's XML viewer

---

## 🔧 Troubleshooting

### Issue: Connection Refused
**Symptoms:** Cannot connect to the API
**Solutions:**
1. Verify the CallCard microservice is running:
   ```bash
   ssh ec2-user@172.17.165.60
   docker ps | grep callcard
   ```
2. Check if Nginx is running:
   ```bash
   sudo systemctl status nginx
   ```
3. Test direct access to microservice:
   ```bash
   curl http://localhost:8080/callcard/actuator/health
   ```

### Issue: 401 Unauthorized
**Symptoms:** All protected endpoints return 401
**Solutions:**
1. Verify your JWT token is valid and not expired
2. Check the token is properly set in environment variables
3. Ensure the Authorization header format is: `Bearer YOUR_TOKEN`
4. Test with a public endpoint first to verify connectivity

### Issue: 404 Not Found
**Symptoms:** Endpoint returns 404
**Solutions:**
1. Verify the URL path is correct
2. Check that the base_url variable is properly set
3. Ensure the service is deployed and running
4. Check Nginx configuration for CallCard routes

### Issue: CORS Errors (Browser)
**Symptoms:** CORS policy blocking requests in browser
**Solutions:**
1. CORS is already configured on the server for REST endpoints
2. If still having issues, use Postman instead of browser
3. Check Nginx CORS headers configuration

### Issue: SOAP Requests Failing
**Symptoms:** SOAP operations return errors
**Solutions:**
1. Verify WSDL is accessible first
2. Check XML structure matches WSDL definition
3. Ensure Content-Type is set to `text/xml`
4. Verify SOAPAction header matches operation name

---

## 📚 Additional Resources

### Related Documentation
- **ENDPOINT_TESTING_REPORT.md** - Comprehensive endpoint testing results
- **DEPLOYMENT_SUCCESS_SUMMARY.md** - Deployment details and configuration
- **EXTERNAL_GATEWAY_INVESTIGATION.md** - External gateway analysis

### API Documentation
- **Swagger UI**: http://172.17.165.60:8080/callcard/swagger-ui.html
- **OpenAPI Spec**: http://172.17.165.60:8080/callcard/api-docs

### Service Endpoints
- **Internal EC2**: http://172.17.165.60:8080/callcard
- **Via Nginx**: http://172.17.165.60/callcard
- **Public DNS**: https://talosmaind.saicongames.com/callcard (when configured)

---

## 🔄 Updating the Collection

If the API changes or new endpoints are added:

1. Open Postman
2. Make changes to the collection
3. Export the collection:
   - Right-click on collection name
   - Select **Export**
   - Choose **Collection v2.1**
   - Save over the existing JSON file

---

## 📞 Support

For API issues or questions:
- **Technical Documentation**: See ENDPOINT_TESTING_REPORT.md
- **Deployment Issues**: See DEPLOYMENT_SUCCESS_SUMMARY.md
- **Gateway Configuration**: See EXTERNAL_GATEWAY_INVESTIGATION.md

---

**Collection Version:** 1.0.0
**Last Updated:** December 27, 2025
**Microservice Version:** 1.0.0-SNAPSHOT
**Spring Boot Version:** 2.7.18
