# On-Premises Infrastructure Setup Guide

This guide details the setup of a self-hosted Kubernetes cluster on VMware Workstation, including Nginx Load Balancer, Cloudflare Tunnel, Rancher UI, and NFS Storage.

## 1. Virtual Machine Inventory

- **Operating System:** Ubuntu Server 24.04 LTS
- **Networking:** Bridged Networking (Static IPs)

| Hostname | IP Address | Hardware Specs | Role & Purpose |
|---|---|---|---|
| `loadbalancer-server` | 192.168.1.101 | 1 vCPU, 2GB RAM | Ingress Gateway (Nginx & Cloudflare Tunnel) |
| `storage-server` | 192.168.1.102 | 2 vCPU, 4GB RAM | NFS Storage Server (Data storage for K8s MySQL) |
| `k8s-master-1` | 192.168.1.110 | 2 vCPU, 4GB RAM | K8s Control Plane Node |
| `k8s-master-2` | 192.168.1.111 | 2 vCPU, 4GB RAM | K8s Worker Node 1 |
| `k8s-master-3` | 192.168.1.112 | 2 vCPU, 4GB RAM | K8s Worker Node 2 |
| `rancher-server` | 192.168.1.115 | 2 vCPU, 4GB RAM | Rancher Web UI (K8s Management Dashboard) |

---

## 2. Load Balancer Setup (`loadbalancer-server`)

The load balancer receives incoming traffic from the internet via Cloudflare Tunnel (without requiring router port forwarding).

### Install Nginx & Cloudflare Tunnel

```bash
# 1. Install Nginx
sudo apt update && sudo apt install nginx -y

# 2. Install Cloudflare Tunnel (cloudflared)
sudo mkdir -p --mode=0755 /usr/share/keyrings
curl -fsSL https://pkg.cloudflare.com/cloudflare-main.gpg | sudo tee /usr/share/keyrings/cloudflare-main.gpg >/dev/null
echo 'deb [signed-by=/usr/share/keyrings/cloudflare-main.gpg] https://pkg.cloudflare.com/cloudflared any main' | sudo tee /etc/apt/sources.list.d/cloudflared.list
sudo apt-get update && sudo apt-get install cloudflared -y

# 3. Enable cloudflared service with token from Cloudflare Dashboard
sudo cloudflared service install <TUNNEL_TOKEN>
```

### Nginx Virtual Host Configuration (`/etc/nginx/conf.d/rancher.conf`)

```nginx
map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}

server {
    listen 80;
    server_name rancher.myproject.bar;

    location / {
        proxy_pass https://192.168.1.115:443;
        proxy_ssl_verify off;

        proxy_http_version 1.1;
        proxy_set_header Host $host;

        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;

        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

---

## 3. Rancher Server Setup (`rancher-server`)

Rancher is installed via Docker Compose to manage and monitor the Kubernetes cluster using a web interface.

```bash
# Install Docker & Docker Compose
sudo apt update && sudo apt install docker.io docker-compose -y
```

**File `docker-compose.yml`:**

```yaml
version: '3'
services:
  rancher-server:
    image: rancher/rancher:v2.9.2
    container_name: rancher-server
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./data/:/var/lib/rancher
    command:
      - --no-cacerts
    environment:
      - CATTLE_AGENT_TLS_MODE=system-store
    privileged: true
```

Start Rancher:
```bash
docker-compose up -d
```

---

## 4. NFS Storage Server Setup (`storage-server`)

A dedicated 40GB virtual disk (`/dev/sdb`) is mounted at `/data` to store data for stateful workloads (like MySQL) on Kubernetes.

### Format and Mount Secondary Disk

Inspect the disk before formatting it:

```bash
lsblk -o NAME,SIZE,TYPE,FSTYPE,MOUNTPOINTS
sudo blkid /dev/sdb
```

```bash
# 1. Format the confirmed secondary disk using ext4 (use 100% capacity)
sudo mkfs.ext4 -m 0 /dev/sdb

# 2. Create /data directory and mount the disk
sudo mkdir /data
echo "/dev/sdb /data ext4 defaults 0 0" | sudo tee -a /etc/fstab
sudo mount -a
sudo systemctl daemon-reload
```

### Configure NFS Server

```bash
# 1. Install NFS Server
sudo apt install nfs-kernel-server -y

# 2. Create shared directory and set permissions
sudo mkdir /data/nfs-shared
sudo chown -R nobody:nogroup /data/nfs-shared
sudo chmod 777 /data/nfs-shared

# 3. Export shared directory to K8s cluster subnet (192.168.1.0/24)
echo '/data/nfs-shared 192.168.1.0/24(rw,sync,no_subtree_check,no_root_squash)' | sudo tee -a /etc/exports
sudo exportfs -a
sudo systemctl enable --now nfs-kernel-server
```

---

## 5. Kubernetes Cluster Setup (`k8s-master-1..3`)

The cluster consists of 1 Control Plane node (`.110`) and 2 Worker nodes (`.111`, `.112`).

### 5.1 Common Setup (Run on ALL 3 NODES)

```bash
# 1. Add hostnames to /etc/hosts
cat <<EOF | sudo tee -a /etc/hosts
192.168.1.110 k8s-master-1
192.168.1.111 k8s-master-2
192.168.1.112 k8s-master-3
EOF

# 2. Disable swap (required by K8s)
sudo swapoff -a
sudo sed -i '/swap.img/s/^/#/' /etc/fstab

# 3. Enable Kernel modules for containerd
echo -e "overlay\nbr_netfilter" | sudo tee /etc/modules-load.d/containerd.conf > /dev/null
sudo modprobe overlay
sudo modprobe br_netfilter

# 4. Configure Sysctl for K8s networking
echo "net.bridge.bridge-nf-call-ip6tables = 1" | sudo tee -a /etc/sysctl.d/kubernetes.conf
echo "net.bridge.bridge-nf-call-iptables = 1" | sudo tee -a /etc/sysctl.d/kubernetes.conf
echo "net.ipv4.ip_forward = 1" | sudo tee -a /etc/sysctl.d/kubernetes.conf
sudo sysctl --system

# 5. Install Container Runtime (containerd)
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmour -o /etc/apt/trusted.gpg.d/docker.gpg
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt update && sudo apt install -y containerd.io
containerd config default | sudo tee /etc/containerd/config.toml >/dev/null 2>&1
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml
sudo systemctl restart containerd && sudo systemctl enable containerd

# 6. Install kubeadm, kubelet, kubectl (Version v1.30)
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.30/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.30/deb/ /" | sudo tee /etc/apt/sources.list.d/kubernetes.list
sudo apt update && sudo apt install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# 7. Install nfs-common to mount NFS volumes
sudo apt install -y nfs-common
```

### 5.2 Initialize Control Plane Node (`k8s-master-1`)

The Pod CIDR is explicitly set to `172.16.0.0/16` so that the Calico Pod network does not overlap the VM network `192.168.1.0/24`.

```bash
# 1. Initialize K8s cluster
sudo kubeadm init --pod-network-cidr=172.16.0.0/16

# 2. Configure kubectl access for regular user
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 3. Download Calico CNI manifest
curl -LO https://raw.githubusercontent.com/projectcalico/calico/v3.25.0/manifests/calico.yaml
```

Before applying `calico.yaml`, set the following environment variable in the `calico-node` container environment:

```yaml
- name: CALICO_IPV4POOL_CIDR
  value: "172.16.0.0/16"
```

Then deploy Calico:

```bash
kubectl apply -f calico.yaml
```

### 5.3 Join Worker Nodes (`k8s-master-2`, `k8s-master-3`)

Run the `kubeadm join` command generated by the Master node on each Worker node:
```bash
sudo kubeadm join 192.168.1.110:6443 --token <token> --discovery-token-ca-cert-hash sha256:<hash>
```

Label worker nodes (run on Master):
```bash
kubectl label node k8s-master-2 node-role.kubernetes.io/worker=
kubectl label node k8s-master-3 node-role.kubernetes.io/worker=
```

### 5.4 Install Helm & Ingress Controller

```bash
# 1. Install Helm v3.16.2
wget https://get.helm.sh/helm-v3.16.2-linux-amd64.tar.gz
tar xvf helm-v3.16.2-linux-amd64.tar.gz
sudo mv linux-amd64/helm /usr/bin/

# 2. Download and configure Nginx Ingress Controller
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm pull ingress-nginx/ingress-nginx
tar -xzf ingress-nginx-4.15.1.tgz
```

Edit `ingress-nginx/values.yaml`:
- Change `type: LoadBalancer` -> `type: NodePort`
- Set HTTP port: `30080`, HTTPS port: `30443`

Install into K8s:
```bash
kubectl create ns ingress-nginx
helm -n ingress-nginx install ingress-nginx -f ingress-nginx/values.yaml ingress-nginx
```

---

## 6. Import K8s Cluster Into Rancher

Import the newly created Kubernetes cluster into Rancher for centralized management:

```bash
curl --insecure -sfL https://rancher.myproject.bar/v3/import/<CLUSTER_TOKEN>.yaml | kubectl apply -f -
```

## 7. CI/CD Runner

The GitHub Actions self-hosted runner runs on `k8s-master-1`.

See the [Self-Hosted Runner Setup Guide](self-hosted-runner-setup.md) for installation, Kubernetes access, and service operations.
