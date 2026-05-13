package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName device_directive
 */
@TableName(value ="device_directive")
@Data
public class DeviceDirective {
    private Long id;

    private Long deviceId;

    private String ddLabel;

    private String ddName;

    private String ddKeyCode;

    private Integer ddGroup;

    private String ddCurrent;

    private String optionData;

    private String commandResult;

    private Integer orderNum;

    private String argsRequired;

    private String description;

    private String commandArgs;
}