package com.xyclub.practice.api.enums;

import lombok.Getter;

/**
 * 题目类型枚举
 * 1单选 2多选 3判断 4简答
 * @author: ChickenWing
 * @date: 2023/10/3
 */
@Getter
public enum SubjectInfoTypeEnum {

    /**
     * 单选
     */
    RADIO(1,"单选"),
    /**
     * 多选
     */
    MULTIPLE(2,"多选"),
    /**
     * 判断
     */
    JUDGE(3,"判断"),
    /**
     * 简答
     */
    BRIEF(4,"简答"),
    ;

    /**
     * 题目类型编码
     */
    public int code;

    /**
     * 题目类型描述
     */
    public String desc;

    SubjectInfoTypeEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取对应的题目类型枚举
     *
     * @param codeVal 类型编码
     * @return 类型枚举，未匹配到返回 null
     */
    public static SubjectInfoTypeEnum getByCode(int codeVal){
        for(SubjectInfoTypeEnum resultCodeEnum : SubjectInfoTypeEnum.values()){
            if(resultCodeEnum.code == codeVal){
                return resultCodeEnum;
            }
        }
        return null;
    }

}
