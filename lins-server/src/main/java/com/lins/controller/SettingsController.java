package com.lins.controller;

import com.lins.config.CommonConfig;
import com.lins.service.LogInfoService;
import com.lins.service.SystemConfigService;
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

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private CommonConfig commonConfig;
    @Resource
    private LogInfoService logInfoService;

    @RequestMapping(value = "pwd")
    public String pwd(Model model, HttpServletRequest request) {
        String msg = request.getParameter("msg");
        if (!StringUtils.isEmpty(msg)) {
            if ("success".equals(msg)) {
                model.addAttribute("msg", "密码修改成功");
            } else {
                model.addAttribute("msg", "密码修改失败，请检查原密码是否正确");
            }
        } else {
            model.addAttribute("msg", "");
        }
        return "settings/password";
    }

    @RequestMapping(value = "savePwd")
    public String savePwd(@RequestParam String currentPwd, @RequestParam String newPwd, Model model) {
        try {
            String dbPwd = systemConfigService.getVal("adminPwd");
            String validPwd = dbPwd != null ? dbPwd : com.lins.util.shorturl.MD5.GetMD5Code(commonConfig.getAdmindPwd());
            if (!validPwd.equals(currentPwd)) {
                return "redirect:/settings/pwd?msg=error";
            }
            systemConfigService.setVal("adminPwd", newPwd);
            return "redirect:/settings/pwd?msg=success";
        } catch (Exception e) {
            logger.error("修改密码错误", e);
            logInfoService.save("", "修改密码错误", StaticKeys.LOG_ERROR);
            return "redirect:/settings/pwd?msg=error";
        }
    }
}
