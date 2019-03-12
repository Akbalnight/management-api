package com.common.services.management.beans.permissionsupdater.model;

import com.common.services.management.beans.management.model.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsCompare
{
    private List<Permission> dbPermissions;
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
