package com.lins.controller;

import com.lins.entity.DingtalkConfig;
import com.lins.entity.FeishuConfig;
import com.lins.entity.MailSet;
import com.lins.service.DingtalkConfigService;
import com.lins.service.FeishuConfigService;
import com.lins.service.LogInfoService;
import com.lins.service.MailSetService;
import com.lins.service.SystemConfigService;
import com.lins.util.msg.DingtalkSender;
import com.lins.util.msg.FeishuSender;
import com.lins.util.msg.WarnMailUtil;
import com.lins.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alert")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Resource
    private MailSetService mailSetService;
    @Resource
    private FeishuConfigService feishuConfigService;
    @Resource
    private DingtalkConfigService dingtalkConfigService;
    @Resource
    private LogInfoService logInfoService;
    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private com.lins.config.MailConfig mailConfig;

    @RequestMapping(value = "list")
    public String list(Model model, HttpServletRequest request) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            List<MailSet> mailList = mailSetService.selectAllByParams(params);
            if (mailList.size() > 0) {
                model.addAttribute("mailSet", mailList.get(0));
            }
            List<FeishuConfig> feishuList = feishuConfigService.selectAllByParams(params);
            if (feishuList.size() > 0) {
                model.addAttribute("feishuConfig", feishuList.get(0));
            }
            List<DingtalkConfig> dingtalkList = dingtalkConfigService.selectAllByParams(params);
            if (dingtalkList.size() > 0) {
                model.addAttribute("dingtalkConfig", dingtalkList.get(0));
            }
            model.addAttribute("allWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("allWarnMail", mailConfig.getAllWarnMail()));
            model.addAttribute("memWarnVal", WarnMailUtil.runtimeConfig.getOrDefault("memWarnVal", String.valueOf(mailConfig.getMemWarnVal().intValue())));
            model.addAttribute("cpuWarnVal", WarnMailUtil.runtimeConfig.getOrDefault("cpuWarnVal", String.valueOf(mailConfig.getCpuWarnVal().intValue())));
            model.addAttribute("memWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("memWarnMail", mailConfig.getMemWarnMail()));
            model.addAttribute("cpuWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("cpuWarnMail", mailConfig.getCpuWarnMail()));
            model.addAttribute("hostDownWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("hostDownWarnMail", mailConfig.getHostDownWarnMail()));
            model.addAttribute("appDownWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("appDownWarnMail", mailConfig.getAppDownWarnMail()));
            model.addAttribute("heathWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("heathWarnMail", mailConfig.getHeathWarnMail()));
            model.addAttribute("containerDownWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("containerDownWarnMail", mailConfig.getContainerDownWarnMail()));
        } catch (Exception e) {
            logger.error("查询告警设置错误", e);
            logInfoService.save("查询告警设置错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        String tab = request.getParameter("tab");
        if (StringUtils.isEmpty(tab)) {
            tab = "email";
        }
        model.addAttribute("tab", tab);
        String msg = request.getParameter("msg");
        if (!StringUtils.isEmpty(msg)) {
            if (msg.equals("save")) {
                model.addAttribute("msg", "保存成功");
            } else if (msg.equals("test")) {
                String result = request.getParameter("result");
                if ("success".equals(result)) {
                    model.addAttribute("msg", "测试发送成功");
                } else {
                    model.addAttribute("msg", "测试发送失败，请查看日志");
                }
            }
        } else {
            model.addAttribute("msg", "");
        }
        return "alert/list";
    }

    @RequestMapping(value = "config")
    public String config(Model model, HttpServletRequest request) {
        try {
            model.addAttribute("allWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("allWarnMail", mailConfig.getAllWarnMail()));
            model.addAttribute("memWarnVal", WarnMailUtil.runtimeConfig.getOrDefault("memWarnVal", String.valueOf(mailConfig.getMemWarnVal().intValue())));
            model.addAttribute("cpuWarnVal", WarnMailUtil.runtimeConfig.getOrDefault("cpuWarnVal", String.valueOf(mailConfig.getCpuWarnVal().intValue())));
            model.addAttribute("memWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("memWarnMail", mailConfig.getMemWarnMail()));
            model.addAttribute("cpuWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("cpuWarnMail", mailConfig.getCpuWarnMail()));
            model.addAttribute("hostDownWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("hostDownWarnMail", mailConfig.getHostDownWarnMail()));
            model.addAttribute("appDownWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("appDownWarnMail", mailConfig.getAppDownWarnMail()));
            model.addAttribute("heathWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("heathWarnMail", mailConfig.getHeathWarnMail()));
            model.addAttribute("containerDownWarnMail", WarnMailUtil.runtimeConfig.getOrDefault("containerDownWarnMail", mailConfig.getContainerDownWarnMail()));
        } catch (Exception e) {
            logger.error("查询告警配置错误", e);
        }
        String msg = request.getParameter("msg");
        model.addAttribute("msg", msg != null ? "保存成功" : "");
        return "alert/config";
    }

    @RequestMapping(value = "save")
    public String save(@RequestParam(defaultValue = "email") String type,
                       MailSet mailSet,
                       FeishuConfig feishuConfig,
                       DingtalkConfig dingtalkConfig,
                       Model model, HttpServletRequest request) {
        try {
            if ("email".equals(type)) {
                if (StringUtils.isEmpty(mailSet.getId())) {
                    mailSetService.save(mailSet);
                } else {
                    mailSetService.updateById(mailSet);
                }
                StaticKeys.mailSet = mailSet;
            } else if ("feishu".equals(type)) {
                if (StringUtils.isEmpty(feishuConfig.getId())) {
                    feishuConfigService.save(feishuConfig);
                } else {
                    feishuConfigService.updateById(feishuConfig);
                }
                StaticKeys.feishuConfig = feishuConfig;
            } else if ("dingtalk".equals(type)) {
                if (StringUtils.isEmpty(dingtalkConfig.getId())) {
                    dingtalkConfigService.save(dingtalkConfig);
                } else {
                    dingtalkConfigService.updateById(dingtalkConfig);
                }
                StaticKeys.dingtalkConfig = dingtalkConfig;
            }
        } catch (Exception e) {
            logger.error("保存告警设置错误", e);
            logInfoService.save("保存告警设置错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return "redirect:/alert/list?msg=save&tab=" + type;
    }

    @RequestMapping(value = "test")
    public String test(@RequestParam(defaultValue = "email") String type,
                       MailSet mailSet,
                       FeishuConfig feishuConfig,
                       DingtalkConfig dingtalkConfig,
                       Model model, HttpServletRequest request) {
        String result = "success";
        try {
            if ("email".equals(type)) {
                if (StringUtils.isEmpty(mailSet.getId())) {
                    mailSetService.save(mailSet);
                } else {
                    mailSetService.updateById(mailSet);
                }
                StaticKeys.mailSet = mailSet;
                result = WarnMailUtil.sendMail(mailSet.getToMail(), "LINS测试邮件发送", "LINS测试邮件发送");
            } else if ("feishu".equals(type)) {
                if (StringUtils.isEmpty(feishuConfig.getId())) {
                    feishuConfigService.save(feishuConfig);
                } else {
                    feishuConfigService.updateById(feishuConfig);
                }
                StaticKeys.feishuConfig = feishuConfig;
                if (!FeishuSender.send(feishuConfig, "LINS测试", "这是一条测试消息")) {
                    result = "error";
                }
            } else if ("dingtalk".equals(type)) {
                if (StringUtils.isEmpty(dingtalkConfig.getId())) {
                    dingtalkConfigService.save(dingtalkConfig);
                } else {
                    dingtalkConfigService.updateById(dingtalkConfig);
                }
                StaticKeys.dingtalkConfig = dingtalkConfig;
                if (!DingtalkSender.send(dingtalkConfig, "LINS测试", "这是一条测试消息")) {
                    result = "error";
                }
            }
        } catch (Exception e) {
            logger.error("测试告警设置错误", e);
            logInfoService.save("测试告警设置错误", e.toString(), StaticKeys.LOG_ERROR);
            result = "error";
        }
        return "redirect:/alert/list?msg=test&result=" + result + "&tab=" + type;
    }

    @RequestMapping(value = "saveConfig")
    public String saveConfig(HttpServletRequest request) {
        try {
            saveConfigVal("allWarnMail", request.getParameter("allWarnMail"));
            saveConfigVal("memWarnVal", request.getParameter("memWarnVal"));
            saveConfigVal("cpuWarnVal", request.getParameter("cpuWarnVal"));
            saveConfigVal("memWarnMail", request.getParameter("memWarnMail"));
            saveConfigVal("cpuWarnMail", request.getParameter("cpuWarnMail"));
            saveConfigVal("hostDownWarnMail", request.getParameter("hostDownWarnMail"));
            saveConfigVal("appDownWarnMail", request.getParameter("appDownWarnMail"));
            saveConfigVal("heathWarnMail", request.getParameter("heathWarnMail"));
            saveConfigVal("containerDownWarnMail", request.getParameter("containerDownWarnMail"));
        } catch (Exception e) {
            logger.error("保存告警配置错误", e);
            logInfoService.save("保存告警配置错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return "redirect:/alert/list?msg=save&tab=config";
    }

    private void saveConfigVal(String key, String value) {
        if (StringUtils.isEmpty(value)) return;
        try {
            systemConfigService.setVal(key, value);
            WarnMailUtil.runtimeConfig.put(key, value);
        } catch (Exception e) {
            logger.error("保存配置项失败，key={}", key, e);
        }
    }
}
