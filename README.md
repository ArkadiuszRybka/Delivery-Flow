# DeliveryFlow

Cloud-native delivery management platform built with Spring Boot microservices.

> 🚧 Project in active development — see build stages below.

## Architecture
- 4 Spring Boot microservices (API Gateway, Order, Tracking, Notification)
- PostgreSQL per service, Redis, Kafka
- Kubernetes on AWS EKS (Terraform)
- Jenkins + ArgoCD GitOps

## Tech Stack
Java 21 · Spring Boot 4.0.6 · PostgreSQL · Redis · Kafka · Docker · Kubernetes · Terraform · AWS

## Build Stages
- [ ] Phase 1 — Maven multi-module, all 4 services, PostgreSQL
- [ ] Phase 2 — JWT security, API Gateway, Redis, order lifecycle
- [ ] Phase 3 — Resilience4j, Kafka, Testcontainers
- [x] Phase 4 — Distributed tracing, Prometheus, Grafana
- [ ] Phase 5 — Kubernetes, Helm, Jenkins, ArgoCD
- [ ] Phase 6 — Terraform, AWS deployment
