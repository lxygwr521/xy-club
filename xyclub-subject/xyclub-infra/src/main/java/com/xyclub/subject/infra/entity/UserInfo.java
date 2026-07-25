package com.xyclub.subject.infra.entity;

import lombok.Data;

/**
 * User information projected from auth service.
 */
@Data
public class UserInfo {

    private String userName;

    private String nickName;

}
