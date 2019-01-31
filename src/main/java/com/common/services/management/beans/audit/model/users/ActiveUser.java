package com.common.services.management.beans.audit.model.users;

import java.time.Instant;

import com.common.services.management.beans.serv.resourcemanager.ResourceManager;

/**
 * ActiveUser.java
 * Date: 20 сент. 2018 г.
 * Users: vmeshkov
 * Description: Класс, описывающий пользователя в системе
 */
public class ActiveUser
{
    /**
     * Логин пользователя
     */
    private String login;
    /**
     * Идентификатор пользователя
     */
    private String id;

    /**
     * IP адрес, с которого зашел
     */
    private String adress;
    /**
     * Время начала сессии
     */
    private Instant startTime;
    /**
     * Длительность нахождения в сети
     */
    private long duration;
    /**
     * Менеджер ресурсов
     */
    private ResourceManager resourceManager;

    public ActiveUser(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public String getLogin()
    {
        return login;
    }

    public ActiveUser setLogin(String login)
    {
        this.login = login;
        return this;
    }

    public String getAdress()
    {
        return adress;
    }

    public ActiveUser setAdress(String adress)
    {
        this.adress = adress;
        return this;
    }

    public Instant getStartTime()
    {
        return startTime;
    }

    public ActiveUser setStartTime(Instant startTime)
    {
        this.startTime = startTime;
        return this;
    }

    public String getDuration()
    {
        if (this.duration != 0)
        {
            long duration = this.duration / 1000;
            // Преобразуем длительность в виде строки (например 3д. 10ч. 20м.)
            return resourceManager.getResource(ResourceManager.TIME_DURATION,
                duration / 3600 / 24,
                duration / 3600 - (duration / 3600 / 24) * 24,
                (duration % 3600) / 60,
                duration % 60).trim();
        }

        return resourceManager.getResource(ResourceManager.LESS_SECOND);
    }

    public ActiveUser setDuration(long duration)
    {
        this.duration = duration;
        return this;
    }
    
    public String getId()
    {
        return id;
    }

    public ActiveUser setId(String id)
    {
        this.id = id;
        return this;
    }
}
