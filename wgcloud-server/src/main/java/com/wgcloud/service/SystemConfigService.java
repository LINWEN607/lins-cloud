package com.wgcloud.service;

import com.wgcloud.entity.SystemConfig;
import com.wgcloud.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    public String getVal(String key) throws Exception {
        SystemConfig config = systemConfigMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    public void setVal(String key, String value) throws Exception {
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
    }

    @Autowired
    private SystemConfigMapper systemConfigMapper;
}
