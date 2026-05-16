package com.wgcloud.controller;

import com.wgcloud.entity.DingtalkConfig;
import com.wgcloud.entity.FeishuConfig;
import com.wgcloud.entity.MailSet;
import com.wgcloud.service.DingtalkConfigService;
import com.wgcloud.service.FeishuConfigService;
import com.wgcloud.service.LogInfoService;
import com.wgcloud.service.MailSetService;
import com.wgcloud.util.msg.DingtalkSender;
import com.wgcloud.util.msg.FeishuSender;
import com.wgcloud.util.msg.WarnMailUtil;
import com.wgcloud.util.staticvar.StaticKeys;
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
                result = WarnMailUtil.sendMail(mailSet.getToMail(), "WGCLOUD测试邮件发送", "WGCLOUD测试邮件发送");
            } else if ("feishu".equals(type)) {
                if (StringUtils.isEmpty(feishuConfig.getId())) {
                    feishuConfigService.save(feishuConfig);
                } else {
                    feishuConfigService.updateById(feishuConfig);
                }
                StaticKeys.feishuConfig = feishuConfig;
                if (!FeishuSender.send(feishuConfig, "WGCLOUD测试", "这是一条测试消息")) {
                    result = "error";
                }
            } else if ("dingtalk".equals(type)) {
                if (StringUtils.isEmpty(dingtalkConfig.getId())) {
                    dingtalkConfigService.save(dingtalkConfig);
                } else {
                    dingtalkConfigService.updateById(dingtalkConfig);
                }
                StaticKeys.dingtalkConfig = dingtalkConfig;
                if (!DingtalkSender.send(dingtalkConfig, "WGCLOUD测试", "这是一条测试消息")) {
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
}
