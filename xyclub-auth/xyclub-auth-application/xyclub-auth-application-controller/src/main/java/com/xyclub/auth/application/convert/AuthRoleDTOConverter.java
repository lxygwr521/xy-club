package com.xyclub.auth.application.convert;

import com.xyclub.auth.application.dto.AuthRoleDTO;
import com.xyclub.auth.domain.entity.AuthRoleBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色dto转换器
 *
 * @author lxy
 * @date 2026-07-20
 */
@Mapper
public interface AuthRoleDTOConverter {

    AuthRoleDTOConverter INSTANCE = Mappers.getMapper(AuthRoleDTOConverter.class);

    AuthRoleBO convertDTOToBO(AuthRoleDTO authRoleDTO);

}
