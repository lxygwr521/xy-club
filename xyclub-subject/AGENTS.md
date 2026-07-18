# AGENTS.md

本文档面向在 `xyclub-subject` 模块内工作的编码助手和协作者。进入本目录或其子目录处理任务时，优先遵守本文档约定。

## 模块定位

`xyclub-subject` 是 `xy-club` 的题目领域服务，采用 Java 8 + Maven 多模块结构，主要负责题目分类、标签、题目主信息、题型答案、题目查询等能力。

根模块 `pom.xml` 聚合以下子模块：

| 模块 | 职责 |
| --- | --- |
| `xyclub-subject-api` | 对外 API 或共享契约占位模块 |
| `xyclub-common` | 通用枚举、分页对象、统一返回对象、通用依赖 |
| `xyclub-infra` | 持久化实体、DAO、基础 Service、MyBatis XML mapper |
| `xyclub-domain` | 领域 BO、领域服务、题型处理器、MapStruct 转换器 |
| `xyclub-application` | 应用层聚合模块 |
| `xyclub-application/xyclub-application-controller` | REST Controller、DTO、DTO 转换器 |
| `xyclub-application/xyclub-application-job` | 定时任务相关占位模块 |
| `xyclub-application/xyclub-application-mq` | 消息队列相关占位模块 |
| `xyclub-starter` | Spring Boot 启动入口和运行配置 |

不要手动修改 `target/` 下的编译产物、生成代码或拷贝后的配置文件，除非用户明确要求处理生成产物。

## 常用命令

在 `xyclub-subject` 目录执行：

```powershell
mvn validate
mvn compile
mvn clean package
```

常用局部构建：

```powershell
mvn -pl xyclub-infra -am compile
mvn -pl xyclub-domain -am compile
mvn -pl xyclub-application/xyclub-application-controller -am compile
mvn -pl xyclub-starter -am spring-boot:run
```

当前项目可能存在 Maven warning，例如 `xyclub-subject-api` 中 `spring-boot-dependencies` 作为普通 dependency 使用 `import` scope。这属于既有问题，除非任务明确涉及 POM 修复，否则不要顺手改动。

如果 Maven 因写入本地仓库或 `target/maven-status` 失败而报权限错误，先判断是否为本地文件权限或锁定问题，再按当前执行环境的权限规则处理。

## 分层约定

### application-controller

- Controller 只处理 HTTP 入参、出参和基础参数校验。
- 入参/出参对象使用 `DTO`，放在 `application/dto`。
- DTO 与 BO 转换使用 MapStruct converter，保持现有 `INSTANCE` 访问风格。
- Controller 不直接调用 DAO，不直接拼装复杂业务逻辑。

### domain

- 领域层使用 `BO` 表示业务对象。
- 领域服务负责业务编排，例如新增题目时：
  1. 写入 `subject_info`
  2. 根据 `subject_type` 分派到题型 handler
  3. 写入 `subject_mapping`
- 题型逻辑通过 `SubjectTypeHandler` 扩展，新增题型时优先补充 handler，而不是把题型分支散落在领域服务里。
- 对象转换使用 MapStruct，转换接口放在 `domain/convert`。

### infra

- 持久化实体放在 `infra/basic/entity`。
- DAO 接口放在 `infra/basic/dao`，XML 放在 `infra/src/main/resources/mapper`。
- DAO 命名使用 `*Dao`，Service 命名使用 `*Service` 和 `*ServiceImpl`。
- MyBatis XML 的 namespace 必须与 DAO 包名一致，例如：

```xml
<mapper namespace="com.xyclub.subject.infra.basic.dao.SubjectLabelDao">
```

- 新增实体字段时，要同步检查：
  - entity 字段
  - DAO 方法签名
  - Service 接口和实现
  - mapper resultMap
  - select/insert/update/batch SQL
  - BO/DTO/converter 是否需要同步字段

## 表结构和关系文档

题目领域表结构与关系文档位于：

- `framework/table-relationship.md`
- `framework/subject-data-structure.md`

涉及以下核心表时，应先查看这些文档：

| 表 | 说明 |
| --- | --- |
| `subject_category` | 题目分类，支持一级和二级分类 |
| `subject_label` | 标签，包含 `category_id` |
| `subject_info` | 题目主表 |
| `subject_mapping` | 题目、分类、标签关联表 |
| `subject_radio` | 单选题选项和答案 |
| `subject_multiple` | 多选题选项和答案 |
| `subject_judge` | 判断题答案 |
| `subject_brief` | 简答题答案 |

关系要点：

- `subject_category.parent_id` 指向 `subject_category.id`。
- `subject_label.category_id` 指向 `subject_category.id`。
- `subject_mapping.subject_id` 指向 `subject_info.id`。
- `subject_mapping.category_id` 指向 `subject_category.id`。
- `subject_mapping.label_id` 指向 `subject_label.id`。
- 各题型答案表通过 `subject_id` 指向 `subject_info.id`。

## 枚举约定

常用枚举在 `xyclub-common` 中：

- `IsDeletedFlagEnum`
  - `0`: 未删除
  - `1`: 已删除
- `CategoryTypeEnum`
  - `1`: 一级分类
  - `2`: 二级分类
- `SubjectInfoTypeEnum`
  - `1`: 单选
  - `2`: 多选
  - `3`: 判断
  - `4`: 简答

业务代码中优先使用枚举，不要散落魔法数字。

## 编码风格

- Java 版本保持 Java 8 兼容。
- 使用 4 个空格缩进。
- 优先沿用已有 Lombok 注解，例如 `@Data`、`@Getter`、`@Slf4j`。
- 日志使用 `Slf4j`，复杂入参可参考现有服务用 `JSON.toJSONString` 输出。
- 保持包名在 `com.xyclub.subject` 下。
- 修改中文注释或文档时使用 UTF-8 编码。

## 数据库和 SQL 注意事项

- 当前代码主要依靠业务逻辑维护表关系，不要假设数据库一定有显式外键。
- 多数表有 `is_deleted` 字段；查询业务数据时应注意过滤未删除数据。
- XML 中批量插入和 `insertOrUpdateBatch` 容易出现字段顺序不一致问题，新增字段时务必核对列名和值顺序。
- `subject_brief.subjectId` 当前实体类型是 `Integer`，其他题型表多为 `Long`。除非任务明确要求统一类型，否则不要顺手改动。

## 测试和验证

当前项目测试体系不完整，修改代码后至少执行相关模块编译：

```powershell
mvn compile
```

如只修改持久化层：

```powershell
mvn -pl xyclub-infra -am compile
```

如修改领域逻辑：

```powershell
mvn -pl xyclub-domain -am compile
```

如修改 Controller 或 DTO：

```powershell
mvn -pl xyclub-application/xyclub-application-controller -am compile
```

如果新增测试，放在对应模块的 `src/test/java` 下，测试类命名建议使用被测类名加 `Test`，例如 `SubjectLabelDomainServiceImplTest`。

## 配置和安全

- `xyclub-starter/src/main/resources/application.yml` 包含本地数据库、Druid 和加密相关配置。
- 不要提交真实生产密钥、生产数据库地址或私人凭据。
- 如需新增环境差异配置，优先使用本地覆盖、启动参数或环境变量。

## 协作注意事项

- 先读现有代码和 mapper，再动手改。
- 保持改动范围与任务相关，不做无关重构。
- 遇到已有未提交改动，不要回滚；如果影响当前任务，基于现状继续修改。
- 不要因为编译生成了 `target/` 变更就把它们当作源码改动处理。
- 如果修改了表结构相关代码，请同步更新 `framework/table-relationship.md`。
