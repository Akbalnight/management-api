package com.common.services.management.beans.management.model;

import java.util.List;

/**
 * RoleNameList.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для работы со списком названий ролей
 */
public class RoleNameList
{
    private List<String> roles;

    /**
     * Конструктор по умолчанию
     */
    public RoleNameList()
    {
    }

    /**
     * Возвращает список названий ролей
     *
     * @return возвращает список названий ролей
     */
    public List<String> getRoles()
    {
        return roles;
    }

    /**
     * Устнавливает список названий ролей
     *
     * @param roles список названий ролей
     */
    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }
}
