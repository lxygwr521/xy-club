package com.xyclub.subject.infra.basic.service.impl;

import com.xyclub.subject.common.entity.PageResult;
import com.xyclub.subject.common.enums.SubjectInfoTypeEnum;
import com.xyclub.subject.infra.basic.entity.EsSubjectFields;
import com.xyclub.subject.infra.basic.entity.SubjectInfoEs;
import com.xyclub.subject.infra.basic.es.EsIndexInfo;
import com.xyclub.subject.infra.basic.es.EsRestClient;
import com.xyclub.subject.infra.basic.es.EsSearchRequest;
import com.xyclub.subject.infra.basic.es.EsSourceData;
import com.xyclub.subject.infra.basic.service.SubjectEsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class SubjectEsServiceImpl implements SubjectEsService {

    @Override
    public boolean insert(SubjectInfoEs subjectInfoEs) {
        EsSourceData esSourceData = new EsSourceData();
        Map<String, Object> data = convert2EsSourceData(subjectInfoEs);
        esSourceData.setDocId(subjectInfoEs.getDocId().toString());
        esSourceData.setData(data);
        return EsRestClient.insertDoc(getEsIndexInfo(), esSourceData);
    }

    private Map<String, Object> convert2EsSourceData(SubjectInfoEs subjectInfoEs) {
        Map<String, Object> data = new HashMap<>();
        data.put(EsSubjectFields.SUBJECT_ID, subjectInfoEs.getSubjectId());
        data.put(EsSubjectFields.DOC_ID, subjectInfoEs.getDocId());
        data.put(EsSubjectFields.SUBJECT_NAME, subjectInfoEs.getSubjectName());
        data.put(EsSubjectFields.SUBJECT_ANSWER, subjectInfoEs.getSubjectAnswer());
        data.put(EsSubjectFields.SUBJECT_TYPE, subjectInfoEs.getSubjectType());
        data.put(EsSubjectFields.CREATE_USER, subjectInfoEs.getCreateUser());
        data.put(EsSubjectFields.CREATE_TIME, subjectInfoEs.getCreateTime());
        return data;
    }

    @Override
    public PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs req) {
        PageResult<SubjectInfoEs> pageResult = new PageResult<>();
        // 将业务查询条件转换成 ES 查询请求，统一复用 EsRestClient 执行。
        EsSearchRequest esSearchRequest = createSearchListQuery(req);
        SearchResponse searchResponse = EsRestClient.searchWithTermQuery(getEsIndexInfo(), esSearchRequest);

        List<SubjectInfoEs> subjectInfoEsList = new LinkedList<>();
        if (searchResponse == null || searchResponse.getHits() == null
                || searchResponse.getHits().getHits() == null) {
            pageResult.setPageNo(req.getPageNo());
            pageResult.setPageSize(req.getPageSize());
            pageResult.setRecords(subjectInfoEsList);
            pageResult.setTotal(0);
            return pageResult;
        }
//        从完整响应中取出 命中文档集合（包含总数、得分、文档列表等）
        SearchHits searchHits = searchResponse.getHits();
//        从集合中取出 文档数组（每个元素是一条具体的文档）
        SearchHit[] hits = searchHits.getHits();

        for (SearchHit hit : hits) {
            SubjectInfoEs subjectInfoEs = convertResult(hit);
            if (Objects.nonNull(subjectInfoEs)) {
                subjectInfoEsList.add(subjectInfoEs);
            }
        }
        pageResult.setPageNo(req.getPageNo());
        pageResult.setPageSize(req.getPageSize());
        pageResult.setRecords(subjectInfoEsList);
        pageResult.setTotal(Long.valueOf(searchHits.getTotalHits().value).intValue());
        return pageResult;
    }

    /**
     * 将 Elasticsearch 的命中文档（SearchHit）转换为业务实体对象（SubjectInfoEs）
     *
     * 核心功能：
     * 1. 从 SearchHit 中提取 _source 字段数据，映射到实体对象
     * 2. 将 ES 返回的文档得分（_score）转换为百分制（0-100分），保留2位小数
     * 3. 如果存在高亮片段（highlight），用高亮内容覆盖原字段值（用于前端展示）
     *
     * @param hit Elasticsearch 返回的单条命中文档
     * @return 转换后的业务实体，如果源数据为空则返回 null
     */
    private SubjectInfoEs convertResult(SearchHit hit) {
        // 1. 获取文档的原始数据（_source 字段），以 Map 形式返回
        //    key 为字段名，value 为字段值（类型为 Object，需自行转型）
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();

        // 2. 如果源数据为空，直接返回 null，避免后续空指针异常
        if (CollectionUtils.isEmpty(sourceAsMap)) {
            return null;
        }

        // 3. 创建业务实体对象，准备填充数据
        SubjectInfoEs result = new SubjectInfoEs();

        // 4. 从 Map 中提取各字段值，并设置到实体对象中
        //    使用 MapUtils 工具类安全获取，避免字段缺失时抛出异常
        result.setSubjectId(MapUtils.getLong(sourceAsMap, EsSubjectFields.SUBJECT_ID));
        result.setSubjectName(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_NAME));
        result.setSubjectAnswer(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_ANSWER));
        result.setDocId(MapUtils.getLong(sourceAsMap, EsSubjectFields.DOC_ID));
        result.setSubjectType(MapUtils.getInteger(sourceAsMap, EsSubjectFields.SUBJECT_TYPE));

        // 5. 处理文档得分（相关性评分）
        //    ES 返回的 _score 通常是小数（如 0.85），乘以 100 转换为百分制（如 85.00）
        //    setScale(2, RoundingMode.HALF_UP)：保留2位小数，四舍五入
        result.setScore(new BigDecimal(String.valueOf(hit.getScore()))
                .multiply(new BigDecimal("100.00"))
                .setScale(2, RoundingMode.HALF_UP));

        // 6. 获取 ES 返回的高亮片段（highlight）
        //    当搜索关键词匹配到字段内容时，ES 会将匹配部分用 <em> 标签包裹返回
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();

        // 7. 处理题目名称（subjectName）的高亮
        //    如果该字段有高亮片段，则用高亮内容覆盖原始值，用于前端展示带高亮效果的标题
        HighlightField subjectNameField = highlightFields.get(EsSubjectFields.SUBJECT_NAME);
        if (Objects.nonNull(subjectNameField)) {
            Text[] fragments = subjectNameField.getFragments();      // 高亮片段可能被拆分成多个 Text
            StringBuilder subjectNameBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectNameBuilder.append(fragment);                 // 拼接所有高亮片段
            }
            result.setSubjectName(subjectNameBuilder.toString());    // 覆盖原值
        }

        // 8. 处理题目答案（subjectAnswer）的高亮（逻辑同上）
        HighlightField subjectAnswerField = highlightFields.get(EsSubjectFields.SUBJECT_ANSWER);
        if (Objects.nonNull(subjectAnswerField)) {
            Text[] fragments = subjectAnswerField.getFragments();
            StringBuilder subjectAnswerBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectAnswerBuilder.append(fragment);
            }
            result.setSubjectAnswer(subjectAnswerBuilder.toString());
        }

        // 9. 返回完整的业务实体对象
        return result;
    }

    /**
     * 创建 ES 搜索请求对象（构建查询 DSL）
     *
     * 核心功能：
     * 1. 构建多字段匹配查询：题目名称（权重高） + 题目答案（权重正常）
     * 2. 强制过滤题目类型（只查简答题）
     * 3. 设置高亮样式，匹配关键词用红色显示
     * 4. 支持分页查询
     *
     * @param req 前端传入的搜索请求参数（关键词、题目类型、分页信息等）
     * @return 封装好的 ES 搜索请求对象，供后续执行搜索使用
     */
    private EsSearchRequest createSearchListQuery(SubjectInfoEs req) {
        // 1. 创建 ES 搜索请求对象（自定义封装，包含查询条件、高亮、分页等）
        EsSearchRequest esSearchRequest = new EsSearchRequest();

        // 2. 创建 Bool 查询构建器（用于组合多个查询条件）
        //    BoolQuery 包含：must（必须满足）、should（应该满足）、mustNot（必须不满足）、filter（过滤）
        BoolQueryBuilder bq = new BoolQueryBuilder();

        // ==================== 3. 构建 should 条件（至少满足一个） ====================

        // 3.1 题目名称匹配查询（权重 boost = 2，提升其重要性）
        //     当关键词命中题目名称时，该文档得分会更高，排名更靠前
        MatchQueryBuilder subjectNameQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_NAME, req.getKeyWord());
        subjectNameQueryBuilder.boost(2);              // 权重翻倍，优先展示命中名称的结果
        bq.should(subjectNameQueryBuilder);            // 添加到 should 子句

        // 3.2 题目答案匹配查询（权重保持默认 1）
        MatchQueryBuilder subjectAnswerQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_ANSWER, req.getKeyWord());
        bq.should(subjectAnswerQueryBuilder);          // 添加到 should 子句

        // ==================== 4. 构建 must 条件（必须满足） ====================

        // 4.1 题目类型过滤（只查询指定类型的题目）
        //     注意：这里硬编码了 BRIEF（简答题）类型，说明该方法专门用于简答题搜索
        MatchQueryBuilder subjectTypeQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_TYPE, SubjectInfoTypeEnum.BRIEF.getCode());
        bq.must(subjectTypeQueryBuilder);              // 必须满足：题目类型 = BRIEF

        // 5. 设置 minimumShouldMatch = 1
        //    表示 should 子句中至少需要匹配 1 个条件（即：题目名称或题目答案至少命中一个）
        //    避免出现既不匹配名称、也不匹配答案的无关文档
        bq.minimumShouldMatch(1);

        // ==================== 6. 构建高亮（Highlight）配置 ====================

        // 6.1 创建高亮构建器，对所有字段（*）开启高亮
        //     requireFieldMatch = false 表示即使查询条件没有指定该字段，也返回高亮片段
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("*")
                .requireFieldMatch(false);

        // 6.2 设置高亮标签（前端渲染时，匹配关键词会显示为红色）
        //     注意：这里使用了内联样式，实际生产建议使用 CSS class（如 class="highlight"）
        highlightBuilder.preTags("<span style = \"color:red\">");
        highlightBuilder.postTags("</span>");

        // 7. 组装 esSearchRequest 对象，并设置各项参数
        esSearchRequest.setBq(bq);                     // 设置 Bool 查询
        esSearchRequest.setHighlightBuilder(highlightBuilder);  // 设置高亮配置
        esSearchRequest.setFields(EsSubjectFields.FIELD_QUERY);  // 指定返回哪些字段（减少数据传输）
        esSearchRequest.setFrom((req.getPageNo() - 1) * req.getPageSize());  // 分页起始位置
        esSearchRequest.setSize(req.getPageSize());    // 每页大小
        esSearchRequest.setNeedScroll(false);          // 不需要游标分页（适用于普通分页场景）

        // 8. 返回构建好的搜索请求对象
        return esSearchRequest;
    }

    private EsIndexInfo getEsIndexInfo() {
        EsIndexInfo esIndexInfo = new EsIndexInfo();
        esIndexInfo.setClusterName("73438a827b55");
        esIndexInfo.setIndexName("subject_index");
        return esIndexInfo;
    }
}
