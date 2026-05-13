package com.example.coreserver.repository;

import com.example.coreserver.entity.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 配置数据访问接口
 * 
 * @author: zhanghenan
 */
@Repository
public interface ConfigRepository extends JpaRepository<Config, Integer> {

    /**
     * 根据配置键获取配置
     * 
     * @param configKey 配置键
     * @return 配置对象
     */
    Optional<Config> findByConfigKey(String configKey);

    /**
     * 根据配置名称获取配置
     * 
     * @param configName 配置名称
     * @return 配置对象
     */
    Optional<Config> findByConfigName(String configName);

    /**
     * 根据配置名称批量查询
     *
     * @param configNames 配置名称列表
     * @return 配置列表
     */
    List<Config> findByConfigNameIn(List<String> configNames);

    /**
     * 查询所有配置类型为Y的配置
     * 
     * @return 配置列表
     */
    List<Config> findByConfigType(String configType);

    /**
     * 根据配置键批量查询
     * 
     * @param configKeys 配置键列表
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configKey IN :configKeys")
    List<Config> findByConfigKeys(@Param("configKeys") List<String> configKeys);
}
