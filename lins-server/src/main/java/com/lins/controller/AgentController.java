package com.lins.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lins.entity.*;
import com.lins.service.ContainerInfoService;
import com.lins.service.ContainerStateService;
import com.lins.service.LogInfoService;
import com.lins.service.SystemInfoService;
import com.lins.util.TokenUtils;
import com.lins.util.msg.WarnMailUtil;
import com.lins.util.staticvar.BatchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    private static final Map<String, Long> logMatchDedupCache = new ConcurrentHashMap<>();
    private static final long DEDUP_WINDOW_MS = 5000;
    private static final Pattern SYSLOG_PREFIX = Pattern.compile("^\\S+\\s+\\S+\\s+\\S+\\s*");


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

    private static String stripLogPrefix(String line) {
        if (line == null) return "";
        Matcher m = SYSLOG_PREFIX.matcher(line);
        if (m.find()) {
            return line.substring(m.end());
        }
        return line;
    }

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
                    cs.setId(com.lins.util.UUIDUtil.getUUID());
                    cs.setCreateTime(new java.util.Date());
                    try {
                        containerStateService.save(cs);
                    } catch (Exception e) {
                        logger.error("保存容器状态错误", e);
                    }
                }
            }
            if (logMonitorMatch != null) {
                long now = System.currentTimeMillis();
                for (Object obj : logMonitorMatch) {
                    JSONObject match = (JSONObject) obj;
                    String hostname = match.getStr("hostname");
                    String logFilePath = match.getStr("logFilePath");
                    String matchedLine = match.getStr("matchedLine");
                    String normalized = stripLogPrefix(matchedLine);
                    String cacheKey = hostname + "|" + logFilePath + "|" + normalized;
                    Long lastTime = logMatchDedupCache.get(cacheKey);
                    if (lastTime != null && (now - lastTime) < DEDUP_WINDOW_MS) {
                        continue;
                    }
                    logMatchDedupCache.put(cacheKey, now);
                    WarnMailUtil.sendLogMatchWarn(hostname, logFilePath, matchedLine);
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
