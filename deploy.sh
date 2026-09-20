#!/bin/bash
# deploy.sh - One-shot deployment script for Ubuntu 22.04/24.04
# Run as root or with sudo on a fresh server
set -e

echo "=== CompuTaQuest Production Deployment ==="

# Validate REPO_URL is set
if grep -q "YOUR_REPO_URL" deploy.sh; then
  echo "ERROR: Reemplaza YOUR_REPO_URL en deploy.sh con la URL real del repositorio"
  exit 1
fi

# 1. System updates and Docker installation
echo "[1/6] Installing Docker..."
apt update && apt upgrade -y
apt install -y curl git ufw
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
usermod -aG docker $SUDO_USER 2>/dev/null || true

# 2. Firewall
echo "[2/6] Configuring firewall..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# 3. Clone repo
echo "[3/6] Cloning repository..."
cd /opt
if [ ! -d "computaquest" ]; then
  git clone YOUR_REPO_URL computaquest
fi
cd computaquest

# 4. Create production .env
echo "[4/6] Setting up environment..."
if [ ! -f ".env.prod" ]; then
  cp .env.prod.example .env.prod
  echo ""
  echo ">>> EDITA /opt/computaquest/.env.prod con los valores reales <<<"
  echo ">>> Presiona Enter cuando termines <<<"
  read -r
fi

# 5. Build and start containers
echo "[5/6] Building and starting services..."
export $(cat .env.prod | grep -v '^#' | xargs)
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

echo ""
echo "=== Deployment Complete ==="
echo "Logs: docker compose -f docker-compose.prod.yml logs -f"
