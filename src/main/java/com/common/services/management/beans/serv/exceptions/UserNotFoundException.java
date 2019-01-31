package com.common.services.management.beans.serv.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * NotFoundException.java
 * Date: 16 нояб. 2018 г.
 * Users: amatveev
 * Description: Исключение - пользователь с указанным логином не найден
 */
@SuppressWarnings("serial")
@Component
public class UserNotFoundException extends NotFoundException
{
    private static final String ERROR_USER_NOT_EXIST = "error.auth.userNotExist";

    public UserNotFoundException applyUsername(String username)
    {
         this.applyParameters(ERROR_USER_NOT_EXIST, username);
         return this;
    }

    @Override
    protected void init()
    {
        super.init();
        setStatus(HttpStatus.NOT_FOUND);
    }
}