package com.common.services.management.beans.management.model;

import java.util.List;

/**
 * Role.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для описания ролей пользователей
 */
public class Role
{
    private String name;
    private String description;
    private RoleJsonObject jsonData;
    private List<Permission> permissions;

    /**
     * Возвращает описание роли
     *
     * @return описание роли
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Устанавливает описание роли
     *
     * @param description описание роли
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Возвращает json объект роли
     *
     * @return возвращает json объект роли
     */
    public RoleJsonObject getJsonData()
    {
        return jsonData;
    }

    /**
     * Устанавливает json объект роли
     *
     * @param jsonData json объект роли
     */
    public void setJsonData(RoleJsonObject jsonData)
    {
        this.jsonData = jsonData;
    }

    /**
     * Возвращает название роли
     *
     * @return название роли
     */
    public String getName()
    {
        return name;
    }

    /**
     * Устанавливает название роли
     *
     * @param name название роли
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Устанавливает список пермиссий
     *
     * @param permissions - список пермиссий
     */
    public void setPermissions(List<Permission> permissions)
    {
        this.permissions = permissions;
    }

    /**
     * Возвращает список пермиссий
     *
     * @return
     */
    public List<Permission> getPermissions()
    {
        return permissions;
    }
}
