package com.common.services.management.beans.permissionsupdater.service;

import com.common.services.management.beans.management.model.Permission;
import com.common.services.management.beans.servicesinfo.ServiceInfo;

import java.util.List;

/**
 * ApiReader.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Интерфейс для получения списка пермиссий сервиса
 */
public interface ApiReader
{
    /**
     * Получение пермиссий сервиса
     * @param info Данные сервиса
     * @return Возвращает список пермиссий сервиса
     */
    List<Permission> read(ServiceInfo info);
}
