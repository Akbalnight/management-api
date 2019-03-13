package com.common.services.management.beans.permissionsupdater.swaggermodel;


import java.util.*;

/**
 * Swagger.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Описание корневого объекта из API Swagger
 */
public class Swagger
{
    protected String swagger = "2.0";
    protected String basePath;
    protected Map<String, Path> paths;

    public Swagger basePath(String basePath)
    {
        this.setBasePath(basePath);
        return this;
    }

    public Swagger paths(Map<String, Path> paths)
    {
        this.setPaths(paths);
        return this;
    }

    public Swagger path(String key, Path path)
    {
        if (this.paths == null)
            this.paths = new LinkedHashMap<String, Path>();
        this.paths.put(key, path);
        return this;
    }

    public String getSwagger()
    {
        return swagger;
    }

    public void setSwagger(String swagger)
    {
        this.swagger = swagger;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public Map<String, Path> getPaths()
    {
        if (paths == null)
            return null;
        Map<String, Path> sorted = new LinkedHashMap<String, Path>();
        List<String> keys = new ArrayList<String>();
        keys.addAll(paths.keySet());
        Collections.sort(keys);

        for (String key : keys)
        {
            sorted.put(key, paths.get(key));
        }
        return sorted;
    }

    public void setPaths(Map<String, Path> paths)
    {
        this.paths = paths;
    }

    public Path getPath(String path)
    {
        if (this.paths == null)
            return null;
        return this.paths.get(path);
    }
}
