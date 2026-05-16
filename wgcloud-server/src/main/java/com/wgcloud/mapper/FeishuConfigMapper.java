package com.wgcloud.mapper;

import com.wgcloud.entity.FeishuConfig;

import java.util.List;
import java.util.Map;

public interface FeishuConfigMapper {

    List<FeishuConfig> selectAllByParams(Map<String, Object> map) throws Exception;

    List<FeishuConfig> selectByParams(Map<String, Object> params) throws Exception;

    FeishuConfig selectById(String id) throws Exception;

    void save(FeishuConfig FeishuConfig) throws Exception;

    int updateById(FeishuConfig FeishuConfig) throws Exception;

    int deleteById(String[] id) throws Exception;
}
