package com.wgcloud.entity;

import java.util.Date;

public class ContainerInfo extends BaseEntity {

    private static final long serialVersionUID = -2913111613773445949L;

    private String hostname;

    private String containerName;

    private String state;

    private Double cpuPer;

    private Double memPer;

    private Date createTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getCpuPer() {
        return cpuPer;
    }

    public void setCpuPer(Double cpuPer) {
        this.cpuPer = cpuPer;
    }

    public Double getMemPer() {
        return memPer;
    }

    public void setMemPer(Double memPer) {
        this.memPer = memPer;
    }
}
