package com.xyclub.subject.infra.basic.es;

import lombok.Data;

import java.io.Serializable;

/**
 * es集群配置
 */
@Data
public class EsClusterConfig implements Serializable {

    /**
     * 集群名称
     */
    private String name;

    /**
     * 集群节点，多个节点用逗号分隔
     */
    private String nodes;
}
