package com.xyclub.subject.infra.rpc;

import com.xyclub.auth.api.UserFeignService;
import com.xyclub.auth.entity.AuthUserDTO;
import com.xyclub.auth.entity.Result;
import com.xyclub.subject.infra.entity.UserInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Wraps auth Feign calls and hides remote DTOs from subject domain code.
 */
@Component
public class UserRpc {

    @Resource
    private UserFeignService userFeignService;
//    根据用户名，调用认证服务（Auth）的方法获取用户的详细信息，并将其转换为当前服务（Subject）内部使用的 UserInfo 对象。
    public UserInfo getUserInfo(String userName) {
        AuthUserDTO authUserDTO = new AuthUserDTO();
        authUserDTO.setUserName(userName);
        Result<AuthUserDTO> result = userFeignService.getUserInfo(authUserDTO);
        UserInfo userInfo = new UserInfo();
        if (!result.getSuccess()) {
            return userInfo;
        }
        AuthUserDTO data = result.getData();
        userInfo.setUserName(data.getUserName());
        userInfo.setNickName(data.getNickName());
        return userInfo;
    }
}
