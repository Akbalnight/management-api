package com.common.services.management.logging;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import com.common.services.management.beans.serv.exceptions.ServiceException;


/**
 * Logger.java
 * Date: 4 сен. 2018 г.
 * Users: vmeshkov
 * Description: Логгер
 */
@Component
@Configurable
public class Logger
{
    private org.slf4j.Logger slf4logger;
    
    /**
     * @param name имя логгера
     */
    public Logger()
    {
        // Создадим логгер
        slf4logger = LoggerFactory.getLogger("debug");
    }

    /**
     * @param msg логгируемая строка
     */
    public void debug(String msg)
    {
        slf4logger.debug(msg);
    }

    /**
     * @param th логгируемое исключение
     */
    public void debug(Throwable th)
    {
        debug(th.getMessage());
    }

    /**
     * @param th логгируемое исключение
     */
    public void error(Throwable th)
    {
        slf4logger.error(th.getMessage());
    }

    /**
     * @param th логгируемое исключение
     */
    public void error(ServiceException se)
    {
        slf4logger.error(se.getMessage(), se.getException());
    }

    /**
     * @param prefix строка - заголовок
     * @param th     логгируемое исключение
     */
    public void debug(String prefix, Throwable th)
    {
        String msg = new StringBuilder(prefix).append("-").append(th.getMessage()).toString();
        slf4logger.debug(msg);
    }

    /**
     * @param prefix строка - заголовок
     * @param th     логгируемое исключение
     */
    public void error(String prefix, Throwable th)
    {
        String msg = new StringBuilder(prefix).append("-").append(th.getMessage()).toString();
        slf4logger.error(msg);
    }
}
