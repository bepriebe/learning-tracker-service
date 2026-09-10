#!/usr/bin/env bash
set +x
set -euo pipefail

KUBE=(kubectl --context=learning-tracker-homelab --namespace=learning-tracker-dev)
EXISTING_SECRET="$("${KUBE[@]}" get secret learning-tracker-db --ignore-not-found -o name)"
if [[ -n "$EXISTING_SECRET" ]]; then
  echo 'learning-tracker-db already exists; kept unchanged.'
  exit 0
fi

umask 077
SECRET_DIR="$(mktemp -d)"
trap 'rm -rf -- "$SECRET_DIR"' EXIT
openssl rand -hex 32 | tr -d '\n' > "$SECRET_DIR/password"
"${KUBE[@]}" create secret generic learning-tracker-db \
  --from-file=password="$SECRET_DIR/password"
