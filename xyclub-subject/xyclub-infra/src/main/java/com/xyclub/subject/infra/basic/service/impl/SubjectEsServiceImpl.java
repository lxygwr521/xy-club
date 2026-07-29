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

        SearchHits searchHits = searchResponse.getHits();
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

    private SubjectInfoEs convertResult(SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        if (CollectionUtils.isEmpty(sourceAsMap)) {
            return null;
        }
        SubjectInfoEs result = new SubjectInfoEs();
        result.setSubjectId(MapUtils.getLong(sourceAsMap, EsSubjectFields.SUBJECT_ID));
        result.setSubjectName(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_NAME));
        result.setSubjectAnswer(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_ANSWER));
        result.setDocId(MapUtils.getLong(sourceAsMap, EsSubjectFields.DOC_ID));
        result.setSubjectType(MapUtils.getInteger(sourceAsMap, EsSubjectFields.SUBJECT_TYPE));
        result.setScore(new BigDecimal(String.valueOf(hit.getScore())).multiply(new BigDecimal("100.00")
                .setScale(2, RoundingMode.HALF_UP)));

        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        // 如果 ES 返回高亮片段，优先用高亮内容覆盖原字段。
        HighlightField subjectNameField = highlightFields.get(EsSubjectFields.SUBJECT_NAME);
        if (Objects.nonNull(subjectNameField)) {
            Text[] fragments = subjectNameField.getFragments();
            StringBuilder subjectNameBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectNameBuilder.append(fragment);
            }
            result.setSubjectName(subjectNameBuilder.toString());
        }

        HighlightField subjectAnswerField = highlightFields.get(EsSubjectFields.SUBJECT_ANSWER);
        if (Objects.nonNull(subjectAnswerField)) {
            Text[] fragments = subjectAnswerField.getFragments();
            StringBuilder subjectAnswerBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectAnswerBuilder.append(fragment);
            }
            result.setSubjectAnswer(subjectAnswerBuilder.toString());
        }

        return result;
    }

    private EsSearchRequest createSearchListQuery(SubjectInfoEs req) {
        EsSearchRequest esSearchRequest = new EsSearchRequest();
        BoolQueryBuilder bq = new BoolQueryBuilder();
        // 题目名称匹配权重更高，命中名称的结果会优先展示。
        MatchQueryBuilder subjectNameQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_NAME, req.getKeyWord());

        bq.should(subjectNameQueryBuilder);
        subjectNameQueryBuilder.boost(2);

        MatchQueryBuilder subjectAnswerQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_ANSWER, req.getKeyWord());
        bq.should(subjectAnswerQueryBuilder);

        MatchQueryBuilder subjectTypeQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_TYPE, SubjectInfoTypeEnum.BRIEF.getCode());
        bq.must(subjectTypeQueryBuilder);
        // 名称和答案至少命中一个，同时必须满足题目类型过滤。
        bq.minimumShouldMatch(1);

        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags("<span style = \"color:red\">");
        highlightBuilder.postTags("</span>");

        esSearchRequest.setBq(bq);
        esSearchRequest.setHighlightBuilder(highlightBuilder);
        esSearchRequest.setFields(EsSubjectFields.FIELD_QUERY);
        esSearchRequest.setFrom((req.getPageNo() - 1) * req.getPageSize());
        esSearchRequest.setSize(req.getPageSize());
        esSearchRequest.setNeedScroll(false);
        return esSearchRequest;
    }

    private EsIndexInfo getEsIndexInfo() {
        EsIndexInfo esIndexInfo = new EsIndexInfo();
        esIndexInfo.setClusterName("73438a827b55");
        esIndexInfo.setIndexName("subject_index");
        return esIndexInfo;
    }
}
