# Elasticsearch 全文检索功能分析

---

## 一、功能概述

题目全文检索功能允许用户通过关键词搜索题目名称和题目答案。底层基于 Elasticsearch 7.5.2，使用 `RestHighLevelClient` 进行索引和搜索，分词器采用 IK Analyzer (`ik_smart`)，搜索结果支持高亮显示和相关性评分。

核心流程：
- **写入**：新增题目时，同步将题目信息写入 ES 索引
- **检索**：通过 Bool Query 组合关键词匹配、类型过滤、高亮和分页

---

## 二、涉及文件

| 文件 | 层级 | 职责 |
|------|------|------|
| `xyclub-subject/.../application/controller/SubjectController.java` | Controller | 暴露 `POST /subject/getSubjectPageBySearch` 接口 |
| `xyclub-subject/.../domain/service/SubjectInfoDomainService.java` | Domain 接口 | 定义 `getSubjectPageBySearch()` 方法 |
| `xyclub-subject/.../domain/service/impl/SubjectInfoDomainServiceImpl.java` | Domain 实现 | 组装搜索条件，调用 infra 层 ES 查询 |
| `xyclub-subject/.../common/util/IdWorkerUtil.java` | Common 工具 | 雪花算法生成全局唯一 ID，作为 ES 文档 `doc_id` |
| `xyclub-subject/.../infra/basic/service/SubjectEsService.java` | Infra 接口 | ES 索引和检索服务接口 |
| `xyclub-subject/.../infra/basic/service/impl/SubjectEsServiceImpl.java` | **Infra 核心实现** | 构建 Bool Query、处理高亮、解析搜索结果 |
| `xyclub-subject/.../infra/basic/es/EsRestClient.java` | **ES 客户端封装** | 封装 RestHighLevelClient，提供通用的 CRUD 与搜索方法 |
| `xyclub-subject/.../infra/basic/es/EsConfigProperties.java` | ES 配置 | `@ConfigurationProperties(prefix = "es.cluster")` 读取集群配置 |
| `xyclub-subject/.../infra/basic/es/EsClusterConfig.java` | ES 配置 POJO | 单个集群配置：`name`（集群名）、`nodes`（节点列表） |
| `xyclub-subject/.../infra/basic/es/EsIndexInfo.java` | ES 索引信息 | `clusterName` + `indexName`，标识目标集群和索引 |
| `xyclub-subject/.../infra/basic/es/EsSourceData.java` | ES 数据载体 | `docId` + `data`（Map），用于传输待索引的文档数据 |
| `xyclub-subject/.../infra/basic/es/EsSearchRequest.java` | ES 搜索请求 | 封装 BoolQueryBuilder、字段、分页、排序、高亮、滚动参数 |
| `xyclub-subject/.../infra/basic/entity/SubjectInfoEs.java` | ES 文档实体 | 映射 ES 索引中的字段（subjectId, subjectName, subjectAnswer 等） |
| `xyclub-subject/.../infra/basic/entity/EsSubjectFields.java` | ES 字段常量 | 定义所有 ES 字段名常量和查询字段数组 |
| `xyclub-subject/.../common/enums/SubjectInfoTypeEnum.java` | 枚举 | `BRIEF(4, "简答")` — ES 搜索固定只检索简答类型题目 |
| `xyclub-subject/.../common/entity/PageResult.java` | 分页实体 | 封装分页查询结果（records, total, pageNo, pageSize） |
| `xyclub-subject/.../common/entity/PageInfo.java` | 分页基类 | 请求分页参数（pageNo 默认 1，pageSize 默认 20） |

### Maven 依赖（xyclub-infra/pom.xml）

```xml
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>7.5.2</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-client</artifactId>
    <version>7.5.2</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.5.2</version>
</dependency>
```

### ES 配置（application.yml）

```yaml
es:
  cluster:
    esConfigs:
      - name: 3b0d8be29ddf        # 集群名称（逻辑标识）
        nodes: 182.92.176.188:9200  # ES 节点 IP:端口
```

配置由 `EsConfigProperties` 通过 `@ConfigurationProperties(prefix = "es.cluster")` 自动绑定，`EsRestClient` 在 `@PostConstruct` 阶段初始化 `RestHighLevelClient` 并缓存到 `CLIENT_MAP`。

---

## 三、数据存储

### 3.1 ES 索引

| 属性 | 值 | 说明 |
|------|-----|------|
| **集群名称** | `3b0d8be29ddf` | application.yml 中配置的逻辑名 |
| **索引名称** | `subject_index` | 硬编码在 `SubjectEsServiceImpl.getEsIndexInfo()` 中 |
| **集群名称常量** | `"73438a827b55"` | ⚠️ 硬编码在 `SubjectEsServiceImpl.getEsIndexInfo()` 中，与 yml 配置不一致 — 实际运行时 yml 中配置的 `3b0d8be29ddf` 被初始化到 `CLIENT_MAP` 的 key，但代码中 `getEsIndexInfo()` 的 `clusterName` 却用了另一个值，**存在取不到对应 client 的 bug** |
| **ES 版本** | 7.5.2 | RestHighLevelClient |
| **文档 ID** | 雪花算法生成的 Long 值 | 写入前通过 `IdWorkerUtil(1,1,1).nextId()` 生成 |
| **分词器** | `ik_smart` | IK Analyzer 粗粒度分词，用于中文搜索分析 |

### 3.2 索引字段映射

ES 文档包含以下字段（由 `EsSubjectFields` 定义常量，`SubjectInfoEs` 定义实体）：

| 字段名 | Java 类型 | 说明 | 可检索 |
|--------|-----------|------|--------|
| `doc_id` | Long | 文档唯一 ID（雪花算法生成） | ✅（字段过滤返回） |
| `subject_id` | Long | 关联 MySQL `subject_info` 表主键 | ✅ |
| `subject_name` | String | 题目名称 | ✅（全文检索，权重 boost=2） |
| `subject_answer` | String | 题目答案 | ✅（全文检索，权重 boost=1） |
| `subject_type` | Integer | 题目类型（1=单选,2=多选,3=判断,4=简答） | ✅（must 过滤条件） |
| `create_user` | String | 创建人 | ✅（字段过滤返回） |
| `create_time` | Long | 创建时间戳（毫秒） | ✅（字段过滤返回） |

**查询返回字段**（`FIELD_QUERY` 数组）：
```java
{ SUBJECT_ID, SUBJECT_NAME, SUBJECT_ANSWER, SUBJECT_TYPE, DOC_ID, CREATE_USER, CREATE_TIME }
```

在 ES 查询时通过 `searchSourceBuilder.fetchSource(fields, null)` 只返回这 7 个字段，减少网络传输开销。

### 3.3 ES 与 MySQL 的数据关系

```
MySQL (subject_info 表)                    Elasticsearch (subject_index)
┌──────────────────────┐                  ┌──────────────────────────┐
│ id (PK)              │  ←── subject_id  │ subject_id               │
│ subject_name         │  ←── 字段映射 ─→  │ subject_name              │
│ subject_type         │                  │ subject_type              │
│ ...                  │                  │ subject_answer  (综合题解)│
└──────────────────────┘                  │ doc_id (雪花ID, PK)       │
                                          │ create_user              │
                                          │ create_time (时间戳)      │
                                          └──────────────────────────┘
```

---

## 四、工作流程

### 4.1 写入 ES（新增题目时同步触发）

```
POST /subject/add (SubjectController.add)
  │
  └─ SubjectInfoDomainServiceImpl.add(subjectInfoBO)
       │
       ├─ 1. 写入 MySQL subject_info 表
       ├─ 2. 写入 subject_mapping（分类-标签关联）
       ├─ 3. SubjectTypeHandler 写入题目详情表（根据题目类型路由）
       │
       └─ 4. ★ 写入 Elasticsearch
            │
            ├─ 生成文档 ID: docId = new IdWorkerUtil(1,1,1).nextId()
            │    // Snowflake: workerId=1, datacenterId=1, sequence=1
            │
            ├─ 构建文档数据: SubjectInfoEs {
            │    docId, subjectId, subjectName, subjectAnswer,
            │    subjectType, createUser, createTime
            │  }
            │
            └─ subjectEsService.insert(subjectInfoEs)
                 └─ EsRestClient.insertDoc(esIndexInfo, esSourceData)
                      └─ client.index(indexRequest)  // PUT /subject_index/_doc/{docId}
```

**关键代码** (`SubjectInfoDomainServiceImpl.java:103-111`):
```java
SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
subjectInfoEs.setDocId(new IdWorkerUtil(1, 1, 1).nextId());
subjectInfoEs.setSubjectId(subjectInfo.getId());
subjectInfoEs.setSubjectAnswer(subjectInfoBO.getSubjectAnswer());
subjectInfoEs.setCreateTime(new Date().getTime());
subjectInfoEs.setCreateUser("鸡翅");
subjectInfoEs.setSubjectName(subjectInfo.getSubjectName());
subjectInfoEs.setSubjectType(subjectInfo.getSubjectType());
subjectEsService.insert(subjectInfoEs);
```

> ⚠️ **注意**：`createUser` 硬编码为 `"鸡翅"` 而非实际登录用户，这是一个待修复的问题。

`SubjectEsServiceImpl.insert()` 将 `SubjectInfoEs` 转换为 `Map<String, Object>`：
```java
Map<String, Object> data = new HashMap<>();
data.put(EsSubjectFields.SUBJECT_ID, subjectInfoEs.getSubjectId());
data.put(EsSubjectFields.DOC_ID, subjectInfoEs.getDocId());
data.put(EsSubjectFields.SUBJECT_NAME, subjectInfoEs.getSubjectName());
// ... 其他字段
```

`EsRestClient.insertDoc()` 执行底层 ES 索引请求：
```java
IndexRequest indexRequest = new IndexRequest(esIndexInfo.getIndexName());
indexRequest.source(esSourceData.getData());
indexRequest.id(esSourceData.getDocId());
getClient(esIndexInfo.getClusterName()).index(indexRequest, COMMON_OPTIONS);
```

> ⚠️ **潜在 bug**：`insertDoc` 用 `esIndexInfo.getClusterName()` 从 `CLIENT_MAP` 取 client。`SubjectEsServiceImpl.getEsIndexInfo()` 写死了 `clusterName = "73438a827b55"`，但 yml 中配置的集群名是 `"3b0d8be29ddf"`，这会导致 `getClient()` 返回 `null`，引发 NPE。实际部署时大概率已修改代码中的硬编码值。

### 4.2 全文检索

```
POST /subject/getSubjectPageBySearch (SubjectController.getSubjectPageBySearch)
  │
  ├─ 校验: keyWord ≠ null/blank
  ├─ DTO → BO 转换
  ├─ 设置分页参数
  │
  └─ SubjectInfoDomainServiceImpl.getSubjectPageBySearch(subjectInfoBO)
       │
       └─ subjectEsService.querySubjectList(subjectInfoEs)
            │
            ├─ 1. 构建 Bool Query (createSearchListQuery)
            │    ┌─────────────────────────────────────────────┐
            │    │ BoolQueryBuilder                           │
            │    │   ├─ should: match(SUBJECT_NAME, keyword)  │
            │    │   │           .boost(2)  ← 名称权重 2 倍    │
            │    │   ├─ should: match(SUBJECT_ANSWER, keyword)│
            │    │   │           .boost(1)  ← 答案权重 1 倍    │
            │    │   ├─ must: match(SUBJECT_TYPE, 4)          │
            │    │   │          ← 只检索简答题                  │
            │    │   └─ minimumShouldMatch: 1                 │
            │    └─────────────────────────────────────────────┘
            │
            ├─ 2. 构建高亮
            │    HighlightBuilder
            │      ├─ field("*"), requireFieldMatch(false)
            │      ├─ preTags:  "<span style = \"color:red\">"
            │      └─ postTags: "</span>"
            │
            ├─ 3. 构建搜索请求 (EsSearchRequest)
            │    ├─ bq (Bool Query)
            │    ├─ fields: FIELD_QUERY (7 个字段)
            │    ├─ from: (pageNo - 1) * pageSize
            │    ├─ size: pageSize
            │    ├─ highlightBuilder
            │    └─ needScroll: false
            │
            ├─ 4. 执行搜索
            │    EsRestClient.searchWithTermQuery(indexInfo, searchRequest)
            │    └─ RestHighLevelClient.search(searchRequest)
            │
            └─ 5. 解析结果 convertResult(SearchHit)
                 ├─ 提取 sourceAsMap → SubjectInfoEs
                 ├─ 相关性得分转换: score = hitScore × 100 (保留2位小数)
                 ├─ 高亮片段覆盖原字段: subjectName、subjectAnswer
                 └─ 组装 PageResult<SubjectInfoEs>
```

**Bool Query 构建详情** (`SubjectEsServiceImpl.createSearchListQuery()`):

```java
BoolQueryBuilder bq = new BoolQueryBuilder();

// SHOULD 子句（或关系）—— 名称和答案至少命中一个
MatchQueryBuilder subjectNameQueryBuilder =
    QueryBuilders.matchQuery(SUBJECT_NAME, req.getKeyWord());
bq.should(subjectNameQueryBuilder);
subjectNameQueryBuilder.boost(2);   // 名称权重 2 倍，命中名称的文档排序更靠前

MatchQueryBuilder subjectAnswerQueryBuilder =
    QueryBuilders.matchQuery(SUBJECT_ANSWER, req.getKeyWord());
bq.should(subjectAnswerQueryBuilder);
// 默认 boost = 1

// MUST 子句（且关系）—— 固定只检索简答题
MatchQueryBuilder subjectTypeQueryBuilder =
    QueryBuilders.matchQuery(SUBJECT_TYPE, SubjectInfoTypeEnum.BRIEF.getCode());  // 4
bq.must(subjectTypeQueryBuilder);

// 至少需要一个 SHOULD 子句命中
bq.minimumShouldMatch(1);
```

**查询逻辑语义**：

> `(subject_name 匹配 "关键词" 权重×2) OR (subject_answer 匹配 "关键词" 权重×1) AND subject_type == 4`

即：在所有**简答题**中，搜索题目名称或答案包含关键词的文档，名称匹配的排序更靠前。

### 4.3 搜索结果的评分处理

ES 返回的原始 `_score` 是 0~1 区间的浮点数，代码将其放大便于前端展示：

```java
result.setScore(
    new BigDecimal(String.valueOf(hit.getScore()))
        .multiply(new BigDecimal("100.00"))
        .setScale(2, RoundingMode.HALF_UP)
);
```

例如：ES 返回 `_score = 0.8542` → 最终 score = `85.42`。

### 4.4 高亮处理

搜索结果中，匹配的关键词会被包裹红色标签：

```
原始文本:   "Java 是一种面向对象的编程语言"
高亮结果:   "Java 是一种<span style = "color:red">面向对象</span>的编程语言"
```

实现方式：
- `HighlightBuilder.field("*")` 对所有字段生效
- `requireFieldMatch(false)` 即使查询只匹配了某个字段，其他字段的高亮也会生效
- 如果 `subjectName` 有高亮片段，用高亮片段**覆盖**原字段值
- 如果 `subjectAnswer` 有高亮片段，同样用高亮片段覆盖

```java
Map<String, HighlightField> highlightFields = hit.getHighlightFields();
HighlightField subjectNameField = highlightFields.get(SUBJECT_NAME);
if (Objects.nonNull(subjectNameField)) {
    // 拼接高亮片段，覆盖原始 subjectName
    result.setSubjectName(fragments → StringBuilder → toString());
}
// subjectAnswer 同理
```

---

## 五、EsRestClient 架构分析

### 5.1 多集群设计

`EsRestClient` 支持连接多个 ES 集群，通过 `static Map<String, RestHighLevelClient> CLIENT_MAP` 按集群名称缓存客户端：

```java
@PostConstruct
public void initialize() {
    List<EsClusterConfig> esConfigs = esConfigProperties.getEsConfigs();
    for (EsClusterConfig esConfig : esConfigs) {
        RestHighLevelClient client = initRestClient(esConfig);
        CLIENT_MAP.put(esConfig.getName(), client);
    }
}
```

当前配置只有单集群单节点，但架构上预留了多集群扩展能力。

### 5.2 提供的能力矩阵

| 方法 | 对应 ES API | 用途 |
|------|-------------|------|
| `insertDoc()` | `IndexRequest` | 新增/覆盖指定 ID 的文档 |
| `batchInsertDoc()` | `BulkRequest` + `IndexRequest` | 批量新增文档 |
| `updateDoc()` | `UpdateRequest.doc()` | 按文档 ID 局部更新字段 |
| `batchUpdateDoc()` | `BulkRequest` + `UpdateRequest` | 批量局部更新，自动跳过无 docId 的数据 |
| `deleteDoc()` | `DeleteRequest` | 按文档 ID 删除单条文档 |
| `delete()` | `DeleteByQueryRequest` + `matchAll` | 删除索引下的所有文档 |
| `updateByQuery()` | `UpdateByQueryRequest` | 按条件批量更新（支持 Script） |
| `isExistDocById()` | `GetRequest.exists()` | 判断指定文档是否存在 |
| `getDocById()` | `GetRequest` | 按文档 ID 查完整 source，支持字段过滤 |
| `searchWithTermQuery()` | `SearchRequest` + `SearchSourceBuilder` | **核心搜索方法**：Bool Query + 字段过滤 + 分页 + 高亮 + 排序 + Scroll |
| `getAnalyze()` | `LowLevelClient.performRequest("GET", "_analyze")` | 调用 `_analyze` API，用 `ik_smart` 分词器对文本分词 |

### 5.3 searchWithTermQuery 执行流程

```java
public static SearchResponse searchWithTermQuery(EsIndexInfo esIndexInfo, EsSearchRequest req) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(bq);
    searchSourceBuilder.fetchSource(fields, null);     // 只返回指定字段
    searchSourceBuilder.from(from).size(size);          // 分页

    if (highlightBuilder != null) {
        searchSourceBuilder.highlighter(highlightBuilder); // 高亮
    }
    if (StringUtils.isNotBlank(sortName)) {
        searchSourceBuilder.sort(sortName, sortOrder);     // 自定义排序
    }
    searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC)); // 按相关性降序

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(esIndexInfo.getIndexName());
    searchRequest.source(searchSourceBuilder);

    if (Boolean.TRUE.equals(needScroll)) {
        searchRequest.scroll(new Scroll(TimeValue.timeValueMinutes(minutes)));
    }

    return getClient(esIndexInfo.getClusterName()).search(searchRequest, COMMON_OPTIONS);
}
```

排序优先级：自定义排序（`sortName`） > 相关性评分（`_score` 降序）。

---

## 六、分词器

系统使用 **IK Analyzer** 中文分词器，在 `getAnalyze()` 方法中通过 Low Level Client 调用 ES 的 `_analyze` API：

```java
public static List<String> getAnalyze(EsIndexInfo esIndexInfo, String text) throws Exception {
    Request request = new Request("GET", "_analyze");
    JSONObject entity = new JSONObject();
    entity.put("analyzer", "ik_smart");   // IK 粗粒度分词
    entity.put("text", text);
    request.setJsonEntity(entity.toJSONString());
    Response response = getClient(...).getLowLevelClient().performRequest(request);
    // 解析 tokens 数组，提取每个 token
}
```

`ik_smart` 是 IK Analyzer 的粗粒度分词模式，会将文本做最粗粒度的切分，占用资源较少。与之对应的 `ik_max_word` 则会做最细粒度的切分。

分词示例：
```
输入: "中华人民共和国"
ik_smart:  [中华人民共和国]
ik_max_word: [中华人民共和国, 中华人民, 中华, 华人, 人民共和国, 人民, 共和, 国]
```

> ⚠️ **注意**：`getAnalyze()` 方法当前仅在 `EsRestClient` 中定义，项目中**没有任何代码调用它**。这是一个预留的调试/管理工具类方法。

---

## 七、ID 生成策略

ES 文档的 `_id` 使用**雪花算法（Snowflake）**生成，实现类为 `IdWorkerUtil`：

```java
public IdWorkerUtil(long workerId, long datacenterId, long sequence) { ... }
```

当前调用方式：
```java
new IdWorkerUtil(1, 1, 1).nextId()
// workerId=1, datacenterId=1, sequence=1
```

参数含义：
- `workerId` (5 bits)：机器 ID，同机房内唯一
- `datacenterId` (5 bits)：数据中心 ID
- `sequence` (12 bits)：每毫秒内的序列号

总计 64 bits = 1 (未使用) + 41 (时间戳偏移) + 5 (datacenterId) + 5 (workerId) + 12 (sequence)。

> ⚠️ **注意**：每次调用 `new IdWorkerUtil(1,1,1)` 都创建新实例，构造函数中 `sequence` 初始化为传入的参数值（1），这可能导致同一毫秒内重复 ID。建议将 `IdWorkerUtil` 实例化为单例 Bean。

---

## 八、完整数据流图

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           写入流程（新增题目时触发）                         │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  POST /subject/add                                                       │
│      │                                                                   │
│      ▼                                                                   │
│  SubjectController.add()                                                 │
│      │                                                                   │
│      ▼                                                                   │
│  SubjectInfoDomainServiceImpl.add()                                      │
│      │                                                                   │
│      ├────────────────┬────────────────────┬─────────────────┐           │
│      ▼                ▼                    ▼                 ▼           │
│  ┌─────────┐   ┌────────────┐    ┌──────────────────┐  ┌─────────────┐  │
│  │  MySQL   │   │ Subject    │    │  IdWorkerUtil    │  │  Redis ZSet │  │
│  │subject_* │   │Mapping     │    │  .nextId()       │  │ subject_rank│  │
│  │  表      │   │关联表       │    │  → docId         │  │ +1 贡献分   │  │
│  └─────────┘   └────────────┘    └────────┬─────────┘  └─────────────┘  │
│                                           │                              │
│                                           ▼                              │
│                               ┌───────────────────────┐                  │
│                               │ SubjectEsServiceImpl  │                  │
│                               │      .insert()        │                  │
│                               └───────────┬───────────┘                  │
│                                           │                              │
│                                           ▼                              │
│                               ┌───────────────────────┐                  │
│                               │     EsRestClient      │                  │
│                               │    .insertDoc()       │                  │
│                               │  IndexRequest         │                  │
│                               └───────────┬───────────┘                  │
│                                           │                              │
│                                           ▼                              │
│                               ┌───────────────────────┐                  │
│                               │   Elasticsearch       │                  │
│                               │   PUT /subject_index/ │                  │
│                               │        _doc/{docId}   │                  │
│                               └───────────────────────┘                  │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                           检索流程（用户搜索时触发）                         │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  POST /subject/getSubjectPageBySearch                                    │
│  请求体: { "keyWord": "面向对象", "pageNo": 1, "pageSize": 20 }           │
│      │                                                                   │
│      ▼                                                                   │
│  SubjectController.getSubjectPageBySearch()                              │
│      │                                                                   │
│      ▼                                                                   │
│  SubjectInfoDomainServiceImpl.getSubjectPageBySearch()                   │
│      │                                                                   │
│      ▼                                                                   │
│  SubjectEsServiceImpl.querySubjectList()                                 │
│      │                                                                   │
│      ├─ createSearchListQuery()                                          │
│      │   ┌────────────────────────────────────────┐                      │
│      │   │ BoolQueryBuilder                       │                      │
│      │   │   should: subjectName match "面向对象"   │ boost=2             │
│      │   │   should: subjectAnswer match "面向对象" │ boost=1             │
│      │   │   must: subjectType == 4 (简答)         │                      │
│      │   │   minimumShouldMatch: 1                │                      │
│      │   └────────────────────────────────────────┘                      │
│      │                                                                   │
│      ├─ HighlightBuilder                                                 │
│      │   preTag="<span style=\"color:red\">"                             │
│      │   postTag="</span>"                                               │
│      │                                                                   │
│      └─ EsRestClient.searchWithTermQuery()                              │
│           │                                                              │
│           ▼                                                              │
│      ┌────────────────────────────────────────────┐                      │
│      │  Elasticsearch                             │                      │
│      │  POST /subject_index/_search               │                      │
│      │  {                                         │                      │
│      │    "query": { "bool": { ... } },           │                      │
│      │    "highlight": { ... },                   │                      │
│      │    "from": 0, "size": 20,                  │                      │
│      │    "_source": ["subject_id", "subject_name",│                     │
│      │       "subject_answer", ...]                │                      │
│      │  }                                         │                      │
│      └──────────────────┬─────────────────────────┘                      │
│                         │                                                │
│                         ▼                                                │
│      ┌────────────────────────────────────────────┐                      │
│      │  ES 响应解析                               │                      │
│      │  ├─ hit.sourceAsMap → SubjectInfoEs        │                      │
│      │  ├─ hit.score × 100 → BigDecimal          │                      │
│      │  └─ hit.highlightFields → 覆盖原始字段     │                      │
│      └──────────────────┬─────────────────────────┘                      │
│                         │                                                │
│                         ▼                                                │
│      ┌────────────────────────────────────────────┐                      │
│      │  PageResult<SubjectInfoEs>                 │                      │
│      │  ├─ records: [高亮后的题目列表]             │                      │
│      │  ├─ total: 匹配总数                        │                      │
│      │  ├─ pageNo, pageSize                       │                      │
│      │  └─ 返回前端                                │                      │
│      └────────────────────────────────────────────┘                      │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 九、架构分层总结

```
┌──────────────────────────────────────────────────────┐
│  Controller 层 (application-controller)               │
│  SubjectController.getSubjectPageBySearch()           │
│  - 参数校验（关键词非空）                               │
│  - DTO → BO 转换                                      │
│  - 委托 Domain 层                                      │
└───────────────────────┬──────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────┐
│  Domain 层 (domain)                                   │
│  SubjectInfoDomainServiceImpl.getSubjectPageBySearch()│
│  - 组装搜索条件领域对象 SubjectInfoEs                   │
│  - 委托 Infra 层执行查询                               │
│  - 不关心 ES 底层 API 细节                             │
└───────────────────────┬──────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────┐
│  Infra 业务层 (infra/basic/service)                   │
│  SubjectEsServiceImpl                                 │
│  - 构建 BoolQueryBuilder（should/must/boost）          │
│  - 构建 HighlightBuilder（标签/字段匹配）               │
│  - 解析 SearchHit → SubjectInfoEs（含高亮覆盖）          │
│  - 评分转换（_score × 100）                            │
│  - 组装 PageResult                                     │
│  - 硬编码索引信息（clusterName, indexName）             │
└───────────────────────┬──────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────┐
│  Infra 通用层 (infra/basic/es)                        │
│  EsRestClient — 封装 RestHighLevelClient              │
│  - 多集群连接管理（CLIENT_MAP + @PostConstruct）        │
│  - 通用 CRUD：insert/update/delete/batchInsert        │
│  - 通用搜索：searchWithTermQuery（Bool + 分页 + 高亮）  │
│  - 分词工具：getAnalyze（ik_smart）                    │
│  EsSearchRequest — 搜索参数 DTO                        │
│  EsIndexInfo — 集群+索引标识                            │
│  EsSourceData — 文档数据载体                            │
│  EsConfigProperties — @ConfigurationProperties 配置绑定 │
└───────────────────────┬──────────────────────────────┘
                        │
                        ▼
              ┌──────────────────┐
              │  Elasticsearch    │
              │  RestHighLevel    │
              │  Client (7.5.2)   │
              └──────────────────┘
```

---

## 十、关键设计决策与注意事项

### 设计决策

1. **仅索引简答题**：Bool Query 中 `must(subjectType == 4)` 将检索范围限定为简答题。单选、多选、判断类型的题目不参与全文检索。这意味着搜索功能只对简答题有效。

2. **权重策略**：题目名称权重 2 倍于题目答案。用户搜索时，名称匹配的题目排序高于仅答案匹配的题目，体现了"题目名称更关键"的业务判断。

3. **同步写入而非异步**：新增题目时，ES 索引与 MySQL 写入在**同一个 `@Transactional` 方法**中执行。ES 写入失败会导致整个事务回滚，保证数据一致性，但也意味着 ES 不可用时新增题目接口会失败。

4. **RestHighLevelClient 而非 Spring Data ES**：项目选择直接封装原生 ES 客户端，而非使用 Spring Data Elasticsearch。这提供了更灵活的 API 控制，但需要自行处理连接管理、异常处理和序列化。

5. **雪花 ID 作为文档 ID**：使用 Snowflake 算法生成全局唯一的 `doc_id`，而非复用 MySQL 主键。这允许未来可能的多表聚合索引场景，但也增加了 ID 管理的复杂度。

6. **高亮全局字段匹配**：`field("*").requireFieldMatch(false)` 对所有字段开启高亮，即使查询中未明确匹配该字段。这确保用户总能看到高亮效果，但可能产生不必要的开销。

### 已知问题

| 问题 | 位置 | 说明 |
|------|------|------|
| **集群名不一致** | `SubjectEsServiceImpl.getEsIndexInfo()` vs `application.yml` | 代码中硬编码 `clusterName="73438a827b55"`，yml 中配置 `name: 3b0d8be29ddf`，导致 `getClient` 返回 null |
| **createUser 硬编码** | `SubjectInfoDomainServiceImpl.add()` | ES 文档中 `createUser` 固定为 `"鸡翅"`，未使用 `LoginUtil.getLoginId()` |
| **单例问题** | `SubjectInfoDomainServiceImpl.add()` | 每次新增题目都 `new IdWorkerUtil(1,1,1)`，sequence 每次都初始化为 1 |
| **无索引 Mapping 管理** | 全局 | 项目中未见 ES 索引 mapping 的定义或初始化代码，依赖 ES 动态 mapping |
| **无异常降级** | 整体 | ES 不可用时，写入会抛异常回滚事务，检索会抛异常返回"全文检索失败"。没有 ES 挂掉时回退到 MySQL LIKE 查询的降级方案 |
| **getAnalyze 未被调用** | `EsRestClient` | 分词方法已实现但无任何调用方，属于预留/调试代码 |

### 优化建议

1. **修复集群名不一致**：将 `getEsIndexInfo()` 中的 `clusterName` 改为从配置文件读取或与 yml 保持一致。
2. **修复 createUser**：改为 `LoginUtil.getLoginId()` 获取实际登录用户名。
3. **IdWorkerUtil 改为单例 Bean**：避免每次 new 实例导致的序列号重复风险。
4. **增加索引初始化**：在启动时通过 `CreateIndexRequest` 初始化索引 mapping，特别是为中文全文检索字段配置 `ik_max_word` 分词器和 `ik_smart` 搜索时分析器。
5. **添加 ES 健康检查**：利用 Spring Boot Actuator 或自定义 HealthIndicator 监控 ES 连接状态。
6. **考虑异步写入**：将 ES 索引操作从数据库事务中解耦（如通过消息队列/事件机制），提高写入接口的响应速度和可用性。
7. **扩展题目检索类型**：当前只检索简答题，可根据业务需要扩展为支持其他题目类型的检索，或改为可配置的类型过滤。
