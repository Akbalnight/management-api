package com.common.services.management.beans.management.model;

import java.util.List;

/**
 * LdapGroup.java
 * Date: 11 янв. 2019 г.
 * Users: amatveev
 * Description: Класс для работы со связями LDAP группы и ролей
 */
public class LdapGroup
{
    /**
     * Название LDAP группы
     */
    private String group;

    /**
     * Список названий ролей
     */
    private List<String> roles;

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }
}