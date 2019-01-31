package com.common.services.management.beans.management.model;

/**
 * LdapPermissions.java
 * Date: 14 нояб. 2018 г.
 * Users: amatveev
 * Description: Класс для параметров подключения к LDAP
 */
public class LdapPermissions
{
    // Пользователь
    private String userDn;
    // Пароль пользователя
    private String password;

    public String getUserDn()
    {
        return userDn;
    }

    public void setUserDn(String userDn)
    {
        this.userDn = userDn;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}

