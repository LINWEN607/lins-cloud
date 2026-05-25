package com.lins.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailConfig {


    private Double memWarnVal = 98d;
    private Double cpuWarnVal = 98d;
    private String memWarnMail;
    private String cpuWarnMail;
    private String hostDownWarnMail;
    private String appDownWarnMail;
    private String heathWarnMail;
    private String containerDownWarnMail;
    private String logMatchWarnMail;
    private String allWarnMail;


    public Double getMemWarnVal() {
        if (memWarnVal == null) {
            return 98d;
        }
        return memWarnVal;
    }

    public void setMemWarnVal(Double memWarnVal) {
        this.memWarnVal = memWarnVal;
    }

    public Double getCpuWarnVal() {
        if (cpuWarnVal == null) {
            return 98d;
        }
        return cpuWarnVal;
    }

    public void setCpuWarnVal(Double cpuWarnVal) {
        this.cpuWarnVal = cpuWarnVal;
    }

    public String getMemWarnMail() {
        if (StringUtils.isEmpty(memWarnMail) || "yes".equals(memWarnMail) || "true".equals(memWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setMemWarnMail(String memWarnMail) {
        this.memWarnMail = memWarnMail;
    }

    public String getCpuWarnMail() {
        if (StringUtils.isEmpty(cpuWarnMail) || "yes".equals(cpuWarnMail) || "true".equals(cpuWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setCpuWarnMail(String cpuWarnMail) {
        this.cpuWarnMail = cpuWarnMail;
    }

    public String getHostDownWarnMail() {
        if (StringUtils.isEmpty(hostDownWarnMail) || "yes".equals(hostDownWarnMail) || "true".equals(hostDownWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setHostDownWarnMail(String hostDownWarnMail) {
        this.hostDownWarnMail = hostDownWarnMail;
    }

    public String getAppDownWarnMail() {
        if (StringUtils.isEmpty(appDownWarnMail) || "yes".equals(appDownWarnMail) || "true".equals(appDownWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setAppDownWarnMail(String appDownWarnMail) {
        this.appDownWarnMail = appDownWarnMail;
    }

    public String getHeathWarnMail() {
        if (StringUtils.isEmpty(heathWarnMail) || "yes".equals(heathWarnMail) || "true".equals(heathWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setHeathWarnMail(String heathWarnMail) {
        this.heathWarnMail = heathWarnMail;
    }

    public String getContainerDownWarnMail() {
        if (StringUtils.isEmpty(containerDownWarnMail) || "yes".equals(containerDownWarnMail) || "true".equals(containerDownWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setContainerDownWarnMail(String containerDownWarnMail) {
        this.containerDownWarnMail = containerDownWarnMail;
    }

    public String getLogMatchWarnMail() {
        if (StringUtils.isEmpty(logMatchWarnMail) || "yes".equals(logMatchWarnMail) || "true".equals(logMatchWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setLogMatchWarnMail(String logMatchWarnMail) {
        this.logMatchWarnMail = logMatchWarnMail;
    }

    public String getAllWarnMail() {
        if (StringUtils.isEmpty(allWarnMail) || "yes".equals(allWarnMail) || "true".equals(allWarnMail)) {
            return "yes";
        }
        return "false";
    }

    public void setAllWarnMail(String allWarnMail) {
        this.allWarnMail = allWarnMail;
    }
}
