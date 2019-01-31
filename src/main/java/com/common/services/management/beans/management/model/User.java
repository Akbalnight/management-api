package com.common.services.management.beans.management.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для описания пользователей
 */
public class User
{
    private Integer id;
    private String name;
    private String password;
    private Boolean enabled;
    private String email;
    private Map<String, Object> jsonData;
    private List<Role> roles = Collections.emptyList();
    // Список объектов пользователя
    private List<Integer> objects = Collections.emptyList();
    // Флаг является ли пользователь LDAP пользователем
    private Boolean ldap;

    public List<Integer> getObjects()
    {
        return objects;
    }

    public void setObjects(List<Integer> objects)
    {
        this.objects = objects;
    }

    public Boolean getLdap()
    {
        return ldap;
    }

    public void setLdap(Boolean ldap)
    {
        this.ldap = ldap;
    }

    /**
     * Возвращает id пользователя
     * @return возвращает id пользователя
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * Устанавливает id пользователя
     * @param id id пользователя
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * Возвращает json объект пользователя
     * @return возвращает json объект пользователя
     */
    public Map<String, Object> getJsonData()
    {
        return jsonData;
    }

    /**
     * Устанавливает json объект пользователя
     * @param jsonData json объект пользователя
     */
    public void setJsonData(Map<String, Object> jsonData)
    {
        this.jsonData = jsonData;
    }

    /**
     * Возвращает email
     * @return возвращает email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Устанавливает email
     * @param email email пользователя
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * Возвращает список ролей пользователя
     * @return список ролей
     */
    public List<Role> getRoles()
    {
        return roles;
    }

    /**
     * Устанавливает список ролей пользователя
     * @param roles список ролей пользователя
     */
    public void setRoles(List<Role> roles)
    {
        this.roles = roles;
    }

    /**
     * Возвращает логин
     * @return логин
     */
    public String getName()
    {
        return name;
    }

    /**
     * Устанавливает логин
     * @param name логин
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Возвращает пароль
     * @return пароль
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Устанавливает пароль
     * @param password пароль
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Устанавливает флаг разрешения аутентификации пользователя
     * @return флаг разрешения аутентификации пользователя
     */
    public Boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Возвращает разрешение аутентификации пользователя
     * @param enabled флаг разрешения аутентификации пользователя
     */
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }
}
