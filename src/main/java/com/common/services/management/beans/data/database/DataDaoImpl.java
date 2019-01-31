package com.common.services.management.beans.data.database;

import com.common.services.management.datasource.DataSourceManager;
import com.common.services.management.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * DataDaoImpl.java
 * Date: 14 янв. 2019 г.
 * Users: vmeshkov
 * Description: Реализация интерфейса по работе с базой
 */
@Component
public class DataDaoImpl
    implements DataDao
{
    /**
     * Название конфигурации базы данных
     */
    public static final String DB_CONFIG_NAME = "main";
    /**
     * Путь к файлу с SQL скриптами создания таблиц для аудита
     */
    private static final String RESOURCE_CREATE_TABLES = "/db_applied_data_create_tables.sql";
    /**
     * Шаблон для выполнения SQL запросов
     */
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    /**
     * Подключение шаблона запросов и источника БД
     * @param dataSourceManager менеджер конфигураций БД
     */
    @Autowired
    DataSourceManager dataSourceManager;
    
    /**
     * Логгер
     */
    @Autowired
    private Logger logger;
    
    /**
     *  Получить список данных: текст нотификации, заголовок и текст письма
     */
    private static final String SQL_NOTIFICATIONS_DATA = "select "
        + "id," 
        + "description," 
        + "email_title," 
        + "email_body,"
        + "props "
        + "from notifications_data";
    
    /**
     *  Удалить данные
     */
    private static final String SQL_DELETE_DATA = "delete " 
        + "from notifications_data "
        + "where id = :id";
    
    /**
     *  Добавить данные
     */
    private static final String SQL_PUT_DATA = "insert " 
        + "into notifications_data("
        + "id,"
        + "description,"
        + "email_title,"
        + "email_body,"
        + "props"
        + ") "
        + "values("
        + ":id,"
        + ":description,"
        + ":email_title,"
        + ":email_body,"
        + "cast(:props as json)"
        + ")";

    /**
     * Подключение шаблона для выполнения SQL запросов
     */
    @PostConstruct
    public void init()
    {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSourceManager.getDataSource(DB_CONFIG_NAME));
        try
        {
            executeSqlFromFile(RESOURCE_CREATE_TABLES);
        }
        catch (IOException e)
        {
            logger.error(e);
        }
    }
    
    /**
     * Выполняет SQL скрипт из указанного файла
     * @param path
     * @throws IOException
     */
    private void executeSqlFromFile(String path) throws IOException
    {
        InputStreamReader streamReader = new InputStreamReader(getClass().getResourceAsStream(path), "UTF-8");
        LineNumberReader reader = new LineNumberReader(streamReader);
        String query = ScriptUtils.readScript(reader, "--", ";");
        jdbcTemplate.getJdbcOperations().execute(query);
    }

    @Override
    public Map<Integer, String []> getNotifications()
    {
        Map<Integer, String []> result = new HashMap<>();
        jdbcTemplate.query(SQL_NOTIFICATIONS_DATA, (ResultSet rs) ->
        {
            if (rs.isBeforeFirst())
            {
                return;
            }
            do
            {
                result.put(rs.getInt(1), new String[] {
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5)});
            }
            while (rs.next());
        });
        
        return result;
    }

    @Override
    @Transactional
    public void putNotification(int id, String[] data)
    {
        MapSqlParameterSource map = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(SQL_DELETE_DATA, map);
        map.addValue("description", data[0]);
        map.addValue("email_title", data[1]);
        map.addValue("email_body", data[2]);
        map.addValue("props", data[3]);
        jdbcTemplate.update(SQL_PUT_DATA, map);
    }
}
