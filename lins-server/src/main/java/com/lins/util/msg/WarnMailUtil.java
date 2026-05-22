package com.lins.util.msg;

import com.lins.common.ApplicationContextHelper;
import com.lins.config.MailConfig;
import com.lins.entity.*;
import com.lins.service.LogInfoService;
import com.lins.service.SystemInfoService;
import com.lins.util.DateUtil;
import com.lins.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @version v2.3
 * @ClassName:WarnMailUtil.java
 * @author: http://www.wgstart.com
 * @date: 2019年11月16日
 * @Description: WarnMailUtil.java
 * @Copyright: 2017-2021 wgcloud. All rights reserved.
 */
public class WarnMailUtil {

    private static final Logger logger = LoggerFactory.getLogger(WarnMailUtil.class);

    public static final String content_suffix = "";

    public static Map<String, String> runtimeConfig = new ConcurrentHashMap<>();

    private static LogInfoService logInfoService = (LogInfoService) ApplicationContextHelper.getBean(LogInfoService.class);
    private static SystemInfoService systemInfoService = (SystemInfoService) ApplicationContextHelper.getBean(SystemInfoService.class);
    private static MailConfig mailConfig = (MailConfig) ApplicationContextHelper.getBean(MailConfig.class);

    private static String getConfig(String key) {
        String val = runtimeConfig.get(key);
        return val != null ? val : null;
    }

    private static boolean isAlertEnabled(String runtimeKey, java.util.function.Supplier<String> fallback) {
        String allVal = runtimeConfig.get("allWarnMail");
        if (allVal != null && StaticKeys.NO_SEND_WARN.equals(allVal)) return false;
        String specificVal = runtimeConfig.get(runtimeKey);
        if (specificVal != null) return !StaticKeys.NO_SEND_WARN.equals(specificVal);
        if (StaticKeys.NO_SEND_WARN.equals(mailConfig.getAllWarnMail())) return false;
        return !StaticKeys.NO_SEND_WARN.equals(fallback.get());
    }

    private static boolean sendToAllChannels(String title, String content) {
        boolean anySucceeded = false;
        boolean anyConfigured = false;
        if (StaticKeys.mailSet != null) {
            anyConfigured = true;
            if ("success".equals(sendMail(StaticKeys.mailSet.getToMail(), title, content))) {
                anySucceeded = true;
            }
        }
        if (StaticKeys.feishuConfig != null && "1".equals(StaticKeys.feishuConfig.getEnabled())) {
            anyConfigured = true;
            if (FeishuSender.send(StaticKeys.feishuConfig, title, content)) {
                anySucceeded = true;
            }
        }
        if (StaticKeys.dingtalkConfig != null && "1".equals(StaticKeys.dingtalkConfig.getEnabled())) {
            anyConfigured = true;
            if (DingtalkSender.send(StaticKeys.dingtalkConfig, title, content)) {
                anySucceeded = true;
            }
        }
        if (!anySucceeded) {
            logger.warn("告警推送失败：所有通知通道均未送达，请检查配置。title={}", title);
        }
        return anySucceeded;
    }


    /**
     * 判断系统内存使用率是否超过98%，超过则发送告警邮件
     *
     * @param memState
     * @param toMail
     * @return
     */
    public static boolean sendWarnInfo(MemState memState) {
        if (!isAlertEnabled("memWarnMail", mailConfig::getMemWarnMail)) {
            return false;
        }
        String key = memState.getHostname();
        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(key))) {
            return false;
        }
        if (memState.getUsePer() != null && memState.getUsePer() >= mailConfig.getMemWarnVal()) {
            try {
                String title = "内存告警：" + memState.getHostname();
                String commContent = "服务器：" + memState.getHostname() + ",内存使用率为" + Double.valueOf(memState.getUsePer()) + "%，可能存在异常，请查看";
                sendToAllChannels(title, commContent);
                WarnPools.MEM_WARN_MAP.put(key, "1");
                logInfoService.save(memState.getHostname(), commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                logger.error("发送内存告警失败：", e);
                logInfoService.save("发送内存告警错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }

        return false;
    }

    /**
     * 判断系统cpu使用率是否超过98%，超过则发送告警邮件
     *
     * @param cpuState
     * @param toMail
     * @return
     */
    public static boolean sendCpuWarnInfo(CpuState cpuState) {
        if (!isAlertEnabled("cpuWarnMail", mailConfig::getCpuWarnMail)) {
            return false;
        }
        String key = cpuState.getHostname();
        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(key))) {
            return false;
        }
        if (cpuState.getSys() != null && cpuState.getSys() >= mailConfig.getCpuWarnVal()) {
            try {
                String title = "CPU告警：" + cpuState.getHostname();
                String commContent = "服务器：" + cpuState.getHostname() + ",CPU使用率为" + Double.valueOf(cpuState.getSys()) + "%，可能存在异常，请查看";
                sendToAllChannels(title, commContent);
                WarnPools.MEM_WARN_MAP.put(key, "1");
                logInfoService.save(cpuState.getHostname(), commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                logger.error("发送CPU告警失败：", e);
                logInfoService.save("发送CPU告警错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }

        return false;
    }


    /**
     * 服务接口不通发送告警邮件
     *
     * @param cpuState
     * @param toMail
     * @return
     */
    public static boolean sendHeathInfo(HeathMonitor heathMonitor, boolean isDown) {
        if (!isAlertEnabled("heathWarnMail", mailConfig::getHeathWarnMail)) {
            return false;
        }
        String key = heathMonitor.getId();
        if (isDown) {
            if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(key))) {
                return false;
            }
            try {
                String title = "服务接口检测告警：" + heathMonitor.getAppName();
                String commContent = "服务接口：" + heathMonitor.getHeathUrl() + "，响应状态码为" + heathMonitor.getHeathStatus() + "，可能存在异常，请查看";
                sendToAllChannels(title, commContent);
                WarnPools.MEM_WARN_MAP.put(key, "1");
                logInfoService.save(heathMonitor.getAppName(), commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                logger.error("发送服务健康检测告警失败：", e);
                logInfoService.save("发送服务健康检测告警错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        } else {
            WarnPools.MEM_WARN_MAP.remove(key);
            try {
                String title = "服务接口恢复正常通知：" + heathMonitor.getAppName();
                String commContent = "服务接口恢复正常通知：" + heathMonitor.getHeathUrl() + "，响应状态码为" + heathMonitor.getHeathStatus() + "";
                sendToAllChannels(title, commContent);
                logInfoService.save(heathMonitor.getAppName(), commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                logger.error("发送服务接口恢复正常通知失败：", e);
                logInfoService.save("发送服务接口恢复正常通知错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }
        return false;
    }

    /**
     * 主机下线发送告警邮件
     *
     * @param systemInfo 主机信息
     * @param isDown     是否是下线告警，true下线告警，false上线恢复
     * @return
     */
    public static boolean sendHostDown(SystemInfo systemInfo, boolean isDown) {
        if (!isAlertEnabled("hostDownWarnMail", mailConfig::getHostDownWarnMail)) {
            return false;
        }
        String key = systemInfo.getId();
        if (isDown) {
            if ("SENT".equals(WarnPools.MEM_WARN_MAP.get(key))) {
                return false;
            }
            String title = "主机下线告警：" + systemInfo.getHostname();
            String commContent = "主机已经超过10分钟未上报数据，可能已经下线：" + systemInfo.getHostname() + "，备注：" + systemInfo.getRemark()
                    + "。如果不再监控该主机在列表删除即可，同时不会再收到该主机告警通知";
            if (sendToAllChannels(title, commContent)) {
                WarnPools.MEM_WARN_MAP.put(key, "SENT");
            } else {
                WarnPools.MEM_WARN_MAP.remove(key);
            }
            logInfoService.save(systemInfo.getHostname(), commContent, StaticKeys.LOG_ERROR);
        } else {
            WarnPools.MEM_WARN_MAP.remove(key);
            try {
                String title = "主机恢复上线通知：" + systemInfo.getHostname();
                String commContent = "主机已经恢复上线：" + systemInfo.getHostname() + "，备注：" + systemInfo.getRemark()
                        + "。";
                sendToAllChannels(title, commContent);
                logInfoService.save(systemInfo.getHostname(), commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                logger.error("发送主机恢复上线通知失败：", e);
                logInfoService.save("发送主机恢复上线通知错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }
        return false;
    }

    /**
     * 进程下线发送告警邮件
     *
     * @param AppInfo 进程信息
     * @param isDown  是否是下线告警，true下线告警，false上线恢复
     * @return
     */
    public static boolean sendAppDown(AppInfo appInfo, boolean isDown) {
        if (!isAlertEnabled("appDownWarnMail", mailConfig::getAppDownWarnMail)) {
            return false;
        }
        String key = appInfo.getId();
        if (isDown) {
            if ("SENT".equals(WarnPools.MEM_WARN_MAP.get(key))) {
                return false;
            }
            String title = "进程下线告警：" + appInfo.getHostname() + "，" + appInfo.getAppName();
            String commContent = "进程已经超过10分钟未上报数据，可能已经下线：" + appInfo.getHostname() + "，" + appInfo.getAppName()
                    + "。如果不再监控该进程在列表删除即可，同时不会再收到该进程告警通知";
            if (sendToAllChannels(title, commContent)) {
                WarnPools.MEM_WARN_MAP.put(key, "SENT");
            } else {
                WarnPools.MEM_WARN_MAP.remove(key);
            }
            logInfoService.save(appInfo.getHostname(), commContent, StaticKeys.LOG_ERROR);
        } else {
            WarnPools.MEM_WARN_MAP.remove(key);
            try {
                String title = "进程恢复上线通知：" + appInfo.getHostname() + "，" + appInfo.getAppName();
                String commContent = "进程恢复上线通知：" + appInfo.getHostname() + "，" + appInfo.getAppName();
                sendToAllChannels(title, commContent);
                logInfoService.save(appInfo.getHostname(), commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                logger.error("发送进程恢复上线通知失败：", e);
                logInfoService.save("发送进程恢复上线通知错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }
        return false;
    }

    /**
     * 容器下线发送告警
     *
     * @param containerInfo 容器信息
     * @param isDown        是否是下线告警，true下线告警，false上线恢复
     * @return
     */
    public static boolean sendContainerDown(ContainerInfo containerInfo, boolean isDown) {
        if (!isAlertEnabled("containerDownWarnMail", mailConfig::getContainerDownWarnMail)) {
            return false;
        }
        String key = containerInfo.getId();
        if (isDown) {
            if ("SENT".equals(WarnPools.MEM_WARN_MAP.get(key))) {
                return false;
            }
            String title = "容器下线告警：" + containerInfo.getHostname() + "，" + containerInfo.getContainerName();
            String commContent = "容器已经超过10分钟未上报数据，可能已经下线：" + containerInfo.getHostname() + "，" + containerInfo.getContainerName();
            if (sendToAllChannels(title, commContent)) {
                WarnPools.MEM_WARN_MAP.put(key, "SENT");
            } else {
                WarnPools.MEM_WARN_MAP.remove(key);
            }
            logInfoService.save(containerInfo.getHostname(), commContent, StaticKeys.LOG_ERROR);
        } else {
            WarnPools.MEM_WARN_MAP.remove(key);
            try {
                String title = "容器恢复上线通知：" + containerInfo.getHostname() + "，" + containerInfo.getContainerName();
                String commContent = "容器恢复上线通知：" + containerInfo.getHostname() + "，" + containerInfo.getContainerName();
                sendToAllChannels(title, commContent);
                logInfoService.save(containerInfo.getHostname(), commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                logger.error("发送容器恢复上线通知失败：", e);
                logInfoService.save("发送容器恢复上线通知错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }
        return false;
    }

    public static void sendLogMatchWarn(String hostname, String logFilePath, String matchedLine) {
        if (!isAlertEnabled("logMatchWarnMail", () -> "yes")) {
            return;
        }
        try {
            String now = DateUtil.getNowTime().toString().substring(0, 19);
            String title = "日志匹配告警：" + hostname;
            StringBuilder sb = new StringBuilder();
            sb.append("时间：").append(now).append("\n");
            sb.append("主机IP：").append(hostname).append("\n");
            sb.append("日志文件：").append(logFilePath).append("\n");
            sb.append("匹配内容：").append(matchedLine);
            String commContent = sb.toString().trim();
            sendToAllChannels(title, commContent);
            logInfoService.save(hostname, commContent, StaticKeys.LOG_ERROR);
        } catch (Exception e) {
            logger.error("发送日志匹配告警失败：", e);
            logInfoService.save("发送日志匹配告警错误", e.toString(), StaticKeys.LOG_ERROR);
        }
    }

    public static String sendMail(String mails, String mailTitle, String mailContent) {
        try {
            HtmlEmail email = new HtmlEmail();
            email.setHostName(StaticKeys.mailSet.getSmtpHost());
            email.setSmtpPort(Integer.valueOf(StaticKeys.mailSet.getSmtpPort()));
            if ("1".equals(StaticKeys.mailSet.getSmtpSSL())) {
                email.setSSL(true);
            }
            email.setAuthenticator(new DefaultAuthenticator(StaticKeys.mailSet.getFromMailName(), StaticKeys.mailSet.getFromPwd()));
            email.setFrom(StaticKeys.mailSet.getFromMailName());//发信者
            email.setSubject("[LINS监控系统] " + mailTitle);//标题
            email.setCharset("UTF-8");//编码格式
            email.setHtmlMsg(mailContent + content_suffix);//内容
            email.addTo(mails.split(";"));
            email.setSentDate(new Date());
            email.send();//发送
            return "success";
        } catch (Exception e) {
            logger.error("发送邮件错误：", e);
            logInfoService.save("发送邮件错误", e.toString(), StaticKeys.LOG_ERROR);
            return "error";
        }
    }


}
