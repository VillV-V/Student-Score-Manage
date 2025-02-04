package com.gxnzd.scoresystem.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token的注解拦截器，拦截所有路径
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
