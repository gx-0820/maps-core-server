package com.example.coreserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coreserver.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
