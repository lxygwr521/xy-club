# Repository Guidelines

## 项目结构与模块组织

这是一个 Java 8 多模块 Maven 服务，根目录 `pom.xml` 聚合题目领域相关模块：

- `xyclub-common`：公共实体、枚举、统一返回对象，以及 Lombok、MapStruct 等通用依赖。
- `xyclub-domain`：领域对象、转换器和领域服务实现。
- `xyclub-infra`：持久化实体、MyBatis-Plus DAO、基础服务、工具类，以及 `src/main/resources/mapper` 下的 XML 映射文件。
- `xyclub-application`：应用层适配模块，其中 `xyclub-application-controller` 放置 REST Controller、DTO 和 DTO 转换器；`job`、`mq` 模块用于后续任务和消息相关逻辑。
- `xyclub-starter`：Spring Boot 启动入口和运行配置，配置文件位于 `src/main/resources/application.yml`。
- `xyclub-subject-api`：API 相关模块和共享依赖管理占位。

Java 包名保持在 `com.xyclub.subject` 下。不要手动修改 `target/` 目录中的编译或生成产物。

## 构建、测试与本地运行

- `mvn clean package`：清理并构建所有模块，同时执行 Maven 测试阶段。
- `mvn test`：运行整个 reactor 中的测试。
- `mvn -pl xyclub-starter -am spring-boot:run`：启动服务及其依赖模块，默认端口为 `3000`。
- `mvn -pl xyclub-infra -am package`：仅构建基础设施模块及其依赖，适合修改持久化代码时使用。

除非处理特定模块任务，命令应从仓库根目录执行。

## 编码风格与命名约定

使用 4 空格缩进，并保持 Java 8 兼容。遵循现有分层命名：Controller 入参和出参使用 `DTO`，领域对象使用 `BO`，MyBatis 接口使用 `Dao`，服务接口和实现使用 `Service`、`ServiceImpl`。优先沿用已有 Lombok 注解，如 `@Data`、`@Getter`、`@Slf4j`。对象转换使用 MapStruct 接口，并保留 `INSTANCE` 访问方式。

## 测试指南

当前 POM 中尚未提交测试框架依赖，且只有空的 `xyclub-domain/src/test` 目录。新增业务行为时，应在对应模块的 `src/test/java` 下补充测试。测试类按被测对象命名，例如 `SubjectCategoryDomainServiceImplTest`。如需 Spring Boot 集成测试，请先在对应模块添加必要的 Maven 测试依赖。

## 提交与 Pull Request 规范

当前 Git 历史使用较短提交信息，例如 `init`、`Initial commit`。后续提交建议使用简洁的祈使句摘要，例如 `add category query validation`。提交 PR 时说明变更目的、影响模块、本地执行过的命令；如有任务编号请关联；涉及 API 变更时附上示例请求和响应。

## 安全与配置提示

`application.yml` 中的数据源、Druid 登录信息和加密密钥属于环境相关配置。不要提交真实生产密钥；优先通过本地覆盖配置或部署环境变量注入。
