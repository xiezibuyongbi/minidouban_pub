package com.minidouban.configuration;

import com.minidouban.interceptor.AlreadyLoggedOnInterceptor;
import com.minidouban.interceptor.AuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfigurer implements WebMvcConfigurer {
    @Resource
    private AuthorizationInterceptor authorizationInterceptor;
    @Resource
    private AlreadyLoggedOnInterceptor alreadyLoggedOnInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alreadyLoggedOnInterceptor)
                .addPathPatterns("/login**", "/register**", "reset_password**");
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/logout**", "/reading_list**", "/add-book**", "/rename-list**", "/create-list**",
                                 "/delete-list**", "/delete-all-list**", "/remove-book**");
    }

}
