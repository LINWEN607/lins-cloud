package com.lins.mapper;

import com.lins.entity.SystemConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemConfigMapper {

    SystemConfig selectByKey(String configKey) throws Exception;

    void save(SystemConfig systemConfig) throws Exception;

    int updateByKey(SystemConfig systemConfig) throws Exception;

    void deleteByKey(String configKey) throws Exception;

    List<SystemConfig> selectAll() throws Exception;
}
