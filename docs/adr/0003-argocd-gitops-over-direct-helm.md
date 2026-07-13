# ADR 0003 — Use ArgoCD for Kubernetes deployments (GitOps)

Date: 2025-06
Status: Accepted (CI tool superseded by ADR 0006 — see below; ArgoCD/GitOps decision itself still stands)

## Context

Jenkins CI pipeline builds images and pushes them to ECR.
The deployment step needs to update the running services on EKS.
Initial approach: Jenkins runs `helm upgrade` directly on EKS at the end of the pipeline (push-based).

## Decision

Use ArgoCD for deployments instead of direct Helm from Jenkins.
Jenkins updates the image tag in `helm/*/values.yaml` and pushes to Git.
ArgoCD watches the Git repo and syncs EKS to match the declared state (pull-based GitOps).

## Consequences

**Positive:**
- Git is the single source of truth for cluster state
- Full audit trail — every deployment is a Git commit (who, what, when)
- Rollback = `git revert` → ArgoCD auto-syncs
- ArgoCD detects and alerts on manual changes (drift detection)
- Separation of concerns — Jenkins owns build, ArgoCD owns deploy

**Negative:**
- Additional component to install and maintain on EKS
- Slightly more complex initial setup than direct `helm upgrade`
- Jenkins needs write access to the Git repo

## Alternatives considered

- **Direct `helm upgrade` from Jenkins** — rejected: no drift detection, rollback is manual, no Git audit trail
- **Flux** — considered but ArgoCD has better UI and wider adoption in job market
