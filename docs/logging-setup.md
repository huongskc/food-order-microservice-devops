# Logging Setup Guide

This guide installs centralized logging for Kubernetes using Loki, Grafana Alloy, and the existing Grafana instance.

The configuration is intentionally small for this homelab:

- Loki runs in single-binary mode with one replica.
- Grafana Alloy runs as a DaemonSet and collects Pod stdout/stderr logs.
- Loki stores data on a 5 GiB static NFS volume.
- Log retention is limited to three days.

## 1. Prepare the NFS Directory

Run these commands on `storage-server`:

```bash
sudo mkdir -p /data/nfs-shared/logging/loki
sudo chown -R nobody:nogroup /data/nfs-shared/logging
sudo chmod -R 777 /data/nfs-shared/logging
```

## 2. Create the Namespace and Static Storage

Run these commands on `k8s-master-1`:

```bash
kubectl create namespace logging
kubectl apply -f ~/logging/storage.yaml
```

## 3. Install Loki

This setup pins the Loki chart to version `18.11.2`, which deploys Loki `3.7.6`.

Add and update the Grafana Community repository:

```bash
helm repo add grafana-community https://grafana-community.github.io/helm-charts
helm repo update
```

Check the selected chart version:

```bash
helm search repo grafana-community/loki --versions | head -n 10
helm show chart grafana-community/loki --version 18.11.2
```

Install the pinned chart in namespace `logging`:

```bash
helm upgrade --install loki grafana-community/loki \
  --version 18.11.2 \
  --namespace logging \
  --values ~/logging/loki-values.yaml
```

Verify the Helm release and Kubernetes resources:

```bash
helm -n logging list
kubectl -n logging rollout status statefulset/loki --timeout=5m
kubectl -n logging get pods -o wide
kubectl -n logging get statefulset,service,pvc
kubectl -n logging get endpoints loki loki-memberlist
kubectl -n logging logs statefulset/loki --tail=100
```

The expected state is:

- Helm release `loki` is `deployed`;
- Pod `loki-0` is `1/1 Running`;
- PVC `logging-loki-pvc` remains `Bound`;
- `/var/loki` is mounted from `logging-loki-pvc` as read-write;
- Loki reports `Loki started` in its logs.

## 4. Install Grafana Alloy

This setup pins the Alloy chart to version `1.12.0`, which deploys Alloy
`v1.19.0`.

Add and update the official Grafana Helm repository:

```bash
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
```

Check the selected chart version:

```bash
helm search repo grafana/alloy --versions | head -n 10
helm show chart grafana/alloy --version 1.12.0
```

The Alloy configuration is split by responsibility:

- `alloy-configmap.yaml` contains the log discovery, relabeling, processing, and
  Loki write pipeline;
- `alloy-values.yaml` contains Helm deployment settings, resources, security
  context, DaemonSet toleration, and minimum RBAC.

Create the ConfigMap, then install Alloy in namespace `logging`:

```bash
kubectl apply -f ~/logging/alloy-configmap.yaml

helm upgrade --install alloy grafana/alloy \
  --version 1.12.0 \
  --namespace logging \
  --values ~/logging/alloy-values.yaml
```

If `alloy-configmap.yaml` is changed later, apply it and restart the DaemonSet because the lightweight setup disables the config-reloader sidecar:

```bash
kubectl apply -f ~/logging/alloy-configmap.yaml
kubectl -n logging rollout restart daemonset/alloy
```

### Verify Alloy

```bash
kubectl -n logging rollout status daemonset/alloy --timeout=5m
kubectl -n logging get configmap alloy-config
kubectl -n logging get daemonset alloy
kubectl -n logging get pods -l app.kubernetes.io/instance=alloy -o wide
kubectl -n logging logs daemonset/alloy --tail=100
```

The expected state is:

- DaemonSet `alloy` has three desired, current, and ready Pods;
- one Alloy Pod runs on each Kubernetes node;
- the Alloy logs contain no configuration, authorization, or Loki write errors.

## 5. Add the Loki Datasource to Grafana

Apply the datasource:

```bash
kubectl apply -f ~/logging/grafana-datasource.yaml
kubectl -n monitoring get configmap grafana-datasource-loki
kubectl -n monitoring logs deployment/monitoring-grafana \
  -c grafana-sc-datasources --tail=50
```

Open Grafana, go to **Connections > Data sources**, and verify that datasource **Loki** is present.

Click **Test** on the Loki datasource page. Grafana should report
`Data source successfully connected`.