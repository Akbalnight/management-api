package com.common.services.management.beans.management.model;

/**
 * ChangePassword.java
 * Date: 11 дек. 2018 г.
 * Users: amatveev
 * Description: Класс для изменения пароля пользователя
 */
public class ChangePassword
{
    // Старый пароль
    private String oldPassword;
    // Новый пароль
    private String newPassword;

    public String getOldPassword()
    {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword)
    {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }
}
