package com.wgcloud.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wgcloud.entity.*;
import com.wgcloud.service.ContainerInfoService;
import com.wgcloud.service.ContainerStateService;
import com.wgcloud.service.LogInfoService;
import com.wgcloud.service.LogMonitorService;
import com.wgcloud.service.SystemInfoService;
import com.wgcloud.util.TokenUtils;
import com.wgcloud.util.msg.WarnMailUtil;
import com.wgcloud.util.staticvar.BatchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version v2.3
 * @ClassName:AgentController.java
 * @author: http://www.wgstart.com
 * @date: 2019年11月16日
 * @Description: AgentController.java
 * @Copyright: 2017-2021 wgcloud. All rights reserved.
 */
@Controller
@RequestMapping("/agent")
public class AgentController {


    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 40, 2, TimeUnit.MINUTES, new LinkedBlockingDeque<>());


    @Resource
    private LogInfoService logInfoService;
    @Resource
    private SystemInfoService systemInfoService;
    @Resource
    private ContainerStateService containerStateService;
    @Resource
    private ContainerInfoService containerInfoService;
    @Autowired
    private TokenUtils tokenUtils;

    @ResponseBody
    @RequestMapping("/minTask")
    public JSONObject minTask(@RequestBody String paramBean) {
        JSONObject agentJsonObject = (JSONObject) JSONUtil.parse(paramBean);
        JSONObject resultJson = new JSONObject();
        if (!tokenUtils.checkAgentToken(agentJsonObject)) {
            logger.error("token is invalidate");
            resultJson.put("result", "error：token is invalidate");
            return resultJson;
        }
        JSONObject cpuState = agentJsonObject.getJSONObject("cpuState");
        JSONObject memState = agentJsonObject.getJSONObject("memState");
        JSONObject sysLoadState = agentJsonObject.getJSONObject("sysLoadState");
        JSONArray appInfoList = agentJsonObject.getJSONArray("appInfoList");
        JSONArray appStateList = agentJsonObject.getJSONArray("appStateList");
        JSONObject logInfo = agentJsonObject.getJSONObject("logInfo");
        JSONObject systemInfo = agentJsonObject.getJSONObject("systemInfo");
        JSONObject netIoState = agentJsonObject.getJSONObject("netIoState");
        JSONArray deskStateList = agentJsonObject.getJSONArray("deskStateList");
        JSONArray containerStateList = agentJsonObject.getJSONArray("containerStateList");
        JSONArray logMonitorMatch = agentJsonObject.getJSONArray("logMonitorMatch");

        try {

            if (logInfo != null) {
                LogInfo bean = new LogInfo();
                BeanUtil.copyProperties(logInfo, bean);
                BatchData.LOG_INFO_LIST.add(bean);
            }
            if (cpuState != null) {
                CpuState bean = new CpuState();
                BeanUtil.copyProperties(cpuState, bean);
                BatchData.CPU_STATE_LIST.add(bean);
                Runnable runnable = () -> {
                    WarnMailUtil.sendCpuWarnInfo(bean);
                };
                executor.execute(runnable);

            }
            if (memState != null) {
                MemState bean = new MemState();
                BeanUtil.copyProperties(memState, bean);
                BatchData.MEM_STATE_LIST.add(bean);
                Runnable runnable = () -> {
                    WarnMailUtil.sendWarnInfo(bean);
                };
                executor.execute(runnable);
            }
            if (sysLoadState != null) {
                SysLoadState bean = new SysLoadState();
                BeanUtil.copyProperties(sysLoadState, bean);
                BatchData.SYSLOAD_STATE_LIST.add(bean);
            }
            if (netIoState != null) {
                NetIoState bean = new NetIoState();
                BeanUtil.copyProperties(netIoState, bean);
                BatchData.NETIO_STATE_LIST.add(bean);
            }
            if (appInfoList != null && appStateList != null) {
                List<AppInfo> appInfoResList = JSONUtil.toList(appInfoList, AppInfo.class);
                for (AppInfo appInfo : appInfoResList) {
                    BatchData.APP_INFO_LIST.add(appInfo);
                }
                List<AppState> appStateResList = JSONUtil.toList(appStateList, AppState.class);
                for (AppState appState : appStateResList) {
                    BatchData.APP_STATE_LIST.add(appState);
                }
            }
            if (systemInfo != null) {
                SystemInfo bean = new SystemInfo();
                BeanUtil.copyProperties(systemInfo, bean);
                BatchData.SYSTEM_INFO_LIST.add(bean);
            }
            if (deskStateList != null) {
                for (Object jsonObjects : deskStateList) {
                    DeskState bean = new DeskState();
                    BeanUtil.copyProperties(jsonObjects, bean);
                    BatchData.DESK_STATE_LIST.add(bean);
                }
            }
            if (containerStateList != null) {
                java.util.List<ContainerState> containerStateResList = JSONUtil.toList(containerStateList, ContainerState.class);
                for (ContainerState cs : containerStateResList) {
                    cs.setId(com.wgcloud.util.UUIDUtil.getUUID());
                    cs.setCreateTime(new java.util.Date());
                    try {
                        containerStateService.save(cs);
                    } catch (Exception e) {
                        logger.error("保存容器状态错误", e);
                    }
                }
            }
            if (logMonitorMatch != null) {
                LogMonitorService logMonitorService = (LogMonitorService) com.wgcloud.common.ApplicationContextHelper.getBean(LogMonitorService.class);
                Map<String, String> hostnameRemarkMap = new HashMap<>();
                try {
                    List<SystemInfo> sysList = systemInfoService.selectAllByParams(new HashMap<>());
                    for (SystemInfo si : sysList) {
                        hostnameRemarkMap.put(si.getHostname(), si.getRemark());
                    }
                } catch (Exception e) {
                    logger.error("查询主机信息错误", e);
                }
                Map<String, List<String>> grouped = new LinkedHashMap<>();
                for (Object obj : logMonitorMatch) {
                    JSONObject match = (JSONObject) obj;
                    String hostname = match.getStr("hostname");
                    String logFilePath = match.getStr("logFilePath");
                    String matchedLine = match.getStr("matchedLine");
                    String key = hostname + "|" + logFilePath;
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(matchedLine);
                    LogInfo li = new LogInfo();
                    li.setId(com.wgcloud.util.UUIDUtil.getUUID());
                    li.setHostname(hostname);
                    li.setInfoContent("日志匹配: " + matchedLine);
                    li.setCreateTime(new java.util.Date());
                    BatchData.LOG_INFO_LIST.add(li);
                }
                for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                    String[] parts = entry.getKey().split("\\|", 2);
                    String hostname = parts[0];
                    String logFilePath = parts[1];
                    List<String> lines = entry.getValue();
                    String remark = hostnameRemarkMap.get(hostname);
                    StringBuilder sb = new StringBuilder();
                    int limit = Math.min(lines.size(), 5);
                    for (int i = 0; i < limit; i++) {
                        sb.append(lines.get(i)).append("\n");
                    }
                    if (lines.size() > 5) {
                        sb.append("... 共 ").append(lines.size()).append(" 条匹配");
                    }
                    WarnMailUtil.sendLogMatchWarn(hostname, remark, logFilePath, sb.toString().trim());
                }
            }
            resultJson.put("result", "success");
        } catch (Exception e) {
            e.printStackTrace();
            resultJson.put("result", "error：" + e.toString());
        } finally {
            return resultJson;
        }
    }

}
