# Kubernetes Deployment Guide

This guide deploys the Food Order application to the on-premises Kubernetes cluster. Infrastructure setup is documented in the [On-Premises Infrastructure Setup](./onprem-setup.md) guide.

## 1. Create Configuration

```bash
kubectl create namespace food-order
kubectl apply -f k8s/configmap.yaml
```

Copy `k8s/secret.example.yaml` to `k8s/secret.yaml`, add local credentials, and
apply it:

```bash
kubectl apply -f k8s/secret.yaml
```

## 2. Deploy Storage and Dependencies

Create the NFS directories on `storage-server` before applying the PersistentVolumes:

```bash
sudo mkdir -p /data/nfs-shared/food-order/mysql
sudo mkdir -p /data/nfs-shared/food-order/user-uploads
sudo mkdir -p /data/nfs-shared/food-order/restaurant-uploads
```

Deploy storage and MySQL:

```bash
kubectl apply -f k8s/storage.yaml
kubectl apply -f k8s/mysql.yaml
```

Open the MySQL client with the root password from the Kubernetes Secret:

```bash
kubectl -n food-order exec -it deployment/mysql -- mysql -u root -p
```

Create the databases and grant access to the application user. Replace `<DB_PASSWORD>` with the value of `DB_PASSWORD` in `k8s/secret.yaml`:

```sql
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS restaurant_db;
CREATE DATABASE IF NOT EXISTS order_db;

CREATE USER IF NOT EXISTS 'food_order_app'@'%' IDENTIFIED BY '<DB_PASSWORD>';
GRANT ALL PRIVILEGES ON user_db.* TO 'food_order_app'@'%';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'food_order_app'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'food_order_app'@'%';
FLUSH PRIVILEGES;
```

Deploy the remaining dependencies:

```bash
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/zookeeper.yaml
kubectl apply -f k8s/kafka.yaml
```

## 3. Deploy Application Services

```bash
kubectl apply -f k8s/eureka.yaml
kubectl apply -f k8s/notification.yaml
kubectl apply -f k8s/user.yaml
kubectl apply -f k8s/restaurant.yaml
kubectl apply -f k8s/order.yaml
kubectl apply -f k8s/api-gateway.yaml
kubectl apply -f k8s/frontend.yaml
```

## 4. Verify the Deployment

Check cluster resources:

```bash
kubectl get nodes -o wide
kubectl -n food-order get pods -o wide
kubectl -n food-order get services
kubectl -n food-order get endpoints
kubectl -n food-order get ingress
kubectl -n food-order get pvc
kubectl get pv
```

Check the public website and API from Windows:

```powershell
curl.exe -I https://food.myproject.bar/
curl.exe "https://food.myproject.bar/api/v1/restaurants?page=0&size=8"
```
