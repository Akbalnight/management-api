package com.common.services.management.beans.serv.resourcemanager;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MessagesConfig.java
 * Date: 5 сент. 2018 г.
 * Users: vmeshkov
 * Description: Класс для конфигурации (настройка ресурсного менеджера)
 */

@Configuration
public class MessagesConfig
    implements WebMvcConfigurer
{
    /*
     * Настроим ресурсный менеджер, файл с ресурсами, кодировку
     */
    @Bean
    public MessageSource messageSource()
    {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Файл с сообщениями
        messageSource.setBasename("classpath:resources");
        // Зададим кодировку  
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
