package com.common.services.management.beans.management.model;

import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Permission.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Класс для описания пермиссий
 */
public class Permission
{
    private Integer id;
    private String description;
    private String path;
    private HttpMethod method;
    private PermissionJsonObject jsonData;
    private List<Role> roles = Collections.emptyList();

    /**
     * Возвращяет json объект пермиссии
     *
     * @return возвращяет json объект пермиссии
     */
    public PermissionJsonObject getJsonData()
    {
        return jsonData;
    }

    /**
     * Устанавливает json объект пермиссии
     *
     * @param jsonData json объект пермиссии
     */
    public void setJsonData(PermissionJsonObject jsonData)
    {
        this.jsonData = jsonData;
    }

    /**
     * Возвращает id
     *
     * @return возвращает id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * Устанавливает id
     *
     * @param id id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * Возвращает описание пермиссии
     *
     * @return возвращает описание пермиссии
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Устанавливает описание пермиссии
     *
     * @param description описание пермиссии
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Возвращает url путь сервиса
     *
     * @return возвращает url путь сервиса
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Устанавливает url путь сервиса
     *
     * @param path url путь сервиса
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Возвращает http метод
     *
     * @return возвращает http метод
     */
    public HttpMethod getMethod()
    {
        return method;
    }

    /**
     * Устанавливает http метод
     *
     * @param method http метод
     */
    public void setMethod(HttpMethod method)
    {
        this.method = method;
    }

    /**
     * Возвращает список ролей
     *
     * @return возвращает список ролей
     */
    public List<Role> getRoles()
    {
        return roles;
    }

    /**
     * Устанавливает список ролей
     *
     * @param roles список ролей
     */
    public void setRoles(List<Role> roles)
    {
        this.roles = roles;
    }

    /**
     * Устанавливает http метод
     *
     * @param method http метод
     */
    public void setMethod(String method)
    {
        this.method = HttpMethod.resolve(method);
    }


    /**
     * Возвращает список названий ролей
     *
     * @return возвращает список названий ролей
     */
    public List<String> getRolesNames()
    {
        List<String> result = new ArrayList<String>();
        for (Role role : getRoles())
        {
            result.add(role.getName());
        }
        return result;
    }
}
