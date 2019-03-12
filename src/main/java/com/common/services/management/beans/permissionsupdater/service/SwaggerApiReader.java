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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.*;

/**
 *
 * Получение списка пермиссий сервиса из API swagger
 */
@Component
public class SwaggerApiReader
        implements ApiReader
{
    @Autowired
    Logger logger;

    @Override
    public List<Permission> read(ServiceInfo info)
    {
        Swagger swagger = readApi(info.getApiUrl());
        if (swagger == null)
        {
            return Collections.emptyList();
        }
        String basePath = swagger.getBasePath();
        basePath = (basePath.equals("/") ? "" : basePath);
        List<Permission> permissions = new ArrayList<>();

        for (Map.Entry<String, Path> path : swagger.getPaths().entrySet())
        {
            String pathPermission = basePath + replaceVariables(path.getKey());
            if (path.getValue().getGet() != null)
            {
                Permission p = new Permission();
                p.setDescription(path.getValue().getGet().getSummary());
                p.setPath(pathPermission);
                p.setMethod(GET);
                permissions.add(p);
            }
            if (path.getValue().getPost() != null)
            {
                Permission p = new Permission();
                p.setDescription(path.getValue().getPost().getSummary());
                p.setPath(pathPermission);
                p.setMethod(POST);
                permissions.add(p);
            }
            if (path.getValue().getPut() != null)
            {
                Permission p = new Permission();
                p.setDescription(path.getValue().getPut().getSummary());
                p.setPath(pathPermission);
                p.setMethod(PUT);
                permissions.add(p);
            }
            if (path.getValue().getDelete() != null)
            {
                Permission p = new Permission();
                p.setDescription(path.getValue().getDelete().getSummary());
                p.setPath(pathPermission);
                p.setMethod(DELETE);
                permissions.add(p);
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

            return e.toString();// TODO
        }
    }

    private Swagger readApi(String apiUrl)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String json = getSwaggerJson(apiUrl);
            JsonNode rootNode = mapper.readTree(json);
            JsonNode swaggerNode = rootNode.get("swagger");
            if (swaggerNode == null)
                return null;

            return mapper.convertValue(rootNode, Swagger.class);
        }
        catch (IOException e)
        {
            logger.error(e);
            return null;
        }
    }

    private String replaceVariables(String path)
    {
        String result = path;
        int index = path.indexOf("{");
        while (index > 0)
        {
            int endIndex = result.indexOf("}");
            result = result.substring(0, index) + "*" + result.substring(endIndex + 1);

            index = result.indexOf("{");
        }
        return result;
    }
}
