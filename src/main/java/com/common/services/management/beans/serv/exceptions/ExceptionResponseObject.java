package com.common.services.management.beans.serv.exceptions;

import org.springframework.http.HttpStatus;

import java.sql.Timestamp;


/**
 * ExceptionResponseObject.java
 * Date: 27 сент. 2018 г.
 * Users: amatveev
 * Description: Объект для ответа на запрос для исключений
 */
public class ExceptionResponseObject
{
    private String path;
    private String message = "Внутренняя ошибка сервиса";
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());


    /**
     * Конструктор по умолчанию
     */
    public ExceptionResponseObject()
    {
    }

    /**
     * Конструктор
     * @param status  HTTP статус ответа
     * @param message сообщение
     * @param path    путь запроса
     */
    public ExceptionResponseObject(HttpStatus status, String message, String path)
    {
        setMessage(message);
        setStatus(status);
        setPath(path);
    }

    /**
     * Конструктор.
     * HTTP статус = INTERNAL_SERVER_ERROR
     * message = Внутренняя ошибка сервиса
     * @param path путь запроса
     */
    public ExceptionResponseObject(String path)
    {
        setPath(path);
    }

    /**
     * Возвращает путь запроса
     * @return возвращает путь запроса
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Устанавливает путь запроса
     * @param path путь запроса
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Возвращает текст сообщения
     * @return возвращает текст сообщения
     */
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
     * Возвращает временную метку запроса
     * @return возвращает временную метку запроса
     */
    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    /**
     * Устанавливает временную метку запроса
     * @param timestamp временна метка запроса
     */
    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }
}
