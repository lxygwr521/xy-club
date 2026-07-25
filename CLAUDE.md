# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build a single module (from its directory)
cd xyclub-auth && mvn clean package -DskipTests

# Build from root (not recommended; modules are independent)
mvn clean package -DskipTests

# Run locally
java -jar xyclub-auth/xyclub-auth-starter/target/xyclub-auth-starter.jar
java -jar xyclub-gateway/target/xyclub-gateway.jar
java -jar xyclub-subject/xyclub-starter/target/xyclub-starter.jar
java -jar xyclub-oss/target/xyclub-oss.jar
java -jar xyclub-wx/target/xyclub-wx.jar
```

There is no root parent POM — each top-level module (`xyclub-auth`, `xyclub-subject`, `xyclub-gateway`, `xyclub-oss`, `xyclub-wx`) is independently buildable with its own dependency management.

## Project Structure

```
xy-club/
├── xyclub-gateway/        # Spring Cloud Gateway (port 5000)
├── xyclub-auth/           # Auth service (port 3011) — DDD 4-layer
│   ├── xyclub-auth-common/          # Shared enums, Result entity
│   ├── xyclub-auth-domain/          # Domain layer (core business logic)
│   ├── xyclub-auth-infra/           # Infra layer (MyBatis, Druid)
│   ├── xyclub-auth-application/     # Controller layer (DTO, REST)
│   └── xyclub-auth-starter/         # Spring Boot starter
├── xyclub-subject/        # Subject/exam service (port 3000) — DDD 4-layer
├── xyclub-oss/            # File storage service (port 4000) — MinIO/Aliyun OSS
└── xyclub-wx/             # WeChat official account service (port 3012)
```

**Tech stack:** Spring Boot 2.4.2, Spring Cloud 2020.0.6, Spring Cloud Alibaba 2021.1, Sa-Token 1.37.0, MyBatis-Plus 3.4.0, MapStruct 1.4.2, Nacos, Redis, MySQL, Java 8.

## Architecture: DDD 4-Layer

Every auth/subject module follows this pattern:

```
Controller (application-controller) → Domain → Infra → Common
     DTO  ──MapStruct──→  BO  ──MapStruct──→  Entity  ──MyBatis──→  DB
```

| Layer | Module | Object | Responsibility |
|-------|--------|--------|----------------|
| Controller | `*-application-controller` | DTO | Validate, convert, delegate. Never write business logic. |
| Domain | `*-domain` | BO | All business rules. Depends on Infra interfaces (not impls). |
| Infra | `*-infra` | Entity | DB access, cache, external APIs. Never make business decisions. |
| Common | `*-common` | Enums, Result | Shared across layers. |

Each table creates ~12 files: DTO + BO + Entity + 2 Converters + Controller + DomainService/Impl + Service/Impl + DAO + XML.

## Gateway Routing

The gateway (`xyclub-gateway`, port 5000) uses `StripPrefix=1` for all routes:

```yaml
spring.cloud.gateway.routes:
  - id: auth     Path=/auth/**      StripPrefix=1 → forwards to lb://xyclub-auth
  - id: subject  Path=/subject/**   StripPrefix=1 → forwards to lb://xyclub-subject
  - id: oss      Path=/oss/**       StripPrefix=1 → forwards to lb://xyclub-oss
```

**Critical rule:** Because the gateway strips the prefix, controllers must NOT repeat it in `@RequestMapping`. For example, a request `GET /subject/category/query` reaches the subject service as `GET /category/query`, so the controller should map `/category`, not `/subject/category`. The wx service is NOT routed through the gateway (WeChat calls it directly at `http://IP:3012/callback`).

## Sa-Token Auth Flow

```
Login:  POST /auth/user/doLogin?validCode=537 → StpUtil.login() → token returned
Request: Header: satoken: xyclub_xxxxx → Gateway SaReactorFilter → checkLogin()
         → StpInterfaceImpl reads Redis auth.role.{loginId} / auth.permission.{loginId}
         → permission check → allow/deny
```

Sa-Token config: `token-name: satoken`, `token-prefix: xyclub`. The token in the HTTP header must be `satoken: xyclub <tokenValue>`.

Gateway protected routes (in `SaTokenConfigure.java`): `/oss/**` requires login, `/subject/**` requires login, `/subject/subject/add` requires `subject:add` permission. `/auth/**` is NOT protected (commented out to allow login).

## Redis Data

| Key | Value | Writer | Reader | TTL |
|-----|-------|--------|--------|-----|
| `auth.role.{userName}` | `[{roleKey, roleName}]` JSON | auth.register() | gateway.StpInterfaceImpl | permanent |
| `auth.permission.{userName}` | `[{permissionKey}]` JSON | auth.register() | gateway.StpInterfaceImpl | permanent |
| `loginCode.{num}` | OpenID | wx.ReceiveTextMsgHandler | auth.doLogin() | 5 min |
| `Authorization:login:token:xyclub_xxx` | session data | Sa-Token (StpUtil.login) | SaTokenReactorFilter | 30 days |

## Key Gotchas

1. **BOM in dependencies, not dependencyManagement**: Parent POMs must use `<dependencyManagement><dependencies>` for BOM imports. Putting a BOM import directly under `<dependencies>` does nothing.

2. **Spring Cloud Alibaba 2021.1 with Spring Boot 2.4.2**: This is a version mismatch (2021.1 targets Spring Boot 2.6.x). It works but can cause dependency convergence issues. Always verify `spring-boot-starter-data-redis` version is compatible.

3. **MySQL `localhost` in application.yml**: Both auth and subject applications have `jdbc:mysql://localhost:3306/xy-club`. On cloud servers without local MySQL, the service will fail to start. Change to the actual MySQL IP.

4. **`spring-cloud-starter-bootstrap` needs explicit version**: Version `3.0.6` matches Spring Cloud 2020.0.6. Without it, Maven may fail to resolve.

5. **`spring-boot-maven-plugin` required for fat JAR**: Every starter module must have this plugin with `<goal>repackage</goal>` or `java -jar` fails with ClassNotFoundException.

6. **WeChat callback goes direct, not through gateway**: The wx service on port 3012 must be publicly accessible for WeChat server callbacks. No gateway route for `/wx/**`.

## JCClub Reference

This project is derived from `jc-club` at `../jc-club`. When syncing commits from jcclub to xyclub, note these package/class name differences:

- jcclub: `com.jingdianjichi.*` → xyclub: `com.xyclub.*`
- jcclub: `com.jingdianjichi.subject.infra.basic.mapper.*Dao` → xyclub: `com.xyclub.subject.infra.basic.dao.*Dao` (subject module uses `dao` package)
- jcclub: `com.jingdianjichi.auth.infra.basic.mapper.*Dao` → xyclub: `com.xyclub.auth.infra.basic.mapper.*Dao` (auth module uses `mapper` package)
- Module prefix: `jc-club-*` → `xyclub-*`
- Additional sync changes needed: gateway `StripPrefix` handling, controller `@RequestMapping` path adjustments
