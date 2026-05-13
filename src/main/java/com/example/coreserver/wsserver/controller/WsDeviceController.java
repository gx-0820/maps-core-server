package com.example.coreserver.wsserver.controller;


import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coreserver.entity.Device;
import com.example.coreserver.entity.DeviceDirective;
import com.example.coreserver.service.DeviceDirectiveService;
import com.example.coreserver.wsserver.netty.NettyDataHolder;
import com.example.coreserver.wsserver.services.utils.DeviceInitUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v12/device")
@Api(tags = "版本v1.2设备控制")
public class WsDeviceController {

    private final NettyDataHolder nettyDataHolder;
    private final DeviceInitUtils deviceInitUtils;
    private final DeviceDirectiveService deviceDirectiveService;

    public WsDeviceController(NettyDataHolder nettyDataHolder, DeviceInitUtils deviceInitUtils, DeviceDirectiveService deviceDirectiveService) {
        this.nettyDataHolder = nettyDataHolder;
        this.deviceInitUtils = deviceInitUtils;
        this.deviceDirectiveService = deviceDirectiveService;
    }


    @GetMapping("/{deviceCode}/{command}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendCommandToDevice(@PathVariable String deviceCode, @PathVariable String command, JSONObject args) {
        nettyDataHolder.command(deviceCode, command, args);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/getDevices")
//    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getDevices(String displayIn) {
        List<Device> list = deviceInitUtils
                .getDevices()
                .stream()
                .filter(device -> StringUtils.isNotBlank(device.getDisplayIn())
                        && displayIn.equals(device.getDisplayIn()))
                .toList();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/getDeviceDirective")
//    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getDeviceDirective(Long deviceId) {
        LambdaQueryWrapper<DeviceDirective> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceDirective::getDeviceId, deviceId);
        List<DeviceDirective> list = deviceDirectiveService.list(wrapper);

        List<DeviceDirective> result = list.stream()
                .collect(Collectors.groupingBy(deviceDirective -> {
                    Integer ddGroup = deviceDirective.getDdGroup();
                    return ddGroup != null ? ddGroup : 0;
                }))
                .entrySet().stream()
                .flatMap(entry -> {
                    Integer groupIndex = entry.getKey();
                    List<DeviceDirective> groupList = entry.getValue();

                    if (groupIndex == 0) {
                        return groupList.stream();
                    } else {
                        return groupList.stream()
                                .filter(e -> "Y".equals(e.getDdCurrent()))
                                .limit(1);
                    }
                })
                .sorted(Comparator.nullsLast(Comparator.comparingInt(DeviceDirective::getOrderNum)))
                .toList();

        return ResponseEntity.ok().body(result);
    }


}
