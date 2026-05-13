package com.example.coreserver.mapper;

import com.example.coreserver.entity.Device;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 70411
* @description 针对表【device(设备表)】的数据库操作Mapper
* @createDate 2026-05-10 23:53:07
* @Entity com.example.coreserver.entity.Device
*/
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

}




