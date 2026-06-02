# ADR 0002 — Use Resilience4j for circuit breaking and retry

Date: 2025-06
Status: Accepted

## Context

Order Service calls Tracking Service synchronously via REST.
If Tracking Service is unavailable, Order Service requests fail with timeouts,
potentially causing cascading failures across the system.

## Decision

Use Resilience4j for all synchronous inter-service calls.
Apply circuit breaker + retry + timeout on Order Service → Tracking Service calls.
Fallback: serve stale tracking data from Redis cache when Tracking Service is unavailable.

## Consequences

**Positive:**
- Prevents cascading failures — circuit opens after 50% failure rate, fails fast instead of waiting
- Graceful degradation — customers still see cached tracking data during Tracking Service outages
- Resilience4j exposes metrics to Prometheus — circuit breaker state visible in Grafana
- Native Spring Boot 3+ integration via `resilience4j-spring-boot3`

**Negative:**
- Additional configuration per protected call
- Fallback data may be stale — acceptable tradeoff for availability

## Alternatives considered

- **No protection** — rejected: single downstream failure would cascade to all order reads
- **Hystrix** — rejected: in maintenance mode, not compatible with Spring Boot 3+
- **Manual try/catch with retry** — rejected: reinventing the wheel, no metrics
