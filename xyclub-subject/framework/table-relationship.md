# xyclub-subject 表结构与关系梳理

本文档根据 `xyclub-subject/xyclub-infra/src/main/java/.../entity` 实体类、`mapper/*.xml` 和领域服务调用关系整理。

## 总览

`xyclub-subject` 当前围绕题目管理设计了 8 张核心表：

| 表名 | 实体 | 作用 |
| --- | --- | --- |
| `subject_category` | `SubjectCategory` | 题目分类表，支持一级分类和二级分类 |
| `subject_label` | `SubjectLabel` | 标签表，标签归属于分类 |
| `subject_info` | `SubjectInfo` | 题目主表，保存题干、难度、类型、分值、解析等 |
| `subject_mapping` | `SubjectMapping` | 题目、分类、标签三者的关联表 |
| `subject_radio` | `SubjectRadio` | 单选题选项与答案表 |
| `subject_multiple` | `SubjectMultiple` | 多选题选项与答案表 |
| `subject_judge` | `SubjectJudge` | 判断题答案表 |
| `subject_brief` | `SubjectBrief` | 简答题答案表 |

通用字段约定：

| 字段 | 含义 |
| --- | --- |
| `id` | 主键 |
| `created_by` | 创建人 |
| `created_time` | 创建时间 |
| `update_by` | 更新人 |
| `update_time` | 更新时间 |
| `is_deleted` | 逻辑删除标识，`0` 表示未删除，`1` 表示已删除 |

## 核心关系

```text
subject_category
  ├─ parent_id -> subject_category.id
  └─ id -> subject_label.category_id

subject_info
  ├─ id -> subject_mapping.subject_id
  ├─ id -> subject_radio.subject_id      subject_type = 1
  ├─ id -> subject_multiple.subject_id   subject_type = 2
  ├─ id -> subject_judge.subject_id      subject_type = 3
  └─ id -> subject_brief.subject_id      subject_type = 4

subject_mapping
  ├─ subject_id -> subject_info.id
  ├─ category_id -> subject_category.id
  └─ label_id -> subject_label.id
```

说明：

- `subject_category` 是自关联结构，通过 `parent_id` 表达一级分类和二级分类。
- `subject_label.category_id` 表示标签所属分类。当前领域逻辑中，查询一级分类下标签时会直接按 `subject_label.category_id` 查询。
- `subject_mapping` 用于题目列表、题目详情等场景中，把题目与分类、标签关联起来。
- 题目答案详情按 `subject_info.subject_type` 分散到不同题型表中。

## 枚举值

### 分类类型 `subject_category.category_type`

来自 `CategoryTypeEnum`：

| 值 | 含义 |
| --- | --- |
| `1` | 一级分类 |
| `2` | 二级分类 |

### 题目类型 `subject_info.subject_type`

来自 `SubjectInfoTypeEnum`：

| 值 | 含义 | 对应答案表 |
| --- | --- | --- |
| `1` | 单选题 | `subject_radio` |
| `2` | 多选题 | `subject_multiple` |
| `3` | 判断题 | `subject_judge` |
| `4` | 简答题 | `subject_brief` |

### 逻辑删除 `is_deleted`

来自 `IsDeletedFlagEnum`：

| 值 | 含义 |
| --- | --- |
| `0` | 未删除 |
| `1` | 已删除 |

## 表结构

### subject_category

题目分类表。支持一级分类和二级分类。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `category_name` | `categoryName` | `String` | 分类名称 |
| `category_type` | `categoryType` | `Integer` | 分类类型，`1` 一级分类，`2` 二级分类 |
| `image_url` | `imageUrl` | `String` | 分类图标或图片地址 |
| `parent_id` | `parentId` | `Long` | 父级分类 ID，指向 `subject_category.id` |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_category.parent_id -> subject_category.id`
- `subject_label.category_id -> subject_category.id`
- `subject_mapping.category_id -> subject_category.id`

### subject_label

题目标签表。标签可以挂在分类下，也可以通过 `subject_mapping` 与题目关联。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `label_name` | `labelName` | `String` | 标签名称 |
| `category_id` | `categoryId` | `Long` | 标签所属分类 ID |
| `sort_num` | `sortNum` | `Integer` | 排序值 |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_label.category_id -> subject_category.id`
- `subject_mapping.label_id -> subject_label.id`

领域逻辑：

- 查询一级分类标签时，使用 `subject_label.category_id = categoryId`。
- 查询二级分类标签时，先查 `subject_mapping.category_id = categoryId`，再批量查 `subject_label.id in (...)`。

### subject_info

题目主表。保存题目的通用信息，不保存具体答案选项。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `subject_name` | `subjectName` | `String` | 题目名称或题干 |
| `subject_difficult` | `subjectDifficult` | `Integer` | 题目难度 |
| `settle_name` | `settleName` | `String` | 出题人名称 |
| `subject_type` | `subjectType` | `Integer` | 题目类型，决定答案表 |
| `subject_score` | `subjectScore` | `Integer` | 题目分数 |
| `subject_parse` | `subjectParse` | `String` | 题目解析 |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_mapping.subject_id -> subject_info.id`
- `subject_radio.subject_id -> subject_info.id`
- `subject_multiple.subject_id -> subject_info.id`
- `subject_judge.subject_id -> subject_info.id`
- `subject_brief.subject_id -> subject_info.id`

### subject_mapping

题目、分类、标签关联表。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `subject_id` | `subjectId` | `Long` | 题目 ID |
| `category_id` | `categoryId` | `Long` | 分类 ID |
| `label_id` | `labelId` | `Long` | 标签 ID |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_mapping.subject_id -> subject_info.id`
- `subject_mapping.category_id -> subject_category.id`
- `subject_mapping.label_id -> subject_label.id`

使用场景：

- 新增题目时，领域服务会按传入的 `categoryIds` 和 `labelIds` 组合批量写入 `subject_mapping`。
- 分页查询题目时，`SubjectInfoDao.xml` 通过 `subject_info a inner join subject_mapping b on a.id = b.subject_id` 按分类和标签过滤。
- 查询题目详情时，先根据 `subject_id` 查询 mapping，再批量查询标签名称。

### subject_radio

单选题选项表。一个单选题通常对应多条选项记录，其中一条 `is_correct = 1`。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `subject_id` | `subjectId` | `Long` | 题目 ID |
| `option_type` | `optionType` | `Integer` | 选项类型，例如 A/B/C/D 的编码 |
| `option_content` | `optionContent` | `String` | 选项内容 |
| `is_correct` | `isCorrect` | `Integer` | 是否正确 |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_radio.subject_id -> subject_info.id`
- 仅当 `subject_info.subject_type = 1` 时使用。

### subject_multiple

多选题选项表。一个多选题通常对应多条选项记录，允许多条 `is_correct = 1`。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `subject_id` | `subjectId` | `Long` | 题目 ID |
| `option_type` | `optionType` | `Long` | 选项类型，例如 A/B/C/D 的编码 |
| `option_content` | `optionContent` | `String` | 选项内容 |
| `is_correct` | `isCorrect` | `Integer` | 是否正确 |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_multiple.subject_id -> subject_info.id`
- 仅当 `subject_info.subject_type = 2` 时使用。

### subject_judge

判断题答案表。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `subject_id` | `subjectId` | `Long` | 题目 ID |
| `is_correct` | `isCorrect` | `Integer` | 判断题答案是否正确 |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_judge.subject_id -> subject_info.id`
- 仅当 `subject_info.subject_type = 3` 时使用。

### subject_brief

简答题答案表。

| 字段 | Java 属性 | 类型线索 | 含义 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | 主键 |
| `subject_id` | `subjectId` | `Integer` | 题目 ID |
| `subject_answer` | `subjectAnswer` | `String` | 简答题参考答案 |
| `created_by` | `createdBy` | `String` | 创建人 |
| `created_time` | `createdTime` | `Date` | 创建时间 |
| `update_by` | `updateBy` | `String` | 更新人 |
| `update_time` | `updateTime` | `Date` | 更新时间 |
| `is_deleted` | `isDeleted` | `Integer` | 逻辑删除标识 |

关系：

- `subject_brief.subject_id -> subject_info.id`
- 仅当 `subject_info.subject_type = 4` 时使用。

## 业务写入关系

新增题目时，`SubjectInfoDomainServiceImpl.add` 的写入顺序是：

1. 写入 `subject_info`，得到题目主键 `subject_info.id`。
2. 根据 `subject_info.subject_type` 选择题型处理器：
   - `1` 写 `subject_radio`
   - `2` 写 `subject_multiple`
   - `3` 写 `subject_judge`
   - `4` 写 `subject_brief`
3. 根据传入的 `categoryIds` 和 `labelIds` 组合写入 `subject_mapping`。

## 查询关系

### 分页查询题目

`SubjectInfoDao.queryPage` 通过 `subject_info` 和 `subject_mapping` 联表：

```sql
from subject_info a
inner join subject_mapping b on a.id = b.subject_id
where b.category_id = #{categoryId}
  and b.label_id = #{labelId}
  and a.is_deleted = 0
  and b.is_deleted = 0
```

支持继续按题目难度 `subject_difficult` 和题目类型 `subject_type` 过滤。

### 查询题目详情

题目详情查询链路：

1. 根据 `subject_info.id` 查询题目主表。
2. 根据 `subject_info.subject_type` 选择对应题型处理器，查询题目答案或选项表。
3. 根据 `subject_mapping.subject_id` 查询题目关联的标签 ID。
4. 根据 `subject_label.id in (...)` 批量查询标签名称。

### 查询分类下标签

标签查询链路：

- 如果分类是一级分类，直接查询：

```text
subject_label.category_id = categoryId
```

- 如果分类是二级分类，先查关联关系：

```text
subject_mapping.category_id = categoryId
```

再根据 `label_id` 批量查询 `subject_label`。

## 关系注意点

- 代码中没有显式数据库外键约束定义，关系主要由业务代码和 mapper SQL 维护。
- 多数表使用 `is_deleted` 做逻辑删除，但 mapper 中仍保留 `deleteById` 物理删除方法。
- `subject_brief.subject_id` 在实体中是 `Integer`，其他答案表是 `Long`；如果后续统一表结构或做外键约束，建议统一为 `Long`。
- `subject_mapping` 是题目筛选的核心表，题目列表按分类、标签过滤都依赖它。
- `subject_label.category_id` 用于标签归属分类；`subject_mapping.category_id + label_id` 用于具体题目在某分类下绑定哪些标签。
