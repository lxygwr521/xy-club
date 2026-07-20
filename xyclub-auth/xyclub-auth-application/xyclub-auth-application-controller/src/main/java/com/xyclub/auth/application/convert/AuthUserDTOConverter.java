package com.xyclub.auth.application.convert;

import com.xyclub.auth.application.dto.AuthUserDTO;
import com.xyclub.auth.domain.entity.AuthUserBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户DTO转换器
 *
 * @author lxy
 * @date 2026-07-20
 */
@Mapper
public interface AuthUserDTOConverter {

    AuthUserDTOConverter INSTANCE = Mappers.getMapper(AuthUserDTOConverter.class);

    AuthUserBO convertDTOToBO(AuthUserDTO authUserDTO);

}
