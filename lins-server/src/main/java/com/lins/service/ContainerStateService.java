package com.lins.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lins.entity.ContainerState;
import com.lins.mapper.ContainerStateMapper;
import com.lins.util.DateUtil;
import com.lins.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ContainerStateService {

    @Autowired
    private ContainerStateMapper containerStateMapper;

    public PageInfo selectByParams(Map<String, Object> params, int currPage, int pageSize) throws Exception {
        PageHelper.startPage(currPage, pageSize);
        List<ContainerState> list = containerStateMapper.selectByParams(params);
        return new PageInfo<ContainerState>(list);
    }

    public void save(ContainerState info) throws Exception {
        info.setId(UUIDUtil.getUUID());
        info.setCreateTime(DateUtil.getNowTime());
        containerStateMapper.save(info);
    }

    @Transactional
    public void saveRecord(List<ContainerState> recordList) throws Exception {
        if (recordList.size() < 1) return;
        for (ContainerState as : recordList) {
            as.setId(UUIDUtil.getUUID());
        }
        containerStateMapper.insertList(recordList);
    }

    public List<ContainerState> selectAllByParams(Map<String, Object> params) throws Exception {
        return containerStateMapper.selectAllByParams(params);
    }

    public List<String> getDistinctContainerNames(String hostname) throws Exception {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("hostname", hostname);
        return containerStateMapper.selectDistinctContainerName(params);
    }

    public void deleteByHostname(Map<String, Object> params) throws Exception {
        containerStateMapper.deleteByHostname(params);
    }
}
