package com.wgcloud.service;

import com.wgcloud.entity.DingtalkConfig;
import com.wgcloud.mapper.DingtalkConfigMapper;
import com.wgcloud.util.DateUtil;
import com.wgcloud.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DingtalkConfigService {

    public void save(DingtalkConfig dingtalkConfig) throws Exception {
        dingtalkConfig.setId(UUIDUtil.getUUID());
        dingtalkConfig.setCreateTime(DateUtil.getNowTime());
        if (dingtalkConfig.getWebhookUrl() != null) {
            dingtalkConfig.setWebhookUrl(dingtalkConfig.getWebhookUrl().trim());
        }
        dingtalkConfigMapper.save(dingtalkConfig);
    }

    public int deleteById(String[] id) throws Exception {
        return dingtalkConfigMapper.deleteById(id);
    }

    public List<DingtalkConfig> selectAllByParams(Map<String, Object> params) throws Exception {
        return dingtalkConfigMapper.selectAllByParams(params);
    }

    public int updateById(DingtalkConfig dingtalkConfig) throws Exception {
        return dingtalkConfigMapper.updateById(dingtalkConfig);
    }

    @Autowired
    private DingtalkConfigMapper dingtalkConfigMapper;
}
