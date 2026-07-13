# ADR 0005 — Authentication in order-service, not a separate auth-service

Date: 2025-06
Status: Accepted

## Context

The system requires user registration, login, and JWT issuance.
An initial plan considered a dedicated auth-service (5th microservice) as the JWT issuer.

## Decision

Authentication endpoints (register, login, refresh token) live in order-service.
There is no auth-service. The project remains at 4 microservices.

JWT tokens are signed with an RSA private key held only by order-service.
API Gateway validates tokens using the RSA public key — no runtime dependency on order-service per request.

## Consequences

**Positive:**
- No additional service to build, deploy, maintain, or operate
- Stateless JWT validation in gateway — no network call to auth on every request
- Simpler local development — one less service to start
- Architecture is easier to explain and justify at this scale
- On CV: "stateless JWT validation with RSA key pair" is a stronger talking point than "I built an auth microservice"

**Negative:**
- order-service owns two concerns (orders + auth) — acceptable at this scale
- If auth ever needs to be extracted, it requires refactoring — not a concern for a portfolio project

## Alternatives considered

- **Separate auth-service** — rejected: adds operational complexity without meaningful benefit at 4-service scale
- **Keycloak** — rejected: heavy dependency, complex setup, obscures the JWT implementation knowledge being demonstrated
- **Symmetric HMAC-SHA256** — rejected in favour of RSA: with RSA, only order-service holds the private key; gateway and other services can verify tokens using only the public key, which is a better security model for microservices
