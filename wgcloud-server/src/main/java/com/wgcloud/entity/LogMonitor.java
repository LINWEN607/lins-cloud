package com.wgcloud.entity;

import java.util.Date;

public class LogMonitor extends BaseEntity {

    private String monitorType;

    private String hostname;

    private String logFilePath;

    private Integer matchSshSuccess = 0;

    private Integer matchSshFailure = 0;

    private Integer matchSshLogout = 0;

    private String customKeywords;

    private Integer enableAlert = 1;

    private String state;

    private String remark;

    private Date createTime;

    public String getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(String monitorType) {
        this.monitorType = monitorType;
    }

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

    public Integer getMatchSshSuccess() {
        return matchSshSuccess;
    }

    public void setMatchSshSuccess(Integer matchSshSuccess) {
        this.matchSshSuccess = matchSshSuccess;
    }

    public Integer getMatchSshFailure() {
        return matchSshFailure;
    }

    public void setMatchSshFailure(Integer matchSshFailure) {
        this.matchSshFailure = matchSshFailure;
    }

    public Integer getMatchSshLogout() {
        return matchSshLogout;
    }

    public void setMatchSshLogout(Integer matchSshLogout) {
        this.matchSshLogout = matchSshLogout;
    }

    public String getCustomKeywords() {
        return customKeywords;
    }

    public void setCustomKeywords(String customKeywords) {
        this.customKeywords = customKeywords;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
