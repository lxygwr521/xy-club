# 题目数据结构关系说明

## 整体关系

当前设计是“题目主表 + 按题型拆分的明细表”。

`SubjectInfo` / `subject_info` 保存所有题型共有的信息，比如题目名称、难度、类型、分数、解析、删除标记等。具体答案和选项不放在主表里，而是根据 `subjectType` 分流到不同题型表。

题型枚举：

```text
1 RADIO    单选
2 MULTIPLE 多选
3 JUDGE    判断
4 BRIEF    简答
```

## SubjectInfo

`SubjectInfoBO` 是领域层的题目聚合对象，包含两类数据。

公共题目信息：

```text
id
subjectName
subjectDifficult
settleName
subjectType
subjectScore
subjectParse
```

业务扩展信息：

```text
subjectAnswer       简答题答案
optionList          单选/多选/判断的选项或答案
categoryIds         新增时绑定的分类
labelIds            新增时绑定的标签
categoryId/labelId  查询列表时筛选用
labelName           查询详情返回用
```

`SubjectInfo` 是基础设施层实体，对应数据库 `subject_info` 表。它只存公共字段，不存具体选项。

新增题目时的主流程：

1. `SubjectInfoBO` 转成 `SubjectInfo`。
2. 插入 `subject_info` 主表。
3. 回填主键到 `subjectInfoBO.id`。
4. 根据 `subjectType` 找到对应的题型 handler。
5. handler 把具体题型数据插入题型表。
6. 插入 `subject_mapping` 绑定分类和标签。

## 具体题型表

具体题型表都通过 `subjectId` 关联 `subject_info.id`。

### 单选题 SubjectRadio

```text
id
subjectId       关联 subject_info.id
optionType      选项标识，例如 A/B/C/D 对应的数字
optionContent   选项内容
isCorrect       是否正确
isDeleted
```

一道单选题在 `subject_radio` 中有多条选项记录，但通常只有一条 `isCorrect = 1`。

### 多选题 SubjectMultiple

```text
id
subjectId
optionType
optionContent
isCorrect
isDeleted
```

一道多选题在 `subject_multiple` 中有多条选项记录，可以有多条 `isCorrect = 1`。

### 判断题 SubjectJudge

```text
id
subjectId
isCorrect
isDeleted
```

一道判断题通常对应 `subject_judge` 一条记录，只需要保存正确/错误，不需要选项内容。

### 简答题 SubjectBrief

```text
id
subjectId
subjectAnswer
isDeleted
```

一道简答题通常对应 `subject_brief` 一条记录，答案存在 `subjectAnswer`。

## SubjectAnswerBO 和 SubjectOptionBO

这里有两个 BO 容易混淆。

`SubjectAnswerBO` 表示“一个选项/一个答案项”：

```text
optionType
optionContent
isCorrect
```

它主要用于单选、多选、判断新增时的 `optionList`。例如一道单选题可能传：

```json
[
  { "optionType": 1, "optionContent": "A选项", "isCorrect": 1 },
  { "optionType": 2, "optionContent": "B选项", "isCorrect": 0 }
]
```

`SubjectOptionBO` 是“题目选项查询结果包装对象”：

```text
subjectAnswer
optionList
```

它不是数据库表。它的作用是查询题目详情时，把不同题型的答案结构统一包装回来：

- 简答题：用 `subjectAnswer`
- 单选/多选：用 `optionList`
- 判断题：理论上可用 `optionList` 或一个只含 `isCorrect` 的结构承载

查询详情时，会先查 `subject_info`，再根据 `subjectType` 调 handler 的 `query`，返回 `SubjectOptionBO` 后和 `SubjectInfo` 合并。

## 关系总结

```text
subject_info.id
    -> subject_radio.subject_id     一对多
    -> subject_multiple.subject_id  一对多
    -> subject_judge.subject_id     一对一
    -> subject_brief.subject_id     一对一
    -> subject_mapping.subject_id   一对多，绑定分类和标签
```

一句话总结：`SubjectInfo` 是题目主信息；`SubjectRadio`、`SubjectMultiple`、`SubjectJudge`、`SubjectBrief` 是按题型拆出来的答案/选项明细；`SubjectAnswerBO` 是单个选项；`SubjectOptionBO` 是查询详情时统一承载“简答答案或选项列表”的包装对象。
