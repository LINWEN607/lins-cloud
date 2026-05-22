package com.lins.entity;

public class LogMonitor extends BaseEntity {

    private String hostname;

    private String logFilePath;

    private String regexPattern;

    private Integer enableAlert = 1;

    private String state;

    private String remark;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public Integer getEnableAlert() {
        return enableAlert;
    }

    public void setEnableAlert(Integer enableAlert) {
        this.enableAlert = enableAlert;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
