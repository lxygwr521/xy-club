# xyclub-gateway 鉴权流程与 Redis 作用

## 一、整体架构

```
┌─────────┐     ┌──────────────┐     ┌──────────────┐     ┌─────────┐
│ Browser │────→│   Gateway    │────→│  Auth Module │────→│  MySQL  │
│         │     │  (Spring     │     │  (注册/登录)  │     │         │
│         │     │   Gateway)   │     │              │     │         │
└─────────┘     └──────┬───────┘     └──────┬───────┘     └─────────┘
                       │                    │
                       │    ┌───────────┐   │
                       └───→│   Redis   │←──┘
                            │ (角色/权限)│
                            └───────────┘
```

**网关是统一入口，Redis 是鉴权数据的中转缓存。**

# 完整鉴权链路

## 1. 登录流程（UserController.doLogin()）

1. 调用 `StpUtil.login(id)` 进行登录
2. Sa-Token 框架生成 token
3. token 写入 Redis（Key: `token:login:session:{token}`）
4. 返回 token 给浏览器，由浏览器保存

---

## 2. 每次请求流程（Gateway 过滤器）

1. 浏览器携带 token 发起请求
2. Gateway 过滤器拦截，Sa-Token 校验 token 是否在 Redis 中存在
    - 若不存在 → 返回 401
    - 若存在 → 继续下一步
3. 调用 `StpInterfaceImpl.getPermissionList(loginId)` 获取用户权限列表
    - 内部调用 `getAuth(loginId, "auth.permission")`
    - 从 Redis 读取权限数据（Key: `auth.permission.{userName}`）
    - 示例返回值：`["subject:add", "subject:delete"]`
4. Sa-Token 比对当前接口所需权限（如 `subject:add`）与用户权限列表
    - 匹配 → 放行
    - 不匹配 → 返回 401

---

## 3. Redis 存储数据结构

| 数据 | 谁写入 | 谁读取 | Key 格式 |
|------|--------|--------|----------|
| Token-Session | Sa-Token 框架（登录时） | Gateway 过滤器（每次请求） | `token:login:session:{token}` |
| 角色/权限 | AuthUserDomainServiceImpl（注册时） | StpInterfaceImpl（鉴权时） | `auth.role.{userName}`<br>`auth.permission.{userName}` |
---

## 二、数据写入流程（用户注册时）

```
UserController.register()
  │
  └── AuthUserDomainServiceImpl.register()
        │
        ├── ① 用户信息写入 MySQL (auth_user 表)
        ├── ② 分配默认角色，写入 auth_user_role 关联表
        │
        ├── ③ 角色缓存到 Redis:
        │     key = "auth.role.{userName}"
        │     val = [{"id":1, "roleKey":"normal_user", ...}]
        │
        └── ④ 权限缓存到 Redis:
              ├── 查询 auth_role_permission 获取角色关联的权限 ID 列表
              ├── 调用 queryByRoleList(permissionIdList) 批量获取权限
              └── key = "auth.permission.{userName}"
                  val = [{"permissionKey":"subject:add", ...}, ...]
```

**关键代码位置：** `AuthUserDomainServiceImpl.java:register()`

```java
// 写角色到 Redis
String roleKey = redisUtil.buildKey(authRolePrefix, authUser.getUserName());
redisUtil.set(roleKey, new Gson().toJson(roleList));

// 写权限到 Redis
List<Long> permissionIdList = rolePermissionList.stream()
    .map(AuthRolePermission::getPermissionId).collect(Collectors.toList());
List<AuthPermission> permissionList = authPermissionService.queryByRoleList(permissionIdList);
String permissionKey = redisUtil.buildKey(authPermissionPrefix, authUser.getUserName());
redisUtil.set(permissionKey, new Gson().toJson(permissionList));
```

---

## 三、网关鉴权流程（每次请求）

### 3.1 Sa-Token 拦截链

```
请求到达 Gateway
  │
  ├── ① Sa-Token 过滤器拦截
  │     └── 校验 token 有效性 (从 Redis 中读取 token-session)
  │
  ├── ② Sa-Token 调用 StpInterfaceImpl，获取角色和权限
  │     │
  │     ├── getRoleList(loginId)
  │     │   └── Redis.get("auth.role.{loginId}")
  │     │       └── 反序列化 → stream → roleKey 列表
  │     │           e.g. ["normal_user", "admin"]
  │     │
  │     └── getPermissionList(loginId)
  │         └── Redis.get("auth.permission.{loginId}")
  │             └── 反序列化 → stream → permissionKey 列表
  │                 e.g. ["subject:add", "subject:delete"]
  │
  ├── ③ Sa-Token 鉴权比对
  │     └── 比对接口所需权限 vs 用户拥有权限
  │
  └── ④ 放行 or 拒绝 (401)
```

### 3.2 StpInterfaceImpl 核心逻辑

```java
private List<String> getAuth(String loginId, String prefix) {
    String authKey = redisUtil.buildKey(prefix, loginId);
    String authValue = redisUtil.get(authKey);

    if (StringUtils.isBlank(authValue)) {
        return Collections.emptyList();  // 无缓存 → 空权限
    }

    List<String> authList = new LinkedList<>();

    if (authRolePrefix.equals(prefix)) {
        // 角色：提取 roleKey
        List<AuthRole> roleList = new Gson().fromJson(authValue,
            new TypeToken<List<AuthRole>>() {}.getType());
        authList = roleList.stream()
            .map(AuthRole::getRoleKey).collect(Collectors.toList());

    } else if (authPermissionPrefix.equals(prefix)) {
        // 权限：提取 permissionKey
        List<AuthPermission> permissionList = new Gson().fromJson(authValue,
            new TypeToken<List<AuthPermission>>() {}.getType());
        authList = permissionList.stream()
            .map(AuthPermission::getPermissionKey).collect(Collectors.toList());
    }
    return authList;
}
```

**为什么需要 TypeToken？** 因为 `Gson.fromJson(json, List.class)` 会丢失泛型信息，反序列化出来的是 `List<LinkedHashMap>` 而非 `List<AuthRole>`。使用 `TypeToken<List<AuthRole>>()  {}.getType()` 可以保留完整泛型类型，确保反序列化出正确的 Java 对象。

---

## 四、Redis 数据结构

| Key 格式 | Value | 写入时机 | 读取时机 |
|----------|-------|---------|---------|
| `auth.role.{userName}` | `[AuthRole JSON数组]` | 用户注册 | 网关每次鉴权 |
| `auth.permission.{userName}` | `[AuthPermission JSON数组]` | 用户注册 | 网关每次鉴权 |

示例：
```
Key: "auth.role.zhangsan"
Val: [{"id":1,"roleName":"普通用户","roleKey":"normal_user",...}]

Key: "auth.permission.zhangsan"
Val: [
  {"id":1,"name":"新增题目","permissionKey":"subject:add",...},
  {"id":2,"name":"删除题目","permissionKey":"subject:delete",...}
]
```

---

## 五、为什么用 Redis 而不是直接查 MySQL？

| 对比 | 网关直查 MySQL | 网关读 Redis |
|------|---------------|-------------|
| **速度** | ~10-50ms/次 (含网络/连接池/查询) | ~1ms/次 (内存操作) |
| **压力** | 每次请求都查库，高并发下 DB 压力大 | DB 只在注册时写，网关无 DB 压力 |
| **耦合** | 网关需要依赖 auth 的 DAO/Service | 网关只依赖 Redis，与 auth 模块解耦 |
| **可用性** | MySQL 宕机则网关完全不可用 | Redis 宕机可用集群/哨兵保障 |

**核心思路：** 将"鉴权数据"从"每次查询"变为"写时缓存"，网关鉴权变成纯 Redis 读操作，性能提升 10-50 倍。

---

## 六、网关模块文件结构

```
xyclub-gateway/
├── auth/
│   ├── SaTokenConfigure.java        # Sa-Token 路由拦截配置
│   └── StpInterfaceImpl.java        # 角色/权限获取（从 Redis 读取）
├── entity/
│   ├── AuthRole.java                # 角色实体（用于 Redis JSON 反序列化）
│   ├── AuthPermission.java          # 权限实体（用于 Redis JSON 反序列化）
│   └── Result.java                  # 统一响应
├── enums/
│   └── ResultCodeEnum.java          # 响应码枚举
├── exception/
│   └── GatewayExceptionHandler.java # 全局异常处理 (SaToken 401 / 500)
├── redis/
│   ├── RedisConfig.java             # Redis 序列化配置
│   └── RedisUtil.java               # Redis 工具类
└── GatewayApplication.java          # 启动类
```
