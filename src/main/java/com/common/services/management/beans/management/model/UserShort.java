package com.common.services.management.beans.management.model;

/**
 * UserShort.java
 * Date: 29 марта 2019 г.
 * Users: amatveev
 * Description: Краткое описание данных пользователя
 */
public class UserShort
{
    /**
     * Id пользователя
     */
    private int id;
    /**
     * Фамилия и инициалы пользователя
     */
    private String shortName;
    /**
     * ФИО
     */
    private String fullName;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getShortName()
    {
        return shortName;
    }

    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }
}