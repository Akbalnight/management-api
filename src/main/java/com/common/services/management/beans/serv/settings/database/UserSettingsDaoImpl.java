package com.common.services.management.beans.serv.settings.database;

import com.common.services.management.datasource.DataSourceManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserSettingsDaoImpl.java
 * Date: 15 янв. 2019 г.
 * Users: vmeshkov
 * Description: Реализация интерфейса, которая работает с базой
 */
@Component
public class UserSettingsDaoImpl
    implements UserSettingsDao
{
    /**
     * Название конфигурации базы данных
     */
    public static final String DB_CONFIG_NAME = "main";

    private static final String PAGE_TAG = "page";
    private static final String SIZE_TAG = "size";
    
    /**
     * Шаблон для выполнения SQL запросов
     */
    private  NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Подключение шаблона запросов и источника БД
     * @param dataSourceManager менеджер конфигураций БД
     */
    @Autowired
    DataSourceManager dataSourceManager;

    /**
     * Подключение шаблона для выполнения SQL запросов
     */
    @PostConstruct
    public void init()
    {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSourceManager.getDataSource(DB_CONFIG_NAME));
    }
    
    public JsonNode getUserSettings(int userId)
    {
        final String SQL_GET_USER_SETTINGS =
            "SELECT settings FROM user_settings WHERE user_id=:userid";
        try
        {
            return jdbcTemplate.queryForObject(SQL_GET_USER_SETTINGS, new MapSqlParameterSource("userid", userId),
                new RowMapper<JsonNode>()
                {
                    @Override
                    public JsonNode mapRow(ResultSet rs, int rowNum)
                        throws SQLException
                    {
                        try
                        {
                            return objectMapper.readTree(rs.getString(1));
                        }
                        catch (IOException e)
                        {
                            throw new SQLException(e);
                        }
                    }
                });
        }
        catch (EmptyResultDataAccessException e)
        {
            return objectMapper.createObjectNode();
        }
    }

    @Override
    public Integer getPageSize(int user_id)
    {
        JsonNode settings = getUserSettings(user_id);
        JsonNode node = settings.get(PAGE_TAG);
        Integer result = null;
        try
        {
            node = node.get(SIZE_TAG);
            result = objectMapper.readerFor(new TypeReference<Integer>()
            {
            }).readValue(node);
        }
        catch (IOException | NullPointerException e )
        {
        }

        return result;
    }
}
