package com.lins.service;

import com.lins.entity.FeishuConfig;
import com.lins.mapper.FeishuConfigMapper;
import com.lins.util.DateUtil;
import com.lins.util.UUIDUtil;
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
