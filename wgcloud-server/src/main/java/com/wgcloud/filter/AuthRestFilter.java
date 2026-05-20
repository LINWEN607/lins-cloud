package com.wgcloud.filter;


import com.wgcloud.entity.AccountInfo;
import com.wgcloud.util.staticvar.StaticKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @version v2.3
 * @ClassName:AuthRestFilter.java
 * @author: http://www.wgstart.com
 * @date: 2019年11月16日
 * @Description: http请求过滤器，拦截不是从路由过来的请求
 * @Copyright: 2017-2021 wgcloud. All rights reserved.
 */
@WebFilter(filterName = "authRestFilter", urlPatterns = {"/*"})
public class AuthRestFilter implements Filter {

    static Logger log = LoggerFactory.getLogger(AuthRestFilter.class);

    String[] static_resource = {"/agent/minTask", "/login/toLogin", "/login/login", "/appInfo/agentList", "/logMonitor/agentList", "/static/", "/settings/"};

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpSession session = request.getSession();
        AccountInfo accountInfo = (AccountInfo) session.getAttribute(StaticKeys.LOGIN_KEY);
        String uri = request.getRequestURI();
        log.debug("uri----" + uri);
        String servletPath = request.getServletPath();
        log.debug("servletPath----" + servletPath);
        menuActive(session, uri);
        for (String ss : static_resource) {
            if (servletPath.startsWith(ss)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
        if (accountInfo == null) {
            response.sendRedirect(request.getContextPath() + "/login/toLogin");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }


    /**
     * 添加菜单标识
     *
     * @param session
     * @param uri
     */
    public void menuActive(HttpSession session, String uri) {
        if (uri.indexOf("/log/") > -1) {
            session.setAttribute("menuActive", "21");
            return;
        }
        if (uri.indexOf("/dash/main") > -1) {
            session.setAttribute("menuActive", "11");
            return;
        }
        if (uri.indexOf("/dash/systemInfoList") > -1 || uri.indexOf("/dash/detail") > -1 || uri.indexOf("/dash/chart") > -1) {
            session.setAttribute("menuActive", "12");
            return;
        }
        if (uri.indexOf("/appInfo") > -1) {
            session.setAttribute("menuActive", "13");
            return;
        }
        if (uri.indexOf("/containerInfo") > -1) {
            session.setAttribute("menuActive", "14");
            return;
        }
        if (uri.indexOf("/alert/config") > -1) {
            session.setAttribute("menuActive", "32");
            return;
        }
        if (uri.indexOf("/mailset") > -1 || uri.indexOf("/alert") > -1) {
            session.setAttribute("menuActive", "31");
            return;
        }
        if (uri.indexOf("/dbInfo") > -1) {
            session.setAttribute("menuActive", "41");
            return;
        }
        if (uri.indexOf("/dbTable") > -1) {
            session.setAttribute("menuActive", "42");
            return;
        }
        if (uri.indexOf("/logMonitor/ssh") > -1) {
            session.setAttribute("menuActive", "431");
            return;
        }
        if (uri.indexOf("/logMonitor/nginx") > -1) {
            session.setAttribute("menuActive", "432");
            return;
        }
        if (uri.indexOf("/logMonitor") > -1) {
            session.setAttribute("menuActive", "43");
            return;
        }
        if (uri.indexOf("/heathMonitor") > -1) {
            session.setAttribute("menuActive", "51");
            return;
        }
        if (uri.indexOf("/settings/pwd") > -1) {
            session.setAttribute("menuActive", "61");
            return;
        }
        session.setAttribute("menuActive", "11");
        return;

    }

}
