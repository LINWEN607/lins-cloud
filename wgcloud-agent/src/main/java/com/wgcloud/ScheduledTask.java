package com.wgcloud;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wgcloud.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @version V2.3
 * @ClassName:ScheduledTask.java
 * @author: wgcloud
 * @date: 2019年11月16日
 * @Description: ScheduledTask.java
 * @Copyright: 2017-2021 www.wgstart.com. All rights reserved.
 */
@Component
public class ScheduledTask {

    private Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    public static List<AppInfo> appInfoList = Collections.synchronizedList(new ArrayList<AppInfo>());
    public static List<LogMonitor> logMonitorList = Collections.synchronizedList(new ArrayList<LogMonitor>());
    private static Map<String, Long> logFilePositions = new ConcurrentHashMap<>();
    @Autowired
    private RestUtil restUtil;
    @Autowired
    private CommonConfig commonConfig;

    private SystemInfo systemInfo = null;

    private static final Pattern SSH_SUCCESS_PATTERN = Pattern.compile("Accepted\\s+(password|publickey)\\s+for|session\\s+opened\\s+for", Pattern.CASE_INSENSITIVE);
    private static final Pattern SSH_FAILURE_PATTERN = Pattern.compile("Failed\\s+password\\s+for|authentication\\s+failure", Pattern.CASE_INSENSITIVE);
    private static final Pattern SSH_LOGOUT_PATTERN = Pattern.compile("session\\s+closed\\s+for|disconnected\\s+from", Pattern.CASE_INSENSITIVE);


    /**
     * 线程池
     */
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 2, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    /**
     * 60秒后执行，每隔120秒执行, 单位：ms。
     */
    @Scheduled(initialDelay = 59 * 1000L, fixedRate = 120 * 1000)
    public void minTask() {
        List<AppInfo> APP_INFO_LIST_CP = new ArrayList<AppInfo>();
        APP_INFO_LIST_CP.addAll(appInfoList);
        List<LogMonitor> LOG_MONITOR_LIST_CP = new ArrayList<LogMonitor>();
        LOG_MONITOR_LIST_CP.addAll(logMonitorList);
        JSONObject jsonObject = new JSONObject();
        LogInfo logInfo = new LogInfo();
        Timestamp t = FormatUtil.getNowTime();
        logInfo.setHostname(commonConfig.getBindIp() + "：Agent错误");
        logInfo.setCreateTime(t);
        try {
            oshi.SystemInfo si = new oshi.SystemInfo();

            HardwareAbstractionLayer hal = si.getHardware();
            OperatingSystem os = si.getOperatingSystem();

            // 操作系统信息
            systemInfo = OshiUtil.os(hal.getProcessor(), os);
            systemInfo.setCreateTime(t);
            // 文件系统信息
            List<DeskState> deskStateList = OshiUtil.file(t, os.getFileSystem());
            // cpu信息
            CpuState cpuState = OshiUtil.cpu(hal.getProcessor());
            cpuState.setCreateTime(t);
            // 内存信息
            MemState memState = OshiUtil.memory(hal.getMemory());
            memState.setCreateTime(t);
            // 网络流量信息
            NetIoState netIoState = OshiUtil.net(hal);
            netIoState.setCreateTime(t);
            // 系统负载信息
            SysLoadState sysLoadState = OshiUtil.getLoadState(systemInfo, hal.getProcessor());
            if (sysLoadState != null) {
                sysLoadState.setCreateTime(t);
            }
            if (cpuState != null) {
                jsonObject.put("cpuState", cpuState);
            }
            if (memState != null) {
                jsonObject.put("memState", memState);
            }
            if (netIoState != null) {
                jsonObject.put("netIoState", netIoState);
            }
            if (sysLoadState != null) {
                jsonObject.put("sysLoadState", sysLoadState);
            }
            if (systemInfo != null) {
                if (memState != null) {
                    systemInfo.setVersionDetail(systemInfo.getVersion() + "，总内存：" + oshi.util.FormatUtil.formatBytes(hal.getMemory().getTotal()));
                    systemInfo.setMemPer(memState.getUsePer());
                } else {
                    systemInfo.setMemPer(0d);
                }
                if (cpuState != null) {
                    systemInfo.setCpuPer(cpuState.getSys());
                } else {
                    systemInfo.setCpuPer(0d);
                }
                jsonObject.put("systemInfo", systemInfo);
            }
            if (deskStateList != null) {
                jsonObject.put("deskStateList", deskStateList);
            }
            //进程信息
            if (APP_INFO_LIST_CP.size() > 0) {
                List<AppInfo> appInfoResList = new ArrayList<>();
                List<AppState> appStateResList = new ArrayList<>();
                for (AppInfo appInfo : APP_INFO_LIST_CP) {
                    appInfo.setHostname(commonConfig.getBindIp());
                    appInfo.setCreateTime(t);
                    appInfo.setState("1");
                    String pid = FormatUtil.getPidByFile(appInfo);
                    if (StringUtils.isEmpty(pid)) {
                        continue;
                    }
                    AppState appState = OshiUtil.getLoadPid(pid, os, hal.getMemory());
                    if (appState != null) {
                        appState.setCreateTime(t);
                        appState.setAppInfoId(appInfo.getId());
                        appInfo.setMemPer(appState.getMemPer());
                        appInfo.setCpuPer(appState.getCpuPer());
                        appInfoResList.add(appInfo);
                        appStateResList.add(appState);
                    }
                }
                jsonObject.put("appInfoList", appInfoResList);
                jsonObject.put("appStateList", appStateResList);
            }

            // container state
            try {
                String[] cmd = {"/bin/sh", "-c", "docker stats --no-stream --all --format \"{{json .}}\""};
                Process process = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                JSONArray containerStateList = new JSONArray();
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        cn.hutool.json.JSONObject dockerJson = JSONUtil.parseObj(line);
                        JSONObject cs = new JSONObject();
                        cs.put("hostname", commonConfig.getBindIp());
                        cs.put("containerName", dockerJson.getStr("Name"));
                        String cpuStr = dockerJson.getStr("CPUPerc");
                        if (cpuStr != null) {
                            cs.put("cpuPer", Double.parseDouble(cpuStr.replace("%", "")));
                        }
                        String memStr = dockerJson.getStr("MemPerc");
                        if (memStr != null) {
                            cs.put("memPer", Double.parseDouble(memStr.replace("%", "")));
                        }
                        cs.put("memUsage", dockerJson.getStr("MemUsage"));
                        containerStateList.add(cs);
                    }
                }
                reader.close();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder errMsg = new StringBuilder();
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) {
                        errMsg.append(errLine);
                    }
                    errReader.close();
                    logger.error("docker stats 执行失败: {}", errMsg.toString());
                }
                if (containerStateList.size() > 0) {
                    jsonObject.put("containerStateList", containerStateList);
                }
            } catch (Exception e) {
                logger.error("采集容器状态失败", e);
            }

            // log monitor
            if (LOG_MONITOR_LIST_CP.size() > 0) {
                JSONArray logMatchArray = new JSONArray();
                for (LogMonitor lm : LOG_MONITOR_LIST_CP) {
                    if (!"1".equals(lm.getState())) continue;
                    if (StringUtils.isEmpty(lm.getLogFilePath())) continue;
                    try {
                        File logFile = new File(lm.getLogFilePath());
                        if (!logFile.exists() || !logFile.isFile()) continue;
                        String key = lm.getId();
                        Long lastPos = logFilePositions.get(key);
                        if (lastPos == null) {
                            lastPos = 0L;
                            long fileLen = logFile.length();
                            if (fileLen > 8192) {
                                lastPos = fileLen - 8192;
                            }
                        }
                        if (lastPos > logFile.length()) {
                            lastPos = 0L;
                        }
                        RandomAccessFile raf = new RandomAccessFile(logFile, "r");
                        raf.seek(lastPos);
                        String line;
                        while ((line = raf.readLine()) != null) {
                            if (line.trim().length() == 0) continue;
                            String lineStr = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                            String matchedType = matchLogLine(lm, lineStr);
                            if (matchedType != null) {
                                JSONObject match = new JSONObject();
                                match.put("logMonitorId", lm.getId());
                                match.put("hostname", commonConfig.getBindIp());
                                match.put("logFilePath", lm.getLogFilePath());
                                match.put("matchedLine", lineStr);
                                match.put("matchedType", matchedType);
                                logMatchArray.add(match);
                            }
                        }
                        logFilePositions.put(key, raf.getFilePointer());
                        raf.close();
                    } catch (Exception e) {
                        logger.error("读取日志文件失败: {}", lm.getLogFilePath(), e);
                    }
                }
                if (logMatchArray.size() > 0) {
                    jsonObject.put("logMonitorMatch", logMatchArray);
                }
            }

            logger.debug("---------------" + jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logInfo.setInfoContent(e.toString());
        } finally {
            if (!StringUtils.isEmpty(logInfo.getInfoContent())) {
                jsonObject.put("logInfo", logInfo);
            }
            restUtil.post(commonConfig.getServerUrl() + "/lins/agent/minTask", jsonObject);
        }

    }


    private String matchLogLine(LogMonitor lm, String line) {
        if (lm.getMatchSshSuccess() == 1 && SSH_SUCCESS_PATTERN.matcher(line).find()) {
            return "ssh_success";
        }
        if (lm.getMatchSshFailure() == 1 && SSH_FAILURE_PATTERN.matcher(line).find()) {
            return "ssh_failure";
        }
        if (lm.getMatchSshLogout() == 1 && SSH_LOGOUT_PATTERN.matcher(line).find()) {
            return "ssh_logout";
        }
        if (!StringUtils.isEmpty(lm.getCustomKeywords())) {
            String[] keywords = lm.getCustomKeywords().split(",");
            for (String kw : keywords) {
                String trimmed = kw.trim();
                if (trimmed.length() > 0 && line.contains(trimmed)) {
                    return "custom";
                }
            }
        }
        return null;
    }


    /**
     * 30秒后执行，每隔5分钟执行, 单位：ms。
     * 获取监控进程
     */
    @Scheduled(initialDelay = 28 * 1000L, fixedRate = 300 * 1000)
    public void appInfoListTask() {
        JSONObject jsonObject = new JSONObject();
        LogInfo logInfo = new LogInfo();
        Timestamp t = FormatUtil.getNowTime();
        logInfo.setHostname(commonConfig.getBindIp() + "：Agent获取进程列表错误");
        logInfo.setCreateTime(t);
        try {
            JSONObject paramsJson = new JSONObject();
            paramsJson.put("hostname", commonConfig.getBindIp());
            String resultJson = restUtil.post(commonConfig.getServerUrl() + "/lins/appInfo/agentList", paramsJson);
            if (resultJson != null) {
                JSONArray resultArray = JSONUtil.parseArray(resultJson);
                appInfoList.clear();
                if (resultArray.size() > 0) {
                    appInfoList = JSONUtil.toList(resultArray, AppInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo.setInfoContent(e.toString());
        } finally {
            if (!StringUtils.isEmpty(logInfo.getInfoContent())) {
                jsonObject.put("logInfo", logInfo);
            }
            restUtil.post(commonConfig.getServerUrl() + "/lins/agent/minTask", jsonObject);
        }
    }


    /**
     * 35秒后执行，每隔5分钟执行, 单位：ms。
     * 获取日志监控配置
     */
    @Scheduled(initialDelay = 35 * 1000L, fixedRate = 300 * 1000)
    public void logMonitorListTask() {
        LogInfo logInfo = new LogInfo();
        Timestamp t = FormatUtil.getNowTime();
        logInfo.setHostname(commonConfig.getBindIp() + "：Agent获取日志监控配置错误");
        logInfo.setCreateTime(t);
        try {
            JSONObject paramsJson = new JSONObject();
            paramsJson.put("hostname", commonConfig.getBindIp());
            String resultJson = restUtil.post(commonConfig.getServerUrl() + "/lins/logMonitor/agentList", paramsJson);
            if (resultJson != null) {
                JSONArray resultArray = JSONUtil.parseArray(resultJson);
                logMonitorList.clear();
                if (resultArray.size() > 0) {
                    logMonitorList = JSONUtil.toList(resultArray, LogMonitor.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo.setInfoContent(e.toString());
        } finally {
            if (!StringUtils.isEmpty(logInfo.getInfoContent())) {
                JSONObject logJson = new JSONObject();
                logJson.put("logInfo", logInfo);
                restUtil.post(commonConfig.getServerUrl() + "/lins/agent/minTask", logJson);
            }
        }
    }


}
