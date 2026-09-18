# CompuTaQuest - Deployment Guide

## Architecture

```
Internet → Nginx (80/443) → Frontend (React:3000)
                          → Backend (Spring Boot:4000) → MongoDB
```

## Prerequisites

- Linux server (Ubuntu 22.04+) with 2GB+ RAM
- Docker & Docker Compose installed
- Domain name pointing to server IP (for SSL)
- SSH access to server

## Quick Deploy (University Server with Docker)

```bash
# 1. SSH into server
ssh user@university-server-ip

# 2. Clone repository
git clone YOUR_REPO_URL /opt/computaquest
cd /opt/computaquest

# 3. Create production environment file
cp .env.prod.example .env.prod
nano .env.prod   # Fill in real values

# 4. Deploy
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# 5. Verify
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f
```

## Free Hosting: Oracle Cloud Always Free Tier

### Step 1: Create Instance

1. Go to https://cloud.oracle.com/free
2. Sign up (requires credit card for verification, no charges)
3. Navigate: Compute → Instances → Create Instance
4. Configuration:
   - Image: Ubuntu 22.04
   - Shape: **VM.Standard.A1.Flex** (ARM)
   - OCPUs: 4, RAM: 24GB
   - Upload SSH public key
5. Note the public IP address

### Step 2: Open Firewall Ports

Oracle Cloud Console → Networking → VCN → Security Lists → Default:
- Add Ingress Rule: Source `0.0.0.0/0`, Protocol TCP, Ports 80
- Add Ingress Rule: Source `0.0.0.0/0`, Protocol TCP, Ports 443

### Step 3: Server Setup

```bash
ssh -i your-key ubuntu@PUBLIC_IP

# Install Docker
sudo apt update && sudo apt upgrade -y
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu

# Create swap (important for 1GB+ VMs)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Firewall
sudo ufw allow 22/tcp && sudo ufw allow 80/tcp && sudo ufw allow 443/tcp && sudo ufw --force enable
```

### Step 4: Deploy App

```bash
# Clone and configure
cd /opt
sudo git clone YOUR_REPO_URL computaquest
cd computaquest
sudo cp .env.prod.example .env.prod
sudo nano .env.prod   # Set real values

# Build and start
sudo docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# Check status
sudo docker compose -f docker-compose.prod.yml ps
sudo docker compose -f docker-compose.prod.yml logs -f backend
```

## SSL with Let's Encrypt

```bash
# 1. Start without SSL first (nginx listens on port 80)
# 2. Get certificate
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot --webroot-path=/var/www/certbot \
  -d your-domain.com --agree-tos -m your@email.com

# 3. Edit nginx-prod.conf: uncomment HTTPS server block

# 4. Reload nginx
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

## QR Code Generation

### Option A: Frontend Library (Recommended)

Install in React app:
```bash
npm install qrcode.react
```

Create component:
```jsx
import { QRCodeSVG } from 'qrcode.react';

function AppQRCode() {
  const appUrl = import.meta.env.VITE_APP_URL || window.location.origin;
  
  return (
    <div className="qr-container">
      <h3>Scan to open CompuTaQuest</h3>
      <QRCodeSVG 
        value={appUrl} 
        size={200}
        bgColor="#ffffff"
        fgColor="#000000"
        level="H"
        includeMargin={true}
      />
      <p>{appUrl}</p>
    </div>
  );
}
```

### Option B: Backend Endpoint

Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
```

Add controller:
```java
@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    @GetMapping
    public void generateQr(
            @RequestParam String url,
            HttpServletResponse response) throws Exception {
        
        BitMatrix matrix = new QRCodeWriter().encode(
            url, BarcodeFormat.QR_CODE, 300, 300);
        
        response.setContentType("image/png");
        MatrixToImageWriter.writeToStream(
            matrix, "PNG", response.getOutputStream());
    }
}
```

### Option C: External Service

Use https://api.qrserver.com to generate QR codes without backend code:
```
https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=https://your-domain.com
```

## Security for Child Users (COPPA Compliance)

### Backend Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // If using JWT
            .cors(cors -> cors.configurationSource(corsConfigSource()))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'"))
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(Customizer.withDefaults())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
```

### Key Security Measures

| Requirement | Implementation |
|-------------|----------------|
| **No tracking cookies** | Don't use Google Analytics or third-party trackers |
| **No personal data collection** | Only collect username, no real names/emails from children |
| **HTTPS only** | Redirect all HTTP to HTTPS |
| **CSP headers** | Prevent XSS attacks |
| **Rate limiting** | Prevent brute-force attacks |
| **Input validation** | Sanitize all user inputs |
| **Data minimization** | Collect only what's necessary |
| **No third-party data sharing** | Don't send data to external services |
| **Audit logging** | Log access patterns for security review |
| **Parental consent** | Implement if collecting ANY personal data |

### MongoDB Security

```yaml
# In docker-compose.prod.yml, MongoDB uses auth:
environment:
  MONGO_INITDB_ROOT_USERNAME: ${MONGO_USER}
  MONGO_INITDB_ROOT_PASSWORD: ${MONGO_PASSWORD}
```

Connection string in backend uses auth:
```
mongodb://${MONGO_USER}:${MONGO_PASSWORD}@mongodb:27017/computaquest?authSource=admin
```

## Production Environment Variables

| Variable | Example | Description |
|----------|---------|-------------|
| `MONGO_USER` | `computaquest_admin` | MongoDB username |
| `MONGO_PASSWORD` | `xK9#mP2$vL5nQ8` | Strong random password |
| `JWT_SECRET` | `a1b2c3...` (64 chars) | Random secret for JWT signing |
| `OPENAI_API_KEY` | `sk-...` | Your OpenAI API key |
| `FRONTEND_URL` | `https://app.university.edu` | Production URL |
| `DOMAIN` | `app.university.edu` | Server domain |

## Monitoring

```bash
# Container stats
docker stats

# Logs
docker compose -f docker-compose.prod.yml logs -f --tail=100

# MongoDB backup
docker exec computaquest-mongodb mongodump --out=/data/backup/$(date +%Y%m%d)
docker cp computaquest-mongodb:/data/backup ./backups/

# Health check
curl http://localhost:4000/actuator/health
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `502 Bad Gateway` | Backend not running: `docker compose logs backend` |
| `413 Entity Too Large` | Add `client_max_body_size 10M;` in nginx |
| MongoDB auth failed | Check `.env.prod` credentials match |
| Container exits immediately | Check logs: `docker compose logs servicename` |
| CORS error in browser | Verify `FRONTEND_URL` matches actual domain |
| SSL certificate error | Ensure domain DNS points to server, then run certbot |
