package com.xyclub.subject.common.enums;

import lombok.Getter;

/**
 * 题目点赞状态。
 */
@Getter
public enum SubjectLikedStatusEnum {

    LIKED(1, "点赞"),
    UN_LIKED(0, "取消点赞");

    public int code;

    public String desc;

    SubjectLikedStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举。
     */
    public static SubjectLikedStatusEnum getByCode(int codeVal) {
        for (SubjectLikedStatusEnum statusEnum : SubjectLikedStatusEnum.values()) {
            if (statusEnum.code == codeVal) {
                return statusEnum;
            }
        }
        return null;
    }

}
