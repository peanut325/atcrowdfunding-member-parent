package com.atguigu.crowd.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CrowdWebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 转发请求的url路径和视图名
        String registerUrl = "/auth/to/member/reg/page";
        String registerViewName = "member-reg";

        // 登录请求的url路径和视图名
        String loginUrl = "/auth/to/member/login/page";
        String loginViewName = "member-login";

        // 主页面请求的url路径和视图名
        String centerUrl = "/auth/to/member/center/page";
        String centerViewName = "member-center";

        // 众筹页面请求的url路径和视图名
        String crowdUrl = "/member/my/crowd";
        String crowdViewName = "member-crowd";

        // 前往注册页面
        registry.addViewController(registerUrl).setViewName(registerViewName);
        // 前往登录页面
        registry.addViewController(loginUrl).setViewName(loginViewName);
        // 前往主页面
        registry.addViewController(centerUrl).setViewName(centerViewName);
        // 前往众筹页面
        registry.addViewController(crowdUrl).setViewName(crowdViewName);
    }
}
