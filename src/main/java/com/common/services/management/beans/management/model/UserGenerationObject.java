package com.common.services.management.beans.management.model;

/**
 * UserGenerationObject.java
 * Date: 14 дек. 2018 г.
 * Users: amatveev
 * Description: Класс связи пользователя с объектом генерации
 */
public class UserGenerationObject
{
    private Integer id;
    private Integer userId;
    private Integer generationObjectId;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getGenerationObjectId()
    {
        return generationObjectId;
    }

    public void setGenerationObjectId(Integer generationObjectId)
    {
        this.generationObjectId = generationObjectId;
    }

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }
}