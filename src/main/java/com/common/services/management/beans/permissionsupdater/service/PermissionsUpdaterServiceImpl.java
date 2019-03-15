package com.common.services.management.beans.permissionsupdater.service;

import com.common.services.management.beans.management.model.Permission;
import com.common.services.management.beans.management.service.UsersManagementService;
import com.common.services.management.beans.permissionsupdater.model.PermissionsCompare;
import com.common.services.management.beans.servicesinfo.ServiceInfo;
import com.common.services.management.beans.servicesinfo.ServicesInfoReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PermissionsUpdaterServiceImpl.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Класс с методами сравнения/обновления пермиссий сервисов и БД
 */
@Component
public class PermissionsUpdaterServiceImpl
        implements PermissionsUpdaterService
{
    @Autowired
    private ApiReader apiReader;

    @Autowired
    private UsersManagementService usersManagementService;

    @Override
    public PermissionsCompare comparePermissions()
    {
        PermissionsCompare permissionsCompare = new PermissionsCompare();
        List<ServiceInfo> servicesInfo = new ServicesInfoReader().readServicesInfo();
        List<Permission> dbPermissions = usersManagementService.getAllPermissions();
        permissionsCompare.setDbPermissions(dbPermissions);
        servicesInfo.forEach(info ->
        {
            List<Permission> permissions = getPermissionsFromService(info);
            permissionsCompare.addServicePermissions(info.getName(), permissions);
        });
        removeDuplicatePermissions(permissionsCompare);
        return permissionsCompare;
    }

    @Override
    public int mergePermissions()
    {
        // Список пермиссий сервисов, отсутсвующих в базе данных
        PermissionsCompare permissionsCompare = comparePermissions();
        // Список пермиссий всех сервисов
        List<Permission> permissions = new ArrayList<>();
        permissionsCompare.getServicesPermissions()
                .values()
                .forEach(servicePermissions -> permissions.addAll(servicePermissions));
        // Добавим в БД пермисии, которые были найдены в сервисах, но не найдены в БД
        return usersManagementService.addPermissions(permissions);
    }

    /**
     * Удаляет одинаковые пермиссии из списков базы данных и сервисов
     * @param permissionsCompare Объект со списками пермиссий базы данных и сервисов
     */
    private void removeDuplicatePermissions(PermissionsCompare permissionsCompare)
    {
        AntPathMatcher matcher = new AntPathMatcher();
        Set<Permission> existingPermissionsDB = new HashSet<>();
        List<Permission> servicesPermissions = new ArrayList<>();
        List<Permission> existingPermissionsService = new ArrayList<>();
        permissionsCompare.getServicesPermissions()
                .values()
                .forEach(servicePermissions -> servicesPermissions.addAll(servicePermissions));

        // Если пермиссия из БД не должна участвововать в сравнении, пропустим её
        List<Permission> dbPermissions = permissionsCompare.getDbPermissions()
                .stream()
                .filter(dbPermission ->
                        dbPermission.getJsonData() == null
                                || dbPermission.getJsonData().getSkipWhenComparing() == null
                                || !dbPermission.getJsonData().getSkipWhenComparing())
                .collect(Collectors.toList());

        // Сравним все пермиссии сервисов с пермиссией из БД
        dbPermissions.stream()
                .forEach(dbPermission ->
                        servicesPermissions.stream()
                                .filter(servicePermission -> servicePermission.getMethod() == dbPermission.getMethod()
                                        && matcher.match(dbPermission.getPath(), servicePermission.getPath()))
                                .forEach(servicePermission ->
                                {
                                    existingPermissionsDB.add(dbPermission);
                                    existingPermissionsService.add(servicePermission);
                                })
                );
        // Очистим список пермиссий сервисов, которые были найдены в БД
        permissionsCompare.getServicesPermissions().values()
                .forEach(list -> list.removeAll(existingPermissionsService));
        // Очистим список пермиссий БД, которые были найдены в сервисах
        permissionsCompare.getDbPermissions().removeAll(existingPermissionsDB);
    }

    private List<Permission> getPermissionsFromService(ServiceInfo info)
    {
        return apiReader.read(info);
    }
}
