package com.common.services.management;


import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer
{
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        String configName = System.getProperty("config.name.management");
        return configName != null ?
            application.properties(String.format("spring.config.name=%s", configName)).sources(ManagementApplication.class) :
            application.sources(ManagementApplication.class);
    }
}