package com.example.coreserver.mapper;

import com.example.coreserver.entity.DeviceConf;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 70411
* @description 针对表【device_conf(设备参数表)】的数据库操作Mapper
* @createDate 2026-05-10 23:53:07
* @Entity com.example.coreserver.entity.DeviceConf
*/
@Mapper
public interface DeviceConfMapper extends BaseMapper<DeviceConf> {

}




