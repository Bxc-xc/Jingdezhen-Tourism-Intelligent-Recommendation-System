# 基于B/S模式的景德镇旅游智能推荐系统 — 技术文档

=========================================================================
第一章  系统概述
=========================================================================

本系统是一套面向景德镇旅游场景的智能推荐平台，采用前后端分离的B/S架构。后端使用 Java 17 编写，基于 Spring Boot 3.2.0 框架提供 RESTful 风格的接口服务；前端使用 Vue 3 框架构建单页面应用（SPA），配合 Element Plus 组件库完成页面渲染；数据库采用 MySQL 8.0 进行数据持久化存储，使用 Spring Data JPA / Hibernate 作为 ORM 框架。

系统围绕景德镇旅游这一核心场景，实现了从景点浏览、智能推荐、行程规划、酒店预订、团购下单、评论互动到收藏管理的完整业务闭环。系统区分三种用户角色——游客、商家和管理员，不同角色拥有不同的功能权限和界面视图。

=========================================================================
第二章  技术选型与依赖
=========================================================================

---------------------------------------------------------------------------
2.1  后端技术栈
---------------------------------------------------------------------------

后端基于 Spring Boot 生态构建，主要依赖如下：

（1）Spring Boot 3.2.0：Web 框架核心，提供自动配置、内嵌 Tomcat、RESTful 接口支持。所有接口通过 @RestController 注解定义，统一返回 JSON 格式数据。

（2）Spring Data JPA + Hibernate：ORM 框架，将 Java 实体类映射为数据库表，开发者无需手写 SQL 即可完成大部分数据操作。配置 ddl-auto: update，启动时自动同步表结构。

（3）Spring Security + JWT（jjwt 0.12.3）：安全认证框架。用户登录后由后端签发 JWT Token，前端每次请求在 Authorization 请求头中携带 Token，后端通过 JwtAuthenticationFilter 过滤器验证身份。会话采用无状态（STATELESS）模式，不依赖服务端 Session。

（4）Spring AOP：切面编程，配合自定义 @LogOperation 注解，对关键业务操作自动记录操作日志，无需在业务代码中手动埋点。

（5）Spring WebSocket + Spring Messaging：实现服务端向客户端的实时数据推送，用于管理员仪表板的实时数据同步。

（6）MySQL Connector 8.0.33：MySQL 数据库驱动。

（7）Springdoc OpenAPI：自动生成 API 文档，方便前后端联调。

（8）Spring Boot Validation：基于注解的参数校验（@NotBlank、@Min、@Max 等），在 Controller 层统一拦截非法入参。

---------------------------------------------------------------------------
2.2  前端技术栈
---------------------------------------------------------------------------

（1）Vue 3.4.0：渐进式 JavaScript 框架，用于构建用户界面。系统使用组合式 API（Composition API）编写页面组件，所有页面均为 .vue 单文件组件。

（2）Vue Router 4.2.5：官方路由管理器，负责 URL 与页面组件之间的映射。系统使用 History 模式，路由按角色分为游客端、商家端、管理员端三组。

（3）Pinia 2.1.7：Vue 3 官方推荐的状态管理库，替代 Vuex。系统用它存储登录 Token、当前用户信息，并支持持久化到 localStorage 或 sessionStorage。

（4）Element Plus 2.4.4：基于 Vue 3 的 UI 组件库，提供表格、表单、弹窗、菜单、日期选择器等丰富组件。系统中几乎所有界面元素均使用该组件库。

（5）Axios 1.6.2：基于 Promise 的 HTTP 客户端，用于向后端发送请求。系统对其进行了二次封装，添加了请求拦截器（自动注入 JWT Token）和响应拦截器（统一错误处理、401 自动跳转登录页）。

（6）ECharts 5.4.3 + vue-echarts 6.6.1：数据可视化图表库，用于管理员仪表板的统计图表展示。

（7）Vue i18n 9.14.4：国际化插件，支持中英文界面切换。

（8）Vite 5.0.8：前端构建工具，提供极速的开发服务器和生产构建能力。

（9）unplugin-auto-import + unplugin-vue-components：自动导入插件，无需手动 import Element Plus 组件和 Vue 组合式 API，减少样板代码。

=========================================================================
第三章  系统架构
=========================================================================

---------------------------------------------------------------------------
3.1  整体架构说明
---------------------------------------------------------------------------

系统采用经典的 B/S 三层架构：

第一层——表现层（浏览器端）：由 Vue 单页面应用承担。用户通过浏览器访问前端页面，所有界面渲染、用户交互、表单验证、路由导航均在浏览器中完成。页面需要数据时，通过 Axios 向后端发送 HTTP 请求。

第二层——业务逻辑层（Spring Boot 后端）：接收前端的 HTTP 请求，经过 Spring Security 过滤链（JWT 验证、权限校验）后，进入 Controller → Service 的调用链，执行相应业务逻辑（如推荐排序、智能行程生成、库存扣减等），通过 JPA Repository 操作数据库，最终将处理结果以 JSON 格式返回给前端。

第三层——数据层（MySQL 数据库）：负责所有数据的持久化存储，包括用户信息、景点数据、商家信息、订单记录、评论数据等。Spring Data JPA 将 Java 对象映射为数据库表记录，开发者通过 Repository 接口即可完成大部分查询，无需手写 SQL。

---------------------------------------------------------------------------
3.2  前后端交互方式
---------------------------------------------------------------------------

前端和后端之间通过 HTTP 协议通信，数据格式约定如下：

请求方面：GET 请求的参数通过 URL 查询字符串传递。POST/PUT 请求的参数通过请求体传递，格式为 application/json。所有需要鉴权的请求都会在 Authorization 请求头中携带 Bearer Token。

响应方面：后端统一返回 JSON 格式响应。认证相关接口返回 { success, message, data, token } 结构；业务接口直接返回数据对象或列表；异常情况返回 HTTP 状态码（401 未认证、403 无权限、400 参数错误、500 服务器错误）。

Token 机制：用户登录成功后，后端使用 jjwt 库生成包含用户 ID 和角色信息的 JWT Token，有效期在配置中设定。前端将 Token 存储在 localStorage 或 sessionStorage（取决于"记住我"选项），后续每次请求由 Axios 请求拦截器自动附加到请求头。

---------------------------------------------------------------------------
3.3  目录结构说明
---------------------------------------------------------------------------

项目根目录结构如下：

pom.xml：Maven 项目依赖配置文件，声明了 Spring Boot、Spring Security、JPA、JWT 等所有后端依赖。

src/main/java/com/jdz/tourism/：后端 Java 源码根目录。
  - controller/：23 个 Controller 类，负责接收 HTTP 请求并调用 Service。
  - service/：25 个 Service 类，封装核心业务逻辑。
  - entity/：24 个 JPA 实体类，对应数据库表结构。
  - repository/：JPA Repository 接口，提供数据库 CRUD 操作。
  - config/：配置类，包括 SecurityConfig（安全配置）、JwtConfig（JWT 配置）、WebSocketConfig（WebSocket 配置）等。
  - utils/：工具类，包括 JwtUtil（Token 生成与验证）等。
  - annotation/：自定义注解，如 @LogOperation（操作日志标记）。

src/main/resources/application.yml：Spring Boot 全局配置文件，定义了数据库连接、服务端口、文件上传限制、地图 API 等配置。

database/：数据库 SQL 脚本目录，包含建表语句和初始数据。

frontend/：Vue 前端项目根目录。
  - src/views/：页面组件，按角色分为 tourist/、merchant/、admin/ 三个子目录。
  - src/components/：公共组件，按业务分为 hotel/、plan/、scenic/、recommend/ 等子目录。
  - src/api/：22 个 API 封装模块，将后端接口封装为 JavaScript 函数。
  - src/stores/：Pinia 状态管理，包含 user.js（用户状态）等。
  - src/router/：路由配置，按角色分组定义路由规则。
  - src/utils/：工具函数，包括 request.js（Axios 封装）、i18n 配置等。

=========================================================================
第四章  后端架构详解
=========================================================================

---------------------------------------------------------------------------
4.1  Spring Boot 配置
---------------------------------------------------------------------------

核心配置集中在 src/main/resources/application.yml 文件中：

服务器配置：服务端口为 8888，响应编码强制使用 UTF-8。

数据库配置：使用 MySQL 8.0，数据库名为 tourism，连接地址为本机 localhost:3306。JPA 配置 ddl-auto: update，启动时自动根据实体类同步表结构，无需手动执行建表 SQL（初次部署除外）。

文件上传配置：单文件最大 5MB，单次请求最大 10MB，上传文件存储在项目根目录的 uploads/ 文件夹下。

地图 API 配置：支持高德地图和百度地图，通过 map.api.enabled 开关控制是否启用真实地图 API，关闭时使用降级方案（返回模拟路线数据）。

仪表板配置：dashboard.use-mock 控制管理员仪表板是否使用模拟数据，开发阶段设为 true 返回高质量演示数据，生产环境设为 false 聚合真实业务数据。

---------------------------------------------------------------------------
4.2  数据模型层详解
---------------------------------------------------------------------------

系统共定义 24 个 JPA 实体类，下面按业务分组说明。

【用户体系】

User（用户表）：核心用户表，存储所有用户共有的信息。字段包括自增主键 id、用户名 username（唯一索引）、BCrypt 加密密码 password（序列化时自动隐藏）、角色枚举 role（TOURIST/MERCHANT/ADMIN）、手机号 phone、邮箱 email、个人简介 bio、头像 avatar、注册时间 createdAt。角色字段决定了用户可访问的功能范围和界面视图。

【景点与商家】

ScenicSpot（景点表）：存储景德镇周边景点的基础数据。字段包括名称、简介、票价、开放时间、分类 category、文化标签 tags（逗号分隔，如"陶瓷,古窑,非遗工艺"）、访问量 visitCount（用于热度排序）、经纬度、封面图 image、多图集合 scenicImages（逗号分隔）、综合评分 rating、地址等。tags 字段是推荐算法的核心输入，用于构建用户兴趣画像和计算景点与用户偏好的匹配程度。

Merchant（商家表）：存储平台内各类店铺的基础信息。category 字段区分商家类型：FOOD（美食）、CERAMIC（陶瓷体验）、HOTEL（酒店）、SCENIC（景点）、OTHER（其他）。商家与 User 表通过一对一关系关联，商家在游客端的展示状态取决于管理员的审核结果。

【订单与预订】

Order（订单表）：记录用户发起的预订操作，关联用户和商家，包含状态、时间和金额等信息。

Reservation（预订表）：酒店预订的详细记录，包含入住日期、退房日期、联系人信息和预订状态。

RoomType（房型表）：酒店的房型信息，包含床型、面积、含早情况、价格和库存。

GroupBuy（团购表）：商家发布的团购套餐，包含团购价、原价、库存、有效期和使用说明。状态分为待审核、已上架、已下架。

GroupBuyOrder（团购订单表）：用户下单的团购记录，关联用户、商家和团购套餐，包含购买数量、使用日期和联系人信息。

【评论与互动】

Review（评价表）：用户对景点、商家或市集的评价，包含评分（1-5分）、文字内容、图片（逗号分隔 URL）、商家回复内容和回复时间。支持软删除（deleted 字段），删除后不影响统计数据。

Reply（回复表）：评价的嵌套回复，支持树形结构。通过 parentReply 外键实现多级嵌套，通过 isMerchantReply 字段区分商家回复和普通用户回复，商家回复在前端以特殊样式展示。

【行程规划】

TravelPlan（行程规划表）：用户生成或手动编排的行程方案。包含行程天数、起止日期、预算类型（low/medium/high）、交通方式（car/bus/train/plane）、兴趣标签（JSON 格式）和详细行程内容（planDetails，JSON 格式，按天存储每日地点安排）。

TravelPlanVersion（行程版本表）：行程的历史版本记录，支持版本回溯。

【其他】

Activity / MerchantActivity（活动表）：商家发布的限时活动信息。

Favorite / MarketplaceFavorite / MerchantFavorite（收藏表）：分别记录用户对景点、市集和商家的收藏关系，通过唯一约束防止重复收藏。

MerchantApplication（商家申请表）：商家入驻时提交的资质材料，包含营业执照、身份证等，等待管理员审核。

OperationLog（操作日志表）：系统操作记录，由 AOP 切面自动写入，记录操作用户、操作类型、操作时间等信息。

SystemConfig（系统配置表）：系统公告、参数等可配置项。

---------------------------------------------------------------------------
4.3  安全认证体系
---------------------------------------------------------------------------

认证方案：系统使用 Spring Security + JWT 实现无状态认证。

登录流程：用户提交用户名和密码，AuthController 调用 UserService 在 user 表中查找用户，使用 BCryptPasswordEncoder 的 matches() 方法验证密码。验证通过后，JwtUtil 使用 jjwt 库生成包含用户 ID 和角色信息的 JWT Token，返回给前端。

请求认证：JwtAuthenticationFilter 过滤器在每次请求到达 Controller 之前执行，从 Authorization 请求头中提取 Bearer Token，调用 JwtUtil 验证签名和有效期，解析出用户信息并注入到 Spring Security 的 SecurityContext 中，供后续业务代码使用。

权限控制：SecurityConfig 中通过 authorizeHttpRequests 配置了接口的访问权限。公开接口（景点浏览、美食查询、评论查看等）无需登录即可访问；需要登录的接口（收藏、下单、发表评论等）要求携带有效 Token；商家端和管理员端接口还要求对应的角色权限。

密码安全：所有密码使用 BCrypt 算法加密存储，BCrypt 自带随机盐值，相同密码每次加密结果不同，有效防止彩虹表攻击。

CORS 配置：允许所有来源的跨域请求，支持所有 HTTP 方法，预检请求缓存 3600 秒。开发阶段方便前后端分离调试，生产环境建议限制为前端域名。

---------------------------------------------------------------------------
4.4  Controller 层说明
---------------------------------------------------------------------------

系统共有 23 个 Controller 类，每个类负责一组业务接口，均以 /api/ 为前缀。以下说明主要模块：

AuthController（/api/auth）：处理用户注册、登录、忘记密码、Token 验证等认证相关接口。注册时支持同时传入商家扩展信息（店铺名称、分类等），一次请求完成用户和商家记录的创建。

ScenicSpotController（/api/scenic）：景点的增删改查、推荐排序、关键词搜索、分类筛选、访问量统计。推荐接口接收可选的 userId 参数，Service 层根据用户是否登录选择不同的推荐策略。

TravelPlanController（/api/plan）：行程规划的创建、查询、更新、删除，以及智能行程生成接口。智能生成接口接收天数、偏好类型和预算参数，调用 SmartTripService 执行候选筛选、路线优化和按天分配逻辑。

MerchantController（/api/merchant）：商家信息的增删改查、分类查询、资质审核状态查询。商家端的店铺管理、房型管理、订单管理等接口也在此 Controller 下。

ReviewController（/api/reviews）：评价的提交、查询、删除，以及商家回复接口。

ReplyController（/api/replies）：评价回复的创建、查询（树形结构）、删除。支持游客回复评论、游客回复回复、商家回复评论、商家回复回复四种场景。

GroupBuyController（/api/group-buy）：团购套餐的发布、审核、下架，以及用户下单接口。下单接口在数据库层通过带条件的原子 UPDATE 操作防止超卖。

AdminDashboardController（/api/admin）：管理员仪表板的统计数据接口，包括用户数量、订单数量、商家数量、收入趋势等。

---------------------------------------------------------------------------
4.5  Service 层核心逻辑
---------------------------------------------------------------------------

ScenicSpotService（推荐逻辑）：推荐接口根据用户是否登录采用不同策略。对于已登录用户，系统整合用户的收藏记录和评分历史构建标签兴趣画像：收藏某景点时该景点所有标签权重 +1，对景点评分 ≥4 分时标签权重 +2，评分 <4 分时不调整（避免将负面体验误判为兴趣）。构建画像后，使用加权 Jaccard 相似度计算用户画像与各景点标签集合的匹配程度，匹配度越高排名越靠前。对于未登录用户，直接按访问量从高到低排序展示热门景点，保证页面在任何情况下都有内容可展示。

SmartTripService（智能行程生成）：行程生成分四步执行。第一步，按用户选择的偏好类型从数据库获取对应类别的景点和商家数据，陶瓷工坊（CERAMIC_DIY）和陶瓷市集（MARKETPLACE）不论偏好如何都强制纳入候选集，保证每份行程都包含陶瓷文化体验节点。第二步，按预算档次过滤：低预算下票价超过 80 元的景点被排除，中高预算不限制。第三步，对有地理坐标的景点使用贪心最近邻算法优化游览顺序，以景德镇市中心为起点，依次选择距当前位置最近的景点，使路线更紧凑。第四步，按天分配：单日最多安排 5 个地点，多日行程每天最多 4 个，每天末尾自动添加一家酒店作为住宿推荐。

GroupBuyService（防超卖）：团购下单时，库存扣减通过数据库层的原子 UPDATE 操作实现：UPDATE group_buy SET stock = stock - quantity WHERE id = ? AND stock >= quantity。只有当前库存大于等于购买数量时才执行扣减，并发场景下库存已被抢占时本次扣减失败，Service 层对整个下单事务进行回滚，确保订单记录和库存扣减始终一致。

UserService（用户管理）：注册时使用 BCryptPasswordEncoder 对密码加密存储。支持商家注册时同步创建 Merchant 记录。删除用户时级联处理关联的订单、收藏、行程等数据。

RealtimeDataService（实时推送）：通过 Spring WebSocket 向管理员仪表板推送实时数据更新，当用户注册、订单创建、商家申请等事件发生时，触发 WebSocket 消息推送，管理员无需刷新页面即可看到最新数据。

=========================================================================
第五章  前端架构详解
=========================================================================

---------------------------------------------------------------------------
5.1  应用启动与初始化
---------------------------------------------------------------------------

前端应用的启动入口是 frontend/src/main.js。当用户在浏览器中打开系统页面时，Vue 框架执行以下初始化步骤：

第一步，创建 Vue 3 应用实例并注册三个核心插件：Vue Router（路由管理）、Pinia（状态管理）、Element Plus（UI 组件库）。同时注册 Element Plus 的图标组件库。

第二步，配置 Vue i18n 国际化插件，加载中英文语言包，支持运行时切换界面语言。

第三步，注册全局路由守卫。守卫在每次路由跳转前执行：如果目标路由标记了 requiresAuth: true，则检查 Pinia 中是否存在有效 Token；如果 Token 不存在，跳转到登录页并携带 redirect 参数，登录成功后自动跳回原页面；如果 Token 存在但用户信息未加载，调用 initUser() 从后端重新获取用户信息。

---------------------------------------------------------------------------
5.2  路由系统
---------------------------------------------------------------------------

路由配置文件 frontend/src/router/index.js 按角色将路由分为四组：

游客端路由（无需登录即可访问大部分页面）：
- /：首页，展示 Banner 轮播、快捷导航、热门景点、推荐内容
- /recommend：智能推荐页，支持景点/美食/酒店/路线/陶瓷工坊五种类型切换
- /scenic/:id：景点详情页
- /hotel/:id：酒店详情页
- /food/:id：美食详情页
- /route/:id：路线详情页
- /marketplace/:id：市集详情页
- /ceramic-workshop/:id：陶瓷工坊详情页
- /ceramic-experience：陶瓷体验专题页
- /plan：行程规划列表页（需登录）
- /plan/:id：行程详情页
- /profile：个人中心（需登录）

商家端路由（需登录且角色为 MERCHANT）：
- /merchant：商家中心首页
- /merchant/shop：店铺信息管理
- /merchant/rooms：房型管理（酒店类商家）
- /merchant/room-calendar：房态日历管理
- /merchant/orders：订单管理
- /merchant/reviews：评价管理与回复
- /merchant/qualification：资质申请
- /merchant/statistics：数据统计
- /merchant/activities：活动管理
- /merchant/group-buy：团购管理

管理员端路由（需登录且角色为 ADMIN）：
- /admin：管理后台仪表板
- /admin/audit：商家资质审核
- /admin/statistics：统计分析
- /admin/system：系统管理（景点/用户/商家数据维护）
- /admin/review：评价管理
- /admin/room-types：房型管理
- /admin/logs：操作日志
- /admin/group-buy：团购审核管理
- /admin/activities：活动管理

认证页面：
- /login：登录页
- /register：注册页
- /forgot-password：忘记密码页

---------------------------------------------------------------------------
5.3  全局状态管理
---------------------------------------------------------------------------

Pinia Store（frontend/src/stores/user.js）维护以下全局状态：

token：当前用户的 JWT 登录令牌。登录成功时写入，退出登录时清除。根据"记住我"选项决定存储在 localStorage（持久）还是 sessionStorage（会话级）。

user：当前登录用户的详细信息（用户名、角色、头像等）。由 initUser() 函数在登录后或页面刷新时从后端重新获取。

isLoggedIn：计算属性，当 token 和 user 均存在时为 true。

userRole：计算属性，返回当前用户角色（TOURIST/MERCHANT/ADMIN），用于路由守卫和页面权限判断。

---------------------------------------------------------------------------
5.4  HTTP 通信层
---------------------------------------------------------------------------

frontend/src/utils/request.js 对 Axios 进行了二次封装：

基础配置：baseURL 设置为后端地址（开发环境通过 Vite 代理转发，生产环境直接指向后端域名），超时时间 15 秒。

请求拦截器：在每个请求发出前，从 Pinia Store 中取出当前 Token，自动注入到 Authorization 请求头（格式：Bearer {token}），各页面调用 API 时无需手动处理 Token。

响应拦截器：收到后端响应后统一处理。如果 HTTP 状态码为 401（未认证），自动清除本地 Token 和用户信息并跳转到登录页；如果为 403（无权限），弹出权限不足提示；如果发生网络异常（后端未启动、请求超时等），弹出"系统异常"提示。

---------------------------------------------------------------------------
5.5  API 接口封装层
---------------------------------------------------------------------------

frontend/src/api/ 目录下共有 22 个 API 封装模块，将后端接口逐一封装为独立的 JavaScript 函数，按业务模块分组：

- auth.js：登录、注册、获取用户信息、修改密码
- scenic.js：景点列表、详情、推荐、搜索
- food.js：美食列表、详情
- route.js：路线列表、详情
- marketplace.js：市集列表、详情
- merchant.js：商家信息、分类查询、资质审核
- plan.js：行程规划的增删改查、智能生成
- review.js：评价提交、查询、删除
- reply.js：回复创建、查询（树形）、删除
- activity.js：活动列表、创建、更新、删除
- roomType.js：房型查询、管理
- order.js：订单创建、查询、状态更新
- groupBuy.js：团购套餐查询、下单
- favoriteGateway.js：收藏/取消收藏、收藏列表
- merchantFavorite.js：商家收藏相关
- merchantActivity.js：商家活动管理
- merchantApplication.js：商家资质申请提交与查询
- operationLog.js：操作日志查询
- adminDashboard.js：管理员仪表板统计数据
- systemConfig.js：系统配置读取与更新
- search.js：全局关键词搜索
- comment.js：评论相关

---------------------------------------------------------------------------
5.6  主要页面说明
---------------------------------------------------------------------------

【首页（Home.vue）】
首页包含 Banner 轮播图（7 张景德镇风景图片，带 Ken Burns 缩放动画）、悬浮搜索框、快捷导航栏（找景点/寻美食/看路线/订酒店/玩陶艺五个入口）、热门景点卡片列表、陶瓷体验专区和推荐内容区。快捷导航栏通过负边距覆盖在 Banner 底部，形成视觉层次感。

【智能推荐页（Recommend.vue）】
推荐页支持景点、美食、酒店、路线、陶瓷工坊五种内容类型切换。右侧内容区顶部设有排序控件（评分/价格/热度升降序），排序状态通过 URL 参数持久化，刷新后保留。内容以卡片网格形式展示，底部配有分页控件。左侧分类导航栏固定显示，支持快速切换。

【景点详情页（ScenicDetail.vue）】
展示景点的图片画廊（轮播）、基本信息、特色亮点、游玩提示、地图位置、相关团购套餐、用户评论区（含嵌套回复）。右侧侧边栏提供收藏按钮、加入行程按钮和预订入口。

【酒店详情页（HotelDetail.vue）】
左右分栏布局，左侧展示图片画廊、酒店亮点标签、设施列表、酒店介绍、商家活动、用户评论区；右侧侧边栏展示综合评分、评价标签云和预订须知。点击"选择房间"弹出预订对话框，以卡片形式列出各房型信息，用户选择房型并填写入住日期、联系人信息后完成预订。

【行程规划页（Plan.vue）】
支持手动规划和智能生成两种模式。智能生成模式下，用户选择出行天数、偏好类型（景点游览/美食体验/文化体验/休闲放松）和预算档次，系统自动生成多日行程方案。生成结果以时间线组件展示，每天作为独立区块，区块内各地点通过连接线串联，展示地点名称、类型标签、评分、地址和参考价格。

【个人中心（Profile.vue）】
展示用户基本信息，支持修改头像、昵称、简介和联系方式。包含我的行程、我的收藏、我的订单、我的评价等子页面，聚合展示用户的所有历史数据。

【商家中心（merchant/Home.vue）】
商家登录后的主页，展示店铺基本信息、近期订单概览、评价统计等。左侧导航菜单提供店铺管理、房型管理、订单管理、评价管理、活动管理、团购管理等功能入口。

【管理员后台（admin/）】
仪表板展示用户总数、商家总数、订单总数、收入趋势等统计卡片，通过 ECharts 绘制折线图和柱状图，支持 WebSocket 实时数据更新。审核页面展示待审核的商家申请列表，管理员可查看资质材料并给出通过/拒绝意见。系统管理页面以标签页形式分类管理景点、用户、商家等基础数据。

=========================================================================
第六章  核心业务流程
=========================================================================

---------------------------------------------------------------------------
6.1  用户认证流程
---------------------------------------------------------------------------

用户打开系统后首先看到登录页面。输入账号和密码后提交，前端将凭据以 JSON 格式发送给后端 /api/auth/login 接口。后端在 user 表中查找用户，使用 BCryptPasswordEncoder.matches() 验证密码，成功后使用 jjwt 生成包含用户 ID 和角色的 JWT Token 返回给前端。

前端收到 Token 后，根据"记住我"选项将其存入 localStorage 或 sessionStorage，同时将用户信息存入 Pinia Store。此后每次请求，Axios 请求拦截器自动将 Token 附加到 Authorization 请求头。

后端 JwtAuthenticationFilter 在每次请求到达 Controller 前执行 Token 验证。Token 有效则将用户信息注入 SecurityContext；Token 无效或过期则返回 401 状态码，前端响应拦截器收到 401 后自动清除本地数据并跳转登录页。

退出登录时，前端清空 Pinia Store 和本地存储中的 Token 和用户信息，跳转到登录页。后端 Token 本身会在有效期到达后自然失效（JWT 无状态，服务端无需主动销毁）。

---------------------------------------------------------------------------
6.2  智能推荐流程
---------------------------------------------------------------------------

推荐接口（GET /api/scenic/recommend）接收可选的 userId 参数，Service 层根据用户是否登录选择策略：

未登录用户：直接按景点访问量（visitCount）从高到低排序，返回热门景点列表，保证页面始终有内容展示（冷启动回退策略）。

已登录用户：
1. 从数据库查询该用户的收藏记录和评分记录。
2. 构建标签兴趣画像：遍历收藏的景点，将其所有标签权重 +1；遍历评分记录，评分 ≥4 分的景点标签权重 +2，评分 <4 分不调整。
3. 计算每个候选景点与用户画像的匹配分数（加权 Jaccard 相似度：交集标签权重之和 / 并集标签权重之和）。
4. 按匹配分数降序排列，已收藏或已评分的景点适当降权（避免重复推荐）。
5. 分页截取当页数据返回。

系统同时支持三种推荐模式（通过 mode 参数切换）：内容推荐（纯标签匹配）、混合推荐（标签匹配 + 热度加权）、协同过滤（参考相似用户的收藏行为）。

---------------------------------------------------------------------------
6.3  行程规划流程
---------------------------------------------------------------------------

手动规划：用户在行程规划页面手动添加景点、美食、酒店等地点，自由安排每天的行程顺序，保存后存入 travel_plan 表。

智能生成：用户填写出行天数、偏好类型和预算档次，点击"智能规划行程"按钮，前端调用 POST /api/plan/smart-generate 接口，SmartTripService 执行以下步骤：

1. 候选筛选：按偏好类型从数据库获取对应景点和商家。陶瓷工坊和陶瓷市集强制纳入候选集。
2. 预算过滤：低预算档次排除票价 >80 元的景点。
3. 路线优化：对有坐标的景点使用贪心最近邻算法排序，减少游览路线的总距离。
4. 按天分配：单日最多 5 个地点，多日每天最多 4 个，每天末尾插入一家酒店。
5. 生成结果以 JSON 格式存入 planDetails 字段，返回给前端展示。

用户确认行程后点击保存，行程数据写入数据库，可在个人中心查看和二次编辑。

---------------------------------------------------------------------------
6.4  酒店预订流程
---------------------------------------------------------------------------

用户在酒店详情页查看房型列表，选择房型后弹出预订对话框，填写入住日期、退房日期和联系人信息，点击提交。

后端 OrderController 接收预订请求，执行两项校验：
1. 日期合法性校验：入住日期不能早于今天，退房日期不能早于入住日期。
2. 重复预订校验：检查同一商家在同一时间段是否已有待确认或已确认的预订记录，防止冲突。

两项校验通过后，预订记录写入 reservation 表，初始状态为"待确认"。商家在后台收到预订通知后可选择确认或拒绝，确认后状态更新为"已确认"，服务结束后标记为"已完成"，用户也可在此之前主动取消。

---------------------------------------------------------------------------
6.5  团购下单流程
---------------------------------------------------------------------------

用户在商家详情页浏览已审核上架的团购套餐，选择套餐后填写使用日期、购买数量和联系人信息，点击提交。

后端 GroupBuyController 接收下单请求，调用 GroupBuyService 执行防超卖逻辑：在数据库层执行带条件的原子 UPDATE：UPDATE group_buy SET stock = stock - quantity WHERE id = ? AND stock >= quantity。如果受影响行数为 0（说明库存不足），Service 层抛出异常，整个下单事务回滚，返回"库存不足"提示。如果扣减成功，同步创建 group_buy_order 记录，订单状态为"待使用"。

---------------------------------------------------------------------------
6.6  评论与回复流程
---------------------------------------------------------------------------

用户在景点、酒店等详情页底部的评论区提交评价，包含评分（1-5星）、文字内容和可选图片。评论写入 review 表后实时显示在列表中。

其他用户或商家可以对评论发起回复，回复写入 reply 表，通过 parentReply 外键形成树形结构。前端加载评论时，同步获取每条评论的回复树，按层级渲染嵌套结构。商家回复通过 isMerchantReply 字段标识，前端以橙蓝渐变背景配合"商家回复"徽标与普通用户回复形成视觉区分。

=========================================================================
第七章  数据库设计
=========================================================================

---------------------------------------------------------------------------
7.1  关系总览
---------------------------------------------------------------------------

系统使用 MySQL 数据库，数据库名为 tourism。核心关联关系如下：

User 表是系统核心，通过外键被 Merchant（一对一）、TravelPlan（多对多）、Review（多对一）、Favorite（多对一）、Order（多对一）等表引用。

Merchant 表通过外键关联 User（所属账号）和 ScenicSpot（关联景点，仅 SCENIC 类型商家使用）。

ScenicSpot 表被 Review（评价）、Favorite（收藏）、TravelPlan（行程）等表引用。

Review 表同时关联 User（评价者）、ScenicSpot（被评景点）、Merchant（被评商家）、Marketplace（被评市集），三个目标字段均可为空，通过非空字段判断评价对象类型。

Reply 表通过 parentReply 自引用外键实现树形嵌套结构。

GroupBuy 表关联 Merchant（发布商家），GroupBuyOrder 表关联 User（下单用户）和 GroupBuy（套餐）。

---------------------------------------------------------------------------
7.2  主要数据表清单
---------------------------------------------------------------------------

序号  表名                      说明                    主要关联
1     user                      用户主表                无
2     merchant                  商家表                  → user, scenic_spot
3     scenic_spot               景点表                  无
4     travel_plan               行程规划表              → user
5     travel_plan_version       行程版本历史            → travel_plan
6     review                    评价表                  → user, scenic_spot, merchant, marketplace
7     reply                     评价回复表              → review, user, merchant, reply(自引用)
8     room_type                 房型表                  → merchant
9     reservation               酒店预订表              → user, merchant, room_type
10    food                      美食表                  → merchant
11    marketplace               市集表                  无
12    group_buy                 团购套餐表              → merchant
13    group_buy_order           团购订单表              → user, merchant, group_buy
14    order                     通用订单表              → user, merchant
15    activity                  活动表                  无
16    merchant_activity         商家活动关联表          → merchant, activity
17    favorite                  游客收藏表              → user, scenic_spot, merchant
18    marketplace_favorite      市集收藏表              → user, marketplace
19    merchant_favorite         商家收藏表              → user, merchant
20    merchant_application      商家资质申请表          → user
21    operation_log             操作日志表              → user
22    system_config             系统配置表              无

=========================================================================
第八章  文件清单与职责索引
=========================================================================

---------------------------------------------------------------------------
8.1  后端文件
---------------------------------------------------------------------------

pom.xml：Maven 依赖配置，声明 Spring Boot 3.2.0、Spring Security、JPA、JWT、WebSocket、Validation 等所有后端依赖。

src/main/resources/application.yml：Spring Boot 全局配置，定义服务端口（8888）、数据库连接（MySQL tourism）、文件上传限制、地图 API 开关等。

src/main/java/com/jdz/tourism/config/SecurityConfig.java：Spring Security 配置，定义接口访问权限规则、JWT 过滤器注册、CORS 配置、BCrypt 密码编码器 Bean。

src/main/java/com/jdz/tourism/config/JwtAuthenticationFilter.java：JWT 认证过滤器，在每次请求前验证 Token 并注入用户信息到 SecurityContext。

src/main/java/com/jdz/tourism/utils/JwtUtil.java：JWT 工具类，封装 Token 生成、解析、验证逻辑。

src/main/java/com/jdz/tourism/annotation/LogOperation.java：自定义操作日志注解，标注在需要记录日志的方法上。

src/main/java/com/jdz/tourism/controller/AuthController.java：认证接口，处理注册、登录、Token 验证。

src/main/java/com/jdz/tourism/controller/ScenicSpotController.java：景点接口，处理景点 CRUD、推荐、搜索。

src/main/java/com/jdz/tourism/controller/TravelPlanController.java：行程规划接口，处理行程增删改查和智能生成。

src/main/java/com/jdz/tourism/controller/MerchantController.java：商家接口，处理商家信息管理和资质审核。

src/main/java/com/jdz/tourism/controller/ReviewController.java：评价接口，处理评价提交、查询、删除和商家回复。

src/main/java/com/jdz/tourism/controller/ReplyController.java：回复接口，处理嵌套回复的创建和树形查询。

src/main/java/com/jdz/tourism/controller/GroupBuyController.java：团购接口，处理套餐管理和防超卖下单。

src/main/java/com/jdz/tourism/controller/AdminDashboardController.java：管理员仪表板接口，提供统计数据。

src/main/java/com/jdz/tourism/service/ScenicSpotService.java：景点业务逻辑，包含推荐算法（标签画像、Jaccard 相似度计算）。

src/main/java/com/jdz/tourism/service/SmartTripService.java：智能行程生成逻辑，包含候选筛选、预算过滤、路线优化、按天分配四个步骤。

src/main/java/com/jdz/tourism/service/GroupBuyService.java：团购业务逻辑，包含防超卖的原子库存扣减。

src/main/java/com/jdz/tourism/service/UserService.java：用户业务逻辑，包含注册、密码加密、商家同步创建。

src/main/java/com/jdz/tourism/service/RealtimeDataService.java：WebSocket 实时推送服务，向管理员仪表板推送数据更新。

database/tourism.sql：数据库完整导出脚本，包含所有表结构和初始样例数据，导入后即可获得可运行的演示环境。

---------------------------------------------------------------------------
8.2  前端文件
---------------------------------------------------------------------------

frontend/package.json：前端依赖配置，声明 Vue 3、Element Plus、Pinia、Vue Router、ECharts、Axios、Vue i18n 等依赖。

frontend/vite.config.js：Vite 构建配置，配置开发服务器代理（将 /api 请求转发到后端 8888 端口）、自动导入插件等。

frontend/src/main.js：Vue 应用入口，注册插件、配置路由守卫。

frontend/src/App.vue：根组件，包含 router-view 和全局样式。

frontend/src/router/index.js：路由配置，按游客/商家/管理员三组定义路由规则，配置路由守卫。

frontend/src/stores/user.js：Pinia 用户状态管理，维护 token、user、isLoggedIn、userRole 等状态。

frontend/src/utils/request.js：Axios 二次封装，配置基础 URL、请求拦截器（注入 Token）、响应拦截器（统一错误处理）。

frontend/src/api/（22个文件）：后端接口封装，将所有接口封装为独立 JavaScript 函数，页面组件直接调用。

frontend/src/views/tourist/Home.vue：首页，包含 Banner 轮播、快捷导航、热门景点、推荐内容区。

frontend/src/views/tourist/Recommend.vue：智能推荐页，支持五种内容类型切换和多维度排序筛选。

frontend/src/views/tourist/ScenicDetail.vue：景点详情页，展示景点信息、评论区、相关推荐。

frontend/src/views/tourist/HotelDetail.vue：酒店详情页，包含房型选择和预订功能。

frontend/src/views/tourist/Plan.vue：行程规划页，支持手动编排和智能生成两种模式。

frontend/src/views/tourist/Profile.vue：个人中心，聚合展示用户的行程、收藏、订单、评价。

frontend/src/views/merchant/Home.vue：商家中心首页，展示店铺概览和快捷操作入口。

frontend/src/views/admin/SystemManage.vue：系统管理页，以标签页形式管理景点、用户、商家等基础数据。

frontend/src/views/admin/GroupBuyManage.vue：团购审核管理页，管理员审核商家提交的团购套餐。

frontend/src/components/plan/SmartTripResult.vue：智能行程结果展示组件，以时间线形式渲染多日行程。

frontend/src/components/review/ReviewSection.vue：评论区组件，支持评价提交、嵌套回复展示、商家回复标识。

frontend/src/components/GroupBuySection.vue：团购区块组件，在商家详情页展示可购买的团购套餐。

=========================================================================
第九章  部署说明
=========================================================================

后端启动：
1. 确保本机已安装 JDK 17 和 MySQL 8.0。
2. 创建数据库：CREATE DATABASE tourism CHARACTER SET utf8mb4;
3. 导入初始数据：mysql -u root -p tourism < database/tourism.sql
4. 修改 application.yml 中的数据库密码。
5. 执行：mvn spring-boot:run 或在 IDE 中运行主类。
6. 后端服务启动在 http://localhost:8888。

前端启动：
1. 确保本机已安装 Node.js 18+。
2. 进入 frontend 目录：cd frontend
3. 安装依赖：npm install
4. 启动开发服务器：npm run dev
5. 前端服务启动在 http://localhost:5173。

演示账号（初始数据中包含）：
- 管理员：admin / admin123
- 商家：merchant01 / 123456
- 游客：tourist01 / 123456

跨角色演示：由于 Token 存储在 localStorage/sessionStorage（同源共享），同一浏览器下多个标签页会共用同一登录状态。如需同时演示三个角色，建议使用三个不同的浏览器（Chrome、Edge、Firefox）分别登录，或使用 Chrome 普通窗口 + Chrome 无痕窗口 + Edge 的组合。
