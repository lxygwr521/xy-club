package com.xyclub.auth.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息BO
 *
 * @author lxy
 * @date 2026-07-20
 */
@Data
public class AuthUserBO implements Serializable {

    private Long id;

    private String userName;

    private String nickName;

    private String email;

    private String phone;

    private String password;

    private Integer sex;

    private String avatar;

    private Integer status;

    private String introduce;

    private String extJson;

}
