# 日常记账理财管理系统

课程设计项目：基于 Vue 3、Spring Boot、MyBatis 和 MySQL Server 9.7.0 的个人记账理财管理系统。

## 技术栈

- 前端：Vue 3 + Vite + Element Plus + ECharts
- 后端：Java 21 + Spring Boot + MyBatis + Maven
- 数据库：MySQL Server 9.7.0
- 架构：前后端分离，前端统一调用 `/api/**`

## 目录结构

```text
backend/                 Spring Boot 后端
frontend/                Vue 3 前端
database/                数据库 SQL
scripts/windows/         Windows 测试脚本
scripts/linux/           Ubuntu 24 启动脚本
docs/                    nginx 与 systemd 手动配置说明
```

## 本地开发

1. 安装 JDK 21、Maven、Node.js、MySQL Server 9.7.0。
2. 创建并初始化数据库：

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p daily_finance < database/init-data.sql
```

3. Windows 一键启动：

```bat
scripts\windows\start.cmd
```

或：

```powershell
.\scripts\windows\start.ps1
```

4. 访问：

- 前端：http://localhost:5173
- 后端：http://localhost:8080

默认演示账号：

- 用户名：`demo`
- 密码：`123456`

## Ubuntu 24 运行

`scripts/linux/start.sh` 默认只在源码变化时重新编译，并启动后端服务；生产环境推荐由 nginx 直接托管 `frontend/dist`，减少低内存服务器上的常驻 Node 进程。脚本不执行 `git pull`、不初始化数据库、不修改 nginx、不注册 systemd。

```bash
cd /opt/daily-finance
git pull
./scripts/linux/start.sh
```

nginx 和 systemd 手动命令见 [nginx-systemd.md](nginx-systemd.md)。
