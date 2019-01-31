package com.common.services.management.beans.management.model;

import java.util.Map;

/**
 * RoleJsonObject.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для описания json объекта роли
 */
public class RoleJsonObject
{
    /**
     * Список объектов для сервисов
     */

    private Map<String, String> objects;
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
}
