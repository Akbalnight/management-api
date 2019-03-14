package com.common.services.management.beans.servicesinfo;

/**
 * ServiceInfo.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Данные сервиса
 */
public class ServiceInfo
{
    /**
     * Название сервиса
     */
    private String name;

    /**
     * URL адрес с API сервиса
     */
    private String apiUrl;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getApiUrl()
    {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl)
    {
        this.apiUrl = apiUrl;
    }
}
