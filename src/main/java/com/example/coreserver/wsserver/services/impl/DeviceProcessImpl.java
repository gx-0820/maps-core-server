package com.example.coreserver.wsserver.services.impl;

import com.example.coreserver.wsserver.base.AbsDeviceProcess;
import com.example.coreserver.wsserver.base.WSType;
import com.example.coreserver.wsserver.netty.NettyDataHolder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class DeviceProcessImpl extends AbsDeviceProcess {


    protected DeviceProcessImpl(NettyDataHolder nettyDataHolder) {
        super(nettyDataHolder);
    }

    @Override
    public WSType code() {
        return WSType.DEVICE;
    }

    @Override
    public void message(String message) {

    }
}
