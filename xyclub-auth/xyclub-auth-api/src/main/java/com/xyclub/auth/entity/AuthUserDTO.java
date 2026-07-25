package com.xyclub.auth.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * User DTO shared between auth service and Feign callers.
 */
@Data
public class AuthUserDTO implements Serializable {

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
