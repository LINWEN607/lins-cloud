package com.wgcloud.mapper;

import com.wgcloud.entity.LogMonitor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface LogMonitorMapper {

    List<LogMonitor> selectAllByParams(Map<String, Object> map) throws Exception;

    List<LogMonitor> selectByParams(Map<String, Object> params) throws Exception;

    LogMonitor selectById(String id) throws Exception;

    void save(LogMonitor logMonitor) throws Exception;

    void insertList(List<LogMonitor> recordList) throws Exception;

    int updateById(LogMonitor logMonitor) throws Exception;

    int deleteById(String[] id) throws Exception;

    int deleteByHostName(Map<String, Object> map) throws Exception;

    int countByParams(Map<String, Object> params) throws Exception;
}
