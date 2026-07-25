# xy-club 后端岗位面试问题清单

本文档以严格但负责的后端面试官视角整理，问题围绕当前 `xy-club` 刷题社区项目展开。建议准备时不要只背结论，要能结合代码路径、接口链路、数据表和具体取舍展开说明。

## 一、项目背景与动机

### 1. 你为什么做 `xy-club` 这个项目？

追问：

- 这个项目解决的核心用户问题是什么？
- 和普通 CRUD 后台相比，它有哪些业务复杂度？
- 为什么选择“刷题社区”作为项目主题，而不是电商、博客或管理系统？
- 你认为这个项目中最能体现后端能力的模块是哪一个？为什么？

考察点：

- 是否能讲清业务背景，而不是只罗列技术栈。
- 是否理解题目、分类、标签、用户、权限、文件、微信登录之间的业务关系。
- 是否能把项目价值和技术实现联系起来。

回答提醒：

- 可以从“题库内容管理 + 用户认证授权 + 网关统一入口 + 文件存储 + 微信验证码登录”几个方向展开。
- 不要只说“为了学习微服务”，要说明微服务拆分带来的业务边界和协作收益。

## 二、个人贡献与项目真实性

### 2. 你在这个项目中具体负责了哪些功能？

追问：

- 哪些模块是你从零设计的，哪些是参考已有代码同步或改造的？
- 你改过哪些关键类？能说出类名和作用吗？
- 如果我让你现场画出一次新增题目的调用链，你能画出来吗？
- 你做过哪些不是“照着写”的优化或问题修复？

考察点：

- 是否真正写过项目，而不是只看过代码。
- 是否能说出具体文件、类、接口和调用链。
- 是否能区分个人贡献、参考实现和二次优化。

可重点准备的贡献示例：

- `xyclub-subject` 题目领域：分类、标签、题目主表、题型答案表、题目关联关系。
- `xyclub-gateway` 鉴权：Sa-Token 路由拦截、Redis 读取角色权限、异常统一返回。
- 登录上下文透传：`LoginFilter` 写入 `loginId` 请求头，`LoginInterceptor` 恢复上下文，`LoginContextHolder` 保存请求内用户信息。
- 分类标签查询优化：`CompletableFuture` + `ThreadPoolExecutor` 并发查询二级分类下标签。
- `xyclub-oss` 存储适配：`StorageAdapter` 抽象存储能力，支持 MinIO 和预留 AliOSS。
- `xyclub-wx` 微信验证码登录：公众号消息生成验证码，Redis 暂存 OpenID，Auth 服务反查并登录。

## 三、整体架构与服务拆分

### 3. 项目为什么拆成 auth、gateway、subject、oss、wx 这些模块？

追问：

- 每个服务的职责边界是什么？
- 如果不拆服务，用单体项目实现，有什么问题？
- 当前拆分是否存在过度设计？哪些模块可以合并？
- 服务之间如何通信？当前有没有使用 RPC 或消息队列？
- Nacos 在这个项目里承担了什么角色？

考察点：

- 是否理解服务边界，而不是机械拆目录。
- 是否知道当前项目还处在演进阶段，部分 mq/job/api 模块是预留。
- 是否能客观看待微服务成本，例如部署复杂度、链路排查、配置管理、分布式一致性。

### 4. 你的项目为什么采用 DDD 分层？

追问：

- application-controller、domain、infra、common 各自负责什么？
- 为什么 Controller 不直接调用 DAO？
- DTO、BO、Entity 为什么要分开？
- MapStruct 在项目里解决了什么问题？
- DDD 在这个项目里有没有带来额外成本？

考察点：

- 是否理解“职责隔离”，而不是把 DDD 当作文件夹命名。
- 是否能说明 Controller -> Domain -> Infra -> Common 的依赖关系。
- 是否知道 DDD 的代价：类多、转换多、简单 CRUD 会显得重。

## 四、技术选型

### 5. 为什么选择 Spring Boot + Spring Cloud Alibaba + Nacos？

追问：

- Spring Boot 负责什么，Spring Cloud 负责什么？
- Nacos 的注册中心和配置中心分别解决什么问题？

注册中心解决什么问题？
  解决“服务在哪里”的问题，让微服务之间能动态发现和调用彼此。

服务启动时向注册中心注册自己的 IP 和端口

调用方从注册中心发现目标服务的实例列表，实现负载均衡

注册中心定期心跳检测，自动剔除故障实例
解决“配置怎么管”的问题，让配置集中管理、动态刷新，无需重启应用。

将 application.yml 中的配置搬到 Nacos 上集中管理

修改配置后，实时推送到所有应用，无需重启
- 本地启动报 `localhost:8848 connection refused` 可能是什么原因？
- 服务关闭后，Nacos 中实例状态会怎么变化？
- 如果 Nacos 挂了，已运行服务还能不能继续处理请求？

考察点：

- 是否理解服务注册发现和配置管理。
- 是否知道 Nacos 客户端心跳、临时实例、配置拉取的基本机制。
- 是否能结合曾经遇到的 Nacos 连接失败日志排查。

### 6. 为什么鉴权选择 Sa-Token，而不是 Spring Security？

追问：

- Sa-Token 在项目中完成了哪些事情？
 
Sa-Token 在项目中负责用户登录后的 token 生成和登录态维护，并在 Gateway 层完成统一登录校验、权限校验，同时结合 Redis
- `SaTokenConfigure` 的作用是什么？
- `StpInterfaceImpl` 为什么要实现角色和权限查询？
- Sa-Token 的 token 信息存在哪里？

Redis中，key是token,value是loginID
- 如果 Redis 中 token 丢失，会发生什么？

考察点：

- 是否理解 Sa-Token 的登录态、角色、权限模型。
- 是否知道网关统一鉴权与业务服务鉴权的区别。
- 是否能说清 Redis 在鉴权链路中的位置。

### 7. 为什么用 Redis 缓存角色权限？

追问：

- 为什么不每次请求都去 MySQL 查询权限？
- Redis 缓存的 key 和 value 大概是什么结构？
- 角色权限变更后，缓存如何更新？
- Redis 缓存击穿、穿透、雪崩分别是什么？本项目可能遇到哪一种？
- 如果 Redis 挂了，网关鉴权应该放行还是拒绝？

考察点：

- 是否理解缓存是为了降低网关鉴权延迟和数据库压力。
- 是否知道缓存一致性问题。
- 是否能给出失败策略，而不是只说“加缓存”。

## 五、网关与统一鉴权

### 8. 请求为什么会先经过 Gateway？

追问：

- 是 Nacos 决定请求先进 Gateway 吗？
- 前端、域名、负载均衡和网关之间是什么关系？
- 如果用户绕过 Gateway 直接访问 subject 服务，会有什么风险？
- 业务服务如何避免被绕过网关直接调用？
- Gateway 中路由规则如何把 `/subject/**` 转发到 subject 服务？

考察点：

- 是否明白“统一入口”来自部署和访问入口设计，不是服务注册自动保证。
- 是否能区分注册中心、网关、前端访问地址、内网隔离。

### 9. Gateway 里如何完成登录校验和权限校验？

追问：

- `/oss/**`、`/subject/**`、`/subject/subject/add` 分别如何拦截？
- `checkLogin` 和 `checkPermission` 有什么区别？
- 鉴权失败后如何返回统一 JSON？
- Gateway 是 WebFlux，和传统 Servlet Filter 有什么不同？
- GlobalFilter、SaReactorFilter、异常处理器的执行顺序你怎么理解？

考察点：

- 是否理解 Gateway 是响应式模型。
- 是否能解释登录校验、权限校验、异常转换的链路。

### 10. 登录用户 ID 是如何从 Gateway 传到 subject 服务的？

追问：

- `LoginFilter` 做了什么？
- 为什么通过请求头传 `loginId`？
- `LoginInterceptor` 为什么要在 `afterCompletion` 中清理 ThreadLocal？
- 为什么使用 `InheritableThreadLocal`？
- 如果异步线程池复用线程，ThreadLocal 可能带来什么问题？
- 如果有人伪造 `loginId` 请求头怎么办？

考察点：

- 是否理解用户上下文透传。
- 是否知道 ThreadLocal 内存泄漏和脏数据风险。
- 是否有安全意识：业务服务最好只信任来自网关的内部流量，或增加签名/内网隔离。

## 六、认证与微信登录

### 11. Auth 模块的 RBAC 模型是怎么设计的？

追问：

- `auth_user`、`auth_role`、`auth_permission`、`auth_user_role`、`auth_role_permission` 分别是什么关系？
- 权限表为什么要有 `parent_id`？
- 用户注册时如何分配默认角色？
- 角色权限批量分配如何保证一致性？
- 为什么使用逻辑删除？

考察点：

- 是否理解 RBAC 的用户-角色-权限多对多关系。
- 是否能说明事务边界和逻辑删除的业务意义。

### 12. 微信验证码登录链路是怎样的？

追问：

- 用户在公众号发送“验证码”后，系统做了什么？
- Redis 中 `loginCode.{code}` 存的是什么？为什么不是简单存 `1`？
- 验证码为什么要设置 5 分钟过期？
- Auth 服务拿到验证码后如何完成登录？
- 首次登录自动注册，重复登录如何处理？
- 这种验证码登录有什么安全风险？

考察点：

- 是否理解 OpenID、验证码、Redis TTL、Sa-Token 登录之间的关系。
- 是否能分析验证码撞库、短码冲突、重复注册、过期删除等问题。

## 七、题目领域与数据模型

### 13. subject 模块核心表有哪些？它们之间是什么关系？

追问：

- `subject_category` 如何表示一级分类和二级分类？
- `subject_label` 和 `subject_mapping` 都有分类/标签关系，区别是什么？
- `subject_info` 为什么不直接保存所有题型答案？
- 单选、多选、判断、简答为什么拆成不同答案表？
- `subject_mapping` 在分页查询题目时起什么作用？

考察点：

- 是否熟悉题库业务的数据结构。
- 是否能解释表设计背后的查询场景。
- 是否能发现当前设计中的改进点，例如外键缺失、字段类型不一致。

### 14. 新增一道题目的完整写入流程是什么？

追问：

- Controller、Domain、Infra 分别做了什么？
- `SubjectInfoDomainServiceImpl.add` 的步骤是什么？
- 题目主表和题型答案表的写入顺序是什么？
- 如果题型答案写入失败，主表已经插入怎么办？
- `subject_mapping` 是怎么生成的？

考察点：

- 是否能讲清事务、主键回填、题型分派、关联表批量写入。
- 是否能主动提到应使用事务保证一致性。

### 15. 题型处理为什么用 `SubjectTypeHandler`？

追问：

- 这是策略模式还是简单接口实现？为什么？
- `SubjectTypeHandlerFactory` 如何收集不同处理器？
- 新增一种题型时需要改哪些代码？
- 如果不用这个设计，直接写 if/else 会有什么问题？
- 当前工厂返回 null 时有没有风险？怎么改进？

考察点：

- 是否理解策略模式、工厂、开闭原则。
- 是否能识别异常兜底和扩展成本。

## 八、OSS 存储与适配器模式

### 16. OSS 模块为什么抽象 `StorageAdapter`？

追问：

- MinIO 和 AliOSS 的差异被隔离在哪里？
- `FileService` 为什么依赖接口而不是具体实现？
- `StorageConfig` 如何选择具体适配器？
- 如果未来接入腾讯云 COS，需要改哪些地方？
- 适配器模式和普通接口实现的区别是什么？

考察点：

- 是否理解适配器模式的目标是屏蔽外部服务差异，统一内部调用模型。
- 是否能说明这比在业务代码里写 `if minio else ali` 更容易扩展和测试。

### 17. 文件上传功能可能遇到哪些问题？

追问：

- 大文件上传如何处理？
- 文件名冲突如何避免？
- 文件类型和大小如何校验？
- 上传成功但业务保存失败怎么办？
- 如何做私有文件访问控制？

考察点：

- 是否有生产意识，而不是只会调用 SDK。
- 是否能考虑幂等、回滚、权限、对象生命周期。

## 九、并发优化与性能

### 18. 分类标签查询为什么要做多线程优化？

追问：

- 原始实现有什么性能问题？
- `CompletableFuture.supplyAsync` 具体并发了哪一部分？
- 线程池核心线程数、最大线程数、队列大小怎么设置？
- `CallerRunsPolicy` 的含义是什么？
- 单个分类标签查询失败，为什么不影响其他分类？
- 并发查询会不会把数据库打爆？

考察点：

- 是否理解并发优化的前提是多个独立 IO 查询可以并行。
- 是否知道线程池不是越大越好，需要结合 DB 连接池和机器资源。
- 是否能说明降级、超时、异常处理、限流。

### 19. 如果用户量扩大 10 倍，你会怎么改？

追问：

- Gateway 层怎么扩容？
- Auth 鉴权链路怎么扛住更高 QPS？
- subject 查询热点如何优化？
- MySQL 表和索引怎么调整？
- Redis 如何做高可用？
- 文件服务和 MinIO 如何扩展？
- 日志、监控、告警需要补什么？

考察点：

- 是否能分层思考：入口、缓存、数据库、服务实例、对象存储、监控。
- 是否能提出可落地方案，而不是笼统说“加机器”。

可参考回答方向：

- Gateway、auth、subject、oss 水平扩容，多实例注册到 Nacos。
- Redis 使用哨兵或集群，热点权限缓存设置合理 TTL 和更新策略。
- MySQL 增加索引、读写分离、慢 SQL 优化，必要时按业务维度分库分表。
- subject 列表查询增加缓存和分页保护，避免深分页。
- OSS 使用对象存储集群或云存储，静态资源走 CDN。
- 增加 Prometheus/Grafana、链路追踪、接口耗时、线程池和连接池监控。

## 十、数据库与 MyBatis

### 20. 项目中 MyBatis-Plus 和 XML SQL 如何分工？

追问：

- 简单 CRUD 为什么可以用 MyBatis-Plus？
- 复杂联表查询为什么保留 XML？
- `SubjectInfoDao.queryPage` 大概怎么实现过滤？
- 如何排查慢 SQL？
- `SqlStatementInterceptor` 和完整 SQL 日志有什么作用？

考察点：

- 是否理解 ORM 工具和手写 SQL 的边界。
- 是否能结合题目分页、分类标签过滤讲清 SQL。

### 21. 数据库连接失败你怎么排查？

追问：

- `Communications link failure` 常见原因有哪些？
- 如何确认 MySQL 是否启动、端口是否正确、账号密码是否正确？
- Druid 初始化失败会影响 Spring Boot 启动吗？
- Nacos 配置中心和本地配置哪个优先？
- 线上如何避免配置错误导致服务全部不可用？

考察点：

- 是否具备基础故障排查能力。
- 是否能从网络、配置、账号、驱动、连接池几个维度分析。

## 十一、异常处理、幂等与一致性

### 22. 项目中哪些地方需要事务？

追问：

- 用户注册为什么需要事务？
- 新增题目为什么需要事务？
- 分配角色权限为什么需要事务？
- 缓存写入 Redis 失败，数据库已提交怎么办？
- 事务能不能覆盖 Redis 操作？

考察点：

- 是否理解本地事务边界。
- 是否知道 DB 和 Redis 的一致性要通过补偿、重试、消息或延迟双删等方式处理。

### 23. 哪些接口需要考虑幂等？

追问：

- 用户重复注册如何处理？
- 文件重复上传如何处理？
- 角色权限重复分配如何处理？
- 微信验证码重复提交如何处理？
- 如何用唯一索引、token、Redis setNx 实现幂等？

考察点：

- 是否能从真实业务风险出发分析重复请求。

## 十二、可观测性与排查能力

### 24. 如果线上用户反馈“登录后访问题目接口还是 401”，你怎么排查？

追问：

- 前端是否携带 token？
- Gateway 是否命中正确路由？
- Sa-Token 是否能从 Redis 找到 session？
- `StpInterfaceImpl` 是否能读到角色权限？
- Redis 中权限 key 是否和 loginId 一致？
- 业务服务是否绕过 Gateway 被直接访问？

考察点：

- 是否能按请求链路逐层排查。
- 是否能区分登录失败、鉴权失败、路由失败、上下文透传失败。

### 25. 如果 subject 查询接口变慢，你怎么定位？

追问：

- 先看接口耗时、SQL 耗时还是线程池状态？
- 如何判断是数据库慢还是应用层组装慢？
- 如何查看慢 SQL 和执行计划？
- 线程池队列堆积说明什么？
- 是否可能是 N+1 查询问题？

考察点：

- 是否有性能排查路径。
- 是否能结合当前分类标签查询并发优化讲清问题。

## 十三、安全性

### 26. 当前项目有哪些安全风险？

追问：

- `loginId` 请求头能否被伪造？
- 微信验证码只有 0-999，是否容易被爆破？
- 密码使用 MD5 加盐是否足够安全？
- Druid 数据库密码加密解决的是什么问题？
- 文件上传是否存在恶意文件风险？
- 接口是否有防重放、防刷、限流？

考察点：

- 是否能主动暴露项目不足。
- 是否知道安全设计不是只做登录鉴权。

## 十四、项目不足与改进

### 27. 如果让你继续改进这个项目，你会优先改哪些地方？

追问：

- 哪些模块目前只是骨架或预留？
- 哪些地方缺少单元测试或集成测试？
- 哪些配置应该收敛到配置中心？
- 哪些表需要补索引？
- 哪些接口需要限流、熔断、降级？
- target 文件、生成代码是否应该进入 Git？

考察点：

- 是否能客观看待自己的项目。
- 是否能按优先级改进，而不是列一堆空泛方向。

可准备的改进方向：

- 补充 `.gitignore`，避免 `target/` 编译产物污染提交。
- 完善 Nacos 配置、环境隔离和启动文档。
- 对核心接口增加单元测试和集成测试。
- 对登录、鉴权、微信验证码增加限流和防爆破。
- 对 DB 表补充唯一索引和常用查询索引。
- 对 Redis 缓存增加 TTL、刷新、失效和降级策略。
- 增加全链路日志 traceId、监控告警和慢接口统计。
- 对 ThreadLocal 上下文透传增加更安全的内部调用校验。

## 十五、压力面追问

### 28. 如果我认为你的项目只是“照着教程写的”，你怎么证明你真的理解？

追问：

- 你能现场讲出一次请求从前端到数据库的完整链路吗？
- 你能说出一个你主动发现并修复的问题吗？
- 你能说出一个你认为当前设计不合理的地方吗？
- 你能把某个功能从单机演进到高并发版本吗？
- 如果删除某个模块，系统会受什么影响？

考察点：

- 是否能用“具体问题 + 具体代码 + 具体取舍”证明项目真实性。

### 29. 如果让你只保留三个技术亮点，你会选哪三个？

追问：

- 为什么这三个最重要？
- 每个亮点解决了什么问题？
- 有没有数据或场景证明它有效？
- 它们的缺点是什么？

建议准备方向：

- Gateway + Sa-Token + Redis 的统一鉴权链路。
- DDD 分层 + DTO/BO/Entity + MapStruct 的业务隔离。
- Subject 题型策略模式和分类标签查询的并发优化。

### 30. 如果面试官问“这个项目上线了吗”，你怎么回答？

追问：

- 如果没有上线，如何说明它仍然有工程价值？
- 本地环境依赖哪些中间件？
- 如何用 Docker Compose 简化启动？
- 线上部署需要哪些配置？
- 如何保证不同环境配置不混乱？

考察点：

- 是否诚实。
- 是否理解从本地项目到可部署系统还差哪些工程化工作。

## 十六、建议重点复习代码路径

面试前建议重点熟悉以下文件或目录：

| 方向 | 路径 |
| --- | --- |
| 网关鉴权配置 | `xyclub-gateway/src/main/java/com/xyclub/gateway/auth/SaTokenConfigure.java` |
| 角色权限读取 | `xyclub-gateway/src/main/java/com/xyclub/gateway/auth/StpInterfaceImpl.java` |
| 网关异常处理 | `xyclub-gateway/src/main/java/com/xyclub/gateway/exception/GatewayExceptionHandler.java` |
| 登录上下文透传 | `xyclub-gateway/src/main/java/com/xyclub/gateway/filter/LoginFilter.java` |
| subject 登录上下文恢复 | `xyclub-subject/xyclub-application/xyclub-application-controller/src/main/java/com/xyclub/subject/application/interceptor/LoginInterceptor.java` |
| 用户注册/登录 | `xyclub-auth/xyclub-auth-domain/src/main/java/com/xyclub/auth/domain/service/impl/AuthUserDomainServiceImpl.java` |
| 题目新增/查询 | `xyclub-subject/xyclub-domain/src/main/java/com/xyclub/subject/domain/service/impl/SubjectInfoDomainServiceImpl.java` |
| 题型策略工厂 | `xyclub-subject/xyclub-domain/src/main/java/com/xyclub/subject/domain/handler/subject/SubjectTypeHandlerFactory.java` |
| 分类标签查询优化 | `xyclub-subject/xyclub-domain/src/main/java/com/xyclub/subject/domain/service/impl/SubjectCategoryDomainServiceImpl.java` |
| 线程池配置 | `xyclub-subject/xyclub-domain/src/main/java/com/xyclub/subject/domain/config/ThreadPoolConfig.java` |
| OSS 适配器 | `xyclub-oss/src/main/java/com/xyclub/oss/adapter/StorageAdapter.java` |
| 微信验证码 | `xyclub-wx/src/main/java/com/xyclub/wx/handler/ReceiveTextMsgHandler.java` |
| 题目表关系 | `xyclub-subject/framework/table-relationship.md` |

## 十七、最终复盘题

请用 3 分钟回答：

> `xy-club` 是一个面向刷题场景的社区后端项目。我负责的重点是认证授权、题目领域、网关统一鉴权、文件存储适配和微信验证码登录。项目采用 Spring Boot + Spring Cloud Alibaba + Nacos 的微服务架构，业务服务内部使用 DDD 分层，认证侧使用 Sa-Token + Redis 实现登录态和角色权限缓存，网关负责统一鉴权并向下游透传登录上下文。题目模块围绕分类、标签、题目主表、题型答案表和关联表建模，并通过题型 Handler 扩展不同题型，通过线程池并发优化分类标签聚合查询。

继续追问：

- 这段介绍中，每一句你都能展开到代码和表结构吗？
- 如果只能讲一个最难点，你会讲哪个？
- 如果只能讲一个不足，你会讲哪个？
- 如果给你两周继续完善，你的改进计划是什么？
