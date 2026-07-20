package com.xyclub.auth.domain.convert;

import com.xyclub.auth.domain.entity.AuthRoleBO;
import com.xyclub.auth.infra.basic.entity.AuthRole;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色bo转换器
 *
 * @author lxy
 * @date 2026-07-20
 */
@Mapper
public interface AuthRoleBOConverter {

    AuthRoleBOConverter INSTANCE = Mappers.getMapper(AuthRoleBOConverter.class);

    AuthRole convertBOToEntity(AuthRoleBO authRoleBO);

}
