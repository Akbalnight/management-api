package com.common.services.management;

import com.common.services.management.beans.serv.exceptions.ExceptionResponseObject;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * GlobalExceptionHandler.java
 * Date: 27 сент. 2018 г.
 * Users: amatveev
 * Description: Глобалный обработчик исключений приложения
 */
@ControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler
{

    @Autowired
    private Logger logger;

    /**
     * Обработчик исключения {@link ServiceException}
     * @param ex объект исключения
     * @return возвращает обработанное исключение в виде {@link ExceptionResponseObject}
     */
    @ExceptionHandler
    public ResponseEntity<ExceptionResponseObject> handleServiceException(ServiceException ex)
    {
        logException(ex);
        String path = getRequestPath();
        ExceptionResponseObject responseObject = new ExceptionResponseObject(ex.getStatus(), ex.getMessage(), path);
        return new ResponseEntity<>(responseObject, responseObject.getStatus());
    }

    /**
     * Логирует исключение
     * @param ex объект исключения
     */
    private void logException(ServiceException ex)
    {
        if (ex.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR)
        {
            logger.error(ex);
        }
        else
        {
            logger.debug(ex);
        }
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseObject> handleServiceException(DataAccessException ex)
    {
        logger.error(ex);
        String path = getRequestPath();
        ExceptionResponseObject responseObject = new ExceptionResponseObject(path);
        return new ResponseEntity<>(responseObject, responseObject.getStatus());
    }

    /**
     * Возвращает путь запроса в формате 'HTTP_METHOD PATH?QUERY_STRING'
     * @return возвращает путь запроса
     */
    private String getRequestPath()
    {
        try
        {
            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null)
            {
                String path = request.getMethod() + " " + request.getServletPath();
                if (request.getQueryString() != null)
                {
                    path += "?" + request.getQueryString();
                }
                return path;
            }
        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
        return "";
    }

    /**
     * Возвращает текущий запрос
     * @return возвращает текущий запрос
     */
    private HttpServletRequest getCurrentHttpRequest()
    {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes)
        {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return request;
        }
        return null;
    }
}