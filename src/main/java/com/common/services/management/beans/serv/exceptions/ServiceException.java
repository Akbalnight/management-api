package com.common.services.management.beans.serv.exceptions;


import com.common.services.management.beans.serv.resourcemanager.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * ServiceException.java
 * Date: 27 сент. 2018 г.
 * Users: amatveev
 * Description: Общий класс для исключений
 */
@SuppressWarnings("serial")
@Component
public class ServiceException extends RuntimeException
{
    private HttpStatus status;
    private String message;
    private Throwable exception;

    @Autowired
    private ResourceManager resourceManager;

    protected void init()
    {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = "Внутренняя ошибка сервиса";
        this.exception = null;
    }

    /**
     * Конструктор по умолчанию
     */
    public ServiceException()
    {
        super("");
    }

    /**
     * Конструктор
     * @param message сообщение
     */
    public ServiceException(String message)
    {
        super(message);
    }

    /**
     * Получить сообщение из менеджера сообщений
     * @param key -  ключ
     * @param args - список аргументов
     * @return сообщение
     */
    private String getMessage(String key, Object ...args)
    {
        return resourceManager.getResource(key, args);
    }

    /**
     * Возвращает объект исключения, установив указанные парамметры
     * @param status      HTTP статус ответа
     * @param messageCode текст сообщения или название сообщения в файле с ресурсами
     * @param args        список аргументов для сообщения
     * @return возвращает объект исключения
     */
    public ServiceException applyParameters(HttpStatus status, String messageCode, Object... args)
    {
        init();
        setStatus(status);
        setMessage(getMessage(messageCode, args));
        return this;
    }

    /**
     * Возвращает объект исключения, установив указанные парамметры.
     * HTTP статус = INTERNAL_SERVER_ERROR
     * @param messageCode текст сообщения или название сообщения в файле с ресурсами
     * @param args        список аргументов для сообщения
     * @return возвращает объект исключения
     */
    public ServiceException applyParameters(String messageCode, Object... args)
    {
        init();
        setStatus(status);
        setMessage(getMessage(messageCode, args));
        return this;
    }

    /**
     * Возвращает объект исключения, установив указанные парамметры.
     * HTTP статус = INTERNAL_SERVER_ERROR
     * message Внутренняя ошибка сервиса
     * @param exception исключение
     * @return возвращает объект исключения
     */
    public ServiceException applyParameters(Throwable exception)
    {
        init();
        setException(exception);
        return this;
    }

    /**
     * Возвращает HTTP статус ответа
     * @return возвращает HTTP статус ответа
     */
    public HttpStatus getStatus()
    {
        return status;
    }

    /**
     * Устанавливает HTTP статус ответа
     * @param status HTTP статус ответа
     */
    public void setStatus(HttpStatus status)
    {
        this.status = status;
    }

    /**
     * Возвращает текст сообщения
     * @return возвращает текст сообщения
     */
    @Override
    public String getMessage()
    {
        return message;
    }

    /**
     * Устанавливает текст сообщения
     * @param message текст сообщения
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Возвращает объект исключения
     * @return возвращает объект исключения
     */
    public Throwable getException()
    {
        return exception;
    }

    /**
     * Устанавливает объект исключения
     * @param exception объект исключения
     */
    public void setException(Throwable exception)
    {
        this.exception = exception;
    }
}
