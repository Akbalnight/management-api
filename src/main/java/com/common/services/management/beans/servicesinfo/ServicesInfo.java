package com.common.services.management.beans.servicesinfo;

import java.util.List;

/**
 *
 * Список сервисов с их данными
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
