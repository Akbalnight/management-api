package com.common.services.management.logging;

import com.common.services.management.details.Details;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LoggerInterceptor.java
 * Date: 1 нояб. 2018 г.
 * Users: vmeshkov
 * Description: Настройка логгера
 */
@Component
public class LoggerInterceptor
    implements HandlerInterceptor
{
    private static final String SESSIONID = "sessionId";
    private static final String USERID = "userId";
    
    @Override
    public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response, Object handler)
        throws Exception
    {
        try
        {
            Details details = Details.getDetails();
            if (details != null)
            {
                String sesssinId = details.getSessionId();
                // Определим sessionid
                if (sesssinId != null && !sesssinId.isEmpty())
                {
                    MDC.put(SESSIONID, sesssinId);
                }
                String userId = details.getUserId();
                // Определим userId
                if (userId != null && !userId.isEmpty())
                {
                    MDC.put(USERID, userId);
                }
            }
        }
        catch (Exception e)
        {
            // ингорируем, т.к. вызвался не для сессии
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response, Object handler, Exception ex)
        throws Exception
    {
        MDC.remove(SESSIONID);
        MDC.remove(USERID);

        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

}
