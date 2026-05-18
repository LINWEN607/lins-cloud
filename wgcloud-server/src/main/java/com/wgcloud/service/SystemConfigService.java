package com.wgcloud.service;

import com.wgcloud.entity.SystemConfig;
import com.wgcloud.mapper.SystemConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    public String getVal(String key) {
        try {
            SystemConfig config = systemConfigMapper.selectByKey(key);
            return config != null ? config.getConfigValue() : null;
        } catch (Exception e) {
            logger.warn("查询系统配置失败，key={}", key, e);
            return null;
        }
    }

    public void setVal(String key, String value) {
        try {
            SystemConfig config = systemConfigMapper.selectByKey(key);
            if (config != null) {
                config.setConfigValue(value);
                systemConfigMapper.updateByKey(config);
            } else {
                config = new SystemConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                systemConfigMapper.save(config);
            }
        } catch (Exception e) {
            logger.error("保存系统配置失败，key={}", key, e);
        }
    }

    @Autowired
    private SystemConfigMapper systemConfigMapper;
}
