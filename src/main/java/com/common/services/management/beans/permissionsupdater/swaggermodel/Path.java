package com.common.services.management.beans.permissionsupdater.swaggermodel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Path.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Описание пути из API Swagger
 */
@JsonPropertyOrder({"get", "post", "put", "delete"})
public class Path
{
    private Operation get;
    private Operation put;
    private Operation post;
    private Operation delete;

    public Path set(String method, Operation op)
    {
        if ("get".equals(method))
            return get(op);
        if ("put".equals(method))
            return put(op);
        if ("post".equals(method))
            return post(op);
        if ("delete".equals(method))
            return delete(op);
        return null;
    }

    public Path get(Operation get)
    {
        this.get = get;
        return this;
    }

    public Path put(Operation put)
    {
        this.put = put;
        return this;
    }

    public Path post(Operation post)
    {
        this.post = post;
        return this;
    }

    public Path delete(Operation delete)
    {
        this.delete = delete;
        return this;
    }

    public Operation getGet()
    {
        return get;
    }

    public void setGet(Operation get)
    {
        this.get = get;
    }

    public Operation getPut()
    {
        return put;
    }

    public void setPut(Operation put)
    {
        this.put = put;
    }

    public Operation getPost()
    {
        return post;
    }

    public void setPost(Operation post)
    {
        this.post = post;
    }

    public Operation getDelete()
    {
        return delete;
    }

    public void setDelete(Operation delete)
    {
        this.delete = delete;
    }
}

