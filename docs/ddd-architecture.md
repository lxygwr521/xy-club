# DDD 架构详解 — 以 xyclub 项目为例

## 一、什么是 DDD

**DDD（Domain-Driven Design，领域驱动设计）** 是一种软件架构方法论，核心思想是：**代码的组织方式应该反映业务领域的结构，而不是技术框架的层次。**

### 传统三层 vs DDD 分层

```
传统 MVC                         DDD 四层
─────────                        ────────
Controller                       Controller    ← 接口层（适配器）
    ↓                                ↓
Service                         Domain        ← 领域层（核心业务逻辑）
    ↓                                ↓
DAO / Mapper                    Infra         ← 基础设施层（数据库、缓存、消息队列）
                                     ↑
                                Common        ← 公共层（跨层共享）
```

**关键区别：** 传统架构中 Service 层通常是最厚的，既包含业务逻辑又包含技术操作。DDD 把"怎么存取数据"（Infra）和"业务要干什么"（Domain）拆开，Domain 层不依赖任何技术框架。

---

## 二、四层职责

```
┌────────────────────────────────────────────────────────────┐
│  Controller (控制器层)                                      │
│  职责：接收 HTTP 请求、参数校验、返回响应                     │
│  依赖：Domain                                               │
│  不该做：写业务逻辑、直接操作数据库                            │
├────────────────────────────────────────────────────────────┤
│  Domain (领域层) ★ 系统核心                                 │
│  职责：封装所有业务规则和逻辑                                │
│  依赖：Infra (面向接口)                                     │
│  不该做：依赖 Controller、依赖 Spring MVC、写 SQL             │
├────────────────────────────────────────────────────────────┤
│  Infra (基础设施层)                                          │
│  职责：数据库操作、缓存、消息队列、外部 API 调用              │
│  依赖：Common                                               │
│  不该做：写业务判断逻辑                                      │
├────────────────────────────────────────────────────────────┤
│  Common (公共层)                                             │
│  职责：跨层共享的枚举、工具类、通用实体                       │
│  依赖：无（被所有层依赖）                                    │
└────────────────────────────────────────────────────────────┘
```

---

## 三、项目中的实际映射

### 3.1 模块划分

```
xyclub-auth/
├── xyclub-auth-application-controller/    ← Controller 层
├── xyclub-auth-domain/                    ← Domain 层（核心）
├── xyclub-auth-infra/                     ← Infra 层
├── xyclub-auth-common/                    ← Common 层
└── xyclub-auth-starter/                   ← 启动模块
```

### 3.2 一个请求的完整流转（以用户注册为例）

```
POST /user/register {userName:"zhangsan", password:"123", email:"z@test.com"}

┌─────────────────────────────────────────────────────────────────────┐
│ ① Controller 层                                                    │
│                                                                    │
│   UserController.register(AuthUserDTO)                             │
│     ├── 参数校验：checkUserInfo() → 用户名/密码/邮箱不能为空          │
│     ├── DTO → BO 转换：AuthUserDTOConverter.convertDTOToBO()        │
│     └── 调用领域层：authUserDomainService.register(authUserBO)       │
│                                                                    │
│   做了：接收、校验、转换、转发                                       │
│   没做：任何业务判断                                                 │
└─────────────────────────────────────────────────────────────────────┘
                                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│ ② Domain 层 ★                                                      │
│                                                                    │
│   AuthUserDomainServiceImpl.register(AuthUserBO)                    │
│     ├── 密码加密：SaSecureUtil.md5BySalt(password, "xyclub")        │
│     ├── 设置状态：status = OPEN, isDeleted = UN_DELETED             │
│     ├── BO → Entity：AuthUserBOConverter.convertBOToEntity()       │
│     ├── 持久化：authUserService.insert(authUser)                   │
│     ├── 查询默认角色：authRoleService.queryByCondition(normal_user)  │
│     ├── 建立关联：authUserRoleService.insert(userRole)              │
│     ├── 写角色到 Redis                                             │
│     └── 写权限到 Redis                                             │
│                                                                    │
│   做了：业务规则判断、调用基础设施、编排流程                           │
│   注意：用的是 Infra 层的接口（*Service），不是实现类                  │
│   通过 Spring DI 注入具体实现                                       │
└─────────────────────────────────────────────────────────────────────┘
                                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│ ③ Infra 层                                                         │
│                                                                    │
│   AuthUserServiceImpl.insert(AuthUser)                              │
│     └── authUserDao.insert(authUser)                               │
│           └── MyBatis XML → MySQL INSERT                            │
│                                                                    │
│   做了：执行数据库操作                                               │
│   没做：任何"为什么插入"的判断                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 四、核心设计模式

### 4.1 依赖倒置

```
Domain 层                      Infra 层
──────────                     ────────
只声明接口                     提供具体实现

AuthUserDomainService   ──→   AuthUserServiceImpl
(领域接口，定义"要做什么")       (基础设施实现，定义"怎么做")

关键：Domain 引用的是 Service 接口，不引用 ServiceImpl
```

```java
// Domain 层 — 只依赖接口
@Resource
private AuthUserService authUserService;        // ← 接口，不是实现类

// Spring 自动注入 Infra 层的实现
// Domain 不知道、不关心具体实现是什么
```

### 4.2 对象转换链

```
【每层使用不同的数据对象，通过 MapStruct 转换】

HTTP 请求 → DTO         （Data Transfer Object，只含请求字段）
     ↓ AuthUserDTOConverter
         BO              （Business Object，领域对象）
     ↓ AuthUserBOConverter
       Entity            （数据库实体，与表一一对应）
     ↓ MyBatis
        DB
```

| 对象类型 | 所属层 | 作用 | 示例 |
|---------|--------|------|------|
| **DTO** | Controller | 接收参数、返回结果 | `AuthUserDTO {userName, email, password}` |
| **BO** | Domain | 承载业务操作数据 | `AuthUserBO {userName, email, password, status}` |
| **Entity** | Infra | 映射数据库表 | `AuthUser {id, userName, ..., createdTime, isDeleted}` |

**为什么每层不同对象？**

```
如果不分层，一个 User 对象同时是：
  - 接收 HTTP 参数的容器  → 可能被恶意传入 createdTime 篡改
  - 核心业务逻辑的载体    → 包含不该暴露给 Controller 的内部字段
  - 数据库的映射         → 数据库加字段就得改业务逻辑代码

分层后各层独立变化：
  - 数据库加字段 → 只改 Entity
  - 前端多传参数 → 只改 DTO
  - 业务规则变化 → 只改 Domain
```

### 4.3 开闭原则

以新增角色管理为例：

```
新需求：需要一个角色管理功能

不用 DDD：在已有的 UserService 里加方法 → 越来越大，最终不可维护

用 DDD：
  ① Controller 层：加 RoleController、AuthRoleDTO、AuthRoleDTOConverter
  ② Domain 层：加 AuthRoleDomainService、AuthRoleBO、AuthRoleBOConverter
  ③ Infra 层：加 AuthRoleService/Impl、AuthRoleDao、AuthRoleDao.xml
  ④ 不改动已有的任何文件 ✅
```

---

## 五、项目中的领域划分（限定上下文）

```
Auth 模块（当前已实现）
├── 用户领域    AuthUser      → auth_user 表
├── 角色领域    AuthRole      → auth_role 表
├── 权限领域    AuthPermission → auth_permission 表（树形结构）
├── 用户-角色   AuthUserRole  → auth_user_role 表
└── 角色-权限   AuthRolePermission → auth_role_permission 表
```

每个子领域都有自己独立的 Controller → Domain → Infra 垂直链路，**功能内聚、边界清晰、互不污染**。

---

## 六、DDD 的代价

| 代价 | 说明 |
|------|------|
| 文件数量多 | 一个表对应 12 个文件（DTO/BO/Entity × 2 Converter × Controller/DomainService/Service/Dao/XML） |
| 转换代码多 | DTO ↔ BO ↔ Entity，每层都要写 Converter |
| 前期慢 | 新建一个业务领域需要创建大量基础文件 |
| 过度设计风险 | 简单 CRUD 业务可能不需要这么重的架构 |

### 如何缓解 — 代码生成器

项目中 jcclub 后续引入的 `easy-gen-code-spring-boot-starter` 自定义代码生成器，就是为解决 DDD 文件多的问题：**只要配好 YAML，一键生成 12 个文件的完整 CRUD 骨架**。

---

## 七、DDD 核心原则速记

```
1. Controller 不写业务    → Controller 只做校验、转换、转发
2. Domain 不碰数据库      → Domain 只调用 Infra 接口，不知道底层是 MySQL 还是 Redis
3. Infra 不做判断         → Infra 只执行 CRUD，不决定"什么时候做、为什么做"
4. 每层有自己的对象        → DTO ≠ BO ≠ Entity，各层独立变化
5. 依赖接口而非实现        → Domain 引用 Service 接口，Spring 注入 Impl
```

### 一句话总结

**DDD 不是更高深——它只是把你本来会混在一起写的代码，按职责拆到不同的包和对象里去，让每一层只操心一件事。**
