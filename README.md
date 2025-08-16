# Organization Platform MVP

A complete Spring Boot application for multi-tenant organization management with JWT authentication, role-based access control, and invitation flows.

## 🚀 Quick Start

### Prerequisites

- **Java 21** (required)
- **PostgreSQL 14+** (running on localhost:5432)
- **Maven 3.6+** (included via wrapper)

### Database Setup

1. **Create PostgreSQL user and database:**

```bash
# Connect to PostgreSQL as superuser
psql postgres

# Create user and database
CREATE USER app WITH PASSWORD 'app';
CREATE DATABASE org_platform OWNER app;
GRANT ALL PRIVILEGES ON DATABASE org_platform TO app;
ALTER USER app CREATEDB;

# Enable required extensions
\c org_platform
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
```

2. **Verify connection:**

```bash
psql org_platform -U app -h localhost
```

### Application Setup

1. **Set Java 21 (if you have multiple versions):**

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.7/libexec/openjdk.jdk/Contents/Home
```

2. **Run the application:**

```bash
./mvnw spring-boot:run
```

3. **Verify startup:**

```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

## 🏗️ Architecture

### Core Components

- **Spring Boot 3.5.4** with Java 21
- **PostgreSQL** database with Flyway migrations
- **JWT Authentication** with 15-minute tokens
- **BCrypt Password Encoding**
- **Role-Based Access Control** (OWNER/ADMIN/USER)
- **Multi-tenant Organizations**

### Database Schema

- `platform_owners` - Platform administrators
- `organizations` - Tenant organizations
- `users` - End users
- `roles` - System roles (OWNER/ADMIN/USER)
- `org_memberships` - User-organization relationships
- `invites` - Invitation management
- `audit_logs` - Activity tracking

## 🔐 Authentication

### Default Credentials

**Platform Owner:**

- Email: `owner@platform.local`
- Password: `owner123`

### JWT Tokens

- **Access Token TTL:** 15 minutes
- **Algorithm:** HS512
- **Claims:** user ID, email, subject type (USER/PLATFORM)

## 📡 API Reference

### 🏢 Platform Admin APIs

#### Login as Platform Owner

```bash
POST /platform/auth/login
Content-Type: application/json

{
  "email": "owner@platform.local",
  "password": "owner123"
}

# Response:
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### Create Organization

```bash
POST /platform/orgs
Content-Type: application/json
Authorization: Bearer {platformToken}

{
  "name": "Acme Inc",
  "slug": "acme-inc"
}

# Response:
{
  "id": "uuid",
  "name": "Acme Inc",
  "slug": "acme-inc",
  "status": "ACTIVE",
  "createdAt": "2025-01-15T..."
}
```

### 👥 User Authentication APIs

#### Accept Invitation

```bash
POST /auth/accept-invite
Content-Type: application/json

{
  "inviteId": "uuid",
  "email": "alice@example.com",
  "tempPassword": "temp123",
  "newPassword": "MySecurePassword123!"
}

# Creates user account and org membership
```

#### User Login

```bash
POST /auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "MySecurePassword123!"
}

# Response:
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 📨 Invitation Management APIs

#### Create Invitation (OWNER/ADMIN only)

```bash
POST /orgs/{orgId}/invites
Content-Type: application/json
Authorization: Bearer {userToken}

{
  "email": "bob@example.com",
  "roleName": "USER",
  "tempPassword": "Welcome123",
  "expiresHours": 72
}

# Response:
{
  "id": "uuid",
  "email": "bob@example.com",
  "status": "PENDING",
  "expiresAt": "2025-01-18T...",
  "role": {
    "name": "USER",
    "description": "Standard member access"
  }
}
```

### 👤 User Profile APIs

#### Get My Memberships

```bash
GET /me/memberships
Authorization: Bearer {userToken}

# Response:
[
  {
    "id": "uuid",
    "orgId": "uuid",
    "userId": "uuid",
    "role": {
      "name": "ADMIN",
      "description": "Manage users & settings"
    },
    "state": "ACTIVE",
    "createdAt": "2025-01-15T..."
  }
]
```

## 🧪 Complete Test Flow

### 1. Platform Owner Workflow

```bash
# 1. Login as platform owner
PLATFORM_TOKEN=$(curl -s -X POST http://localhost:8080/platform/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "owner@platform.local", "password": "owner123"}' | \
  jq -r '.accessToken')

echo "Platform Token: $PLATFORM_TOKEN"

# 2. Create an organization
ORG_RESPONSE=$(curl -s -X POST http://localhost:8080/platform/orgs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $PLATFORM_TOKEN" \
  -d '{"name": "Acme Inc", "slug": "acme-inc"}')

echo "Created Org: $ORG_RESPONSE"

# Extract org ID for later use
ORG_ID=$(echo $ORG_RESPONSE | jq -r '.id')
echo "Org ID: $ORG_ID"
```

### 2. User Invitation Workflow

**Note:** Currently, the platform owner auto-becomes an OWNER of created orgs. In production, you'd typically create a separate admin user first.

```bash
# For this test, we'll use the platform owner's user ID to create invitations
# In a real scenario, you'd have separate admin users

# Get platform owner user ID from JWT token or database
OWNER_USER_ID="be4e25ec-3fd9-4c81-a879-13509903fd1a"  # From our setup

# 3. Create invitation (as org owner/admin)
INVITE_RESPONSE=$(curl -s -X POST http://localhost:8080/orgs/$ORG_ID/invites \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $PLATFORM_TOKEN" \
  -d '{
    "email": "alice@acme.com",
    "roleName": "USER",
    "tempPassword": "Welcome123",
    "expiresHours": 72
  }')

echo "Created Invite: $INVITE_RESPONSE"

# Extract invite ID
INVITE_ID=$(echo $INVITE_RESPONSE | jq -r '.id')
echo "Invite ID: $INVITE_ID"

# 4. Accept invitation (public endpoint)
curl -s -X POST http://localhost:8080/auth/accept-invite \
  -H "Content-Type: application/json" \
  -d '{
    "inviteId": "'$INVITE_ID'",
    "email": "alice@acme.com",
    "tempPassword": "Welcome123",
    "newPassword": "SecurePassword123!"
  }'

echo "Invitation accepted successfully"
```

### 3. User Login & Operations

```bash
# 5. Login as new user
USER_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "alice@acme.com", "password": "SecurePassword123!"}' | \
  jq -r '.accessToken')

echo "User Token: $USER_TOKEN"

# 6. Check user's memberships
curl -s -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8080/me/memberships | jq '.'

# 7. Create another invitation (if user has ADMIN/OWNER role)
curl -s -X POST http://localhost:8080/orgs/$ORG_ID/invites \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "email": "bob@acme.com",
    "roleName": "USER",
    "tempPassword": "Welcome456",
    "expiresHours": 48
  }' | jq '.'
```

## 🔧 Configuration

### Environment Variables

```bash
# JWT Secret (must be 32+ characters)
export JWT_SECRET="your-super-secret-jwt-key-32-chars-minimum"

# Platform Owner Credentials
export PLATFORM_OWNER_EMAIL="admin@yourcompany.com"
export PLATFORM_OWNER_PASSWORD="YourSecurePassword"

# Database (if different from defaults)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/org_platform"
export SPRING_DATASOURCE_USERNAME="app"
export SPRING_DATASOURCE_PASSWORD="app"
```

### Application Properties

Key settings in `application.yml`:

```yaml
security:
  jwt:
    issuer: org-platform
    accessTokenTtlMinutes: 15
    secret: ${JWT_SECRET:change-me-in-production-this-must-be-at-least-32-characters-long}
```

## 🛡️ Security Features

### Authentication

- JWT-based stateless authentication
- Secure password hashing with BCrypt
- Token expiration and validation

### Authorization

- Role-based access control (RBAC)
- Organization-scoped permissions
- Guard classes for fine-grained access control

### Security Headers

- CSRF protection disabled for API-only usage
- XSS protection enabled
- Content type sniffing protection

## 🚨 Error Handling

### Common Error Responses

```json
// 400 Bad Request
{
  "error": "Invalid credentials"
}

// 403 Forbidden
{
  "error": "Platform owner token required"
}

// 404 Not Found
{
  "error": "Resource not found"
}
```

## 🔍 Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Application Info

```bash
curl http://localhost:8080/actuator/info
```

## 🐛 Troubleshooting

### Common Issues

1. **"role 'app' does not exist"**

   ```bash
   # Create PostgreSQL user
   psql postgres -c "CREATE USER app WITH PASSWORD 'app';"
   ```

2. **"type 'citext' does not exist"**

   ```bash
   # Enable citext extension
   psql org_platform -c "CREATE EXTENSION IF NOT EXISTS citext;"
   ```

3. **"JWT key too short"**

   - Ensure JWT secret is at least 32 characters
   - Update `application.yml` or set `JWT_SECRET` environment variable

4. **Java version mismatch**
   ```bash
   # Set Java 21
   export JAVA_HOME=/path/to/java21
   ./mvnw clean compile
   ```

### Database Reset

To reset the database completely:

```bash
# Drop and recreate database
psql postgres -c "DROP DATABASE IF EXISTS org_platform;"
psql postgres -c "CREATE DATABASE org_platform OWNER app;"
psql org_platform -c "CREATE EXTENSION IF NOT EXISTS citext;"
psql org_platform -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"

# Restart application (migrations will run automatically)
```

## 📝 Development Notes

### Next Steps

1. **Email Integration**: Replace temp passwords with email-based invitations
2. **Refresh Tokens**: Add refresh token support for longer sessions
3. **Rate Limiting**: Add rate limiting to authentication endpoints
4. **Admin UI**: Build a web interface for platform administration
5. **API Documentation**: Add OpenAPI/Swagger documentation
6. **Testing**: Add comprehensive unit and integration tests

### Production Considerations

- Use a proper JWT secret (256-bit minimum)
- Enable HTTPS/TLS
- Configure proper database connection pooling
- Set up monitoring and logging
- Implement backup strategies
- Add request validation and rate limiting

## 📚 API Collection

For easier testing, you can import these endpoints into Postman or similar tools:

**Base URL:** `http://localhost:8080`

**Collections:**

- Platform Admin: `/platform/auth/login`, `/platform/orgs`
- User Auth: `/auth/login`, `/auth/accept-invite`
- Invitations: `/orgs/{orgId}/invites`
- Profile: `/me/memberships`
- Health: `/actuator/health`

---

**🎉 You now have a fully functional multi-tenant organization platform with JWT authentication and role-based access control!**
