package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.DeviceConf;
import com.example.coreserver.service.DeviceConfService;
import com.example.coreserver.mapper.DeviceConfMapper;
import org.springframework.stereotype.Service;

/**
* @author 70411
* @description 针对表【device_conf(设备参数表)】的数据库操作Service实现
* @createDate 2026-05-10 23:53:07
*/
@Service
public class DeviceConfServiceImpl extends ServiceImpl<DeviceConfMapper, DeviceConf>
    implements DeviceConfService{

}




