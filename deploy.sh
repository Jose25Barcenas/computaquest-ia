#!/bin/bash
# deploy.sh - One-shot deployment script for Ubuntu 22.04/24.04
# Run as root or with sudo on a fresh server
set -e

echo "=== CompuTaQuest Production Deployment ==="

# 1. System updates and Docker installation
echo "[1/7] Installing Docker..."
apt update && apt upgrade -y
apt install -y curl git ufw
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
usermod -aG docker $SUDO_USER 2>/dev/null || true

# 2. Firewall
echo "[2/7] Configuring firewall..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# 3. Clone repo
echo "[3/7] Cloning repository..."
cd /opt
if [ ! -d "computaquest" ]; then
  git clone YOUR_REPO_URL computaquest
fi
cd computaquest

# 4. Create production .env
echo "[4/7] Setting up environment..."
if [ ! -f ".env.prod" ]; then
  cp .env.prod.example .env.prod
  echo ""
  echo ">>> EDIT /opt/computaquest/.env.prod with real values <<<"
  echo ">>> Press Enter when done <<<"
  read -r
fi

# 5. Build containers
echo "[5/7] Building Docker images..."
export $(cat .env.prod | xargs)
docker compose -f docker-compose.prod.yml --env-file .env.prod build

# 6. Initial start (HTTP only for certbot)
echo "[6/7] Starting services..."
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d

# 7. SSL setup
echo "[7/7] Setting up SSL..."
DOMAIN=$(grep DOMAIN .env.prod | cut -d= -f2)
if [ "$DOMAIN" != "your-domain.com" ] && [ -n "$DOMAIN" ]; then
  docker compose -f docker-compose.prod.yml run --rm certbot certonly \
    --webroot --webroot-path=/var/www/certbot \
    -d "$DOMAIN" --agree-tos -m "your-email@$DOMAIN" --no-eff-email
  
  echo "SSL certificate obtained. Uncomment HTTPS block in nginx-prod.conf and reload."
  docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
fi

echo ""
echo "=== Deployment Complete ==="
echo "App running at: http://${DOMAIN:-localhost}"
echo "Logs: docker compose -f docker-compose.prod.yml logs -f"
