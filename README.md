# 景德镇旅游智能推荐系统

基于 B/S 架构的景德镇旅游智能推荐系统，面向游客、商家和管理员三类角色，提供旅游资源浏览、个性化推荐、智能行程规划、酒店预订、团购下单、评论互动、收藏管理和后台运营管理等功能。

## 项目亮点

- 垂直聚焦景德镇陶瓷文化旅游，覆盖景点、陶瓷工坊、陶瓷市集、美食、酒店和旅游路线。
- 基于收藏、评分和标签画像进行可解释推荐，支持冷启动场景下的热门/评分排序兜底。
- 智能行程生成支持天数、偏好、预算筛选，并结合经纬度使用最近邻策略优化游览顺序。
- 团购下单使用事务和数据库原子库存扣减，避免并发场景下的超卖问题。
- 支持游客端、商家端、管理员端三端分权，以及 JWT 无状态认证。
- 使用 WebSocket 推送订单、审核、评论等实时数据更新。
- 提供操作日志 AOP、OpenAPI 文档、单元测试和前端端到端测试脚本。

## 技术栈

后端：

- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8
- WebSocket
- Springdoc OpenAPI

前端：

- Vue 3
- Vue Router
- Pinia
- Element Plus
- Axios
- ECharts
- Vue I18n
- Vite

## 目录结构

```text
src/main/java/com/jdz/tourism   后端源码
src/main/resources              后端配置
frontend                        Vue 前端项目
database                        数据库脚本
docs                            论文、技术文档和答辩材料
tests                           接口和端到端测试脚本
tools                           数据采集等辅助工具
```

## 本地启动

1. 准备 MySQL 数据库，创建 `tourism` 数据库，并按需导入 `database/tourism.sql`。
2. 设置本地环境变量，避免把数据库密码和密钥写入代码：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="至少32位的JWT签名密钥"
```

3. 启动后端：

```powershell
mvn spring-boot:run
```

4. 启动前端：

```powershell
cd frontend
npm install
npm run dev
```

默认后端端口为 `8888`，前端开发服务由 Vite 提供。

## GitHub 上传建议

仓库已通过 `.gitignore` 排除 `node_modules/`、`target/`、`frontend/dist/`、`.env` 和运行时上传文件 `uploads/`。如果需要上传演示图片，建议单独挑选少量示例图片放到 `docs/` 或 `frontend/public/` 下，不要直接提交整个 `uploads/` 目录。
