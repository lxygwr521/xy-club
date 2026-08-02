package com.xyclub.subject.infra.basic.es;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
//存放指定的文档所要更新或者插入数据的实体。
@Data
public class EsSourceData implements Serializable {

    private String docId;

    private Map<String, Object> data;
}
