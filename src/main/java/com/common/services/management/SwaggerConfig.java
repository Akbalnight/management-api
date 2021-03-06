package com.common.services.management;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * SwaggerConfig.java
 * Date: 5 окт. 2018 г.
 * Users: vmeshkov
 * Description: Конфигурация для работы свагера
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig
{
    @Bean
    public Docket productApi()
    {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.common.services.management"))
                .build()
                .useDefaultResponseMessages(false);
    }
}
