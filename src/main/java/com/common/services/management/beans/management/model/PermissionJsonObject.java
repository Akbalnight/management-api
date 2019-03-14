package com.common.services.management.beans.management.model;

/**
 * PermissionJsonObject.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для описания json объекта пермиссии
 */
public class PermissionJsonObject
{
    /**
     * Конструктор по умолчанию
     */
    public PermissionJsonObject()
    {
    }

    /**
     * Флаг true если пермиссия не должна участвовать при сравнении пермиссий из сервисов
     */
    private Boolean skipWhenComparing;

    public Boolean getSkipWhenComparing()
    {
        return skipWhenComparing;
    }

    public void setSkipWhenComparing(Boolean skipWhenComparing)
    {
        this.skipWhenComparing = skipWhenComparing;
    }
}