package com.xyclub.practice.api.enums;

/**
 * 练习套题生成类型。
 */
public enum SetTypeEnum {

    /** 实时生成 */
    REAL(1, "实时生成"),

    /** 预设套题 */
    PRESET(2, "预设套题");

    private final int code;
    private final String desc;

    SetTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取套题类型编码。
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取套题类型描述。
     */
    public String getDesc() {
        return desc;
    }
}
