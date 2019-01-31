package com.common.services.management.beans.management.model;

/**
 * LdapGroupGenerationObject.java
 * Date: 23 янв. 2019 г.
 * Users: amatveev
 * Description: Класс для связи LDAP групп с id объектов генерации
 */
public class LdapGroupGenerationObject
{
    /**
     * Название LDAP группы
     */
    private String group;

    /**
     * Id объекта генерации
     */
    private int generationObjectId;

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public int getGenerationObjectId()
    {
        return generationObjectId;
    }

    public void setGenerationObjectId(int generationObjectId)
    {
        this.generationObjectId = generationObjectId;
    }
}