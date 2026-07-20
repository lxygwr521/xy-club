package com.xyclub.auth.common.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author lxy
 * @date 2026-07-20
 */
@Getter
public enum AuthUserStatusEnum {

    OPEN(0, "启用"),
    CLOSE(1, "禁用");

    public int code;

    public String desc;

    AuthUserStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AuthUserStatusEnum getByCode(int codeVal) {
        for (AuthUserStatusEnum resultCodeEnum : AuthUserStatusEnum.values()) {
            if (resultCodeEnum.code == codeVal) {
                return resultCodeEnum;
            }
        }
        return null;
    }

}
