package com.common.services.management.beans.permissionsupdater.service;

import com.common.services.management.beans.management.model.Permission;
import com.common.services.management.beans.permissionsupdater.swaggermodel.Path;
import com.common.services.management.beans.permissionsupdater.swaggermodel.Swagger;
import com.common.services.management.beans.servicesinfo.ServiceInfo;
import com.common.services.management.logging.Logger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.*;

/**
 * SwaggerApiReader.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Получение списка пермиссий сервиса из API swagger
 */
@Component
public class SwaggerApiReader
        implements ApiReader
{
    @Autowired
    private Logger logger;

    private ObjectMapper jsonMapper;

    @PostConstruct
    private void init()
    {
        jsonMapper = new ObjectMapper();
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public List<Permission> read(ServiceInfo info)
    {
        Swagger swagger = readApi(info.getApiUrl());
        if (swagger == null)
        {
            return null;
        }
        String basePath = swagger.getBasePath();
        // Добавление названия сервиса к пути пермиссии
        if (basePath.equals("/"))
        {
            basePath += info.getName();
        }
        List<Permission> permissions = new ArrayList<>();
        for (Map.Entry<String, Path> path : swagger.getPaths().entrySet())
        {
            String pathPermission = basePath + replaceVariables(path.getKey());
            if (path.getValue().getGet() != null)
            {
                permissions.add(new Permission(GET, pathPermission, path.getValue().getGet().getSummary()));
            }
            if (path.getValue().getPost() != null)
            {
                permissions.add(new Permission(POST, pathPermission, path.getValue().getPost().getSummary()));
            }
            if (path.getValue().getPut() != null)
            {
                permissions.add(new Permission(PUT, pathPermission, path.getValue().getPut().getSummary()));
            }
            if (path.getValue().getDelete() != null)
            {
                permissions.add(new Permission(DELETE, pathPermission, path.getValue().getDelete().getSummary()));
            }
        }
        return permissions;
    }

    private String getSwaggerJson(String url)
    {
        try
        {
            URLConnection conn = new URL(url).openConnection();
            StringBuilder sb = new StringBuilder();
            String line;
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            logger.error(e);
            return null;
        }
    }

    private Swagger readApi(String apiUrl)
    {
        try
        {
            String json = getSwaggerJson(apiUrl);
            JsonNode rootNode = jsonMapper.readTree(json);
            return jsonMapper.convertValue(rootNode, Swagger.class);
        }
        catch (Exception e)
        {
            logger.error(e);
            return null;
        }
    }

    /**
     * Замена параметров пути на шаблоны
     * @param path Путь сервиса
     * @return Возвращает путь сервиса
     */
    private String replaceVariables(String path)
    {
        // Замена всех {текст} на *
        return path.replaceAll("\\{.+?\\}", "*");
    }
}
