package com.example.coreserver.entity.log;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lord
 * @date 2025/4/4
 * @description 异常日志表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("exception_log")
@ApiModel(value = "ExceptionLog 对象", description = "异常日志表")
public class ExceptionLog implements Serializable {

    private static final Long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键 id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "操作 URI")
    @Column(name = "opt_uri")
    private String optUri;

    @ApiModelProperty(value = "操作方法")
    @Column(name = "opt_method")
    private String optMethod;

    @ApiModelProperty(value = "请求方法：Get，Post，Delete，Put")
    @Column(name = "request_method")
    private String requestMethod;

    @ApiModelProperty(value = "请求参数")
    @Column(name = "request_param")
    private String requestParam;

    @ApiModelProperty(value = "操作描述")
    @Column(name = "opt_desc")
    private String optDesc;

    @ApiModelProperty(value = "错误信息")
    @Column(name = "exception_info")
    private String exceptionInfo;

    @ApiModelProperty(value = "ip 地址")
    @Column(name = "ip_address")
    private String ipAddress;

    @ApiModelProperty(value = "ip 来源")
    @Column(name = "ip_source")
    private String ipSource;
}