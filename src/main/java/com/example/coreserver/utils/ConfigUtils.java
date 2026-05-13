package com.example.coreserver.utils;

import com.example.coreserver.entity.Config;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ConfigUtils {

    /**
     * 提取config列表中的config
     */
    public static BiFunction<String, List<Config>, Config> getConfig = (key, configs) -> {
        if (configs == null) {
            return null;
        }
        return configs.stream()
                .filter(e -> Objects.equals(e.getConfigKey(), key))
                .findFirst()
                .orElse(null);
    };



}
