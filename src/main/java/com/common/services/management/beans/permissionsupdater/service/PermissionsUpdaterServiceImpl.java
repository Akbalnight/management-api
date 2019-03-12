package com.common.services.management.beans.permissionsupdater.service;

import com.common.services.management.beans.management.model.Permission;
import com.common.services.management.beans.management.service.UsersManagementService;
import com.common.services.management.beans.permissionsupdater.model.PermissionsCompare;
import com.common.services.management.beans.servicesinfo.ServiceInfo;
import com.common.services.management.beans.servicesinfo.ServicesInfoReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Класс для сравнения и обновления пермиссий в БД и сервисах.
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
        return usersManagementService.addPermissions(permissions);
    }

    /**
     * Удаляет одинаковые пермиссии из списков базы данных и сервисов
     * @param permissionsCompare Объект со списками пермиссий базы данных и сервисов
     */
    private void removeDuplicatePermissions(PermissionsCompare permissionsCompare)
    {
        List<Permission> existingPermissions = new ArrayList<>();
        permissionsCompare.getDbPermissions().forEach(dbPermission ->
        {
            String service = parseService(dbPermission.getPath());
            if (service != null)
            {
                List<Permission> servicePermissions = permissionsCompare.getServicesPermissions().get(service);
                if (servicePermissions != null)
                {
                    for (Permission servicePermission : servicePermissions)
                    {
                        if (servicePermission.getMethod() == dbPermission.getMethod() && servicePermission.getPath().equalsIgnoreCase(dbPermission.getPath()))
                        {
                            existingPermissions.add(dbPermission);
                            servicePermissions.remove(servicePermission);
                            break;
                        }
                    }
                }
            }
        });
        permissionsCompare.getDbPermissions().removeAll(existingPermissions);
    }

    private String parseService(String path)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        int index = path.indexOf("/");
        if (index > 0)
        {
            return path.substring(0, index);
        }
        return null;
    }

    private List<Permission> getPermissionsFromService(ServiceInfo info)
    {
        return apiReader.read(info);
    }
}
