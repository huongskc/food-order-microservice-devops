# GitHub Actions Self-Hosted Runner Setup

This guide installs a repository-level GitHub Actions runner directly on `k8s-master-1`.

The runner uses a dedicated Linux user and runs as a systemd service. Kubernetes access is limited to the `food-order` namespace.

## 1. Create the Runner User

Run on `k8s-master-1` as root:

```bash
adduser actions-runner
```

Switch to the new user and create the installation directory:

```bash
su actions-runner
cd
mkdir actions-runner
cd actions-runner
```

## 2. Download the GitHub Actions Runner

Download the Linux X64 runner package:

```bash
curl -o actions-runner-linux-x64-2.336.0.tar.gz -L \
  https://github.com/actions/runner/releases/download/v2.336.0/actions-runner-linux-x64-2.336.0.tar.gz
```

Extract the package:

```bash
tar xzf actions-runner-linux-x64-2.336.0.tar.gz
```

## 3. Register the Runner

Open the GitHub repository and go to:

```text
Settings > Actions > Runners > New self-hosted runner
```

Select **Linux** and **X64**. Copy the registration command shown by GitHub and run it as `actions-runner`:

```bash
./config.sh \
  --url https://github.com/huongskc/food-order-microservice-devops \
  --token <REGISTRATION_TOKEN>
```

## 4. Run the Runner as a systemd Service

Return to the root user:

```bash
exit
cd /home/actions-runner/actions-runner
```

Install and start the service under the `actions-runner` user:

```bash
./svc.sh install actions-runner
./svc.sh start
./svc.sh status
```

## 5. Configure Kubernetes Access

The runner needs a kubeconfig before it can run deployment commands. Without a kubeconfig, `kubectl` tries to connect to `localhost:8080`.

Apply the namespace-scoped RBAC manifest through Rancher or with an administrator kubeconfig:

```bash
kubectl apply -f github-actions-rbac.yaml
```

The manifest creates:

- ServiceAccount `github-actions-runner`
- Role `github-actions-runner`
- RoleBinding `github-actions-runner`
- Secret `github-actions-runner-token`

The source script is stored in `scripts/setup-runner-kubeconfig.sh` in this repository. Copy its content to `/root/setup-runner-kubeconfig.sh` on `k8s-master-1`, then run it as root:

```bash
bash /root/setup-runner-kubeconfig.sh
```

The script creates:

```text
/home/actions-runner/.kube/config
```

The generated kubeconfig belongs to `actions-runner`, uses file mode `600`, and defaults to the `food-order` namespace.