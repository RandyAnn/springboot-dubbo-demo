# 食刻印象--饮食记录系统

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Apache Dubbo](https://img.shields.io/badge/Apache%20Dubbo-3.1.0-blue.svg)](https://dubbo.apache.org/)
[![Vue.js](https://img.shields.io/badge/Vue.js-2.6.14-green.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 项目简介

这是一个基于Spring Boot和Apache Dubbo的微服务架构饮食记录系统，支持多端应用（Web管理后台、微信小程序）。系统采用现代化的微服务设计模式，实现了用户管理、食物数据管理、饮食记录、营养分析等核心功能。

### 🎯 主要特性

- **微服务架构**：基于Spring Boot + Dubbo的分布式服务架构
- **多端支持**：Web管理后台 + 微信小程序
- **统一网关**：API Gateway统一入口，支持JWT认证和路由转发
- **事件驱动**：支持Redis Pub/Sub和Kafka的事件系统
- **缓存优化**：多层缓存策略，支持本地缓存和Redis分布式缓存
- **配置管理**：统一的配置属性管理，支持环境区分

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   微信小程序     │    │   Vue.js 前端   │    │   管理后台      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   API Gateway   │
                    │    (8084)       │
                    └─────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Auth Service  │    │  User Service   │    │  Food Service   │
│     (8085)      │    │     (8086)      │    │     (8087)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Diet Service   │    │Nutrition Service│    │  File Service   │
│     (8088)      │    │     (9096)      │    │     (8089)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 2.7.x, Apache Dubbo 3.1.0
- **安全**: Spring Security, JWT
- **数据库**: MySQL 8.0, MyBatis Plus
- **缓存**: Redis 6.x, Caffeine
- **消息队列**: Apache Kafka (可选)
- **服务发现**: Apache Zookeeper
- **网关**: Spring Cloud Gateway

### 前端技术
- **Web端**: Vue.js 2.6.14, Element UI, Vuex, Vue Router
- **小程序**: uni-app, 微信小程序
- **工具**: Axios, ECharts, Sass

### 开发工具
- **构建**: Maven 3.6+
- **JDK**: Java 17+
- **IDE**: IntelliJ IDEA, HBuilderX

## 📦 服务模块

| 服务名称 | 端口 | 描述 |
|---------|------|------|
| api-gateway | 8084 | API网关服务，统一入口和认证 |
| auth-service | 8085 | 认证服务，用户登录和JWT管理 |
| user-service | 8086 | 用户管理服务 |
| food-service | 8087 | 食物数据服务 |
| diet-service | 8088 | 饮食记录服务 |
| nutrition-service | 9096 | 营养分析服务 |
| file-service | 8089 | 文件管理服务 |
| dashboard-service | 8091 | 仪表盘服务 |

### 共享模块
- **shared-kernel**: 共享内核，包含通用配置、事件系统、缓存系统
- **xxx-api-contracts**: 各服务的API契约模块

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.x+
- Zookeeper 3.7+
- Node.js 16+ (前端开发)

### 1. 克隆项目

```bash
git clone <repository-url>
cd spring-boot-dubbo-demo
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE dubbo_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据
mysql -u root -p dubbo_demo < dubbo_demo.sql
```

### 3. 启动基础服务

```bash
# 启动 Zookeeper
zkServer.sh start

# 启动 Redis
redis-server

# 启动 MySQL
systemctl start mysql
```

### 4. 配置文件

复制并修改配置文件：
```bash
cp application-example.yml application.yml
# 根据实际环境修改数据库、Redis等配置
```

### 5. 启动后端服务

```bash
# 按顺序启动各个服务
mvn clean install

# 1. 认证服务
cd auth-service && mvn spring-boot:run &

# 2. 用户服务
cd user-service && mvn spring-boot:run &

# 3. 食物服务
cd food-service && mvn spring-boot:run &

# 4. 饮食服务
cd diet-service && mvn spring-boot:run &

# 5. 营养服务
cd nutrition-service && mvn spring-boot:run &

# 6. 文件服务
cd file-service && mvn spring-boot:run &

# 7. 仪表盘服务
cd dashboard-service && mvn spring-boot:run &

# 8. API网关
cd api-gateway && mvn spring-boot:run &
```

### 6. 启动前端

```bash
# Web管理后台
cd src
npm install
npm run serve

# 微信小程序 (使用HBuilderX打开项目根目录)
# 在HBuilderX中运行到微信开发者工具
```

## 📱 应用访问

- **Web管理后台**: http://localhost:8080
- **API网关**: http://localhost:8084
- **微信小程序**: 通过微信开发者工具预览

## 🔧 开发指南

### API文档

主要API端点：

- **用户认证**: `POST /api/auth/user/login`
- **管理员认证**: `POST /api/auth/admin/login`
- **食物查询**: `GET /api/food/list`
- **饮食记录**: `POST /api/diet-records`
- **营养分析**: `GET /api/nutrition/daily`
- **文件上传**: `POST /api/files/upload`
- **仪表盘**: `GET /api/admin/dashboard/stats`

### 常见问题

1. **服务启动失败**
   ```bash
   # 检查端口占用
   netstat -tulpn | grep :8084

   # 检查Zookeeper连接
   zkCli.sh -server localhost:2181
   ```

2. **数据库连接失败**
   ```bash
   # 检查MySQL服务状态
   systemctl status mysql

   # 测试数据库连接
   mysql -h localhost -u root -p dubbo_demo
   ```

3. **Redis连接问题**
   ```bash
   # 检查Redis状态
   redis-cli ping

   # 查看Redis配置
   redis-cli config get "*"
   ```

4. **前端跨域问题**
   - 检查API Gateway的CORS配置
   - 确认前端请求地址正确

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

- Spring Boot 社区
- Apache Dubbo 社区
- Vue.js 社区

---

如有问题或建议，请提交 [Issue](https://github.com/your-username/spring-boot-dubbo-demo/issues)
