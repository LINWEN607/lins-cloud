package com.lins.mapper;

import com.lins.entity.ContainerState;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ContainerStateMapper {

    public List<ContainerState> selectAllByParams(Map<String, Object> map) throws Exception;

    public List<ContainerState> selectByParams(Map<String, Object> params) throws Exception;

    public List<ContainerState> selectByContainerInfoId(Map<String, Object> map) throws Exception;

    public ContainerState selectById(String id) throws Exception;

    public void save(ContainerState ContainerState) throws Exception;

    public void insertList(List<ContainerState> recordList) throws Exception;

    public int deleteById(String[] id) throws Exception;

    public int deleteByDate(Map<String, Object> map) throws Exception;

    public List<String> selectDistinctContainerName(Map<String, Object> params) throws Exception;
}
