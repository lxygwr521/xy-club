package com.xyclub.auth.application.convert;

import com.xyclub.auth.application.dto.AuthRolePermissionDTO;
import com.xyclub.auth.domain.entity.AuthRolePermissionBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色权限dto转换器
 *
 * @author lxy
 * @date 2026-07-20
 */
@Mapper
public interface AuthRolePermissionDTOConverter {

    AuthRolePermissionDTOConverter INSTANCE = Mappers.getMapper(AuthRolePermissionDTOConverter.class);

    AuthRolePermissionBO convertDTOToBO(AuthRolePermissionDTO authRolePermissionDTO);

}
