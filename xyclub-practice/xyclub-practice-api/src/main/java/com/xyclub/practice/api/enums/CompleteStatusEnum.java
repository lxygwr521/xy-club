package com.xyclub.practice.api.enums;

/**
 * 练习完成状态枚举
 */
public enum CompleteStatusEnum {

    /**
     * 未完成
     */
    NO_COMPLETE(0, "未完成"),

    /**
     * 已完成
     */
    COMPLETE(1, "已完成");

    final private int code;
    final private String desc;

    CompleteStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取状态编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取状态描述
     */
    public String getDesc() {
        return desc;
    }
}
