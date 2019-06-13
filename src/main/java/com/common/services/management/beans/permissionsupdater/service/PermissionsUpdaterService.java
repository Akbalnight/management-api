package com.common.services.management.beans.permissionsupdater.service;

import com.common.services.management.beans.permissionsupdater.model.PermissionsCompare;

import java.util.List;

/**
 * PermissionsUpdaterService.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Интерфейс с методами сравнения/обновления пермиссий сервисов и БД
 */
public interface PermissionsUpdaterService
{
    /**
     * Возвращает пермиссии которых нет в сервисах, но есть в БД и те что есть в сервисах но нет в БД
     * @return Список пермиссий сервисов и БД
     */
    PermissionsCompare comparePermissions();

    /**
     * Добавляет отсутсвующие в базе данных пермиссии из сервисов
     * @param roles Список ролей пользователей
     * @return Возвращает количество добавленных пермиссий
     */
    int mergePermissions(List<String> roles);

    /**
     * Экспорт неиспользуемых пермиссий сервисов в json файлы с названием сервиса
     * @return
     */
    List<String> exportPermissionsJSON();
}
