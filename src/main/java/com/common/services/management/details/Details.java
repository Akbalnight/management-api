package com.common.services.management.details;

import java.util.Optional;

/**
 * Details.java
 * Date: 8 окт. 2018 г.
 * Users: vmeshkov
 * Description: Содержит данные о пользователе и идентификаторе сессии из шины
 */
// TODO: 08.11.2018 use from common?
public class Details
{
    private static final String UNKNOWN = "unknown";
    private static final ThreadLocal<Details> detailsHolder = new InheritableThreadLocal<>(); 

    /**
     * Идентификатор сессии из шины
     */
    private String sessionId;
    /**
     * Идентификатор пользователя
     */
    private String userId;
    
    /**
     * Идентификатор пользователя, как число
     */
    private Integer userIntId;

    public Details(String sessionId, String userId)
    {
        super();
        this.sessionId = Optional.ofNullable(sessionId).orElse(UNKNOWN);
        this.userId = Optional.ofNullable(userId).orElse(UNKNOWN);
        
        try
        {
            this.userIntId = Integer.parseInt(this.userId);
        }
        catch (NumberFormatException e)
        {
            this.userIntId = null;
        }
    }
    
    public String getSessionId()
    {
        return sessionId;
    }

    public String getUserId()
    {
        return userId;
    }
    
    public Integer getUserIntId()
    {
        return userIntId;
    }

    public void setUserIntId(Integer userIntId)
    {
        this.userIntId = userIntId;
    }

    /**
     * Заполнить данные по запросу
     * @param sessionId - идентификатор сессии из шины
     * @param userId - идентификатор пользователя
     */
    public static void setDetails(String sessionId, String userId)
    {
        detailsHolder.set(new Details(sessionId, userId));
    }

    /**
     * Получить данные из потока
     * @return данные
     */
    public static Details getDetails()
    {
        return Optional.ofNullable(detailsHolder.get()).orElse(new Details(UNKNOWN, UNKNOWN));
    }
}
