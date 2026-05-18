# Ubuntu 24 nginx and systemd setup

These commands assume:

- Project path: `/opt/LedgerFlow`
- Backend: `127.0.0.1:8080`
- Frontend static files: `/opt/LedgerFlow/frontend/dist`
- Domain: `finance.zfye.site`
- Cloudflare Origin certificate:
  - `/etc/ssl/cloudflare/origin.pem`
  - `/etc/ssl/cloudflare/origin.key`

Install JDK 21, Maven, Node.js, npm, MySQL Server 9.7.0, and nginx before running the app. `scripts/linux/start.sh` only builds changed files and starts the backend by default. It does not install software, run `git pull`, initialize the database, edit nginx, or register systemd.

## First database initialization

Run this only once on first deployment. Do not run it during normal updates.

```bash
cd /opt/LedgerFlow
mysql --default-character-set=utf8mb4 -u root -p < database/schema.sql
mysql --default-character-set=utf8mb4 -u root -p daily_finance < database/init-data.sql
```

## Production environment file

```bash
cd /opt/LedgerFlow
cp .env.example .env.production
nano .env.production
```

Example:

```env
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=daily_finance
DB_USERNAME=daily_finance
DB_PASSWORD=change_me
SERVER_PORT=8080
SERVER_ADDRESS=127.0.0.1
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=4173
FRONTEND_MODE=static
FORCE_BUILD=0
```

`FRONTEND_MODE=static` is recommended on low-memory servers. nginx serves `frontend/dist` directly, so the server does not need a long-running Vite preview process. Use `FRONTEND_MODE=preview` only when you want to keep the old preview-server behavior.

## Manual start

```bash
cd /opt/LedgerFlow
chmod +x scripts/linux/start.sh
./scripts/linux/start.sh
```

## nginx config for finance.zfye.site

```bash
sudo tee /etc/nginx/sites-available/finance.zfye.site >/dev/null <<'EOF'
server {
    listen 80;
    server_name finance.zfye.site;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name finance.zfye.site;
    root /opt/LedgerFlow/frontend/dist;
    index index.html;

    ssl_certificate     /etc/ssl/cloudflare/origin.pem;
    ssl_certificate_key /etc/ssl/cloudflare/origin.key;

    ssl_session_timeout 1d;
    ssl_protocols TLSv1.2 TLSv1.3;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF
```

Enable nginx config:

```bash
sudo ln -s /etc/nginx/sites-available/finance.zfye.site /etc/nginx/sites-enabled/finance.zfye.site
sudo nginx -t
sudo systemctl reload nginx
```

## systemd service

```bash
sudo tee /etc/systemd/system/ledgerflow.service >/dev/null <<'EOF'
[Unit]
Description=LedgerFlow Personal Finance System
After=network.target mysql.service

[Service]
Type=simple
WorkingDirectory=/opt/LedgerFlow
ExecStart=/opt/LedgerFlow/scripts/linux/start.sh
Restart=always
RestartSec=5
User=www-data
EnvironmentFile=/opt/LedgerFlow/.env.production

[Install]
WantedBy=multi-user.target
EOF
```

Manage service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable ledgerflow
sudo systemctl start ledgerflow
sudo systemctl status ledgerflow
sudo systemctl restart ledgerflow
sudo systemctl stop ledgerflow
journalctl -u ledgerflow -f
```

## Update flow

```bash
cd /opt/LedgerFlow
git pull
sudo systemctl restart ledgerflow
```

Normal updates do not initialize the database and do not overwrite nginx or systemd config.

For a forced rebuild, run:

```bash
cd /opt/LedgerFlow
FORCE_BUILD=1 ./scripts/linux/start.sh
```
