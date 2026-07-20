package com.xyclub.auth.domain.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.xyclub.auth.common.enums.AuthUserStatusEnum;
import com.xyclub.auth.common.enums.IsDeletedFlagEnum;
import com.xyclub.auth.domain.constants.AuthConstant;
import com.xyclub.auth.domain.convert.AuthUserBOConverter;
import com.xyclub.auth.domain.entity.AuthUserBO;
import com.xyclub.auth.domain.service.AuthUserDomainService;
import com.xyclub.auth.infra.basic.entity.AuthRole;
import com.xyclub.auth.infra.basic.entity.AuthUser;
import com.xyclub.auth.infra.basic.entity.AuthUserRole;
import com.xyclub.auth.infra.basic.service.AuthRoleService;
import com.xyclub.auth.infra.basic.service.AuthUserRoleService;
import com.xyclub.auth.infra.basic.service.AuthUserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    private AuthUserRoleService authUserRoleService;

    @Resource
    private AuthRoleService authRoleService;

    private String salt = "xyclub";

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(AuthUserBO authUserBO) {
        AuthUser authUser = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        authUser.setPassword(SaSecureUtil.md5BySalt(authUser.getPassword(), salt));
        authUser.setStatus(AuthUserStatusEnum.OPEN.getCode());
        authUser.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        Integer count = authUserService.insert(authUser);

        // 建立一个初步的角色的关联
        AuthRole authRole = new AuthRole();
        authRole.setRoleKey(AuthConstant.NORMAL_USER);
        AuthRole roleResult = authRoleService.queryByCondition(authRole);
        Long roleId = roleResult.getId();
        Long userId = authUser.getId();
        AuthUserRole authUserRole = new AuthUserRole();
        authUserRole.setUserId(userId);
        authUserRole.setRoleId(roleId);
        authUserRole.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        authUserRoleService.insert(authUserRole);
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
