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
        if (StringUtils.isEmpty(memWarnMail)) {
            return "yes";
        }
        return memWarnMail;
    }

    public void setMemWarnMail(String memWarnMail) {
        this.memWarnMail = memWarnMail;
    }

    public String getCpuWarnMail() {
        if (StringUtils.isEmpty(cpuWarnMail)) {
            return "yes";
        }
        return cpuWarnMail;
    }

    public void setCpuWarnMail(String cpuWarnMail) {
        this.cpuWarnMail = cpuWarnMail;
    }

    public String getHostDownWarnMail() {
        if (StringUtils.isEmpty(hostDownWarnMail)) {
            return "yes";
        }
        return hostDownWarnMail;
    }

    public void setHostDownWarnMail(String hostDownWarnMail) {
        this.hostDownWarnMail = hostDownWarnMail;
    }

    public String getAppDownWarnMail() {
        if (StringUtils.isEmpty(appDownWarnMail)) {
            return "yes";
        }
        return appDownWarnMail;
    }

    public void setAppDownWarnMail(String appDownWarnMail) {
        this.appDownWarnMail = appDownWarnMail;
    }

    public String getHeathWarnMail() {
        if (StringUtils.isEmpty(heathWarnMail)) {
            return "yes";
        }
        return heathWarnMail;
    }

    public void setHeathWarnMail(String heathWarnMail) {
        this.heathWarnMail = heathWarnMail;
    }

    public String getContainerDownWarnMail() {
        if (StringUtils.isEmpty(containerDownWarnMail)) {
            return "yes";
        }
        return containerDownWarnMail;
    }

    public void setContainerDownWarnMail(String containerDownWarnMail) {
        this.containerDownWarnMail = containerDownWarnMail;
    }

    public String getLogMatchWarnMail() {
        if (StringUtils.isEmpty(logMatchWarnMail)) {
            return "yes";
        }
        return logMatchWarnMail;
    }

    public void setLogMatchWarnMail(String logMatchWarnMail) {
        this.logMatchWarnMail = logMatchWarnMail;
    }

    public String getAllWarnMail() {
        if (StringUtils.isEmpty(allWarnMail)) {
            return "yes";
        }
        return allWarnMail;
    }

    public void setAllWarnMail(String allWarnMail) {
        this.allWarnMail = allWarnMail;
    }
}
