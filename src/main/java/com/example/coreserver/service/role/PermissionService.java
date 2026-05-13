package com.example.coreserver.service.role;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.Permission;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
public interface PermissionService extends IService<Permission> {

    Permission getPermissionByCode(String code);

    List<Permission> getAllPermissions();
}
