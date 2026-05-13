package com.example.coreserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置信息实体类
 *
 * @author: zhanghenan
 */
@Data
@Entity
@Table(name = "config", indexes = {
        @Index(name = "idx_config_key", columnList = "config_key")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Config {

    /**
     * 配置ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Integer configId;

    /**
     * 配置名称
     */
    @Column(name = "config_name", nullable = false, length = 255)
    private String configName;

    /**
     * 配置键
     */
    @Column(name = "config_key", nullable = false, length = 255, unique = true)
    private String configKey;

    /**
     * 配置值
     */
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    /**
     * 配置类型（Y/N）
     */
    @Column(name = "config_type", length = 10)
    private String configType;

    /**
     * 创建人
     */
    @Column(name = "create_by", length = 50)
    private String createBy;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Column(name = "update_by", length = 50)
    private String updateBy;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 预持久化事件（创建时）
     */
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }

    /**
     * 预更新事件
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
