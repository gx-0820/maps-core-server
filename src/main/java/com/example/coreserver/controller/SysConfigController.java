package com.example.coreserver.controller;

import com.example.coreserver.common.Result;
import com.example.coreserver.entity.Config;
import com.example.coreserver.repository.ConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/sysConfig")
public class SysConfigController {

    @Autowired
    private ConfigRepository configRepository;

    @GetMapping("/findAll")
    public ResponseEntity<Result> findAll() {
        return ResponseEntity.ok(Result.success(configRepository.findAll()));
    }

    @GetMapping("/update")
    public ResponseEntity<Result> update(@RequestParam(required = false) String key,
                                         @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String value,
                                         @RequestParam(required = false) String remark) {
        Optional<Config> existingConfig = configRepository.findByConfigKey(key);
        Config config = existingConfig.orElseGet(Config::new);
        configRepository.save(buildConfigEntity(config, name, key, value, remark, "admin"));
        return ResponseEntity.ok(Result.success("操作成功"));
    }

    private Config buildConfigEntity(Config config, String name, String key, String value, String remark, String updateBy) {
        config.setConfigName(name);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType("Y");
        config.setRemark(remark);
        if (config.getConfigId() == null) {
            config.setCreateBy(updateBy);
        } else {
            config.setUpdateBy(updateBy);
            config.setUpdateTime(LocalDateTime.now());
        }
        return config;
    }

}
