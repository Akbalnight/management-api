package com.common.services.management.beans.permissionsupdater.swaggermodel;

/**
 * Operation.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Описание операции из API Swagger
 */
public class Operation
{
    private String summary;
    private String operationId;

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }


    public String getOperationId()
    {
        return operationId;
    }

    public void setOperationId(String operationId)
    {
        this.operationId = operationId;
    }
}

