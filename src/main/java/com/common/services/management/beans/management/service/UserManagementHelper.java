package com.common.services.management.beans.management.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * UserManagementHelper.java
 * Date: 02 окт. 2018 г.
 * Users: amatveev
 * Description: Класс с вспомогательными методами для работы с сервисом управления пользователями
 */
public class UserManagementHelper
{
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Обработка названия роли для работы с БД.
     * Приводит название роли в верхний регистр и добавляет префикс 'ROLE_' если его нет.
     * @param role название роли
     * @return возвращает обработанное название роли
     */
    public static String prepareRoleName(String role)
    {
        String newRoleName = role.trim().toUpperCase();
        if (!newRoleName.startsWith(ROLE_PREFIX))
        {
            newRoleName = ROLE_PREFIX + newRoleName;
        }
        return newRoleName;
    }

    /**
     * Обработка списка названий ролей для работы с БД.
     * Приводит название роли в верхний регистр и добавляет префикс 'ROLE_' если его нет.
     * @param roles список названий ролей
     * @return возвращает обработанный список названий ролей
     */
    public static List<String> prepareRolesNames(List<String> roles)
    {
        List<String> newRoles = new ArrayList<String>();
        for (String role : roles)
        {
            newRoles.add(prepareRoleName(role));
        }
        return newRoles;
    }

    /**
     * Обработка логина пользователя для работы с БД.
     * Обрезает пробелы в конце и начале логина, приводит логин к нижнему регистру.
     * @param name логин пользователя
     * @return возвращает обработанный логин пользователя
     */
    public static String prepareUserName(String name)
    {
        String newName = name.toLowerCase().trim();
        return newName;
    }

    /**
     * Хэширует пароль
     * @param password пароль
     * @return возвращает хэш пароля
     */
    public static String preparePassword(String password)
    {
        return new BCryptPasswordEncoder().encode(password);
    }
}
