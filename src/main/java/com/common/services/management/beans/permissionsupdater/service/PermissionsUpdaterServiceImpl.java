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
import java.util.List;

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
        List<Permission> existingPermissionsDB = new ArrayList<>();
        // Проверим каждую пермиссию из БД
        permissionsCompare.getDbPermissions().forEach(dbPermission ->
        {
            if (dbPermission.getJsonData() != null &&
                    dbPermission.getJsonData().getSkipWhenComparing() != null &&
                    dbPermission.getJsonData().getSkipWhenComparing())
            {
                // Если пермиссия из БД не должна участвововать в сравнении, пропустим её
                return;
            }
            // Флаг что пермиссия из БД была найдена в сервисах
            boolean isAdded = false;
            // Сервис для пермиссии из БД
            String service = parseService(dbPermission.getPath());
            if (service != null)
            {
                List<Permission> servicePermissions = permissionsCompare.getServicesPermissions().get(service);
                if (servicePermissions != null)
                {
                    List<Permission> existingPermissionsService = new ArrayList<>();
                    // Сравним все пермиссии сервиса
                    for (Permission servicePermission : servicePermissions)
                    {
                        if (servicePermission.getMethod() == dbPermission.getMethod())
                        {
                            // с пермиссией из БД
                            if (matcher.match(dbPermission.getPath(), servicePermission.getPath()))
                            {
                                if (!isAdded)
                                {
                                    existingPermissionsDB.add(dbPermission);
                                    isAdded = true;
                                }
                                existingPermissionsService.add(servicePermission);
                                if (!dbPermission.getPath().contains("*"))
                                {
                                    // Остановим проверку пермиссий из сервиса если найдено точное соответсвие пути (без шаблонов)
                                    break;
                                }
                            }
                        }
                    }
                    // Очистим список пермиссий сервиса, которые были найдены в БД
                    servicePermissions.removeAll(existingPermissionsService);
                }
            }
        });
        // Очистим список пермиссий БД, которые были найдены в сервисах
        permissionsCompare.getDbPermissions().removeAll(existingPermissionsDB);
    }

    /**
     * Определяет название сервиса по пути пермиссии
     * @param path Путь пермиссии
     * @return Возвращает название сервиса
     */
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
