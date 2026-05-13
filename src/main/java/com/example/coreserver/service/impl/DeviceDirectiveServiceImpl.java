package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.DeviceDirective;
import com.example.coreserver.service.DeviceDirectiveService;
import com.example.coreserver.mapper.DeviceDirectiveMapper;
import org.springframework.stereotype.Service;

/**
* @author 70411
* @description 针对表【device_directive(设备指令表)】的数据库操作Service实现
* @createDate 2026-05-10 23:53:07
*/
@Service
public class DeviceDirectiveServiceImpl extends ServiceImpl<DeviceDirectiveMapper, DeviceDirective>
    implements DeviceDirectiveService{

}




