# ADR 0006 — Use GitHub Actions instead of Jenkins for CI

Date: 2026-07
Status: Accepted

## Context

ADR 0003 originally assumed Jenkins as the CI tool building images and triggering the GitOps flow
(Jenkins updates `helm/*/values.yaml`, pushes to Git, ArgoCD syncs EKS). That assumption was never
implemented and does not fit this project's actual scope.

This is a portfolio project with no long-running infrastructure to host and maintain a Jenkins
server. The repository already lives on GitHub, and the AWS environment is provisioned
on demand and torn down after validation — there is no persistent place to run Jenkins between
sessions.

## Decision

Use GitHub Actions for CI (build, test, image build/push) instead of Jenkins.
The GitOps handoff to ArgoCD described in ADR 0003 is unchanged: the workflow updates the image
tag in `helm/*/values.yaml` and pushes to Git; ArgoCD still owns the deploy step by watching the repo.

## Consequences

**Positive:**
- No infrastructure to host or maintain — GitHub Actions runs entirely on GitHub's own runners
- Native integration with this repo (no webhooks/credentials to wire up between two separate systems)
- Free for a public/portfolio-scale repo
- Same GitOps contract as ADR 0003 — ArgoCD-side setup is untouched

**Negative:**
- Less representative of a typical enterprise Jenkins setup for interview-style discussion
- GitHub Actions minutes/runners are less configurable than a self-hosted Jenkins agent

## Alternatives considered

- **Jenkins** (original ADR 0003 assumption) — rejected: requires a persistently running server;
  no fit for a project whose AWS environment is provisioned and destroyed per validation run
- **GitLab CI** — rejected: repo is hosted on GitHub, would add an unnecessary second platform
