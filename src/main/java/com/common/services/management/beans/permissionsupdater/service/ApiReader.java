package com.common.services.management.beans.permissionsupdater.service;

import com.common.services.management.beans.management.model.Permission;
import com.common.services.management.beans.servicesinfo.ServiceInfo;

import java.util.List;

/**
 *
 * Интерфейс для получения списка пермиссий сервиса
 */
public interface ApiReader
{
    List<Permission> read(ServiceInfo info);
}
