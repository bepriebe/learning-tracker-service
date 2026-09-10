#!/usr/bin/env bash
set -euo pipefail

DEPLOY_IMAGE="${1:?Usage: bash scripts/deploy-dev.sh ghcr.io/bepriebe/learning-tracker-service@sha256:DIGEST}"
if [[ ! "$DEPLOY_IMAGE" =~ ^ghcr\.io/bepriebe/learning-tracker-service@sha256:[a-f0-9]{64}$ ]]; then
  echo 'Expected the immutable GHCR digest of the published Learning Tracker image.' >&2
  exit 1
fi

PROJECT_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
KUBE=(kubectl --context=learning-tracker-homelab --namespace=learning-tracker-dev)

# Check only metadata; never print secret contents.
"${KUBE[@]}" get secret ghcr-pull learning-tracker-db -o name

"${KUBE[@]}" apply -f "$PROJECT_ROOT/k8s/dev/postgres.yaml"
"${KUBE[@]}" rollout status deployment/learning-tracker-postgres --timeout=300s

# Render to stdout: the tracked manifest remains unchanged.
sed "s|ghcr.io/bepriebe/learning-tracker-service:deployment-placeholder|${DEPLOY_IMAGE}|g" \
  "$PROJECT_ROOT/k8s/dev/app.yaml" | "${KUBE[@]}" apply -f -
"${KUBE[@]}" rollout status deployment/learning-tracker-service --timeout=300s

# Readiness includes the database health check.
"${KUBE[@]}" get pods,services,pvc
printf 'Dev deployment ready: %s\n' "$DEPLOY_IMAGE"
