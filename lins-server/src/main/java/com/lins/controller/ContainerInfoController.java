package com.lins.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.lins.entity.ContainerInfo;
import com.lins.entity.ContainerState;
import com.lins.entity.SystemInfo;
import com.lins.service.*;
import com.lins.util.CodeUtil;
import com.lins.util.DateUtil;
import com.lins.util.PageUtil;
import com.lins.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/containerInfo")
public class ContainerInfoController {

    private static final Logger logger = LoggerFactory.getLogger(ContainerInfoController.class);

    @Resource
    private ContainerInfoService containerInfoService;

    @Resource
    private ContainerStateService containerStateService;

    @Resource
    private LogInfoService logInfoService;

    @Resource
    private SystemInfoService systemInfoService;

    @RequestMapping(value = "list")
    public String list(ContainerInfo containerInfo, Model model, HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<SystemInfo> systemInfoList = new ArrayList<>();
        try {
            systemInfoList = systemInfoService.selectAllByParams(new HashMap<>());
        } catch (Exception e) {
            logger.error("获取主机列表用于容器下拉框失败", e);
        }
        model.addAttribute("systemInfoList", systemInfoList);

        Map<String, String> hostRemarkMap = new HashMap<>();
        for (SystemInfo si : systemInfoList) {
            hostRemarkMap.put(si.getHostname(), si.getRemark());
        }
        model.addAttribute("hostRemarkMap", hostRemarkMap);

        try {
            StringBuffer url = new StringBuffer();
            String remark = request.getParameter("remark");
            if (!StringUtils.isEmpty(remark)) {
                remark = CodeUtil.unescape(remark);
                Map<String, Object> sysParams = new HashMap<>();
                sysParams.put("remark", remark);
                List<SystemInfo> hosts = systemInfoService.selectAllByParams(sysParams);
                if (!hosts.isEmpty()) {
                    List<String> hostnameList = new ArrayList<>();
                    for (SystemInfo h : hosts) {
                        hostnameList.add(h.getHostname());
                    }
                    params.put("hostnameList", hostnameList);
                    url.append("&remark=").append(CodeUtil.escape(remark));
                } else {
                    params.put("hostname", "__NO_RESULT__");
                    url.append("&remark=").append(CodeUtil.escape(remark));
                }
            }
            if (!StringUtils.isEmpty(containerInfo.getContainerName())) {
                params.put("containerName", containerInfo.getContainerName().trim());
                url.append("&containerName=").append(CodeUtil.escape(containerInfo.getContainerName()));
            }
            PageInfo pageInfo = containerInfoService.selectByParams(params, containerInfo.getPage(), containerInfo.getPageSize());
            PageUtil.initPageNumber(pageInfo, model);
            model.addAttribute("pageUrl", "/containerInfo/list?1=1" + url.toString());
            model.addAttribute("page", pageInfo);
            model.addAttribute("containerInfo", containerInfo);

            for (Object obj : pageInfo.getList()) {
                ContainerInfo info = (ContainerInfo) obj;
                Map<String, Object> stateParams = new HashMap<String, Object>();
                stateParams.put("hostname", info.getHostname());
                stateParams.put("containerName", info.getContainerName());
                List<ContainerState> stateList = containerStateService.selectAllByParams(stateParams);
                if (stateList.size() > 0) {
                    ContainerState latest = stateList.get(0);
                    info.setCpuPer(latest.getCpuPer());
                    info.setMemPer(latest.getMemPer());
                    info.setCreateTime(latest.getCreateTime());
                }
            }
        } catch (Exception e) {
            logger.error("查询容器信息错误", e);
            logInfoService.save("查询容器信息错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return "container/list";
    }

    @RequestMapping(value = "save")
    public String save(ContainerInfo containerInfo, Model model, HttpServletRequest request) {
        if (StringUtils.isEmpty(containerInfo.getContainerName())) {
            return "redirect:/containerInfo/list";
        }
        try {
            if (StringUtils.isEmpty(containerInfo.getId())) {
                containerInfoService.save(containerInfo);
            } else {
                containerInfoService.updateById(containerInfo);
            }
        } catch (Exception e) {
            logger.error("保存容器错误：", e);
            logInfoService.save(containerInfo.getHostname(), "保存容器错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        return "redirect:/containerInfo/list";
    }

    @ResponseBody
    @RequestMapping(value = "containerNames")
    public List<String> containerNames(HttpServletRequest request) {
        String hostname = request.getParameter("hostname");
        if (StringUtils.isEmpty(hostname)) return new ArrayList<>();
        try {
            return containerStateService.getDistinctContainerNames(hostname);
        } catch (Exception e) {
            logger.error("获取容器列表错误", e);
            return new ArrayList<>();
        }
    }

    @RequestMapping(value = "del")
    public String delete(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String errorMsg = "删除容器信息错误：";
        try {
            String id = request.getParameter("id");
            if (!StringUtils.isEmpty(id)) {
                String hostname = "";
                String containerName = "";
                ContainerInfo delContainer = containerInfoService.selectById(id);
                if (delContainer != null) {
                    hostname = StringUtils.defaultString(delContainer.getHostname());
                    containerName = StringUtils.defaultString(delContainer.getContainerName());
                }
                containerInfoService.deleteById(id.split(","));
                logInfoService.save(hostname, "删除容器：" + hostname + "：" + containerName, StaticKeys.LOG_OPERATION);
            }
        } catch (Exception e) {
            logger.error(errorMsg, e);
            logInfoService.save("删除容器错误", errorMsg + e.toString(), StaticKeys.LOG_ERROR);
        }
        return "redirect:/containerInfo/list";
    }

}
