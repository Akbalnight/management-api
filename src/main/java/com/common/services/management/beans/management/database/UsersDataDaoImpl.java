package com.common.services.management.beans.management.database;

import com.common.services.management.beans.management.model.LdapGroupGenerationObject;
import com.common.services.management.beans.management.model.UserGenerationObject;
import com.common.services.management.beans.serv.exceptions.NotFoundException;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.datasource.DataSourceManager;
import com.common.services.management.libs.ValidateDao;
import com.common.services.management.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * UsersDataDaoImpl.java
 * Date: 11 окт. 2018 г.
 * Users: vmeshkov
 * Description: Реализация методов работы с базой данных данных (обьектов) пользователей
 */
@Component
@Repository
public class UsersDataDaoImpl extends ValidateDao
        implements UsersDataDao
{
    /**
     * Название конфигурации базы данных
     */
    public static final String DB_CONFIG_NAME = "main";
    private static final String RESOURCE_CREATE_TABLES = "/db_data_create_tables.sql";
    private static final String RESOURCE_CREATE_SETTINGS_TABLE = "/db_data_create_settings_tables.sql";
    
    /**
     * SQL проверки целостности таблицы notifications_data 
     */
    private static final String SQL_VALIDATE_NOTIFICATIONS_DATA = "select " + 
        "        id," +
        "        description," +
        "        email_title," +
        "        email_body," +
        "        props " +
        "from notifications_data";

    private static final String SQL_VALIDATE_ROLE_OBJECTS = "SELECT role FROM role_objects";
    private static final String SQL_ADD_USER_OBJECT = "INSERT INTO user_generation_object (user_id, generation_object_id) VALUES(:userid, :objectid)";
    private static final String SQL_CLEAR_USER_OBJECTS = "DELETE FROM user_generation_object WHERE user_id=:userid";
    private static final String SQL_UPDATE_USER_OBJECTS = "UPDATE user_generation_object SET user_id = :userid, generation_object_id = :objectid WHERE id = :id";
    private static final String SQL_REMOVE_USER_OBJECTS = "DELETE FROM user_generation_object WHERE user_id=:userid AND generation_object_id=:objectid";
    private static final String SQL_GET_USER_OBJECTS =
            "SELECT id, user_id, generation_object_id FROM user_generation_object WHERE user_id=:userid";
    private static final String SQL_GET_USER_OBJECT_BY_ROW_ID =
            "SELECT id, user_id, generation_object_id FROM user_generation_object WHERE id=:id";
    private static final String SQL_ADD_ROLE_OBJECTS = "INSERT INTO role_objects (role, service, objects) VALUES(:role, :service, :objects)";
    private static final String SQL_REMOVE_ROLE_OBJECTS = "DELETE FROM role_objects WHERE role=:role";

    private static final String SQL_GET_USER_SETTINGS =
        "SELECT settings FROM user_settings WHERE user_id=:userid";

    private static final String SQL_DELETE_USER_SETTINGS =
        "DELETE FROM user_settings WHERE user_id=:userid";

    private static final String SQL_SET_USER_SETTINGS =
        "insert into user_settings(user_id, settings) values(:userid, cast(:settings as json))";

    private static final String ERROR_GENERATION_OBJECT_ID_FOR_LDAP_GROUP_NOT_EXIST = "error.auth.generationObjectIdForLDAPGroupNotExist";
    private static final String ERROR_LDAP_GROUP_FOR_GENERATION_OBJECT_ID_NOT_EXIST = "error.auth.LDAPGroupForGenerationObjectIdNotExist";

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private Logger logger;

    /**
     * Подключение шаблона запросов и источника БД
     */
    @Autowired
    private DataSourceManager dataSourceManager;

    /**
     * Парсер для json обьектов
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Исключение
     */
    @Autowired
    private ServiceException serviceException;
    @Autowired
    private NotFoundException notFoundException;

    /**
     * Подключение источника БД
     * @return возвращает источник БД
     */
    @Bean
    public DataSource getDataSource()
    {
        return dataSourceManager.getDataSource(DB_CONFIG_NAME);
    }

    /**
     * Подключение шаблона запросов и источника БД
     */
    @PostConstruct
    public void init()
    {
        jdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
        initTables();
    }


    /**
     * Проверка существования таблиц в БД и наличия в них записей
     * @return возращает false если одна из таблиц не существует или не заполнена
     */
    boolean validateDataBaseInit()
    {
        try
        {
            jdbcTemplate.getJdbcOperations().queryForList(SQL_VALIDATE_ROLE_OBJECTS, String.class);
        }
        catch (DataAccessException ex)
        {
            return false;
        }

        return true;
    }

    /**
     * Создание таблиц в БД если они не существуют или пустые
     */
    private void initTables()
    {
        try
        {
            setValidateElements(new ValidateTable[] {
                new ValidateTable(
                    new SQLText(SQL_VALIDATE_NOTIFICATIONS_DATA),
                    -1,
                    new SQLScript("/db_notifications_data_create_tables.sql"),
                    null)
            });
        }
        catch (IOException e)
        {
            logger.error(e);
        }
        
        try
        {
            if (!validateDataBaseInit())
            {
                createTables();
            }
            executeSqlFromFile(RESOURCE_CREATE_SETTINGS_TABLE);
        }
        catch (IOException e)
        {
            logger.error("Ошибка инициализации базы данных данных (обьектов) пользователя", e);
        }
    }

    /**
     * Создание таблиц в БД
     * @throws IOException
     */
    private void createTables() throws IOException
    {
        executeSqlFromFile(RESOURCE_CREATE_TABLES);
    }

    /**
     * Выполняет SQL скрипт из указанного файла
     * @param path
     * @throws IOException
     */
    private void executeSqlFromFile(String path) throws IOException
    {
        InputStreamReader streamReader = new InputStreamReader(getClass().getResourceAsStream(path),
                StandardCharsets.UTF_8);
        LineNumberReader reader = new LineNumberReader(streamReader);
        String query = ScriptUtils.readScript(reader, "--", ";");
        jdbcTemplate.getJdbcOperations().execute(query);
    }

    @Transactional
    @Override
    public void setUserObjects(Integer userId, List<Integer> objects)
    {
        clearUserObjects(userId);
        insertUserObjectsToDB(userId, objects);
    }

    @Transactional
    @Override
    public void addUserObjects(Integer userId, List<Integer> objects)
    {
        List<Integer> existingObjects =
                getUserObjects(userId).stream().map(UserGenerationObject::getGenerationObjectId).collect(toList());
        objects.removeAll(existingObjects);
        if (!objects.isEmpty())
        {
            insertUserObjectsToDB(userId, objects);
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void insertUserObjectsToDB(Integer userId, List<Integer> objects)
    {
        List<Map<String, Object>> batchValues = new ArrayList<>(objects.size());
        for (int object : objects)
        {
            batchValues.add(new MapSqlParameterSource().addValue("userid", userId).addValue("objectid", object).getValues());
        }
        jdbcTemplate.batchUpdate(SQL_ADD_USER_OBJECT, batchValues.toArray(new Map[objects.size()]));
    }

    @Transactional
    @Override
    public void clearUserObjects(Integer userId)
    {
        jdbcTemplate.update(SQL_CLEAR_USER_OBJECTS, new MapSqlParameterSource("userid", userId));
    }

    @Override
    public void updateUserObjectByRowId(Integer rowId, Integer userId, Integer object)
    {
        MapSqlParameterSource params = new MapSqlParameterSource("id", rowId);
        params.addValue("userid", userId);
        params.addValue("objectid", object);
        jdbcTemplate.update(SQL_UPDATE_USER_OBJECTS, params);
    }

    @Override
    public UserGenerationObject getUserObjectByRowId(Integer rowId)
    {
        MapSqlParameterSource params = new MapSqlParameterSource("id", rowId);
        return jdbcTemplate.queryForObject(SQL_GET_USER_OBJECT_BY_ROW_ID, params, new UserGenerationObjectMapper());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public void removeUserObjects(Integer userId, List<Integer> objects)
    {
        List<Map<String, Object>> batchValues = new ArrayList<>(objects.size());
        for (int object : objects)
        {
            batchValues.add(new MapSqlParameterSource().addValue("userid", userId).addValue("objectid", object).getValues());
        }
        jdbcTemplate.batchUpdate(SQL_REMOVE_USER_OBJECTS, batchValues.toArray(new Map[objects.size()]));
    }

    @Override
    public List<UserGenerationObject> getUserObjects(Integer userId)
    {
        final SqlParameterSource params = new MapSqlParameterSource("userid", userId);
        return jdbcTemplate.query(SQL_GET_USER_OBJECTS, params, new UserGenerationObjectMapper());
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void setRoleObjects(String role, Map<String, String> objects)
    {
        removeRoleObjects(role);
        List<Map<String, Object>> batchValues = new ArrayList<>(objects.size());
        for (String service : objects.keySet())
        {
            batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("service", service).addValue(
                    "objects", objects.get(service)).getValues());
        }
        jdbcTemplate.batchUpdate(SQL_ADD_ROLE_OBJECTS, batchValues.toArray(new Map[objects.size()]));
    }

    @Transactional
    @Override
    public void removeRoleObjects(String role)
    {
        jdbcTemplate.update(SQL_REMOVE_ROLE_OBJECTS, new MapSqlParameterSource("role", role));
    }

    /**
     * RowMapper для UserGenerationObject
     */
    public final class UserGenerationObjectMapper
            implements RowMapper<UserGenerationObject>
    {
        @Override
        public UserGenerationObject mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            UserGenerationObject userGenerationObject = new UserGenerationObject();
            userGenerationObject.setId(rs.getInt("id"));
            userGenerationObject.setUserId(rs.getInt("user_id"));
            userGenerationObject.setGenerationObjectId(rs.getInt("generation_object_id"));
            return userGenerationObject;
        }
    }

    @Override
    public JsonNode getUserSettings(int userId)
    {
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
    @Transactional
    public void setUserSettings(int userId, JsonNode settings)
    {
        MapSqlParameterSource map = new MapSqlParameterSource("userid", userId);
        jdbcTemplate.update(SQL_DELETE_USER_SETTINGS, map);
        try
        {
            map.addValue("settings", objectMapper.writeValueAsString(settings));
        }
        catch (JsonProcessingException e)
        {
            throw serviceException.applyParameters(e);
        }
        jdbcTemplate.update(SQL_SET_USER_SETTINGS, map);
    }

    @Override
    public List<LdapGroupGenerationObject> getLdapGroupGenerationObjects()
    {
        final String SQL_GET_ALL_LDAP_GROUP_GENERATION_OBJECTS = "SELECT ldap_group, generation_object_id FROM ldap_generation_objects";
        return jdbcTemplate.query(SQL_GET_ALL_LDAP_GROUP_GENERATION_OBJECTS, new LdapGroupGenerationObjectObjectMapper());
    }

    @Override
    public String getLdapGroupByGenerationObjectId(int generationObjectId)
    {
        final String SQL_GET_LDAP_GROUP_BY_GENERATION_OBJECT_ID = "SELECT ldap_group FROM ldap_generation_objects WHERE generation_object_id = :id";
        try
        {
            return jdbcTemplate.queryForObject(SQL_GET_LDAP_GROUP_BY_GENERATION_OBJECT_ID, new MapSqlParameterSource(
                    "id", generationObjectId), String.class);
        }
        catch (EmptyResultDataAccessException ex)
        {
            throw notFoundException.applyParameters(ERROR_LDAP_GROUP_FOR_GENERATION_OBJECT_ID_NOT_EXIST,
                    generationObjectId);
        }
    }

    @Override
    public int getGenerationObjectIdByLdapGroup(String group)
    {
        final String SQL_GET_GENERATION_OBJECT_ID_BY_LDAP_GROUP = "SELECT generation_object_id FROM ldap_generation_objects WHERE ldap_group = :group";
        try
        {
            return jdbcTemplate.queryForObject(SQL_GET_GENERATION_OBJECT_ID_BY_LDAP_GROUP, new MapSqlParameterSource("group", group), Integer.class);
        }
        catch (EmptyResultDataAccessException ex)
        {
            throw notFoundException.applyParameters(ERROR_GENERATION_OBJECT_ID_FOR_LDAP_GROUP_NOT_EXIST, group);
        }
    }

    @Override
    public void setGenerationObjectIdToLdapGroup(String ldapGroup, int generationObjectId)
    {
        try
        {
            int id = getGenerationObjectIdByLdapGroup(ldapGroup);
            if (id == generationObjectId)
            {
                // связь с указанными параметрами существует в БД
                return;
            }
            removeGenerationObjectFromLdapGroup(ldapGroup, id);
        }
        catch(NotFoundException ex)
        {
            // для ldapGroup нет записи в таблице
        }
        final String SQL_INSERT_LDAP_GROUP_GENERATION_OBJECT_ID =
                "INSERT INTO ldap_generation_objects (ldap_group, generation_object_id) VALUES (:group, :id)";
        jdbcTemplate.update(SQL_INSERT_LDAP_GROUP_GENERATION_OBJECT_ID, new MapSqlParameterSource("group", ldapGroup).addValue("id",
                generationObjectId));
    }

    @Override
    public void removeGenerationObjectFromLdapGroup(String ldapGroup, int generationObjectId)
    {
        final String SQL_REMOVE_GENERATION_OBJECT_ID_FROM_LDAP_GROUP = "DELETE FROM ldap_generation_objects WHERE ldap_group = :group AND generation_object_id = :id";
        SqlParameterSource params = new MapSqlParameterSource("group", ldapGroup).addValue("id", generationObjectId);
        int count = jdbcTemplate.update(SQL_REMOVE_GENERATION_OBJECT_ID_FROM_LDAP_GROUP, params);
        if (count == 0)
        {
            throw notFoundException.applyParameters(ERROR_GENERATION_OBJECT_ID_FOR_LDAP_GROUP_NOT_EXIST, ldapGroup);
        }
    }

    /**
     * RowMapper для LdapGroupGenerationObject
     */
    public final class LdapGroupGenerationObjectObjectMapper
            implements RowMapper<LdapGroupGenerationObject>
    {
        @Override
        public LdapGroupGenerationObject mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            LdapGroupGenerationObject ldapGroupGenerationObject = new LdapGroupGenerationObject();
            ldapGroupGenerationObject.setGroup(rs.getString("ldap_group"));
            ldapGroupGenerationObject.setGenerationObjectId(rs.getInt("generation_object_id"));
            return ldapGroupGenerationObject;
        }
    }

    @Override
    protected NamedParameterJdbcTemplate getJDBCTemplate()
    {
        return jdbcTemplate;
    }
}
