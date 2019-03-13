package com.common.services.management.beans.servicesinfo;

import java.util.List;

/**
 * ServicesInfo.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Список сервисов с их данными
 */
public class ServicesInfo
{
    List<ServiceInfo> services;

    public List<ServiceInfo> getServices()
    {
        return services;
    }

    public void setServices(List<ServiceInfo> services)
    {
        this.services = services;
    }
}
