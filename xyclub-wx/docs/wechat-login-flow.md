# 微信验证码登录链路

## 一、整体流程

```
                            ┌───────────┐
                            │  微信服务器 │
                            └─────┬─────┘
                                  │ POST XML 消息
                                  ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                           xyclub-wx (3012)                              │
│                                                                         │
│  ① 用户发送"验证码"                                                      │
│     ReceiveTextMsgHandler.dealMsg()                                     │
│       ├── 生成随机数 num (0-999)                                        │
│       ├── Redis: loginCode.{num} = {OpenID}  过期5分钟                   │
│       └── 回复 XML: "您当前的验证码是：537！5分钟内有效"                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
                                  │
                                  │ 用户看到验证码 537
                                  ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                           xyclub-auth (3011)                            │
│                                                                         │
│  ② 前端调用 POST /user/doLogin?validCode=537                            │
│     UserController.doLogin()                                            │
│       └── AuthUserDomainServiceImpl.doLogin("537")                      │
│             ├── Redis.get("loginCode.537") → "oXYZ789"                  │
│             ├── register(oXYZ789)                                       │
│             │     ├── 首次: INSERT 新用户, userName=oXYZ789, 无密码      │
│             │     └── 再次: INSERT 失败(重复), 不影响后续流程             │
│             └── StpUtil.login(oXYZ789) → 生成 token                     │
│                                                                         │
│  ③ 返回 token 给前端                                                    │
│     └── 后续请求: Header 携带 token → Gateway Sa-Token 校验              │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 二、Redis 数据结构

### 验证码存储（临时）

| Key | Value | TTL | 写入方 | 读取方 |
|-----|-------|-----|--------|--------|
| `loginCode.537` | `oXYZ789` | 5 分钟 | ReceiveTextMsgHandler | AuthUserDomainServiceImpl |

**写入：** 用户在公众号发送"验证码"时
```java
// ReceiveTextMsgHandler.dealMsg()
String numKey = redisUtil.buildKey("loginCode", "537");
redisUtil.setNx(numKey, "oXYZ789", 5L, TimeUnit.MINUTES);
```

**读取：** 前端调用 `/user/doLogin?validCode=537` 时
```java
// AuthUserDomainServiceImpl.doLogin()
String loginKey = redisUtil.buildKey("loginCode", "537");
String openId = redisUtil.get(loginKey);  // → "oXYZ789"
```

### 鉴权数据存储（持久）

| Key | Value | 写入方 | 读取方 |
|-----|-------|--------|--------|
| `auth.role.oXYZ789` | `[AuthRole JSON]` | AuthUserDomainServiceImpl.register() | StpInterfaceImpl.getRoleList() |
| `auth.permission.oXYZ789` | `[AuthPermission JSON]` | AuthUserDomainServiceImpl.register() | StpInterfaceImpl.getPermissionList() |
| `token:login:session:{token}` | 用户会话 | Sa-Token 框架（登录时） | Gateway 过滤器（每次请求） |

---

## 三、代码调用链

```
微信用户 → 公众号
  │
  ├── 发送"验证码"
  │     └── CallBackController (POST /callback)
  │           └── MessageUtil.parseXml(requestBody)
  │                 └── Map{"MsgType":"text", "Content":"验证码",
  │                         "FromUserName":"oXYZ789", "ToUserName":"gh_abc"}
  │           └── WxChatMsgFactory.getHandlerByMsgType("text")
  │                 └── ReceiveTextMsgHandler.dealMsg()
  │                       ├── Redis.set("loginCode.537", "oXYZ789")
  │                       └── return XML → 用户收到"537"
  │
  ├── 前端调用 POST /user/doLogin?validCode=537
  │     └── UserController.doLogin("537")
  │           └── AuthUserDomainServiceImpl.doLogin("537")
  │                 ├── Redis.get("loginCode.537") → "oXYZ789"
  │                 ├── register(oXYZ789) → MySQL insert + Redis 写角色权限
  │                 └── StpUtil.login(oXYZ789)
  │                       └── Sa-Token 生成 token → Redis + 返回前端
  │
  └── 后续请求携带 token
        └── Gateway → Sa-Token 校验 token
                    → StpInterfaceImpl 从 Redis 读取角色/权限
                    → 鉴权比对 → 放行/拒绝
```

---

## 四、关键设计决策

| 决策 | 原因 |
|------|------|
| 验证码存 value 为 OpenID 而非 "1" | `doLogin` 需要反查是哪个用户，存 OpenID 才能定位到人 |
| 验证码 TTL 5 分钟 | 防止暴力破解，过期后自动失效 |
| 登录即注册 | 微信用户没有注册表单，以 OpenID 为 userName 自动创建账户 |
| 密码为空时跳过加密 | 微信登录无密码，只有邮箱注册才有密码 |
| 重复注册不报错 | insert 失败仅 count=0，不影响后续 StpUtil.login |
