#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/home/ubuntu/jingdezhen-tourism"
REPO_URL="https://github.com/Bxc-xc/-B-S-.git"
DB_NAME="tourism"
DB_USER="tourism"

echo "[1/9] Install system dependencies"
sudo apt update
sudo apt install -y git curl nginx mysql-server openjdk-17-jdk maven openssl

if ! command -v node >/dev/null 2>&1; then
  echo "[2/9] Install Node.js 20"
  curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
  sudo apt install -y nodejs
else
  echo "[2/9] Node.js already installed: $(node -v)"
fi

echo "[3/9] Stop old application if it exists"
sudo systemctl stop tourism 2>/dev/null || true

echo "[4/9] Replace project files"
rm -rf "$APP_DIR"
git clone "$REPO_URL" "$APP_DIR"

echo "[5/9] Configure MySQL"
DB_PASS="$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-20)"
JWT_SECRET="$(openssl rand -base64 48 | tr -d '\n')"

sudo mysql -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "DROP USER IF EXISTS '$DB_USER'@'localhost';"
sudo mysql -e "CREATE USER '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';"
sudo mysql -e "GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'localhost'; FLUSH PRIVILEGES;"
sudo mysql "$DB_NAME" < "$APP_DIR/database/tourism.sql"

echo "[6/9] Build frontend"
cd "$APP_DIR/frontend"
npm ci
npm run build

echo "[7/9] Build backend"
cd "$APP_DIR"
mvn -DskipTests package

echo "[8/9] Create backend service"
sudo tee /etc/tourism.env > /dev/null <<EOF
DB_URL=jdbc:mysql://localhost:3306/$DB_NAME?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
DB_USERNAME=$DB_USER
DB_PASSWORD=$DB_PASS
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=86400000
EOF

sudo tee /etc/systemd/system/tourism.service > /dev/null <<EOF
[Unit]
Description=Jingdezhen Tourism Recommendation System
After=network.target mysql.service

[Service]
User=ubuntu
WorkingDirectory=$APP_DIR
EnvironmentFile=/etc/tourism.env
ExecStart=/usr/bin/java -jar $APP_DIR/target/tourism-recommendation-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now tourism

echo "[9/9] Configure Nginx"
sudo tee /etc/nginx/sites-available/tourism > /dev/null <<EOF
server {
    listen 80;
    server_name _;

    root $APP_DIR/frontend/dist;
    index index.html;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8888/api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8888/uploads/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:8888/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/tourism /etc/nginx/sites-enabled/tourism
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx

echo
echo "Deployment finished."
echo "Open: http://124.223.19.243"
echo "Service status:"
sudo systemctl status tourism --no-pager
