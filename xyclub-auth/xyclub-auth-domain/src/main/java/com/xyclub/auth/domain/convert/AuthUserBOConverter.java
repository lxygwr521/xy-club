package com.xyclub.auth.domain.convert;

import com.xyclub.auth.domain.entity.AuthUserBO;
import com.xyclub.auth.infra.basic.entity.AuthUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户BO转换器
 *
 * @author lxy
 * @date 2026-07-20
 */
@Mapper
public interface AuthUserBOConverter {

    AuthUserBOConverter INSTANCE = Mappers.getMapper(AuthUserBOConverter.class);

    AuthUser convertBOToEntity(AuthUserBO authUserBO);

}
