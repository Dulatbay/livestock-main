# LiveStock API Documentation

Base URL: `/api`

Swagger UI: `/api/swagger-ui.html`
OpenAPI spec: `/api/v3/api-docs`

---

## Files

File serving endpoint. Uploaded files are stored locally and accessible via this endpoint.

### Get File

```
GET /files/{filename}
```

Returns the file content with the appropriate `Content-Type` header.

**Auth:** None (public)

**Path Parameters:**

| Parameter | Type   | Description                   |
|-----------|--------|-------------------------------|
| filename  | string | Filename (e.g. `uuid.jpg`)    |

**Response:** `200 OK` — file binary content with detected media type.

---

## News

News management endpoints. Public read access; write operations require `ROLE_ADMIN` or `ROLE_OPERATOR` authority.

### List News

```
GET /news
```

Returns a paginated list of active (non-deleted) news articles. Supports search by title/description.

**Auth:** None (public)

**Query Parameters:**

| Parameter | Type   | Required | Default | Description                          |
|-----------|--------|----------|---------|--------------------------------------|
| query     | string | no       | —       | Search term (matches title or description, case-insensitive) |
| page      | int    | no       | 0       | Page number (0-based)                |
| size      | int    | no       | 10      | Page size                            |
| sort      | string | no       | createdAt,desc | Sort field and direction       |

**Response:** `200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "title": "Sample news title",
      "description": "Full news text content",
      "imageUrl": "https://example.com/image.jpg",
      "createdAt": "2026-03-03T12:00:00Z",
      "updatedAt": "2026-03-03T12:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10,
  "first": true,
  "last": true,
  "empty": false
}
```

---

### Get News by ID

```
GET /news/{id}
```

Returns a single news article. Returns 404 if the article does not exist or has been soft-deleted.

**Auth:** None (public)

**Path Parameters:**

| Parameter | Type | Description       |
|-----------|------|-------------------|
| id        | long | News article ID   |

**Response:** `200 OK`

```json
{
  "id": 1,
  "title": "Sample news title",
  "description": "Full news text content",
  "imageUrl": "https://example.com/image.jpg",
  "createdAt": "2026-03-03T12:00:00Z",
  "updatedAt": "2026-03-03T12:00:00Z"
}
```

**Error:** `404 Not Found`

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "News not found with id: 999",
  "errors": ["News not found with id: 999"],
  "timestamp": "2026-03-03T12:00:00Z"
}
```

---

### Create News

```
POST /news
Content-Type: multipart/form-data
```

Creates a new news article. Accepts `multipart/form-data` with a JSON part `data` and an optional file part `image`.

**Auth:** Bearer token with `ROLE_ADMIN` or `ROLE_OPERATOR`

**Multipart Parts:**

| Part   | Type             | Required | Description                                              |
|--------|------------------|----------|----------------------------------------------------------|
| data   | application/json | yes      | JSON object with `title` (string, max 150, required) and `description` (string, required) |
| image  | file             | no       | Cover image file (max 10 MB)                             |

**Example (curl):**

```bash
curl -X POST /api/news \
  -H "Authorization: Bearer <token>" \
  -F 'data={"title":"New article","description":"Content"};type=application/json' \
  -F 'image=@photo.jpg'
```

**Response:** `201 Created`

```json
{
  "id": 100,
  "title": "New article",
  "description": "Content",
  "imageUrl": "/files/a1b2c3d4-uuid.jpg",
  "createdAt": "2026-03-03T12:00:00Z",
  "updatedAt": "2026-03-03T12:00:00Z"
}
```

**Errors:**

| Status | Condition                    |
|--------|------------------------------|
| 401    | No authentication token      |
| 403    | Insufficient role            |
| 400    | Validation failed            |

---

### Update News

```
PUT /news/{id}
Content-Type: multipart/form-data
```

Updates an existing news article. If a new `image` part is provided, the old image file is replaced.

**Auth:** Bearer token with `ROLE_ADMIN` or `ROLE_OPERATOR`

**Path Parameters:**

| Parameter | Type | Description       |
|-----------|------|-------------------|
| id        | long | News article ID   |

**Multipart Parts:**

| Part   | Type             | Required | Description                                              |
|--------|------------------|----------|----------------------------------------------------------|
| data   | application/json | yes      | JSON object with `title` (string, max 150, required) and `description` (string, required) |
| image  | file             | no       | New cover image file (max 10 MB). Omit to keep existing. |

**Example (curl):**

```bash
curl -X PUT /api/news/1 \
  -H "Authorization: Bearer <token>" \
  -F 'data={"title":"Updated title","description":"Updated content"};type=application/json' \
  -F 'image=@new-photo.jpg'
```

**Response:** `200 OK` — returns the updated article (same shape as Create response).

**Errors:**

| Status | Condition                    |
|--------|------------------------------|
| 401    | No authentication token      |
| 403    | Insufficient role            |
| 400    | Validation failed            |
| 404    | Article not found            |

---

### Delete News (Soft Delete)

```
DELETE /news/{id}
```

Soft-deletes a news article (sets `is_deleted = true`). The article will no longer appear in list or detail responses.

**Auth:** Bearer token with `ROLE_ADMIN` or `ROLE_OPERATOR`

**Path Parameters:**

| Parameter | Type | Description       |
|-----------|------|-------------------|
| id        | long | News article ID   |

**Response:** `204 No Content`

**Errors:**

| Status | Condition                    |
|--------|------------------------------|
| 401    | No authentication token      |
| 403    | Insufficient role            |
| 404    | Article not found            |
