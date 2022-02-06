package com.atguigu.crowd.filter;

import com.atguigu.crowd.entity.vo.MemberLoginVO;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.fall.constant.CrowdConstant;
import org.fall.utils.AccessPassResources;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CrowdAccessFilter extends ZuulFilter {
    @Override
    public String filterType() {
        // pre表示在微服务前拦截
        return "pre";
    }

    @Override
    public int filterOrder() {
        // 只有一个filter不设置次序
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        // 使用ThreadLocal线程本地化技术获取request
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        // 根据request获取请求路径
        String servletPath = request.getServletPath();

        // 判断路径是否需要放行
        boolean containPath = AccessPassResources.PASS_RES_SET.contains(servletPath);

        // 如果路径在放行的set集合中，返回false放行
        if (containPath){
            return false;
        }

        // 判断路径是否为静态资源
        boolean currentServletPathWhetherStaticResource = AccessPassResources.judgeCurrentServletPathWhetherStaticResource(servletPath);

        // 为静态资源则，返回false放行，反之则返回true执行run方法
        return !currentServletPathWhetherStaticResource;
    }

    @Override
    public Object run() throws ZuulException {
        // 使用ThreadLocal线程本地化技术获取request
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        // 从request中获取session对象
        HttpSession session = request.getSession();

        // 从session中取出保存的登录对象
        Object loginMember =  session.getAttribute(CrowdConstant.ATTR_NAME_LOGIN_MEMBER);

        // 如果没有登录对象
        if (loginMember == null){
            // 从 currentContext 对象中获取 Response 对象
            HttpServletResponse response = currentContext.getResponse();

            // 将提示消息存入session域
            session.setAttribute(CrowdConstant.ATTR_NAME_LOGIN_MEMBER,CrowdConstant.MESSAGE_ACCESS_FORBIDEN);

            // 重定向到登录页面
            try {
                // 直接重定向登录页面，而不是登录请求
                response.sendRedirect("/auth/to/member/login/page");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
