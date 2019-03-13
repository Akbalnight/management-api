package com.common.services.management.beans.permissionsupdater.model;

import com.common.services.management.beans.management.model.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PermissionsCompare.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Хранит список пермиссий БД и список сервисов с их пермиссиями
 */
public class PermissionsCompare
{
    /**
     * Список персиссий из БД
     */
    private List<Permission> dbPermissions;

    /**
     * Список названий сервисов и их пермиссий
     */
    private Map<String, List<Permission>> servicesPermissions = new HashMap<>();

    public List<Permission> getDbPermissions()
    {
        return dbPermissions;
    }

    public void setDbPermissions(List<Permission> dbPermissions)
    {
        this.dbPermissions = dbPermissions;
    }

    public Map<String, List<Permission>> getServicesPermissions()
    {
        return servicesPermissions;
    }

    public void setServicesPermissions(Map<String, List<Permission>> servicesPermissions)
    {
        this.servicesPermissions = servicesPermissions;
    }

    public void addServicePermissions(String name, List<Permission> permissions)
    {
        servicesPermissions.put(name, permissions);
    }
}
