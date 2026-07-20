# xyclub-auth 模块架构文档

## 一、模块概览

```
xyclub-auth                                    ← 父 POM，统一版本管理
├── xyclub-auth-api                            ← API 接口层 (预留)
├── xyclub-auth-common                         ← 公共模块 (枚举/通用实体)
├── xyclub-auth-domain                         ← 领域层 (DDD 核心)
├── xyclub-auth-infra                          ← 基础设施层 (持久化/配置)
├── xyclub-auth-application                    ← 应用层
│   ├── xyclub-auth-application-controller     ←   REST 控制器
│   ├── xyclub-auth-application-job            ←   定时任务 (预留)
│   └── xyclub-auth-application-mq             ←   消息队列 (预留)
└── xyclub-auth-starter                        ← 启动模块
```

**技术栈：** Spring Boot 2.4.2 + Spring Cloud 2020.0.6 + Spring Cloud Alibaba 2021.1 + Sa-Token 1.37.0 + MyBatis-Plus 3.4.0 + MapStruct 1.4.2 + MySQL 8.0 + Druid

---

## 二、分层架构 (DDD)

```
┌─────────────────────────────────────────────┐
│          application-controller              │  ← 接口层：接收请求、参数校验、DTO 转换
│   Controller / DTO / DTOConverter / Config   │
├─────────────────────────────────────────────┤
│                 domain                       │  ← 领域层：核心业务逻辑
│   DomainService / BO / BOConverter / Constant│
├─────────────────────────────────────────────┤
│                  infra                       │  ← 基础设施层：数据持久化、配置
│   Entity / DAO / Service / Config / MapperXML│
├─────────────────────────────────────────────┤
│                 common                       │  ← 公共层：跨层共享
│   Enums / Entity(Result/PageInfo/PageResult) │
└─────────────────────────────────────────────┘
```

**依赖关系：** controller → domain → infra → common

**对象转换链路：**
```
HTTP Request → DTO ──(DTOConverter)──→ BO ──(BOConverter)──→ Entity → DB
                                      (MapStruct)           (MapStruct)
```

---

## 三、模块详情

### 3.1 xyclub-auth-common (公共模块)

```
common/
├── enums/
│   ├── ResultCodeEnum.java        # 响应码 (SUCCESS=200 / FAIL=500)
│   ├── AuthUserStatusEnum.java    # 用户状态 (OPEN=启用 / CLOSE=禁用)
│   └── IsDeletedFlagEnum.java     # 删除标记 (DELETED=1 / UN_DELETED=0)
├── entity/
│   ├── Result.java                # 统一响应 {success, code, message, data}
│   ├── PageInfo.java              # 分页请求 {pageNo, pageSize}
│   └── PageResult.java            # 分页响应 {total, totalPages, result}
├── config/                        # (预留) 公共配置
├── dict/                          # (预留) 公共常量
└── util/                          # (预留) 公共工具
```

---

### 3.2 xyclub-auth-domain (领域层)

```
domain/
├── constants/
│   └── AuthConstant.java          # NORMAL_USER = "normal_user"
├── entity/
│   ├── AuthUserBO.java            # 用户领域对象
│   ├── AuthRoleBO.java            # 角色领域对象
│   └── AuthPermissionBO.java      # 权限领域对象
├── convert/
│   ├── AuthUserBOConverter.java   # UserBO → AuthUser
│   ├── AuthRoleBOConverter.java   # RoleBO → AuthRole
│   └── AuthPermissionBOConverter.java  # PermissionBO → AuthPermission
└── service/
    ├── AuthUserDomainService.java       # 用户领域服务接口
    │   ├── register(BO) → Boolean      # 注册 (含密码加密 + 角色关联)
    │   ├── update(BO) → Boolean        # 更新
    │   └── delete(BO) → Boolean        # 逻辑删除
    ├── AuthRoleDomainService.java       # 角色领域服务接口
    │   ├── add(BO) → Boolean
    │   ├── update(BO) → Boolean
    │   └── delete(BO) → Boolean
    ├── AuthPermissionDomainService.java # 权限领域服务接口
    │   ├── add(BO) → Boolean
    │   ├── update(BO) → Boolean
    │   └── delete(BO) → Boolean
    └── impl/
        ├── AuthUserDomainServiceImpl.java      # @Transactional 注册
        ├── AuthRoleDomainServiceImpl.java
        └── AuthPermissionDomainServiceImpl.java
```

**核心业务规则：**
| 规则 | 说明 |
|------|------|
| 密码加密 | `SaSecureUtil.md5BySalt(password, "xyclub")` |
| 注册事务 | `@Transactional(rollbackFor = Exception.class)`，确保用户插入 + 角色关联一起成功 |
| 默认角色 | 注册时自动分配 `normal_user` 角色 |
| 逻辑删除 | 所有删除操作设为 `is_deleted = 1`，不物理删除 |

---

### 3.3 xyclub-auth-infra (基础设施层)

```
infra/
├── basic/
│   ├── entity/
│   │   ├── AuthUser.java         # auth_user 表实体 (14 字段)
│   │   ├── AuthRole.java         # auth_role 表实体
│   │   ├── AuthUserRole.java     # auth_user_role 关联表
│   │   └── AuthPermission.java   # auth_permission 权限表 (13 字段)
│   ├── mapper/
│   │   ├── AuthUserDao.java
│   │   ├── AuthRoleDao.java
│   │   ├── AuthUserRoleDao.java
│   │   └── AuthPermissionDao.java
│   ├── service/
│   │   ├── AuthUserService.java  + impl/
│   │   ├── AuthRoleService.java  + impl/
│   │   ├── AuthUserRoleService.java + impl/
│   │   └── AuthPermissionService.java + impl/
│   └── utils/
│       └── DruidEncryptUtil.java # 数据库密码加解密
├── config/
│   ├── MybatisConfiguration.java      # MyBatis-Plus 拦截器
│   ├── MybatisPlusAllSqlLog.java      # 完整 SQL 日志打印
│   └── SqlStatementInterceptor.java   # 慢 SQL 监控 (1s/5s/10s 阈值)
├── mq/     (预留)
└── rpc/    (预留)
```

**MyBatis 映射文件：**
```
resources/mapper/
├── AuthUserDao.xml         # auth_user 表 CRUD
├── AuthRoleDao.xml         # auth_role 表 CRUD
├── AuthUserRoleDao.xml     # auth_user_role 表 CRUD
└── AuthPermissionDao.xml   # auth_permission 表 CRUD
```

---

### 3.4 xyclub-auth-application-controller (控制器层)

```
application-controller/
├── config/
│   └── GlobalConfig.java            # MVC 配置：空值忽略、空 Bean 序列化
├── dto/
│   ├── AuthUserDTO.java             # 用户请求 (userName/email/password...)
│   ├── AuthRoleDTO.java             # 角色请求 (roleName/roleKey)
│   └── AuthPermissionDTO.java       # 权限请求 (name/parentId/type/menuUrl...)
├── convert/
│   ├── AuthUserDTOConverter.java    # DTO → UserBO
│   ├── AuthRoleDTOConverter.java    # DTO → RoleBO
│   └── AuthPermissionDTOConverter.java  # DTO → PermissionBO
└── controller/
    ├── UserController.java          # /user/*     (register/update/delete/changeStatus/doLogin/isLogin)
    ├── RoleController.java          # /role/*     (add/update/delete)
    └── PermissionController.java    # /permission/* (add/update/delete)
```

**API 一览：**

| Controller | 端点 | 方法 | 说明 |
|------------|------|------|------|
| UserController | `/user/register` | POST | 用户注册 (含密码加密+默认角色) |
| | `/user/update` | POST | 修改用户信息 |
| | `/user/delete` | POST | 删除用户 (逻辑删除) |
| | `/user/changeStatus` | POST | 启用/禁用用户 |
| | `/user/doLogin` | GET | 测试登录 |
| | `/user/isLogin` | GET | 查询登录状态 |
| RoleController | `/role/add` | POST | 新增角色 |
| | `/role/update` | POST | 修改角色 |
| | `/role/delete` | POST | 删除角色 |
| PermissionController | `/permission/add` | POST | 新增权限 |
| | `/permission/update` | POST | 修改权限 |
| | `/permission/delete` | POST | 删除权限 |

---

## 四、数据库表关系 (ER)

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│  auth_user   │     │  auth_user_role  │     │  auth_role   │
├──────────────┤     ├──────────────────┤     ├──────────────┤
│ id (PK)      │←──→│ user_id  (FK)    │     │ id (PK)      │
│ user_name    │     │ role_id  (FK)    │←──→│ role_name    │
│ nick_name    │     │ created_by       │     │ role_key     │
│ email        │     │ created_time     │     │ created_by   │
│ phone        │     │ update_by        │     │ created_time │
│ password     │     │ update_time      │     │ update_by    │
│ sex          │     │ is_deleted       │     │ update_time  │
│ avatar       │     └──────────────────┘     │ is_deleted   │
│ status       │                              └──────────────┘
│ introduce    │
│ ext_json     │     ┌──────────────────┐
│ created_by   │     │ auth_permission  │
│ created_time │     ├──────────────────┤
│ update_by    │     │ id (PK)          │
│ update_time  │     │ name             │
│ is_deleted   │     │ parent_id        │  ← 树形结构自引用
└──────────────┘     │ type             │
                     │ menu_url         │
                     │ status           │
                     │ show             │
                     │ icon             │
                     │ permission_key   │
                     │ created_by       │
                     │ created_time     │
                     │ update_by        │
                     │ update_time      │
                     │ is_deleted       │
                     └──────────────────┘
```

**关系说明：** 用户 ↔ 角色为多对多（通过 `auth_user_role` 关联表），权限独立管理（树形结构，`parent_id` 自引用），RBAC 模型预留扩展。

---

## 五、配置与启动

### 启动类
`com.xyclub.auth.AuthApplication` (Spring Boot)

### 关键配置 (application.yml)
```yaml
server:
  port: 3011
spring:
  application:
    name: xyclub-auth
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR}
      config:
        server-addr: ${NACOS_ADDR}
sa-token:
  token-name: token
  timeout: 2592000
```

---

## 六、依赖关系矩阵

```
                    ┌───────┐
                    │common │
                    └───┬───┘
                        │
            ┌───────────┼───────────┐
            │           │           │
        ┌───┴───┐   ┌───┴───┐   ┌───┴───┐
        │ infra │←──│domain │←──│control│
        └───────┘   └───────┘   └───────┘
                        │
                        └── controller 依赖 domain
                            domain 依赖 infra
                            infra 依赖 common
                            所有模块依赖 common
```

| 模块 | 依赖 |
|------|------|
| common | lombok, mapstruct, fastjson, guava, commons-lang3, sa-token-redis-jackson, commons-pool2 |
| infra | spring-boot-starter-jdbc, druid, mysql-connector, mybatis-plus, common |
| domain | infra, common |
| controller | spring-boot-starter-web, sa-token, domain |
| starter | controller, job, mq |
