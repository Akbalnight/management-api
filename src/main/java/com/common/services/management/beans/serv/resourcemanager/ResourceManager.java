package com.common.services.management.beans.serv.resourcemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * ResourceManager.java
 * Date: 3 сент. 2018 г.
 * Users: vmeshkov
 * Description: Менеджер сообщений.
 */
@Component("resourceManager")
public class ResourceManager
{
    public static final String SERVICE_NOT_FOUND = "exceptions.service_not_found";
    public static final String PARAMETER_MISSED = "exceptions.parameter_missed";
    public static final String INTERNAL_SERVICE_ERROR =  "exceptions.internal_service_error";
    public static final String TIME_DURATION = "audit.message.time_duration";
    public static final String LESS_SECOND =  "audit.message.less_second";
    public static final String INVALID_PARAMETER_VALUE = "exceptions.invalid_parameter_value";
    public static final String USER_AUTHTORIZATION_ALREADY = "error.auth.message.already_authorized";
    public static final String ERROR_USER_ID_EMPTY = "error.auth.userIdEmpty";


    @Autowired
    private MessageSource resource;

    /**
     * Получить строку по ключу
     * @param key ключ
     * @param args Список аргументов
     * @return  сообщение
     */
    public String getResource(String key, Object ...args)
    {
        return resource.getMessage(key, args, null);
    }
}
