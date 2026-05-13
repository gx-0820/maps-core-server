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
 * @description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("operation_log")
@ApiModel(value = "OperationLog 对象", description = "操作日志表")
public class OperationLog implements Serializable {

    private static final Long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键 id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "操作模块")
    @Column(name = "opt_module")
    private String optModule;

    @ApiModelProperty(value = "操作 URI")
    @Column(name = "opt_uri")
    private String optUri;

    @ApiModelProperty(value = "操作类型：新增，修改等")
    @Column(name = "opt_type")
    private String optType;

    @ApiModelProperty(value = "操作方法")
    @Column(name = "opt_method")
    private String optMethod;

    @ApiModelProperty(value = "操作描述")
    @Column(name = "opt_desc")
    private String optDesc;

    @ApiModelProperty(value = "请求方法：GET，POST，DELETE，PUT")
    @Column(name = "request_method")
    private String requestMethod;

    @ApiModelProperty(value = "请求参数")
    @Column(name = "request_param")
    private String requestParam;

    @ApiModelProperty(value = "返回数据")
    @Column(name = "response_data")
    private String responseData;

    @ApiModelProperty(value = "操作用户 id")
    @Column(name = "user_id")
    private Integer userId;

    @ApiModelProperty(value = "操作用户昵称")
    @Column(name = "nickname")
    private String nickname;

    @ApiModelProperty(value = "操作用户 ip 地址")
    @Column(name = "ip_address")
    private String ipAddress;

    @ApiModelProperty(value = "操作用户 ip 来源")
    @Column(name = "ip_source")
    private String ipSource;
}