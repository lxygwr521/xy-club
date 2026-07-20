package com.xyclub.auth.domain.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.xyclub.auth.common.enums.AuthUserStatusEnum;
import com.xyclub.auth.common.enums.IsDeletedFlagEnum;
import com.xyclub.auth.domain.convert.AuthUserBOConverter;
import com.xyclub.auth.domain.entity.AuthUserBO;
import com.xyclub.auth.domain.service.AuthUserDomainService;
import com.xyclub.auth.infra.basic.entity.AuthUser;
import com.xyclub.auth.infra.basic.service.AuthUserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户领域service实现
 *
 * @author lxy
 * @date 2026-07-20
 */
@Service
@Slf4j
public class AuthUserDomainServiceImpl implements AuthUserDomainService {

    @Resource
    private AuthUserService authUserService;

    private String salt = "xyclub";

    @Override
    @SneakyThrows
    public Boolean register(AuthUserBO authUserBO) {
        AuthUser authUser = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        authUser.setPassword(SaSecureUtil.md5BySalt(authUser.getPassword(), salt));
        authUser.setStatus(AuthUserStatusEnum.OPEN.getCode());
        authUser.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        Integer count = authUserService.insert(authUser);
        return count > 0;
    }

    @Override
    public Boolean update(AuthUserBO authUserBO) {
        AuthUser authUser = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        Integer count = authUserService.update(authUser);
        return count > 0;
    }

    @Override
    public Boolean delete(AuthUserBO authUserBO) {
        AuthUser authUser = new AuthUser();
        authUser.setId(authUserBO.getId());
        authUser.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        Integer count = authUserService.update(authUser);
        return count > 0;
    }
}
