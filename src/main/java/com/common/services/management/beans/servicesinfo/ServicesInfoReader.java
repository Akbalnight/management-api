package com.common.services.management.beans.servicesinfo;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.List;

/**
 * ServicesInfoReader.java
 * Date: 13 марта 2019 г.
 * Users: amatveev
 * Description: Считывает данные сервисов с файла services.yml
 */
public class ServicesInfoReader
{
    private static String FILE_NAME = "services.yml";
    private static String RESOURCE_PATH = "yml/" + FILE_NAME;

    /**
     * Считывает данные сервисов с файла services.yml
     * @return Возвращает список сервисов с их данными
     */
    public List<ServiceInfo> readServicesInfo()
    {
        Yaml yaml = new Yaml(new Constructor(ServicesInfo.class));
        InputStream inputStream = null;

        try
        {
            String path = new File(".").getCanonicalPath() + File.separator + FILE_NAME;
            inputStream = new FileInputStream(path);
        }
        catch (IOException e)
        {
            // Файл со списком сервисов не найден в каталоге. Будет использован файл из ресурсов
        }

        if (inputStream == null)
        {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH);
        }

        ServicesInfo servicesInfo = yaml.load(inputStream);
        return servicesInfo.getServices();
    }
}
