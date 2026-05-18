package com.wgcloud.mapper;

import com.wgcloud.entity.ContainerInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ContainerInfoMapper {

    public List<ContainerInfo> selectAllByParams(Map<String, Object> map) throws Exception;

    public List<ContainerInfo> selectByParams(Map<String, Object> params) throws Exception;

    public ContainerInfo selectById(String id) throws Exception;

    public void save(ContainerInfo ContainerInfo) throws Exception;

    public void insertList(List<ContainerInfo> recordList) throws Exception;

    public void updateList(List<ContainerInfo> recordList) throws Exception;

    public int deleteById(String[] id) throws Exception;

    public int deleteByHostName(Map<String, Object> map) throws Exception;

    public int countByParams(Map<String, Object> params) throws Exception;

    public int updateById(ContainerInfo ContainerInfo) throws Exception;
}
