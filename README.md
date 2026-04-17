# Rebase API

Backend for Rebase built with Spring Boot + Spring Security (JWT auth + persisted DB sessions), without implementing `UserDetails`.

## Base URL

- Local: `http://localhost:9000`
- API prefix: `/api/v1`

## Standard API Envelope

All endpoints return this wrapper:

```json
{
  "isError": false,
  "message": "...",
  "data": {}
}
```

Error format:

```json
{
  "isError": true,
  "message": "Validation failed",
  "data": {
    "timestamp": "2026-04-17T17:30:00.000+00:00",
    "path": "/api/v1/posts/24/comments",
    "error": "Validation failed"
  }
}
```

## Auth Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me` (Bearer required)
- `POST /api/v1/auth/logout` (Bearer required)

`/auth/login` and `/auth/refresh` return:

- `accessToken`
- `refreshToken`
- `sessionId`
- `sessionExpiresAt`
- `user`

## Profile Endpoints

- `GET /api/v1/profiles/{userId}` (Bearer required)
- `PUT /api/v1/profiles` (multipart, Bearer required)

`PUT /profiles` accepts:

- `request` (JSON part matching `UpdateProfileRequest`)
- `avatar` (optional file part)

## Post Endpoints

- `POST /api/v1/posts` (multipart, Bearer required)
- `GET /api/v1/posts?page=0&size=20` (Bearer required)
- `POST /api/v1/posts/{postId}/lgtm` (Bearer required)
- `GET /api/v1/posts/{postId}/comments?page=0&size=20` (Bearer required)
- `POST /api/v1/posts/{postId}/comments` (Bearer required)

### Post create request

`POST /posts` is `multipart/form-data`:

- `request` (JSON `CreatePostRequest`)
- `image` (optional file)

Rules:

- You can send `codeSnippet` or `image`, not both.

### Comment create request

`POST /posts/{postId}/comments` supports either:

1. JSON body

```json
{
  "content": "Nice post!"
}
```

2. Query param

`/api/v1/posts/{postId}/comments?content=Nice%20post!`

## Pagination Contract (Important for Frontend)

For paginated endpoints (`GET /posts`, `GET /posts/{postId}/comments`), `data` is a page object (not a raw array):

```json
{
  "isError": false,
  "message": "Feed fetched successfully",
  "data": {
    "content": [
      {
        "id": 24,
        "content": "Hello",
        "imageUrl": null,
        "codeSnippet": null,
        "codeLanguage": null,
        "createdAt": "2026-04-17T17:46:30Z",
        "lgtmCount": 1,
        "author": {
          "id": 8,
          "username": "akshat",
          "avatarUrl": null
        }
      }
    ],
    "pageable": {},
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "first": true,
    "last": true,
    "numberOfElements": 1,
    "empty": false
  }
}
```

## Date/Time Contract

- Post/comment `createdAt` values are ISO-8601 UTC strings.
- Example: `2026-04-17T17:46:30Z`

## cURL Quickstart

```bash
BASE_URL="http://localhost:9000"
```

### Register

```bash
curl -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "strongPassword123"
  }'
```

### Login

```bash
curl -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "strongPassword123"
  }'
```

Set tokens from login response:

```bash
ACCESS_TOKEN="<paste_access_token_here>"
REFRESH_TOKEN="<paste_refresh_token_here>"
POST_ID="<paste_post_id_here>"
```

### Current user

```bash
curl -X GET "$BASE_URL/api/v1/auth/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Refresh access token

```bash
curl -X POST "$BASE_URL/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

### Logout

```bash
curl -X POST "$BASE_URL/api/v1/auth/logout" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Feed

```bash
curl -X GET "$BASE_URL/api/v1/posts?page=0&size=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Toggle LGTM

```bash
curl -X POST "$BASE_URL/api/v1/posts/$POST_ID/lgtm" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Get comments

```bash
curl -X GET "$BASE_URL/api/v1/posts/$POST_ID/comments?page=0&size=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Add comment

```bash
curl -X POST "$BASE_URL/api/v1/posts/$POST_ID/comments" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Great post!"}'
```

## Run

```bash
./mvnw spring-boot:run
```

## Test

```bash
./mvnw test
```

## Postman

Import `postman/rebase-auth.postman_collection.json`.

