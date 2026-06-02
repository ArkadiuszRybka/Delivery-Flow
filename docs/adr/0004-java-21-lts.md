# ADR 0004 — Use Java 21 LTS

Date: 2025-06
Status: Accepted

## Context

Project requires a Java version that is stable, well-supported across the ecosystem,
and available as a long-term support release on AWS infrastructure.

## Decision

Use Java 21 LTS with Amazon Corretto 21 as the JDK distribution.

## Consequences

**Positive:**
- LTS release — supported until 2031 (Amazon Corretto)
- Virtual Threads available (`spring.threads.virtual.enabled=true`) — better throughput for blocking I/O
- Records, pattern matching, sealed classes — cleaner DTO and domain model code
- Amazon Corretto 21 is the native JDK on AWS EKS — no compatibility surprises
- Full Spring Boot 4.0.x support
- Entire ecosystem (Testcontainers, Resilience4j, MapStruct) well-tested on 21

**Negative:**
- Misses Java 25 features (value objects, stable string templates) — not needed for this project

## Alternatives considered

- **Java 17 LTS** — rejected: older LTS, missing virtual threads and modern records syntax
- **Java 25 LTS** — rejected: released September 2025, ecosystem still catching up; no meaningful features needed for this project's use cases
