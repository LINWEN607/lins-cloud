package com.wgcloud.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wgcloud.entity.ContainerInfo;
import com.wgcloud.mapper.ContainerInfoMapper;
import com.wgcloud.util.DateUtil;
import com.wgcloud.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ContainerInfoService {

    @Autowired
    private ContainerInfoMapper containerInfoMapper;

    public PageInfo selectByParams(Map<String, Object> params, int currPage, int pageSize) throws Exception {
        PageHelper.startPage(currPage, pageSize);
        List<ContainerInfo> list = containerInfoMapper.selectByParams(params);
        return new PageInfo<ContainerInfo>(list);
    }

    public void save(ContainerInfo info) throws Exception {
        info.setId(UUIDUtil.getUUID());
        info.setCreateTime(DateUtil.getNowTime());
        containerInfoMapper.save(info);
    }

    @Transactional
    public void saveRecord(List<ContainerInfo> recordList) throws Exception {
        if (recordList.size() < 1) return;
        for (ContainerInfo as : recordList) {
            as.setId(UUIDUtil.getUUID());
        }
        containerInfoMapper.insertList(recordList);
    }

    public int countByParams(Map<String, Object> params) throws Exception {
        return containerInfoMapper.countByParams(params);
    }

    @Transactional
    public int deleteById(String[] id) throws Exception {
        return containerInfoMapper.deleteById(id);
    }

    @Transactional
    public void updateRecord(List<ContainerInfo> recordList) throws Exception {
        if (recordList.size() < 1) return;
        containerInfoMapper.updateList(recordList);
    }

    public void updateById(ContainerInfo info) throws Exception {
        containerInfoMapper.updateById(info);
    }

    public ContainerInfo selectById(String id) throws Exception {
        return containerInfoMapper.selectById(id);
    }

    public List<ContainerInfo> selectAllByParams(Map<String, Object> params) throws Exception {
        return containerInfoMapper.selectAllByParams(params);
    }
}
