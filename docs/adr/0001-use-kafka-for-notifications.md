# ADR 0001 — Use Kafka for notification delivery

Date: 2025-06
Status: Accepted

## Context

Order Service needs to notify customers on every order status change.
The initial design called Order Service to trigger Notification Service via REST.

## Decision

Use Apache Kafka (topic: `order.events`) for notification delivery instead of synchronous REST calls.
Order Service publishes events. Notification Service consumes them independently.

## Consequences

**Positive:**
- Loose coupling — Order Service does not know about Notification Service
- Notification Service can be down; Kafka retains messages and delivers them when it recovers
- Easy to add future consumers (analytics, audit log) without touching Order Service
- Natural fit for idempotency via `eventId`

**Negative:**
- Eventual consistency — notification may arrive slightly after the status change
- Local dev requires running Kafka (Docker Compose)
- More moving parts than REST

## Alternatives considered

- **REST call from Order Service** — rejected: tight coupling, Notification downtime causes errors in Order Service
- **Spring Events (in-process)** — rejected: does not work across service boundaries
