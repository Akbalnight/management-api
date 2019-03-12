package com.common.services.management.controllers;

import com.common.services.management.beans.management.model.Permission;
import com.common.services.management.beans.permissionsupdater.model.PermissionsCompare;
import com.common.services.management.beans.permissionsupdater.service.PermissionsUpdaterService;
import com.common.services.management.beans.permissionsupdater.swaggermodel.Path;
import com.common.services.management.beans.permissionsupdater.swaggermodel.Swagger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping(path = "updater")
public class PermissionsUpdaterController
{
    @Autowired
    PermissionsUpdaterService updaterService;


    @GetMapping(value = "/permissions/compare")
    @ApiOperation(value = "Сравнение пермиссий базы данных с пермиссиями сервисов",
            notes = "Возвращает список пермиссий, которых нет в сервисах, но есть в БД и тех что есть в сервисах, но нет в БД")
    public PermissionsCompare comparePermissions()
    {
        return updaterService.comparePermissions();
    }

    @GetMapping(value = "/permissions/merge")
    @ApiOperation(value = "Добавляет отсутсвующие в базе данных пермиссиии из сервисов")
    public int mergePermissions()
    {
        return updaterService.mergePermissions();
    }

    @GetMapping(value = "/all")
    public List<Permission> getAllArrivals()
    {
        String url = "http://10.5.31.97:8801/v2/api-docs";
        //String url = "http://10.5.31.96:8080/act/v2/api-docs";
       // String url = "http://10.5.31.96:8080/management/v2/api-docs";

       // Swagger swagger = new SwaggerParser().read(url, Collections.singletonList(a));
       // String s = temp(url);
        //Swagger swagger = new SwaggerParser().read(s);
        Swagger swagger = read(url);


        String basePath = swagger.getBasePath();

        List<Permission> permissions = new ArrayList<>();

        for (Map.Entry<String, Path> path : swagger.getPaths().entrySet())
        {
            String pathPermission = (basePath.equals("/")?"":basePath) + replaceVariables(path.getKey());
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

    private String replaceVariables(String path)
    {
        String result = path;
        int index = path.indexOf("{");
        while (index > 0)
        {
            int endIndex = result.indexOf("}");
            result = result.substring(0, index) + "*" + result.substring(endIndex + 1, result.length());

            index = result.indexOf("{");
        }

        return result;
    }


    private Swagger read(String url)
    {

        try
        {
            JsonNode rootNode = null;
            //ObjectMapper mapper = Json.mapper();
            ObjectMapper mapper = new ObjectMapper();

/*
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Property.class, new PropertyDeserializer());
            module.addDeserializer(Model.class, new ModelDeserializer());
            module.addDeserializer(Parameter.class, new ParameterDeserializer());
            module.addDeserializer(SecuritySchemeDefinition.class, new SecurityDefinitionDeserializer());
            mapper.registerModule(module);*/
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String json = temp(url);
            rootNode = mapper.readTree(json);
            JsonNode swaggerNode = rootNode.get("swagger");
            if (swaggerNode == null)
                return null;

            return mapper.convertValue(rootNode, Swagger.class);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private String temp(String url)
    {
        URLConnection conn = null;
        BufferedReader br = null;
        InputStream is = null;

        try
        {
            conn = new URL(url).openConnection();

            //            conn.setRequestProperty(auth.getKeyName(), auth.getValue());

            StringBuilder sb = new StringBuilder();

            String line;
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            return e.toString();
        }
    }
}
