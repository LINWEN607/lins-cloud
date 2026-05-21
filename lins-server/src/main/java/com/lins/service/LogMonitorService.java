package com.lins.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lins.entity.LogMonitor;
import com.lins.mapper.LogMonitorMapper;
import com.lins.util.DateUtil;
import com.lins.util.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class LogMonitorService {

    @Resource
    private LogMonitorMapper logMonitorMapper;

    public PageInfo selectByParams(Map<String, Object> params, int currPage, int pageSize) throws Exception {
        PageHelper.startPage(currPage, pageSize);
        List<LogMonitor> list = logMonitorMapper.selectByParams(params);
        return new PageInfo<LogMonitor>(list);
    }

    public void save(LogMonitor logMonitor) throws Exception {
        logMonitor.setId(UUIDUtil.getUUID());
        logMonitor.setCreateTime(DateUtil.getNowTime());
        logMonitorMapper.save(logMonitor);
    }

    public int countByParams(Map<String, Object> params) throws Exception {
        return logMonitorMapper.countByParams(params);
    }

    public void updateById(LogMonitor logMonitor) throws Exception {
        logMonitorMapper.updateById(logMonitor);
    }

    @Transactional
    public void deleteById(String[] id) throws Exception {
        logMonitorMapper.deleteById(id);
    }

    public LogMonitor selectById(String id) throws Exception {
        return logMonitorMapper.selectById(id);
    }

    public List<LogMonitor> selectAllByParams(Map<String, Object> params) throws Exception {
        return logMonitorMapper.selectAllByParams(params);
    }
}
