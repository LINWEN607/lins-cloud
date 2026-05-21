package com.lins.mapper;

import com.lins.entity.DingtalkConfig;

import java.util.List;
import java.util.Map;

public interface DingtalkConfigMapper {

    List<DingtalkConfig> selectAllByParams(Map<String, Object> map) throws Exception;

    List<DingtalkConfig> selectByParams(Map<String, Object> params) throws Exception;

    DingtalkConfig selectById(String id) throws Exception;

    void save(DingtalkConfig DingtalkConfig) throws Exception;

    int updateById(DingtalkConfig DingtalkConfig) throws Exception;

    int deleteById(String[] id) throws Exception;
}
