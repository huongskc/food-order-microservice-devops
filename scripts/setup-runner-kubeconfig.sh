#!/usr/bin/env bash
# Create a namespace-scoped kubeconfig for the GitHub Actions runner.
# Run as root on k8s-master-1 after applying github-actions-rbac.yaml.
set -Eeuo pipefail

KUBECONFIG_OUT="/home/actions-runner/.kube/config"
ADMIN="/etc/kubernetes/admin.conf"

SERVER="$(kubectl --kubeconfig="$ADMIN" config view --minify -o jsonpath='{.clusters[0].cluster.server}')"
TOKEN="$(kubectl --kubeconfig="$ADMIN" -n food-order get secret github-actions-runner-token -o jsonpath='{.data.token}' | base64 --decode)"

if [[ -z "$TOKEN" ]]; then
  echo "ServiceAccount token is empty." >&2
  exit 1
fi

mkdir -p "$(dirname "$KUBECONFIG_OUT")"

kubectl config --kubeconfig="$KUBECONFIG_OUT" \
  set-cluster onprem --server="$SERVER" \
  --certificate-authority=/etc/kubernetes/pki/ca.crt --embed-certs=true

kubectl config --kubeconfig="$KUBECONFIG_OUT" \
  set-credentials github-actions-runner --token="$TOKEN"

kubectl config --kubeconfig="$KUBECONFIG_OUT" \
  set-context food-order --cluster=onprem \
  --user=github-actions-runner --namespace=food-order

kubectl config --kubeconfig="$KUBECONFIG_OUT" use-context food-order

chown -R actions-runner "$(dirname "$KUBECONFIG_OUT")"
chmod 700 "$(dirname "$KUBECONFIG_OUT")"
chmod 600 "$KUBECONFIG_OUT"

echo "Kubeconfig created: $KUBECONFIG_OUT"
