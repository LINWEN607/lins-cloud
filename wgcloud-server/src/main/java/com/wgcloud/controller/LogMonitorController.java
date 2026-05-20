package com.wgcloud.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.wgcloud.entity.LogMonitor;
import com.wgcloud.entity.SystemInfo;
import com.wgcloud.service.LogInfoService;
import com.wgcloud.service.LogMonitorService;
import com.wgcloud.service.SystemInfoService;
import com.wgcloud.util.CodeUtil;
import com.wgcloud.util.PageUtil;
import com.wgcloud.util.TokenUtils;
import com.wgcloud.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/logMonitor")
public class LogMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(LogMonitorController.class);

    @Resource
    private LogMonitorService logMonitorService;

    @Resource
    private SystemInfoService systemInfoService;

    @Resource
    private LogInfoService logInfoService;

    @Autowired
    private TokenUtils tokenUtils;

    @ResponseBody
    @RequestMapping("agentList")
    public String agentList(@RequestBody String paramBean) {
        JSONObject agentJsonObject = (JSONObject) JSONUtil.parse(paramBean);
        if (!tokenUtils.checkAgentToken(agentJsonObject)) {
            logger.error("token is invalidate");
            return "error：token is invalidate";
        }
        Map<String, Object> params = new HashMap<String, Object>();
        if (null == agentJsonObject.get("hostname") || StringUtils.isEmpty(agentJsonObject.get("hostname").toString())) {
            return "";
        }
        params.put("hostname", agentJsonObject.get("hostname").toString());
        params.put("state", "1");
        try {
            List<LogMonitor> list = logMonitorService.selectAllByParams(params);
            return JSONUtil.toJsonStr(list);
        } catch (Exception e) {
            logger.error("agent获取日志监控配置错误", e);
            logInfoService.save("agent获取日志监控配置错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return "";
    }

    @RequestMapping("ssh/list")
    public String sshList(LogMonitor logMonitor, Model model, HttpServletRequest request) {
        return list("ssh", logMonitor, model, request);
    }

    @RequestMapping("nginx/list")
    public String nginxList(LogMonitor logMonitor, Model model, HttpServletRequest request) {
        return list("nginx", logMonitor, model, request);
    }

    private String list(String monitorType, LogMonitor logMonitor, Model model, HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("monitorType", monitorType);
        try {
            List<SystemInfo> systemInfoList = systemInfoService.selectAllByParams(new HashMap<>());
            model.addAttribute("systemInfoList", systemInfoList);

            Map<String, String> hostRemarkMap = new HashMap<>();
            for (SystemInfo si : systemInfoList) {
                hostRemarkMap.put(si.getHostname(), si.getRemark());
            }
            model.addAttribute("hostRemarkMap", hostRemarkMap);

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
            PageInfo pageInfo = logMonitorService.selectByParams(params, logMonitor.getPage(), logMonitor.getPageSize());
            PageUtil.initPageNumber(pageInfo, model);
            model.addAttribute("pageUrl", "/logMonitor/" + monitorType + "/list?1=1" + url.toString());
            model.addAttribute("page", pageInfo);
            model.addAttribute("logMonitor", logMonitor);
            model.addAttribute("monitorType", monitorType);
        } catch (Exception e) {
            logger.error("查询日志监控错误", e);
            logInfoService.save("查询日志监控错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return "log/" + monitorType + "/list";
    }

    @RequestMapping("save")
    public String save(LogMonitor logMonitor, Model model, HttpServletRequest request) {
        String monitorType = logMonitor.getMonitorType();
        if (StringUtils.isEmpty(monitorType)) {
            monitorType = "ssh";
        }
        try {
            if (StringUtils.isEmpty(logMonitor.getId())) {
                logMonitor.setMonitorType(monitorType);
                logMonitorService.save(logMonitor);
            } else {
                logMonitorService.updateById(logMonitor);
            }
        } catch (Exception e) {
            logger.error("保存日志监控错误", e);
            logInfoService.save(logMonitor.getHostname(), "保存日志监控错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        return "redirect:/logMonitor/" + monitorType + "/list";
    }

    @RequestMapping("ssh/edit")
    public String sshEdit(Model model, HttpServletRequest request) {
        return edit("ssh", model, request);
    }

    @RequestMapping("nginx/edit")
    public String nginxEdit(Model model, HttpServletRequest request) {
        return edit("nginx", model, request);
    }

    private String edit(String monitorType, Model model, HttpServletRequest request) {
        String id = request.getParameter("id");
        LogMonitor logMonitor = new LogMonitor();
        try {
            List<SystemInfo> systemInfoList = systemInfoService.selectAllByParams(new HashMap<>());
            model.addAttribute("systemInfoList", systemInfoList);
            if (!StringUtils.isEmpty(id)) {
                logMonitor = logMonitorService.selectById(id);
            } else {
                logMonitor.setMonitorType(monitorType);
            }
            model.addAttribute("logMonitor", logMonitor);
            model.addAttribute("monitorType", monitorType);
        } catch (Exception e) {
            logger.error("编辑日志监控错误", e);
            logInfoService.save("编辑日志监控错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return "log/" + monitorType + "/add";
    }

    @RequestMapping("ssh/del")
    public String sshDelete(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return delete("ssh", model, request, redirectAttributes);
    }

    @RequestMapping("nginx/del")
    public String nginxDelete(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return delete("nginx", model, request, redirectAttributes);
    }

    private String delete(String monitorType, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            if (!StringUtils.isEmpty(request.getParameter("id"))) {
                logMonitorService.deleteById(request.getParameter("id").split(","));
            }
        } catch (Exception e) {
            logger.error("删除日志监控错误", e);
            logInfoService.save("删除日志监控错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return "redirect:/logMonitor/" + monitorType + "/list";
    }
}
