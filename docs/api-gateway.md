# API Gateway

Single public entry point for all client requests. No business logic here.
Owned by `api-gateway` service, port `8080`.

---

## Responsibilities

- **JWT validation** — every request (except `/api/v1/auth/**` and `/api/v1/webhooks/**`) must carry a valid JWT in `Authorization: Bearer <token>` header. Invalid or missing token → 401.
- **Routing** — forwards requests to downstream services via Kubernetes DNS.
- **Rate limiting** — limits requests per user/IP to prevent abuse.
- **Circuit breaker** — Resilience4j per downstream route; if a service is unavailable, gateway fails fast with 503 instead of waiting for timeout.

---

## Routing table

| Path prefix | Downstream service | Port |
|---|---|---|
| `/api/v1/orders/**` | order-service | 8081 |
| `/api/v1/tracking/**` | tracking-service | 8082 |
| `/api/v1/auth/**` | order-service (auth endpoints) | 8081 |
| `/api/v1/webhooks/stripe` | order-service (webhook) | 8081 |
| `/api/v1/products/**` | order-service | 8081 |
| `/api/v1/admin/products/**` | order-service | 8081 |

---

## Public endpoints (no JWT required)

```
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/webhooks/stripe     ← Stripe signature verification instead of JWT
GET  /api/v1/products            ← product catalog (public — no JWT required)
GET  /api/v1/products/{id}       ← single product (public — no JWT required)
```

---

## JWT propagation

After validating the JWT, the gateway strips the `Authorization` header and forwards
the decoded user identity as request headers to downstream services:

```
X-User-Id: <UUID>
X-User-Role: USER | ADMIN
```

Downstream services trust these headers without re-validating the token.
They must never be exposed to or accepted from external clients directly.

---

## Rate limiting

- Per authenticated user: 100 requests/minute
- Per IP (unauthenticated): 20 requests/minute
- Exceeding the limit → 429 Too Many Requests

---

## Circuit breaker (Resilience4j)

Each downstream route has its own circuit breaker instance:

| Instance | Failure threshold | Wait duration |
|---|---|---|
| `order-service` | 50% over 10 calls | 10s |
| `tracking-service` | 50% over 10 calls | 10s |

On open circuit → 503 Service Unavailable with RFC 7807 Problem Details body.

---

## Error responses

All errors follow RFC 7807 Problem Details:

```json
{
  "type": "https://deliveryflow.com/errors/unauthorized",
  "title": "Unauthorized",
  "status": 401,
  "detail": "JWT token is missing or invalid",
  "instance": "/api/v1/orders/abc"
}
```

---

## No database

API Gateway has no database, no Redis, no Kafka.
It is a pure routing and security layer.

---

## Local development

Gateway runs on `localhost:8080`. All Postman requests go through this port.
Downstream services should not be called directly during normal testing.

Exception: direct calls to `localhost:8081`, `localhost:8082` etc. are acceptable
when debugging a specific service in isolation.
