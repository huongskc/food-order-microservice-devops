# Monitoring Setup Guide

## 1. Metrics Server

The cluster runs Kubernetes `v1.30.14`, so Metrics Server `v0.7.2` is used.

```bash
kubectl apply -f \
  https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.7.2/components.yaml
```

Check the deployment and the Metrics API:

```bash
kubectl -n kube-system rollout status deployment/metrics-server --timeout=3m
kubectl get apiservice v1beta1.metrics.k8s.io
```

The APIService must show `AVAILABLE=True`.

## 2. Kubelet Certificate Note

This homelab uses kubelet certificates without IP Subject Alternative Names. Metrics Server accesses kubelets through their Internal IP addresses, so TLS verification initially fails and the Metrics API has no ready endpoint.

For this lab, add `--kubelet-insecure-tls` to Metrics Server:

```bash
kubectl -n kube-system patch deployment metrics-server \
  --type=json \
  --patch='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
```

## 3. Verify Resource Metrics

After the rollout completes, check node and Pod usage:

```bash
kubectl top nodes
kubectl top pods -A --sort-by=memory
```

## 4. Install Prometheus and Grafana

The repository provides these prepared files:

```text
monitoring/storage.yaml
monitoring/values.yaml
monitoring/ingress.yaml
```

The configuration is intentionally small for this homelab:

- Prometheus stores seven days of metrics on a 5 Gi NFS volume.
- Grafana uses a separate 5 Gi NFS volume.
- Alertmanager and control-plane metric targets are disabled for the first installation.
- Prometheus, Grafana, and supporting components have modest resource requests and limits.

Before installing the Helm chart, run these commands on `storage-server`:

```bash
sudo mkdir -p /data/nfs-shared/monitoring/prometheus
sudo mkdir -p /data/nfs-shared/monitoring/grafana
sudo chown -R nobody:nogroup /data/nfs-shared/monitoring
sudo chmod -R 777 /data/nfs-shared/monitoring
```

Then run these commands on `k8s-master-1`:
```bash
kubectl create namespace monitoring
kubectl apply -f ~/monitoring/storage.yaml

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm upgrade --install monitoring prometheus-community/kube-prometheus-stack --version 88.5.0 \
--namespace monitoring --values ~/monitoring/values.yaml
```

Verify the workloads and persistent storage:

```bash
kubectl -n monitoring get pods
kubectl -n monitoring get pvc
kubectl get pv monitoring-prometheus-pv monitoring-grafana-pv
```

```bash
kubectl -n monitoring rollout status deployment/monitoring-grafana --timeout=5m
kubectl -n monitoring get pod -l app.kubernetes.io/name=grafana
```

Apply the Grafana Ingress:

```bash
kubectl apply -f ~/monitoring/ingress.yaml
kubectl -n monitoring get ingress grafana-ingress
```

The external Nginx load balancer uses `nginx-lb/grafana.conf`

## 5. Access and Verify Grafana

Open Grafana through the public hostname:

```text
https://grafana.myproject.bar
```

The installed chart provides Kubernetes dashboards for checking:

- cluster resource usage;
- namespace and Pod memory usage;
- node capacity and usage;
- workload and Pod status.

The installation is working when the Prometheus data source is available and the
Kubernetes dashboards display node, namespace, and Pod metrics.
