package com.example.coreserver.wsserver.services.impl;

import com.example.coreserver.wsserver.base.AbsDeviceProcess;
import com.example.coreserver.wsserver.base.WSType;
import com.example.coreserver.wsserver.netty.NettyDataHolder;
import org.springframework.stereotype.Service;

@Service
public class WebProcessImpl extends AbsDeviceProcess {

    protected WebProcessImpl(NettyDataHolder nettyDataHolder) {
        super(nettyDataHolder);
    }

    @Override
    public WSType code() {
        return WSType.WEB;
    }

    @Override
    public void message(String message) {

    }
}
