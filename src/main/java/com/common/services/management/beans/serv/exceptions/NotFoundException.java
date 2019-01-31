package com.common.services.management.beans.serv.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * NotFoundException.java
 * Date: 16 нояб. 2018 г.
 * Users: amatveev
 * Description: Исключение для EmptyResultDataAccessException
 */
@SuppressWarnings("serial")
@Component
public class NotFoundException
        extends ServiceException
{
    public NotFoundException()
    {
        super();
        setStatus(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void init()
    {
        super.init();
        setStatus(HttpStatus.NOT_FOUND);
    }
}