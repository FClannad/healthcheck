package com.example.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JWTInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/")
                .excludePathPatterns("/login", "/register", "/files/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**")
                .excludePathPatterns("/api/crawler/**")
                .excludePathPatterns("/api/literature/**")
                .excludePathPatterns("/medical-literature/**")
                .excludePathPatterns("/simple-ai/**")
                .excludePathPatterns("/ai-history/**")
                .excludePathPatterns("/ai-consultation/stream");  // SSE流式接口不需要JWT拦截器验证
    }

}
