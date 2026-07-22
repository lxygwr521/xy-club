package com.xyclub.auth.domain.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import com.xyclub.auth.common.enums.AuthUserStatusEnum;
import com.xyclub.auth.common.enums.IsDeletedFlagEnum;
import com.xyclub.auth.domain.constants.AuthConstant;
import com.xyclub.auth.domain.convert.AuthUserBOConverter;
import com.xyclub.auth.domain.entity.AuthUserBO;
import com.xyclub.auth.domain.redis.RedisUtil;
import com.xyclub.auth.domain.service.AuthUserDomainService;
import com.xyclub.auth.infra.basic.entity.*;
import com.xyclub.auth.infra.basic.service.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    private AuthPermissionService authPermissionService;

    @Resource
    private AuthRolePermissionService authRolePermissionService;

    @Resource
    private AuthRoleService authRoleService;

    private String salt = "xyclub";

    @Resource
    private RedisUtil redisUtil;

    private String authPermissionPrefix = "auth.permission";

    private String authRolePrefix = "auth.role";

    private static final String LOGIN_PREFIX = "loginCode";


    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(AuthUserBO authUserBO) {
        //校验用户是否存在
        AuthUser existAuthUser = new AuthUser();
        existAuthUser.setUserName(authUserBO.getUserName());
        List<AuthUser> existUser = authUserService.queryByCondition(existAuthUser);
        if (existUser.size() > 0) {
            return true;
        }
        // 1. 转换并加密密码
        // 将业务对象(BO)转换为数据实体(Entity)
        AuthUser authUser = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        // 微信登录场景可能没有密码，只有邮箱注册才需要加密
        if (StringUtils.isNotBlank(authUser.getPassword())) {
            // 使用加盐MD5进行密码加密，更安全
            authUser.setPassword(SaSecureUtil.md5BySalt(authUser.getPassword(), salt));
        }
        // 设置用户状态为启用，逻辑删除标记为未删除
        authUser.setStatus(AuthUserStatusEnum.OPEN.getCode());
        authUser.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        // 插入用户表，返回影响行数
        Integer count = authUserService.insert(authUser);

        // 2. 建立用户与角色的关联（默认分配普通用户角色）
        // 查询默认的“普通用户”角色
        AuthRole authRole = new AuthRole();
        authRole.setRoleKey(AuthConstant.NORMAL_USER);
        AuthRole roleResult = authRoleService.queryByCondition(authRole);
        Long roleId = roleResult.getId();
        // 获取刚刚插入的用户ID
        Long userId = authUser.getId();
        // 构建用户-角色关联实体并插入关联表
        AuthUserRole authUserRole = new AuthUserRole();
        authUserRole.setUserId(userId);
        authUserRole.setRoleId(roleId);
        authUserRole.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        authUserRoleService.insert(authUserRole);

        // 3. 缓存该用户的角色信息到Redis，加速后续鉴权
        // 构建Redis的key，格式类似：auth:role:用户名
        String roleKey = redisUtil.buildKey(authRolePrefix, authUser.getUserName());
        List<AuthRole> roleList = new LinkedList<>();
        roleList.add(authRole);
        // 将角色列表转为JSON字符串存入Redis
        redisUtil.set(roleKey, new Gson().toJson(roleList));

        // 4. 查询并缓存该用户的权限信息到Redis
        // 先根据角色ID，查询出该角色拥有的所有权限ID
        AuthRolePermission authRolePermission = new AuthRolePermission();
        authRolePermission.setRoleId(roleId);
        List<AuthRolePermission> rolePermissionList = authRolePermissionService
                .queryByCondition(authRolePermission);
        // 提取出权限ID列表
        List<Long> permissionIdList = rolePermissionList.stream()
                .map(AuthRolePermission::getPermissionId)
                .collect(Collectors.toList());
        // 再根据权限ID列表，查询出具体的权限详情（如权限字符串）
        List<AuthPermission> permissionList = authPermissionService.queryByRoleList(permissionIdList);
        // 构建权限信息的Redis key，格式类似：auth:permission:用户名
        String permissionKey = redisUtil.buildKey(authPermissionPrefix, authUser.getUserName());
        // 将权限列表转为JSON字符串存入Redis
        redisUtil.set(permissionKey, new Gson().toJson(permissionList));

        // 返回插入是否成功（影响行数 > 0）
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

    /**
     * 微信验证码登录
     * ① Redis 中反查验证码对应的 OpenID
     * ② 调用 register 自动创建用户（已存在则跳过）
     * ③ 执行登录，返回 Sa-Token 令牌
     */
    @Override
    public SaTokenInfo doLogin(String validCode) {
        // 从 Redis 中取验证码对应的 OpenID：loginCode.537 → oXYZ789
        String loginKey = redisUtil.buildKey(LOGIN_PREFIX, validCode);
        String openId =  redisUtil.get(loginKey);
        if (StringUtils.isBlank(openId)) {
            return null;
        }
        // 用 OpenID 作为 userName 自动注册（首次创建用户，再次则 insert 无影响）
        AuthUserBO authUserBO = new AuthUserBO();
        authUserBO.setUserName(openId);
        this.register(authUserBO);
        // 以 OpenID 为 loginId 登录，Sa-Token 自动生成 token 并写入 Redis
        StpUtil.login(openId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return tokenInfo;
    }

    @Override
    public AuthUserBO getUserInfo(AuthUserBO authUserBO) {
        AuthUser authUser = new AuthUser();
        authUser.setUserName(authUserBO.getUserName());
        List<AuthUser> userList = authUserService.queryByCondition(authUser);
        if (CollectionUtils.isEmpty(userList)) {
            return new AuthUserBO();
        }
        AuthUser user = userList.get(0);
        return AuthUserBOConverter.INSTANCE.convertEntityToBO(user);
    }
}
