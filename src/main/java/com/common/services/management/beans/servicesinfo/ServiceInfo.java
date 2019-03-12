package com.common.services.management.beans.servicesinfo;

/**
 *
 * Данные сервиса
 */
public class ServiceInfo
{
    private String name;
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
