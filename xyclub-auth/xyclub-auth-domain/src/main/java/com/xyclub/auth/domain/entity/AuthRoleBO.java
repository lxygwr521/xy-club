package com.xyclub.auth.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色bo
 *
 * @author lxy
 * @date 2026-07-20
 */
@Data
public class AuthRoleBO implements Serializable {

    private Long id;

    private String roleName;

    private String roleKey;

}
