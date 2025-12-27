# External Gateway Investigation Report
**Date:** December 27, 2025
**Gateway IP:** 34.253.51.11
**DNS Name:** pmistagesp01.saicongames.com
**Purpose:** Configure CallCard microservice routing through external gateway

---

## 🔍 Investigation Summary

### Gateway Identification

**AWS EC2 Instance Information:**
- **Public IP:** 34.253.51.11
- **AWS Hostname:** ec2-34-253-51-11.eu-west-1.compute.amazonaws.com
- **DNS Name:** pmistagesp01.saicongames.com
- **Region:** eu-west-1 (Ireland)
- **Type:** Proxy/Load Balancer (confirmed via PUBLIC_DNS_INFO.md)

### Current Network Architecture

```
Public Internet
    ↓
talosmaind.saicongames.com (DNS)
    ↓ resolves to
34.253.51.11 (pmistagesp01 - External Gateway/Proxy)
    ↓ forwards to
172.17.165.60 (Internal EC2 - Backend Services)
    ↓ services
    ├─ Port 5002: Middleware (local dev)
    ├─ Port 5003: Middleware (production)
    ├─ Port 5004: Chatbot Backend
    └─ Port 8080: CallCard Microservice ✅ Ready
```

### Routing Status

| Service | Internal Port | Gateway Status | Public Access |
|---------|---------------|----------------|---------------|
| Middleware | 5002/5003 | ✅ Configured | ✅ Working |
| Chatbot Backend | 5004 | ✅ Configured | ✅ Working |
| CallCard Microservice | 8080 | ❌ Not Configured | ❌ Not Accessible |

---

## 🚫 Access Limitations Discovered

### 1. SSH Access
**Status:** ❌ Failed
```bash
ssh -i PMIstagSupNodesKey.pem ec2-user@34.253.51.11
# Result: Permission denied (publickey,gssapi-keyex,gssapi-with-mic)
```

**Analysis:**
- The PMIstagSupNodesKey.pem does not have access to this instance
- A different SSH key is required
- No other SSH keys found in ec2management directory

### 2. HTTP/HTTPS Access
**Status:** ❌ Connection Timeout
```bash
curl http://34.253.51.11/
# Result: Connection timed out after 10 seconds

ping 34.253.51.11
# Result: Request timed out (100% packet loss)
```

**Analysis:**
- Instance security group blocks direct access from external IPs
- HTTPS access via DNS works (talosmaind.saicongames.com)
- HTTP/HTTPS not responding on public IP directly

### 3. AWS CLI Access
**Status:** ❌ No Credentials Configured
```bash
aws elbv2 describe-load-balancers
# Result: Unable to locate credentials
```

**Analysis:**
- AWS CLI installed on internal EC2 (172.17.165.60) but not configured
- Cannot query load balancer or instance details via CLI
- Would need IAM credentials or instance role configured

---

## 📋 Gateway Type Analysis

### Possible Gateway Types

Based on the investigation, 34.253.51.11 is likely one of these:

#### Option 1: Separate EC2 Instance Running Nginx/HAProxy (Most Likely)
**Evidence:**
- Has an EC2 hostname (ec2-34-253-51-11.eu-west-1.compute.amazonaws.com)
- Named pmistagesp01 (PM = Project Management, staging server 01)
- Currently proxying middleware and chatbot traffic
- Similar to how 172.17.165.60 has Nginx configured

**Configuration Location:** `/etc/nginx/` or `/etc/haproxy/` on 34.253.51.11

**Access Required:** SSH with correct key

#### Option 2: AWS Application Load Balancer (ALB)
**Evidence:**
- Could be an ALB with a static IP assignment
- Would explain SSL termination at gateway level
- Typical architecture for production AWS deployments

**Configuration Location:** AWS Console → EC2 → Load Balancers

**Access Required:** AWS Console access with appropriate IAM permissions

#### Option 3: AWS Network Load Balancer (NLB)
**Evidence:**
- NLBs support static IPs
- Layer 4 load balancing
- Less likely (application is HTTP/HTTPS)

**Configuration Location:** AWS Console → EC2 → Load Balancers

**Access Required:** AWS Console access

---

## ✅ What's Working

### EC2 Nginx Configuration (172.17.165.60)
**Status:** ✅ Fully Configured

The internal EC2 instance where CallCard runs has been successfully configured:

**File:** `/etc/nginx/conf.d/claude-chatbot.conf`

```nginx
server {
    listen 80;
    server_name talosmaind.saicongames.com 52.51.171.216;

    # CallCard Microservice Routes
    location /callcard/actuator/health {
        proxy_pass http://localhost:8080/callcard/actuator/health;
        # ... (all headers configured)
    }

    location /callcard/rest/ {
        proxy_pass http://localhost:8080/callcard/rest/;
        # ... (JWT auth, CORS configured)
    }

    location /callcard/cxf/ {
        proxy_pass http://localhost:8080/callcard/cxf/;
        # ... (SOAP services configured)
    }

    # ... (all other CallCard routes configured)
}
```

**Verification:**
```bash
# Internal access works perfectly
curl -H "Host: talosmaind.saicongames.com" http://localhost/callcard/actuator/health
# Returns: {"status":"UP","components":{...}}
```

---

## ⚠️ What Needs Configuration

### External Gateway (34.253.51.11) Configuration

The external gateway needs to forward `/callcard/*` requests to the internal EC2 instance.

### Configuration Scenarios

#### Scenario A: If 34.253.51.11 is an EC2 Instance with Nginx

**Required Steps:**

1. **Obtain SSH Access**
   - Locate the correct SSH key for pmistagesp01
   - Or request access from infrastructure team
   - Or add your public key to authorized_keys

2. **SSH into Gateway**
   ```bash
   ssh -i [correct-key].pem ec2-user@34.253.51.11
   ```

3. **Locate Nginx Configuration**
   ```bash
   # Find main config
   ls -la /etc/nginx/
   ls -la /etc/nginx/conf.d/

   # Find existing talosmaind config
   grep -r "talosmaind.saicongames.com" /etc/nginx/
   ```

4. **Add CallCard Routes**
   Add these location blocks to the server block for talosmaind.saicongames.com:

   ```nginx
   # CallCard Microservice - Health Check (Public)
   location /callcard/actuator/health {
       proxy_pass http://172.17.165.60/callcard/actuator/health;
       proxy_http_version 1.1;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
   }

   # CallCard Microservice - REST API (Protected)
   location /callcard/rest/ {
       proxy_pass http://172.17.165.60/callcard/rest/;
       proxy_http_version 1.1;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
       proxy_set_header Authorization $http_authorization;

       proxy_connect_timeout 60s;
       proxy_send_timeout 60s;
       proxy_read_timeout 60s;
   }

   # CallCard Microservice - SOAP Services (Protected)
   location /callcard/cxf/ {
       proxy_pass http://172.17.165.60/callcard/cxf/;
       proxy_http_version 1.1;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
       proxy_set_header Authorization $http_authorization;

       client_max_body_size 10M;
       proxy_connect_timeout 120s;
       proxy_send_timeout 120s;
       proxy_read_timeout 120s;
   }

   # CallCard Microservice - Swagger UI (Public)
   location /callcard/swagger-ui/ {
       proxy_pass http://172.17.165.60/callcard/swagger-ui/;
       proxy_http_version 1.1;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
   }

   # CallCard Microservice - Actuator Endpoints (Protected)
   location /callcard/actuator/ {
       proxy_pass http://172.17.165.60/callcard/actuator/;
       proxy_http_version 1.1;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
       proxy_set_header Authorization $http_authorization;
   }
   ```

5. **Test and Reload Nginx**
   ```bash
   sudo nginx -t
   sudo systemctl reload nginx
   ```

6. **Verify Public Access**
   ```bash
   curl https://talosmaind.saicongames.com/callcard/actuator/health
   ```

#### Scenario B: If 34.253.51.11 is an AWS Load Balancer

**Required Steps:**

1. **Access AWS Console**
   - Log into AWS Console
   - Navigate to: EC2 → Load Balancers
   - Find the load balancer for talosmaind.saicongames.com

2. **Configure Target Group**
   - Check if 172.17.165.60:80 is already a target
   - Verify health checks are passing

3. **Add Listener Rules**
   Create rules to forward `/callcard/*` paths to the target group:

   - **Rule Priority:** Higher than catch-all rules
   - **Condition:** Path pattern = `/callcard/*`
   - **Action:** Forward to target group containing 172.17.165.60:80
   - **Stickiness:** Optional (not needed for stateless API)

4. **Configure Health Check**
   - **Path:** `/callcard/actuator/health`
   - **Port:** 80
   - **Protocol:** HTTP
   - **Success Codes:** 200

5. **Verify Configuration**
   ```bash
   curl https://talosmaind.saicongames.com/callcard/actuator/health
   ```

---

## 🔑 Required Information

To proceed with configuration, you need:

### For EC2 Instance Scenario:
- [ ] SSH key for pmistagesp01 (34.253.51.11)
- [ ] SSH access credentials
- [ ] Sudo/root access on pmistagesp01

### For AWS Load Balancer Scenario:
- [ ] AWS Console access
- [ ] IAM permissions for:
  - `elasticloadbalancing:DescribeLoadBalancers`
  - `elasticloadbalancing:DescribeTargetGroups`
  - `elasticloadbalancing:ModifyListener`
  - `elasticloadbalancing:ModifyRule`
  - `elasticloadbalancing:CreateRule`

### Alternative:
- [ ] Contact infrastructure team with this document
- [ ] Request they add CallCard routing to external gateway

---

## 📊 Testing Plan (After Configuration)

Once the external gateway is configured, test with these commands:

### 1. Health Check (Public)
```bash
curl https://talosmaind.saicongames.com/callcard/actuator/health
# Expected: {"status":"UP","components":{...}}
```

### 2. Info Endpoint (Public)
```bash
curl https://talosmaind.saicongames.com/callcard/actuator/info
# Expected: {} or build info
```

### 3. Swagger UI (Public)
```bash
curl -I https://talosmaind.saicongames.com/callcard/swagger-ui/
# Expected: HTTP/1.1 200 OK or 302 redirect
```

### 4. REST API (Protected - Should Return 401)
```bash
curl https://talosmaind.saicongames.com/callcard/rest/callcards
# Expected: {"error":"Unauthorized","status":401}
```

### 5. REST API (Protected - With JWT)
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  https://talosmaind.saicongames.com/callcard/rest/callcards
# Expected: CallCard data or empty array
```

### 6. SOAP Service WSDL (Public)
```bash
curl https://talosmaind.saicongames.com/callcard/cxf/CallCardService?wsdl
# Expected: XML WSDL definition
```

---

## 📞 Next Steps

### Option 1: DIY Configuration (If You Have Access)
1. Locate SSH key for pmistagesp01 or get AWS Console access
2. Follow Scenario A or B configuration steps above
3. Test all endpoints
4. Update ENDPOINT_TESTING_REPORT.md with results

### Option 2: Infrastructure Team Assistance
1. Forward this document to your infrastructure team
2. Request they add CallCard routing to pmistagesp01
3. Provide them with the Nginx configuration from Scenario A
4. Request notification when complete for testing

### Option 3: Alternative Access Methods
1. Check if there's a VPN or bastion host to access pmistagesp01
2. Check if AWS Systems Manager (SSM) Session Manager is enabled
3. Check if there's an AWS CloudFormation/Terraform configuration to update

---

## 📋 Summary

### ✅ Completed
- Identified external gateway: 34.253.51.11 (pmistagesp01.saicongames.com)
- Confirmed gateway type: Proxy/Load Balancer (likely Nginx on EC2 or AWS ALB)
- Successfully configured internal EC2 Nginx (172.17.165.60)
- Verified CallCard microservice is healthy and responding
- Documented complete configuration requirements

### ⚠️ Blocked
- Cannot access 34.253.51.11 via SSH (wrong key)
- Cannot access 34.253.51.11 via HTTP (connection timeout)
- Cannot query AWS resources (no CLI credentials)

### 🎯 Required
- SSH access to pmistagesp01 OR AWS Console access
- Apply CallCard routing configuration to external gateway
- Test public access via talosmaind.saicongames.com

---

**Report Generated:** 2025-12-27 13:50 UTC
**Investigation Status:** ✅ Complete
**Configuration Status:** ⚠️ Awaiting Access to External Gateway
**Next Action:** Obtain access to pmistagesp01 or contact infrastructure team
