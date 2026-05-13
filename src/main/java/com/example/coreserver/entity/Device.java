package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName device
 */
@TableName(value ="device")
@Data
public class Device {
    private Long id;

    private String type;

    private String brand;
    private String displayIn;
    private Integer orderNum;

    private String model;

    private String collectFlag;

    private String name;

    private String location;

    private String description;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private String remark;
}