package com.common.services.management.beans.management.model;

import java.util.List;

/**
 * PermissionIdList.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для работы со списком id
 */
public class PermissionIdList
{

    private List<Integer> ids;

    /**
     * Конструктор по умолчанию
     */
    public PermissionIdList()
    {
    }

    /**
     * Возвращает список id
     *
     * @return возвращает список id
     */
    public List<Integer> getIds()
    {
        return ids;
    }

    /**
     * Устанавливает список id
     *
     * @param ids список id
     */
    public void setIds(List<Integer> ids)
    {
        this.ids = ids;
    }
}
