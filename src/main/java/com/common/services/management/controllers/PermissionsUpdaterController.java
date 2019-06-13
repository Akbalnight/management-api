package com.common.services.management.controllers;

import com.common.services.management.beans.permissionsupdater.model.PermissionsCompare;
import com.common.services.management.beans.permissionsupdater.service.PermissionsUpdaterService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * PermissionsUpdaterController.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Контроллер с методами сравнения/обновления пермиссий сервисов и БД
 */
@RestController
@RequestMapping(path = "updater")
public class PermissionsUpdaterController
{
    @Autowired
    private PermissionsUpdaterService updaterService;

    @GetMapping(value = "/permissions/compare")
    @ApiOperation(value = "Сравнение пермиссий базы данных с пермиссиями сервисов",
            notes = "Возвращает список пермиссий, которых нет в сервисах, но есть в БД и тех что есть в сервисах, но нет в БД")
    public PermissionsCompare comparePermissions()
    {
        return updaterService.comparePermissions();
    }

    @GetMapping(value = "/permissions/merge")
    @ApiOperation(value = "Добавляет отсутсвующие в базе данных пермиссии из сервисов",
            notes = "Если указан список ролей, то все пермиссии будут назначены этим ролям")
    public int mergePermissions(@ApiParam(value = "Список ролей", required = true) @RequestBody List<String> roles)
    {
        return updaterService.mergePermissions(roles);
    }

    @GetMapping(value = "/permissions/export")
    @ApiOperation(value = "Экспорт неиспользуемых пермиссий сервисов в json файлы с названием сервиса")
    public List<String> exportPermissionsJSON()
    {
        return updaterService.exportPermissionsJSON();
    }
}
