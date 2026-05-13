package com.example.coreserver.wsserver.base;

import cn.hutool.json.JSONObject;
import com.example.coreserver.wsserver.netty.NettyDataHolder;

/**
 * 设备接入处理
 */
public abstract class AbsDeviceProcess {
    private final NettyDataHolder nettyDataHolder;

    protected AbsDeviceProcess(NettyDataHolder nettyDataHolder) {
        this.nettyDataHolder = nettyDataHolder;
    }

    /**
     * 编号
     */
    public abstract WSType code();

    public abstract void message(String message);

    /**
     * 给设备发送指令
     * @param deviceCode 设备编号
     * @param command 指令
     * @param args 参数
     */
    protected void sendCommandToDeice(String deviceCode, String command, JSONObject args){
        nettyDataHolder.command(deviceCode, command, args);
    }

    /**
     * 发送数据给前端
     * @param dataType 唯一数据区分类型
     * @param data json数据
     */
    protected void sendToWeb(String dataType ,JSONObject data){
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("dataType", dataType);
        jsonObject.set("data", data);
        nettyDataHolder.forwardToWeb(jsonObject);
    }
}
