# OpenAPI Conventions (v1)

## Base rules
- External APIs must be versioned with `/api/v1`.
- Public endpoints are explicitly documented; everything else is authenticated.
- Request/response DTOs are separate from domain models.

## Error response contract
All 4xx/5xx responses follow:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": ["email must not be blank"],
  "traceId": "a1b2c3..."
}
```

## Naming
- Paths: kebab-case
- JSON fields: camelCase
- Operation IDs: `<resource>_<action>`

## Versioning
- Event schemas include `eventType`, `eventVersion`, and UTC timestamp.
- API breaking changes require new version path (`/api/v2`).
