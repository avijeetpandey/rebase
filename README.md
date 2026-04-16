# Rebase Auth API

JWT-based authentication with persisted DB sessions using Spring Security (without implementing `UserDetails`).

## Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me` (Bearer token required)
- `POST /api/v1/auth/logout` (Bearer token required)

## Request/Response

### Register

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "strongPassword123"
}
```

### Login

```json
{
  "username": "alice",
  "password": "strongPassword123"
}
```

Login returns `accessToken`, `refreshToken`, and persisted `sessionId`.

## Configuration

`src/main/resources/application.properties` includes:

- `security.jwt.secret`
- `security.jwt.access-token-expiry-ms`
- `security.session.expiry-hours`

Set a strong production JWT secret (32+ chars).

## Run

```bash
./mvnw spring-boot:run
```

## Test

```bash
./mvnw -Dtest=JwtServiceTest test
```

## Postman

Import `postman/rebase-auth.postman_collection.json` and run requests in order:

1. Register
2. Login
3. Me
4. Refresh
5. Logout

## cURL Quickstart

Set base URL (default port in this project is `9000`):

```bash
BASE_URL="http://localhost:9000"
```

Register user:

```bash
curl -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "strongPassword123"
  }'
```

Login (copy `accessToken` and `refreshToken` from response `data`):

```bash
curl -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "strongPassword123"
  }'
```

Use tokens in shell variables:

```bash
ACCESS_TOKEN="<paste_access_token_here>"
REFRESH_TOKEN="<paste_refresh_token_here>"
```

Get current user:

```bash
curl -X GET "$BASE_URL/api/v1/auth/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Refresh access token:

```bash
curl -X POST "$BASE_URL/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

Logout current session:

```bash
curl -X POST "$BASE_URL/api/v1/auth/logout" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

