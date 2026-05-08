# Ubuntu 24 手动配置 nginx 与 systemd

以下命令假设项目位于 `/opt/daily-finance`，后端监听 `127.0.0.1:8080`，前端 Vite preview 监听 `127.0.0.1:4173`。

服务端需要提前安装 JDK 21、Maven、Node.js、npm、MySQL Server 9.7.0、nginx。`start.sh` 只检查并使用这些命令，不负责安装软件。

## 首次数据库初始化

只在首次部署时手动执行。后续 `git pull` 更新项目时不要重复执行。

```bash
cd /opt/daily-finance
mysql -u root -p < database/schema.sql
mysql -u root -p daily_finance < database/init-data.sql
```

## 应用环境变量

```bash
sudo cp /opt/daily-finance/.env.example /opt/daily-finance/.env.production
sudo nano /opt/daily-finance/.env.production
```

示例：

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
```

## 手动启动

```bash
cd /opt/daily-finance
chmod +x scripts/linux/start.sh
./scripts/linux/start.sh
```

## nginx 配置

```bash
sudo tee /etc/nginx/sites-available/daily-finance.conf >/dev/null <<'EOF'
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:4173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/daily-finance.conf /etc/nginx/sites-enabled/daily-finance.conf
sudo nginx -t
sudo systemctl reload nginx
```

## systemd 服务

```bash
sudo tee /etc/systemd/system/daily-finance.service >/dev/null <<'EOF'
[Unit]
Description=Daily Finance Management System
After=network.target mysql.service

[Service]
Type=simple
WorkingDirectory=/opt/daily-finance
ExecStart=/opt/daily-finance/scripts/linux/start.sh
Restart=always
RestartSec=5
User=www-data
EnvironmentFile=/opt/daily-finance/.env.production

[Install]
WantedBy=multi-user.target
EOF
```

启用和管理：

```bash
sudo systemctl daemon-reload
sudo systemctl enable daily-finance
sudo systemctl start daily-finance
sudo systemctl status daily-finance
sudo systemctl restart daily-finance
sudo systemctl stop daily-finance
journalctl -u daily-finance -f
```

## 更新流程

```bash
cd /opt/daily-finance
git pull
sudo systemctl restart daily-finance
```

更新流程不会自动初始化数据库，也不会覆盖 nginx 或 systemd 配置。
