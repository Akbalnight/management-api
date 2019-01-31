package com.common.services.management.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * LoggerConfig.java
 * Date: 1 нояб. 2018 г.
 * Users: vmeshkov
 * Description: Конфигуратор для добавления интерцептора логгера
 */
@Configuration
public class LoggerConfig implements WebMvcConfigurer
{
    @Autowired
    private LoggerInterceptor loggerInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(loggerInterceptor);
        WebMvcConfigurer.super.addInterceptors(registry);
    }

}
