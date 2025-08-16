# 📮 Postman Testing Guide

This guide provides step-by-step instructions for testing all APIs using Postman, including complete JSON request bodies and expected responses.

## 🔧 Postman Setup

### 1. Create New Collection

1. Open Postman
2. Click "New" → "Collection"
3. Name it: **"Organization Platform API"**
4. Add description: **"Complete API testing for multi-tenant organization platform"**

### 2. Set Collection Variables

In your collection settings, add these variables:

| Variable        | Initial Value           | Description              |
| --------------- | ----------------------- | ------------------------ |
| `baseUrl`       | `http://localhost:8080` | API base URL             |
| `platformToken` |                         | Platform owner JWT token |
| `userToken`     |                         | User JWT token           |
| `orgId`         |                         | Created organization ID  |
| `inviteId`      |                         | Created invitation ID    |

## 📋 Complete API Test Flow

### Step 1: Health Check

**Purpose:** Verify the application is running

#### Request

- **Method:** `GET`
- **URL:** `{{baseUrl}}/actuator/health`
- **Headers:** None required

#### Expected Response

```json
{
  "status": "UP"
}
```

---

### Step 2: Platform Owner Login

**Purpose:** Authenticate as platform administrator

#### Request

- **Method:** `POST`
- **URL:** `{{baseUrl}}/platform/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "owner@platform.local",
    "password": "owner123"
  }
  ```

#### Expected Response

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiZTRlMjVlYy0zZmQ5LTRjODEtYTg3OS0xMzUwOTkwM2ZkMWEiLCJpc3MiOiJvcmctcGxhdGZvcm0iLCJlbWFpbCI6Im93bmVyQHBsYXRmb3JtLmxvY2FsIiwic3ViVHlwZSI6IlBMQVRGT1JNIiwiaWF0IjoxNzU1MzE5ODQ5LCJleHAiOjE3NTUzMjA3NDl9..."
}
```

#### Post-request Script

Add this script to automatically save the token:

```javascript
pm.test("Login successful", function () {
  pm.response.to.have.status(200);
  var jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("accessToken");
  pm.collectionVariables.set("platformToken", jsonData.accessToken);
});
```

---

### Step 3: Create Organization

**Purpose:** Create a new organization

#### Request

- **Method:** `POST`
- **URL:** `{{baseUrl}}/platform/orgs`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer {{platformToken}}
  ```
- **Body (raw JSON):**
  ```json
  {
    "name": "Acme Corporation",
    "slug": "acme-corp"
  }
  ```

#### Expected Response

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Acme Corporation",
  "slug": "acme-corp",
  "status": "ACTIVE",
  "createdAt": "2025-01-15T20:30:00.123Z"
}
```

#### Post-request Script

```javascript
pm.test("Organization created", function () {
  pm.response.to.have.status(200);
  var jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("id");
  pm.collectionVariables.set("orgId", jsonData.id);
});
```

---

### Step 4: Create User Invitation

**Purpose:** Invite a user to the organization

#### Request

- **Method:** `POST`
- **URL:** `{{baseUrl}}/orgs/{{orgId}}/invites`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer {{platformToken}}
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "alice@acme.com",
    "roleName": "ADMIN",
    "tempPassword": "Welcome123",
    "expiresHours": 72
  }
  ```

#### Expected Response

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "alice@acme.com",
  "role": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "name": "ADMIN",
    "description": "Manage users & settings"
  },
  "expiresAt": "2025-01-18T20:30:00.123Z",
  "status": "PENDING"
}
```

#### Post-request Script

```javascript
pm.test("Invitation created", function () {
  pm.response.to.have.status(200);
  var jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("id");
  pm.collectionVariables.set("inviteId", jsonData.id);
});
```

---

### Step 5: Accept Invitation

**Purpose:** Accept the invitation and create user account

#### Request

- **Method:** `POST`
- **URL:** `{{baseUrl}}/auth/accept-invite`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "inviteId": "{{inviteId}}",
    "email": "alice@acme.com",
    "tempPassword": "Welcome123",
    "newPassword": "SecurePassword123!"
  }
  ```

#### Expected Response

```
Status: 200 OK
(No response body - success indicated by status)
```

---

### Step 6: User Login

**Purpose:** Login as the newly created user

#### Request

- **Method:** `POST`
- **URL:** `{{baseUrl}}/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "alice@acme.com",
    "password": "SecurePassword123!"
  }
  ```

#### Expected Response

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5OTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpc3MiOiJvcmctcGxhdGZvcm0iLCJlbWFpbCI6ImFsaWNlQGFjbWUuY29tIiwic3ViVHlwZSI6IlVTRVIiLCJpYXQiOjE3NTUzMTk4NDksImV4cCI6MTc1NTMyMDc0OX0..."
}
```

#### Post-request Script

```javascript
pm.test("User login successful", function () {
  pm.response.to.have.status(200);
  var jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("accessToken");
  pm.collectionVariables.set("userToken", jsonData.accessToken);
});
```

---

### Step 7: Get User Memberships

**Purpose:** View user's organization memberships

#### Request

- **Method:** `GET`
- **URL:** `{{baseUrl}}/me/memberships`
- **Headers:**
  ```
  Authorization: Bearer {{userToken}}
  ```

#### Expected Response

```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "orgId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "990e8400-e29b-41d4-a716-446655440000",
    "role": {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "name": "ADMIN",
      "description": "Manage users & settings"
    },
    "state": "ACTIVE",
    "createdAt": "2025-01-15T20:35:00.123Z"
  }
]
```

---

### Step 8: Create Another Invitation (as User)

**Purpose:** Test user's ability to invite others (ADMIN/OWNER only)

#### Request

- **Method:** `POST`
- **URL:** `{{baseUrl}}/orgs/{{orgId}}/invites`
- **Headers:**
  ```
  Content-Type: application/json
  Authorization: Bearer {{userToken}}
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "bob@acme.com",
    "roleName": "USER",
    "tempPassword": "Welcome456",
    "expiresHours": 48
  }
  ```

#### Expected Response

```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "bob@acme.com",
  "role": {
    "id": "bb0e8400-e29b-41d4-a716-446655440000",
    "name": "USER",
    "description": "Standard member access"
  },
  "expiresAt": "2025-01-17T20:30:00.123Z",
  "status": "PENDING"
}
```

---

## 🔄 Additional Test Scenarios

### Test Error Cases

#### 1. Invalid Credentials (Platform Owner)

- **URL:** `{{baseUrl}}/platform/auth/login`
- **Body:**
  ```json
  {
    "email": "owner@platform.local",
    "password": "wrongpassword"
  }
  ```
- **Expected:** `400 Bad Request` with error message

#### 2. Invalid Credentials (User)

- **URL:** `{{baseUrl}}/auth/login`
- **Body:**
  ```json
  {
    "email": "alice@acme.com",
    "password": "wrongpassword"
  }
  ```
- **Expected:** `400 Bad Request` with error message

#### 3. Unauthorized Access (No Token)

- **URL:** `{{baseUrl}}/orgs/{{orgId}}/invites`
- **Headers:** No Authorization header
- **Expected:** `403 Forbidden`

#### 4. Insufficient Role (USER trying to invite)

First, create a USER role invitation, accept it, login, then try to create invitation:

- **Expected:** `403 Forbidden` with "Insufficient role" message

#### 5. Duplicate Organization Slug

- **URL:** `{{baseUrl}}/platform/orgs`
- **Body:**
  ```json
  {
    "name": "Another Acme",
    "slug": "acme-corp"
  }
  ```
- **Expected:** `400 Bad Request` with "Slug already exists" message

#### 6. Expired/Invalid Invitation

- **URL:** `{{baseUrl}}/auth/accept-invite`
- **Body:**
  ```json
  {
    "inviteId": "invalid-uuid",
    "email": "test@example.com",
    "tempPassword": "wrong",
    "newPassword": "NewPass123!"
  }
  ```
- **Expected:** `400 Bad Request` with appropriate error message

---

## 🎯 Role-Based Testing

### Testing Different Roles

#### OWNER Role Test

```json
{
  "email": "owner@acme.com",
  "roleName": "OWNER",
  "tempPassword": "Welcome789",
  "expiresHours": 72
}
```

#### ADMIN Role Test

```json
{
  "email": "admin@acme.com",
  "roleName": "ADMIN",
  "tempPassword": "Welcome101",
  "expiresHours": 72
}
```

#### USER Role Test

```json
{
  "email": "user@acme.com",
  "roleName": "USER",
  "tempPassword": "Welcome202",
  "expiresHours": 72
}
```

---

## 📊 Postman Collection Structure

Organize your requests in folders:

```
📁 Organization Platform API
├── 📁 1. Setup & Health
│   └── Health Check
├── 📁 2. Platform Admin
│   ├── Platform Owner Login
│   └── Create Organization
├── 📁 3. Invitation Flow
│   ├── Create Invitation (Platform Owner)
│   ├── Accept Invitation
│   └── Create Invitation (User)
├── 📁 4. User Authentication
│   ├── User Login
│   └── Get My Memberships
└── 📁 5. Error Testing
    ├── Invalid Login
    ├── Unauthorized Access
    ├── Insufficient Role
    └── Duplicate Slug
```

---

## ⚙️ Environment Setup

### Create Environments

#### Local Development

```json
{
  "name": "Local Development",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "enabled": true
    }
  ]
}
```

#### Production (Future)

```json
{
  "name": "Production",
  "values": [
    {
      "key": "baseUrl",
      "value": "https://your-api-domain.com",
      "enabled": true
    }
  ]
}
```

---

## 🔍 Testing Tips

### 1. Token Management

- Tokens expire after 15 minutes
- Use collection variables to share tokens between requests
- Set up automatic token refresh if needed

### 2. Test Order

Always run tests in this order:

1. Health Check
2. Platform Owner Login
3. Create Organization
4. Create Invitation
5. Accept Invitation
6. User Login
7. User Operations

### 3. Data Cleanup

- Use unique email addresses for each test run
- Create organizations with unique slugs
- Consider setting up pre-request scripts for data generation

### 4. Assertions

Add these test scripts to verify responses:

```javascript
// Basic status check
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

// Response time check
pm.test("Response time is less than 2000ms", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});

// JSON structure validation
pm.test("Response has required fields", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("id");
  pm.expect(jsonData).to.have.property("email");
});
```

---

## 📝 Sample Data Sets

### Test Organizations

```json
[
  { "name": "Acme Corporation", "slug": "acme-corp" },
  { "name": "TechStart Inc", "slug": "techstart" },
  { "name": "Global Solutions", "slug": "global-sol" }
]
```

### Test Users

```json
[
  { "email": "alice@acme.com", "role": "ADMIN" },
  { "email": "bob@acme.com", "role": "USER" },
  { "email": "charlie@acme.com", "role": "OWNER" },
  { "email": "diana@acme.com", "role": "USER" }
]
```

---

**🎉 You now have a complete guide to test all APIs in Postman! Import the collection, set up the variables, and run through the test flow to verify everything works perfectly.**
