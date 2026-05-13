package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName device_conf
 */
@TableName(value ="device_conf")
@Data
public class DeviceConf {
    private Long id;

    private Long deviceId;

    private String name;

    private String confKey;

    private String confValue;

    private String description;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private String remark;
}