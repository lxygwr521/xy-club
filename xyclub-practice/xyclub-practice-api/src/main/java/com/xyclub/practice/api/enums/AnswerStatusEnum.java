package com.xyclub.practice.api.enums;

/**
 * 题目作答状态。
 */
public enum AnswerStatusEnum {

    /** 错误 */
    ERROR(0, "错误"),

    /** 正确 */
    CORRECT(1, "正确");

    private final int code;
    private final String desc;

    AnswerStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取作答状态编码。
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取作答状态描述。
     */
    public String getDesc() {
        return desc;
    }
}
