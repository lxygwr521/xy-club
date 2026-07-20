package com.xyclub.auth.application.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色dto
 *
 * @author lxy
 * @date 2026-07-20
 */
@Data
public class AuthRoleDTO implements Serializable {

    private Long id;

    private String roleName;

    private String roleKey;

}
