#!/bin/bash
# deploy.sh - One-shot deployment script for Ubuntu 22.04/24.04
# Run as root or with sudo on a fresh server
set -euo pipefail

echo "=== CompuTaQuest Production Deployment ==="

if [ "$(id -u)" -ne 0 ]; then
  echo "ERROR: Este script debe ejecutarse como root (sudo)"
  exit 1
fi

if grep -q "YOUR_REPO_URL" deploy.sh; then
  echo "ERROR: Reemplaza YOUR_REPO_URL en deploy.sh con la URL real del repositorio"
  exit 1
fi

if grep -q "YOUR_DOMAIN" nginx-prod.conf; then
  echo "WARNING: Recuerda reemplazar YOUR_DOMAIN en nginx-prod.conf"
fi

echo "[1/7] Installing Docker..."
apt update && apt upgrade -y
apt install -y curl git ufw
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
usermod -aG docker "$SUDO_USER" 2>/dev/null || true

echo "[2/7] Configuring firewall..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

echo "[3/7] Cloning repository..."
cd /opt
if [ ! -d "computaquest" ]; then
  git clone "$REPO_URL" computaquest
fi
cd computaquest
git pull origin main

echo "[4/7] Setting up environment..."
if [ ! -f ".env.prod" ]; then
  cp .env.prod.example .env.prod
  echo ""
  echo ">>> EDITA /opt/computaquest/.env.prod con los valores reales <<<"
  echo ">>> Presiona Enter cuando termines <<<"
  read -r
fi

echo "[5/7] Installing Certbot for SSL..."
apt install -y certbot
certbot certonly --webroot -w /var/www/certbot -d "$DOMAIN" --non-interactive --agree-tos --email "$ADMIN_EMAIL" || true

echo "[6/7] Building and starting services..."
export "$(grep -v '^#' .env.prod | xargs)"
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

echo "[7/7] Setting up SSL auto-renewal..."
(crontab -l 2>/dev/null; echo "0 3 * * * certbot renew --quiet && docker compose -f /opt/computaquest/docker-compose.prod.yml exec -T nginx nginx -s reload") | crontab -

echo ""
echo "=== Deployment Complete ==="
echo "Logs: docker compose -f docker-compose.prod.yml logs -f"
