package com.xyclub.auth.domain.service.impl;

import com.xyclub.auth.common.enums.IsDeletedFlagEnum;
import com.xyclub.auth.domain.convert.AuthRoleBOConverter;
import com.xyclub.auth.domain.entity.AuthRoleBO;
import com.xyclub.auth.domain.service.AuthRoleDomainService;
import com.xyclub.auth.infra.basic.entity.AuthRole;
import com.xyclub.auth.infra.basic.service.AuthRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 角色领域service实现
 *
 * @author lxy
 * @date 2026-07-20
 */
@Service
@Slf4j
public class AuthRoleDomainServiceImpl implements AuthRoleDomainService {

    @Resource
    private AuthRoleService authRoleService;

    @Override
    public Boolean add(AuthRoleBO authRoleBO) {
        AuthRole authRole = AuthRoleBOConverter.INSTANCE.convertBOToEntity(authRoleBO);
        authRole.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        Integer count = authRoleService.insert(authRole);
        return count > 0;
    }

    @Override
    public Boolean update(AuthRoleBO authRoleBO) {
        AuthRole authRole = AuthRoleBOConverter.INSTANCE.convertBOToEntity(authRoleBO);
        Integer count = authRoleService.update(authRole);
        return count > 0;
    }

    @Override
    public Boolean delete(AuthRoleBO authRoleBO) {
        AuthRole authRole = new AuthRole();
        authRole.setId(authRoleBO.getId());
        authRole.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        Integer count = authRoleService.update(authRole);
        return count > 0;
    }

}
