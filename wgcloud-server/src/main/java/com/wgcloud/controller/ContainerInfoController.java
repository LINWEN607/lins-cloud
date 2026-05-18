package com.wgcloud.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.wgcloud.entity.ContainerInfo;
import com.wgcloud.entity.ContainerState;
import com.wgcloud.entity.SystemInfo;
import com.wgcloud.service.*;
import com.wgcloud.util.CodeUtil;
import com.wgcloud.util.DateUtil;
import com.wgcloud.util.PageUtil;
import com.wgcloud.util.staticvar.StaticKeys;
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
    public String list(ContainerInfo containerInfo, Model model) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<SystemInfo> systemInfoList = new ArrayList<>();
        try {
            systemInfoList = systemInfoService.selectAllByParams(new HashMap<>());
        } catch (Exception e) {
            logger.error("获取主机列表用于容器下拉框失败", e);
        }
        model.addAttribute("systemInfoList", systemInfoList);
        try {
            StringBuffer url = new StringBuffer();
            String hostname = null;
            if (!StringUtils.isEmpty(containerInfo.getHostname())) {
                hostname = CodeUtil.unescape(containerInfo.getHostname());
                params.put("hostname", hostname.trim());
                url.append("&hostname=").append(CodeUtil.escape(hostname));
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
                    info.setState("1");
                } else {
                    info.setState("2");
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

    @RequestMapping(value = "edit")
    public String edit(HttpServletRequest request) {
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
        ContainerInfo containerInfo = new ContainerInfo();
        try {
            if (!StringUtils.isEmpty(request.getParameter("id"))) {
                containerInfo = containerInfoService.selectById(request.getParameter("id"));
                String hostname = StringUtils.defaultString(containerInfo.getHostname());
                String containerName = StringUtils.defaultString(containerInfo.getContainerName());
                logInfoService.save("删除容器：" + hostname, "删除容器：" + hostname + "：" + containerName, StaticKeys.LOG_ERROR);
                containerInfoService.deleteById(request.getParameter("id").split(","));
            }
        } catch (Exception e) {
            logger.error(errorMsg, e);
            logInfoService.save(StringUtils.defaultString(containerInfo.getHostname()) + ":" + StringUtils.defaultString(containerInfo.getContainerName()), errorMsg + e.toString(), StaticKeys.LOG_ERROR);
        }
        return "redirect:/containerInfo/list";
    }

}
