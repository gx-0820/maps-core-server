package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.Device;
import com.example.coreserver.service.DeviceManagerService;
import com.example.coreserver.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

/**
* @author 70411
* @description 针对表【device(设备表)】的数据库操作Service实现
* @createDate 2026-05-10 23:53:07
*/
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device>
    implements DeviceManagerService {

}




