package com.example.coreserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coreserver.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT t1.permission_code FROM permission t1\n" +
            "     INNER JOIN role_permission t2 ON t2.permission_id = t1.id\n" +
            "     INNER JOIN role t3 ON t3.id = t2.role_id\n" +
            "     INNER JOIN user_role t4 ON t4.role_id = t3.id\n" +
            "     INNER JOIN user t5 ON t5.id = t4.user_id\n" +
            "WHERE t5.id = #{id} AND t1.permission_code IS NOT NULL And t5.status = 0 And t3.status = 0 And t1.status = 0;")
    List<String> getPermissionByUserId(Integer id);
}
