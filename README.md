# DeliveryFlow

Production-grade, cloud-native delivery management platform built as a portfolio project to demonstrate real-world microservices architecture on AWS — from local development to a fully automated GitOps deployment.

This project focuses on backend architecture and cloud infrastructure.
All interactions are performed through REST APIs.

## Overview

A customer places a delivery order, pays for it (Stripe, test mode), and tracks it through its lifecycle in real time. Every status change is published as a Kafka event and triggers a (mocked) customer notification.

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
               ↘ CANCELLED
```

- **PENDING** — order created, awaiting payment confirmation
- **CONFIRMED** — payment succeeded, order accepted
- **SHIPPED** — package picked up, in transit
- **DELIVERED** — package delivered to customer
- **CANCELLED** — cancelled by request, payment failure, or automatic expiry

## Features

- **JWT authentication** — RSA-signed access/refresh tokens, validated once at the gateway, identity forwarded downstream
- **Idempotent payments** — Stripe PaymentIntents keyed by `orderId`, safe to retry without double-charging; webhook signature verified on every request
- **Event-driven notifications** — Kafka consumer with per-event idempotency tracking, manual offset commit tied to the DB transaction, Dead Letter Topic for unrecoverable errors
- **Resilience by default** — timeout + retry + circuit breaker (Resilience4j) on every downstream call, with graceful degradation instead of cascading failures
- **Zero long-lived AWS credentials** — GitHub Actions authenticates via OIDC, in-cluster pods authenticate to Secrets Manager and MSK via IRSA — no static keys anywhere
- **True GitOps** — Git is the single source of truth; ArgoCD keeps the cluster in sync automatically (details below)
- **Full observability** — structured logs with trace correlation, Prometheus metrics, Grafana dashboards, distributed tracing via Zipkin

## Architecture

```
Client
  │
  ▼
api-gateway (8080)        JWT validation, routing, rate limiting, circuit breaking
  │
  ├──► order-service (8081)      auth, order lifecycle, payments, product catalog
  │        │
  │        ├──► tracking-service (8082)   REST, circuit-breaker protected
  │        │
  │        └──► Kafka: order.events
  │                          │
  │                          ▼
  │                  notification-service (8083)   Kafka consumer only
  │
  └──► tracking-service (8082)   direct tracking queries
```

Each service owns its database — no shared databases, no cross-service DB access. Internal REST calls use Kubernetes DNS; asynchronous flows go through Kafka.

| Service | Port | Responsibility |
|---|---|---|
| **api-gateway** | 8080 | Sole public entry point. JWT validation, request routing, per-user/IP rate limiting, Resilience4j circuit breakers on every downstream route. No business logic. |
| **order-service** | 8081 | Core domain owner. Authentication (register/login/refresh, RSA-signed JWT), order lifecycle, Stripe payments (idempotent via `orderId`), Kafka event publishing, product catalog, auto-cancellation of expired orders. |
| **tracking-service** | 8082 | Shipment tracking entries and history per order. Pure REST, no Kafka. |
| **notification-service** | 8083 | Stateless Kafka consumer on `order.events`. Sends mocked email/SMS (logs to console), idempotent via processed-event tracking. |

There is intentionally **no separate auth-service** — authentication lives in `order-service` (see [ADR 0005](docs/adr/0005-auth-in-order-service.md)).

## Tech Stack

| Category | Technologies |
|---|---|
| Language & Framework | Java 21, Spring Boot 4.0.6, Spring MVC |
| Persistence & Caching | PostgreSQL (per service), Redis, Flyway, MapStruct |
| Messaging | Apache Kafka |
| Resilience | Resilience4j (timeout, retry, circuit breaker) |
| Observability | Micrometer Tracing, Zipkin, Prometheus, Grafana |
| Payments | Stripe API (test mode) |
| Containers & Orchestration | Docker (multi-stage builds), Helm, Kubernetes (minikube / AWS EKS) |
| CI/CD & GitOps | GitHub Actions, ArgoCD |
| Infrastructure as Code | Terraform |
| AWS Services | EKS, RDS, ElastiCache, MSK Serverless, ECR, Secrets Manager, External Secrets Operator, VPC/NAT, S3 + DynamoDB |

## Prerequisites

- Java 21 and Maven (or use the bundled `./mvnw` in each service)
- Docker + Docker Compose
- For Kubernetes: `minikube`, `helm`, `kubectl`
- For AWS: `terraform`, AWS CLI configured with credentials
- For payment testing: [Stripe CLI](https://stripe.com/docs/stripe-cli)
- `jq` (used in the Quickstart curl examples below)

## Running Locally

```bash
docker compose up -d postgres redis kafka kafka-ui
```

Each service reads its config from `application.yaml` and can be run individually via Maven or IDE. Ports: gateway `8080`, order `8081`, tracking `8082`, notification `8083`. Kafka UI is available at `localhost:9080`.

For payment testing, forward Stripe webhooks with the Stripe CLI:

```bash
stripe listen --forward-to http://localhost:8080/api/v1/webhooks/stripe
```

Test cards: `4242 4242 4242 4242` (succeeds), `4000 0000 0000 9995` (fails).

## Quickstart

With all services running, the full order flow via `api-gateway`:

```bash
# 1. Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@deliveryflow.local","password":"password123"}'

# 2. Login and grab the access token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@deliveryflow.local","password":"password123"}' | jq -r '.accessToken')

# 3. Browse the product catalog
curl http://localhost:8080/api/v1/products -H "Authorization: Bearer $TOKEN"

# 4. Place an order (use a productId from step 3)
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "items": [{"productId":"<product-id>","quantity":1}],
    "deliveryAddress": {"street":"Main St 1","city":"Warsaw","postalCode":"00-001","country":"Poland"}
  }'

# 5. Check status (updates to CONFIRMED once the Stripe webhook fires)
curl http://localhost:8080/api/v1/orders/<order-id> -H "Authorization: Bearer $TOKEN"
```

## Repository Layout

```
api-gateway/            order-service/            tracking-service/            notification-service/
  Spring Boot service source per module, each with its own Dockerfile and Flyway migrations

helm/                   Helm chart per service (values.yaml base + values-local.yaml + values-aws.yaml)
argocd/                 ArgoCD Application manifests (minikube target); argocd/aws/ for the EKS target
k8s/                    One-off cluster manifests not owned by Terraform (ExternalSecrets, ClusterSecretStore)
terraform/              Root config + modules/ (vpc, rds, elasticache, msk, eks, ecr, secrets)
terraform/bootstrap/    S3 + DynamoDB remote state backend (separate, standalone state)
docs/adr/               Architecture Decision Records
docker-compose.yml       Local infra: Postgres, Redis, Kafka, Kafka UI, (optional) Zipkin/Prometheus/Grafana
.github/workflows/       CI/CD pipeline
```

## Observability

Every service exposes `/actuator/prometheus` and propagates trace context (`traceId`/`spanId`) through REST headers and Kafka message headers via Micrometer Tracing.

```bash
docker compose --profile observability up -d zipkin prometheus grafana
```

- **Prometheus** (`localhost:9090`) scrapes all 4 services, including Resilience4j circuit-breaker state and failure rates (`docker/prometheus/prometheus.yml`)
- **Grafana** (`localhost:3000`, anonymous viewer access) ships with a provisioned `deliveryflow-overview` dashboard and a pre-wired Prometheus datasource (`docker/grafana/provisioning/`)
- **Zipkin** (`localhost:9411`) visualizes distributed traces across gateway → order-service → tracking-service and the Kafka publish/consume hop into notification-service

## Running on Kubernetes (minikube)

```bash
minikube start
docker compose up -d postgres redis kafka
helm install order-service helm/order-service -f helm/order-service/values.yaml -f helm/order-service/values-local.yaml
# ...repeat per service, or let ArgoCD manage it (see argocd/)
```

Services reach the docker-compose infra via `host.minikube.internal`.

## CI/CD & GitOps

```
git push → GitHub Actions (build+test → build & push images to ECR via OIDC → bump image tag in Git)
              → ArgoCD detects drift in helm/*/values.yaml → auto-syncs → rolling deploy on EKS
```

- No static AWS credentials in CI — GitHub Actions authenticates to AWS via OIDC federation.
- ArgoCD `Application` manifests use `syncPolicy.automated { prune: true, selfHeal: true }` — the cluster state always converges to what's in Git, with no manual `kubectl apply` or `helm upgrade`.
- `argocd/` targets minikube (`values.yaml` + `values-local.yaml`); `argocd/aws/` targets EKS (`values.yaml` + `values-aws.yaml`).

## AWS Infrastructure

Provisioned via Terraform, spun up only for validation and torn down afterwards (not a 24/7 deployment).

| Module | Provisions |
|---|---|
| `vpc` | VPC, public/private subnets across 2 AZs, single NAT Gateway, EKS discovery tags |
| `rds` | Single PostgreSQL instance (db.t4g.micro), credentials in Secrets Manager |
| `elasticache` | Single Redis node (cache.t4g.micro) |
| `msk` | MSK Serverless cluster, IAM-only auth, IRSA role scoped to `order.events` topic |
| `eks` | EKS cluster + managed node group, OIDC provider for IRSA |
| `ecr` | One repository per service, GitHub OIDC provider + scoped push role |
| `secrets` | JWT keypair and Stripe keys in Secrets Manager, IRSA role for External Secrets Operator |

Secrets never touch Git or CI logs — they're generated/stored directly in Secrets Manager and synced into the cluster at runtime by External Secrets Operator.

## Status

The full AWS deployment (VPC, EKS, RDS, ElastiCache, MSK Serverless, ECR, Secrets Manager) was provisioned via Terraform and validated end-to-end — registration, login, order creation, Stripe payment, Kafka event publication, and notification consumption all confirmed working on real infrastructure. It has since been torn down to avoid ongoing cost, since this is a portfolio project rather than a live service.
