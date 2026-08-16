package com.example.coreserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置编辑请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigEditRequest {

    /**
     * 配置键（必填）
     */
    private String key;

    /**
     * 配置名称（可选，不传则保持原值）
     */
    private String name;

    /**
     * 配置值（必填）
     */
    private String value;

    /**
     * 备注（可选，不传则保持原值）
     */
    private String remark;
}
