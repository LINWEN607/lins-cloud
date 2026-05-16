package com.wgcloud.service;

import com.wgcloud.entity.FeishuConfig;
import com.wgcloud.mapper.FeishuConfigMapper;
import com.wgcloud.util.DateUtil;
import com.wgcloud.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FeishuConfigService {

    public void save(FeishuConfig feishuConfig) throws Exception {
        feishuConfig.setId(UUIDUtil.getUUID());
        feishuConfig.setCreateTime(DateUtil.getNowTime());
        if (feishuConfig.getWebhookUrl() != null) {
            feishuConfig.setWebhookUrl(feishuConfig.getWebhookUrl().trim());
        }
        feishuConfigMapper.save(feishuConfig);
    }

    public int deleteById(String[] id) throws Exception {
        return feishuConfigMapper.deleteById(id);
    }

    public List<FeishuConfig> selectAllByParams(Map<String, Object> params) throws Exception {
        return feishuConfigMapper.selectAllByParams(params);
    }

    public int updateById(FeishuConfig feishuConfig) throws Exception {
        return feishuConfigMapper.updateById(feishuConfig);
    }

    @Autowired
    private FeishuConfigMapper feishuConfigMapper;
}
