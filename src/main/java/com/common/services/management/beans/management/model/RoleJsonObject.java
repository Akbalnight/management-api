package com.common.services.management.beans.management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * RoleJsonObject.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для описания json объекта роли
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleJsonObject
{
    /**
     * true если роль назначается пользователю из LDAP
     */
    private Boolean isLdap;
    /**
     * Список объектов для сервисов
     */
    private Map<String, String> objects = new HashMap<>();
    /**
     * Конструктор по умолчанию
     */
    public RoleJsonObject()
    {
    }

    public Map<String, String> getObjects()
    {
        return objects;
    }

    public void setObjects(Map<String, String> objects)
    {
        this.objects = objects;
    }

    public Boolean getLdap()
    {
        return isLdap;
    }

    public void setLdap(Boolean ldap)
    {
        isLdap = ldap;
    }
}
