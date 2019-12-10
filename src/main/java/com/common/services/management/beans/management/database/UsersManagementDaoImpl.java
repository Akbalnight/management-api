package com.common.services.management.beans.management.database;

import com.common.services.management.beans.management.model.*;
import com.common.services.management.beans.serv.exceptions.NotFoundException;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.beans.serv.exceptions.UserNotFoundException;
import com.common.services.management.datasource.DataSourceManager;
import com.common.services.management.filters.UsersFilter;
import com.common.services.management.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.common.services.management.beans.management.service.UsersManagementService.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * UsersManagementDaoImpl.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Реализация методов работы с базой данных пользователей
 */
@Component
@Repository
public class UsersManagementDaoImpl
        implements UsersManagementDao
{
    /**
     * Название конфигурации базы данных
     */
    public static final String DB_CONFIG_NAME = "auth";
    private static final String RESOURCE_CREATE_TABLES = "/db_auth_create_tables.sql";

    private static final String SQL_VALIDATE_USERS = "SELECT username FROM users LIMIT 1";
    private static final String SQL_VALIDATE_ROLES = "SELECT name FROM roles LIMIT 1";
    private static final String SQL_VALIDATE_PERMISSIONS = "SELECT id FROM permissions LIMIT 1";
    private static final String SQL_VALIDATE_USER_ROLES = "SELECT username FROM user_roles LIMIT 1";
    private static final String SQL_VALIDATE_ROLE_PERMISSIONS = "SELECT id_permission FROM role_permissions LIMIT 1";

    private static final String PAGINATION = " LIMIT :pageSize OFFSET :offset";


    private static final String SQL_INSERT_PERMISSION_ROLES =
            "INSERT INTO role_permissions (id_permission, role) " + "VALUES (:id, :role)";

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper jsonMapper;

    /**
     * Сообщения для обработки исключений
     */
    private static final String ERROR_USER_EXIST = "error.auth.userExist";
    private static final String ERROR_USER_ID_NOT_EXIST = "error.auth.userIdNotExist";
    private static final String ERROR_ROLE_EXIST = "error.auth.roleExist";
    private static final String ERROR_PERMISSION_EXIST = "error.auth.permissionExist";
    private static final String ERROR_ROLE_NOT_EXIST = "error.auth.roleNotExist";
    private static final String ERROR_PERMISSION_NOT_EXIST = "error.auth.permissionNotExist";
    private static final String ERROR_PERMISSION_WITH_ROLES_EXIST = "error.auth.permissionWithRolesExist";
    private static final String ERROR_PERMISSION_OR_ROLES_NOT_EXIST = "error.auth.permissionOrRolesNotExist";
    private static final String ERROR_ROLE_WITH_PERMISSIONS_EXIST = "error.auth.roleWithPermissionsExist";
    private static final String ERROR_ROLE_OR_PERMISSIONS_NOT_EXIST = "error.auth.roleOrPermissionsNotExist";
    private static final String ERROR_USER_WITH_ROLES_EXIST = "error.auth.userWithRolesExist";
    private static final String ERROR_USER_OR_ROLES_NOT_EXIST = "error.auth.userOrRolesNotExist";
    private static final String ERROR_USER_ROLE_NOT_EXIST = "error.auth.userRoleNotExist";
    private static final String ERROR_PERMISSION_ROLE_NOT_EXIST = "error.auth.permissionRoleNotExist";
    private static final String ERROR_ROLE_PERMISSION_NOT_EXIST = "error.auth.rolePermissionNotExist";
    private static final String ERROR_LDAP_GROUP_WITH_ROLES_EXIST = "error.auth.ldapGroupWithRolesExist";
    private static final String ERROR_ROLE_FROM_LIST_NOT__EXIST = "error.auth.roleFromListNotExist";
    private static final String ERROR_ROLE_LDAP_GROUP_NOT_EXIST = "error.auth.roleLDAPGroupNotExist";
    private static final String ERROR_GET_USERS_INVALID_FILTER = "error.auth.getUsersInvalidFilter";

    /**
     * Объект для генерации исключения
     */
    @Autowired
    private ServiceException serviceException;
    @Autowired
    private NotFoundException notFoundException;
    @Autowired
    private UserNotFoundException userNotFoundException;

    @Autowired
    private Logger logger;

    /**
     * Подключение менеджера конфигураций БД
     */
    @Autowired
    private DataSourceManager dataSourceManager;

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
     *  Подключение шаблона запросов и источника БД
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
    private boolean validateDataBaseInit()
    {
        try
        {
            jdbcTemplate.getJdbcOperations().queryForList(SQL_VALIDATE_USERS, String.class);
            jdbcTemplate.getJdbcOperations().queryForList(SQL_VALIDATE_ROLES, String.class);
            jdbcTemplate.getJdbcOperations().queryForList(SQL_VALIDATE_PERMISSIONS, Integer.class);
            jdbcTemplate.getJdbcOperations().queryForList(SQL_VALIDATE_USER_ROLES, String.class);
            jdbcTemplate.getJdbcOperations().queryForList(SQL_VALIDATE_ROLE_PERMISSIONS, Integer.class);
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
        if (!validateDataBaseInit())
        {
            try
            {
                createTables();
            }
            catch (IOException e)
            {
                logger.error("Ошибка инициализации базы данных пользователей и пермиссий", e);
            }
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

    /**
     * Возвращает данные пользователя с указанным логином
     * @param username логин пользователя
     * @return возвращает данные пользователя с указанным логином
     */
    @Override
    public User getUser(String username)
    {
        try
        {
            final String SQL_GET_USER_BY_NAME =
                    "SELECT username, user_id, enabled, email, json_data, ldap FROM users WHERE username = :username";
            final SqlParameterSource params = new MapSqlParameterSource("username", username);
            return jdbcTemplate.queryForObject(SQL_GET_USER_BY_NAME, params, new RowMapper<User>()
            {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException
                {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("username"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setEmail(rs.getString("email"));
                    user.setLdap(rs.getBoolean("ldap"));

                    String jsonString = rs.getString("json_data");
                    try
                    {
                        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>(){};
                        Map<String, Object> jsonData = jsonMapper.readValue(jsonString, typeRef);
                        user.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    return user;
                }
            });
        }
        catch (EmptyResultDataAccessException e)
        {
            throw userNotFoundException.applyUsername(username);
        }
    }

    /**
     * Возвращает данные пользователя с указанным id
     * @param id id пользователя
     * @return возвращает данные пользователя с указанным id
     */
    @Override
    public User getUser(int id)
    {
        try
        {
            final String SQL_GET_USER_BY_NAME =
                    "SELECT username, user_id, enabled, email, json_data, ldap FROM users WHERE user_id = :id";
            final SqlParameterSource params = new MapSqlParameterSource("id", id);
            return jdbcTemplate.queryForObject(SQL_GET_USER_BY_NAME, params, new RowMapper<User>()
            {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException
                {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("username"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setEmail(rs.getString("email"));
                    user.setLdap(rs.getBoolean("ldap"));

                    String jsonString = rs.getString("json_data");
                    try
                    {
                        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>(){};
                        Map<String, Object> jsonData = jsonMapper.readValue(jsonString, typeRef);
                        user.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    return user;
                }
            });
        }
        catch (EmptyResultDataAccessException e)
        {
            throw notFoundException.applyParameters(ERROR_USER_ID_NOT_EXIST, id);
        }
    }

    /**
     * Добавляет пользователя
     * Пользователя с указанным логином не должно быть в БД
     * @param user данные пользователя
     */
    @Override
    public int addUser(User user)
    {
        String json = null;
        try
        {
            json = jsonMapper.writeValueAsString(user.getJsonData());
        }
        catch (JsonProcessingException e)
        {
            logger.error(e);
        }

        try
        {
            String SQL_ADD_USER = "INSERT INTO users(username,password,enabled,email,json_data,ldap) " +
                    "VALUES (:username,:password,:enabled,:email,cast(:jsonData AS JSON),:ldap)";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("username", user.getName());
            params.addValue("password", user.getPassword());
            params.addValue("email", user.getEmail());
            params.addValue("enabled", user.isEnabled());
            params.addValue("ldap", user.getLdap());
            params.addValue("jsonData", json);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(SQL_ADD_USER, params, keyHolder, new String[]{"user_id"});
            return keyHolder.getKey().intValue();
        }
        catch (DuplicateKeyException ex)
        {
            throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_USER_EXIST, user.getName());
        }
    }

    /**
     * Обновляет данные пользователя
     * Пользователь с указанным логином должен быть записан в БД
     * @param id id пользователя
     * @param user     данные пользователя
     */
    @Override
    public void updateUser(int id, User user)
    {
        String json = null;
        try
        {
            json = jsonMapper.writeValueAsString(user.getJsonData());
        }
        catch (JsonProcessingException e)
        {
            logger.error(e);
        }

        try
        {
            final String SQL_UPDATE_USER =
                    "UPDATE users SET username = :newUsername, enabled = :enabled, email = :email, " +
                            "json_data = cast(:jsonData AS JSON), ldap = :ldap WHERE user_id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            params.addValue("newUsername", user.getName());
            params.addValue("email", user.getEmail());
            params.addValue("enabled", user.isEnabled());
            params.addValue("ldap", user.getLdap());
            params.addValue("jsonData", json);
            int resultCount = jdbcTemplate.update(SQL_UPDATE_USER, params);
            if (resultCount == 0)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_USER_ID_NOT_EXIST, id);
            }
        }
        catch (DuplicateKeyException ex)
        {
            throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_USER_EXIST, user.getName());
        }
    }

    @Override
    public void removeUser(int id)
    {
        final String SQL_REMOVE_USER = "DELETE FROM users WHERE user_id = :id";
        final SqlParameterSource params = new MapSqlParameterSource("id", id);
        int count = jdbcTemplate.update(SQL_REMOVE_USER, params);
        if (count == 0)
        {
            throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_USER_ID_NOT_EXIST, id);
        }
    }

    private List<User> getAllUsers(UsersFilter filter, Pageable pageable)
    {
        String sql = "SELECT username, user_id, enabled, email, json_data, ldap FROM users";
        String condition = "";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filter != null && !filter.isEmpty())
        {
            condition = " WHERE 1=1 ";
            if (filter.getUserIds() != null)
            {
                if (filter.getUserIds().size() > 0)
                {
                    params.addValue("userIds", filter.getUserIds());
                    if (filter.getIsIncludedUsers() == null || filter.getIsIncludedUsers())
                    {
                        condition += " AND user_id IN (:userIds) ";
                    }
                    else
                    {
                        condition += " AND user_id NOT IN (:userIds) ";
                    }
                }
                else
                {
                    if (filter.getIsIncludedUsers() == null || filter.getIsIncludedUsers())
                    {
                        // вернем пустой список для isIncludedUsers = true и пустого списка userIds
                        return Collections.emptyList();
                    }
                }
            }

            if (!CollectionUtils.isEmpty(filter.getJsonDataFilters()))
            {
                for (Map.Entry<String, List<Object>> entry : filter.getJsonDataFilters().entrySet())
                {
                    if (CollectionUtils.isEmpty(entry.getValue()))
                    {
                        throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_GET_USERS_INVALID_FILTER
                                , entry.getKey());
                    }
                    String jsonField = entry.getKey();
                    condition += " AND json_data ->> '" + jsonField + "' IN (:" + jsonField + ")";
                    params.addValue(jsonField, entry.getValue());
                }
            }
        }

        String order = "";
        String pagination = "";
        if (pageable != null)
        {
            if(pageable.getSort().isSorted())
            {
                Sort.Order sortField = pageable.getSort().get().findFirst().get();

                if (sortField.getProperty().equals("username") || sortField.getProperty().equals("user_id"))
                {
                    order += " ORDER BY " + sortField.getProperty() + " " + sortField.getDirection();
                }
                else
                {
                    order += " ORDER BY json_data->>'" + sortField.getProperty() + "' " + sortField.getDirection();
                }
            }
            else
            {
                order += " ORDER BY user_id ";
            }
            if (!StringUtils.isEmpty(pageable.getPageSize()) && !StringUtils.isEmpty(pageable.getPageNumber()))
            {
                params.addValue("pageSize", pageable.getPageSize());
                params.addValue("offset", pageable.getOffset());
                pagination += PAGINATION;
            }
        }

        sql += condition;
        sql += order;
        sql += pagination;
        return jdbcTemplate.query(sql, params, new RowMapper<User>()
        {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setName(rs.getString("username"));
                user.setEnabled(rs.getBoolean("enabled"));
                user.setLdap(rs.getBoolean("ldap"));
                user.setEmail(rs.getString("email"));

                String jsonString = rs.getString("json_data");
                try
                {
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>(){};
                    Map<String, Object> jsonData = jsonMapper.readValue(jsonString, typeRef);
                    user.setJsonData(jsonData);
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
                return user;
            }
        });
    }

    /**
     * Возвращает список пользователей с их данными
     * @param withRoles если флаг true данные пользователей будут содержать список их ролей
     * @param filter
     * @param pageable
     * @return Возвращает список пользователей с их данными
     */
    @Override
    public List<User> getAllUsers(boolean withRoles, UsersFilter filter, Pageable pageable)
    {
        if (!withRoles)
        {
            return getAllUsers(filter, pageable);
        }

        final String SQL_GET_ALL_USERS_WITH_ROLES = "SELECT u.username, u.user_id, u.enabled, u.email, u" +
                ".json_data, u.ldap, r.name AS role, r.description, r.json_data AS roleJson FROM users AS u LEFT JOIN" +
                " user_roles AS ur ON u.username = ur.username LEFT JOIN roles AS r ON r.name = ur.role";
        return jdbcTemplate.query(SQL_GET_ALL_USERS_WITH_ROLES, (ResultSet rs) ->
        {
            Map<String, User> map = new HashMap<>();
            User user;
            while (rs.next())
            {
                String username = rs.getString("username");
                user = map.get(username);
                if (user == null)
                {
                    user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(username);
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setLdap(rs.getBoolean("ldap"));
                    user.setEmail(rs.getString("email"));

                    String jsonString = rs.getString("json_data");
                    try
                    {
                        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>(){};
                        Map<String, Object> jsonData = jsonMapper.readValue(jsonString, typeRef);
                        user.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    user.setRoles(new ArrayList<>());
                    map.put(username, user);
                }
                String roleName = rs.getString("role");
                if (roleName != null)
                {
                    Role role = new Role();
                    role.setName(roleName);
                    role.setDescription(rs.getString("description"));
                    String jsonString = rs.getString("roleJson");
                    try
                    {
                        RoleJsonObject jsonData = jsonMapper.readValue(jsonString, RoleJsonObject.class);
                        role.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    user.getRoles().add(role);
                }
            }
            return new ArrayList<>(map.values());
        });
    }

    /**
     * Возвращает информацию об указанной роли
     * @param roleName название роли
     * @return возвращает информацию об указанной роли
     */
    @Override
    public Role getRole(String roleName)
    {
        try
        {
            final String SQL_GET_ROLE_BY_NAME = "SELECT name, description, json_data FROM roles WHERE name = :roleName";
            final SqlParameterSource params = new MapSqlParameterSource("roleName", roleName);
            return jdbcTemplate.queryForObject(SQL_GET_ROLE_BY_NAME, params, new RowMapper<Role>()
            {
                @Override
                public Role mapRow(ResultSet rs, int rowNum) throws SQLException
                {
                    Role role = new Role();
                    role.setName(rs.getString("name"));
                    role.setDescription(rs.getString("description"));

                    String jsonString = rs.getString("json_data");
                    try
                    {
                        RoleJsonObject jsonData = jsonMapper.readValue(jsonString, RoleJsonObject.class);
                        role.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    return role;
                }
            });
        }
        catch (EmptyResultDataAccessException e)
        {
            throw notFoundException.applyParameters(ERROR_ROLE_NOT_EXIST, roleName);
        }
    }

    /**
     * Добавляет роль
     * Роль с указанным названием не должна существовать в БД
     * @param role данные роли
     */
    @Override
    public void addRole(Role role)
    {
        String json = null;
        try
        {
            json = jsonMapper.writeValueAsString(role.getJsonData());
        }
        catch (JsonProcessingException e)
        {
            logger.error(e);
        }
        try
        {
            final String SQL_ADD_ROLE = "INSERT INTO roles(name,description,json_data) VALUES (:name," +
                    ":description,cast(:jsonData AS JSON))";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", role.getName());
            params.addValue("description", role.getDescription());
            params.addValue("jsonData", json);
            jdbcTemplate.update(SQL_ADD_ROLE, params);
        }
        catch (DuplicateKeyException ex)
        {
            throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_ROLE_EXIST, role.getName());
        }
    }

    /**
     * Обновляет данные роли
     * Роль с указанным названием должна существовать в БД
     * @param roleName название роли
     * @param role     данные роли для обновления
     */
    @Override
    public void updateRole(String roleName, Role role)
    {
        String json = null;
        try
        {
            json = jsonMapper.writeValueAsString(role.getJsonData());
        }
        catch (JsonProcessingException e)
        {
            logger.error(e);
        }
        try
        {
            final String SQL_UPDATE_ROLE = "UPDATE roles SET name = :newRoleName, description = :description, " +
                    "json_data = cast(:jsonData AS JSON) WHERE name = :roleName";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("roleName", roleName);
            params.addValue("newRoleName", role.getName());
            params.addValue("description", role.getDescription());
            params.addValue("jsonData", json);
            int resultCount = jdbcTemplate.update(SQL_UPDATE_ROLE, params);
            if (resultCount == 0)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_ROLE_NOT_EXIST, roleName);
            }
        }
        catch (DuplicateKeyException e)
        {
            throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_ROLE_EXIST, role.getName());
        }
    }

    /**
     * Удаляет роль
     * @param roleName название роли
     */
    @Override
    public void removeRole(String roleName)
    {
        final String SQL_REMOVE_ROLE = "DELETE FROM roles WHERE name=:roleName";
        SqlParameterSource params = new MapSqlParameterSource("roleName", roleName);
        int resultCount = jdbcTemplate.update(SQL_REMOVE_ROLE, params);
        if (resultCount == 0)
        {
            throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_ROLE_NOT_EXIST, roleName);
        }
    }

    private List<Role> getAllRoles()
    {
        final String SQL_GET_ALL_ROLES = "SELECT name, description, json_data FROM roles";
        return jdbcTemplate.query(SQL_GET_ALL_ROLES, new RowMapper<Role>()
        {
            @Override
            public Role mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                Role role = new Role();
                role.setName(rs.getString("name"));
                role.setDescription(rs.getString("description"));

                String jsonString = rs.getString("json_data");
                try
                {
                    RoleJsonObject jsonData = jsonMapper.readValue(jsonString, RoleJsonObject.class);
                    role.setJsonData(jsonData);
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
                return role;
            }
        });
    }

    /**
     * Возвращает список всех ролей
     * @param withPermissions если флаг true роли будут содержать список назначенных им пермиссий
     * @return возвращает список всех ролей
     */
    @Override
    public List<Role> getAllRoles(boolean withPermissions)
    {
        if (!withPermissions)
        {
            return getAllRoles();
        }
        final String SQL_GET_ALL_ROLES_WITH_PERMISSIONS = "SELECT r.name, r.description AS r_description, r.json_data "
                + "AS r_json, p.id AS p_id, p.description AS p_description, p.path AS p_path, p.method AS " +
                "p_method FROM roles AS r LEFT JOIN role_permissions AS rp ON r.name = rp.role LEFT JOIN permissions " +
                "AS p ON p.id = rp.id_permission";
        return jdbcTemplate.query(SQL_GET_ALL_ROLES_WITH_PERMISSIONS, (ResultSet rs) ->
        {
            Map<String, Role> map = new HashMap<>();
            Role role;
            while (rs.next())
            {
                String roleName = rs.getString("name");
                role = map.get(roleName);
                if (role == null)
                {
                    role = new Role();
                    role.setName(roleName);
                    role.setDescription(rs.getString("r_description"));

                    String jsonString = rs.getString("r_json");
                    try
                    {
                        RoleJsonObject jsonData = jsonMapper.readValue(jsonString, RoleJsonObject.class);
                        role.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    role.setPermissions(new ArrayList<>());
                    map.put(roleName, role);
                }
                if (rs.getObject("p_id") != null)
                {
                    Permission permission = new Permission();
                    permission.setId(rs.getInt("p_id"));
                    permission.setDescription(rs.getString("p_description"));
                    permission.setPath(rs.getString("p_path"));
                    permission.setMethod(rs.getString("p_method"));

                    role.getPermissions().add(permission);
                }
            }
            return new ArrayList<>(map.values());
        });
    }

    /**
     * Добавляет пермиссию
     * Пермиссия с укзанными path и method не должна существовать в БД
     * @param permission данные премиссии
     * @return id добавленной пермиссии
     */
    @Override
    public int addPermission(Permission permission)
    {
        String json = null;
        try
        {
            json = jsonMapper.writeValueAsString(permission.getJsonData());
        }
        catch (JsonProcessingException e)
        {
            logger.error(e);
        }
        
        try
        {
            final String SQL_ADD_PERMISSION = "INSERT INTO permissions(description,path,method,json_data) VALUES " +
                    "(:description,:path,:method,cast(:jsonData AS JSON))";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("description", permission.getDescription());
            params.addValue("path", permission.getPath());
            params.addValue("method", permission.getMethod().toString());
            params.addValue("jsonData", json);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(SQL_ADD_PERMISSION, params, keyHolder, new String[]{"id"});
            return keyHolder.getKey().intValue();
        }
        catch (DuplicateKeyException ex)
        {
            throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_PERMISSION_EXIST,
                    permission.getMethod(), permission.getPath());
        }
    }

    /**
     * Удаляет пермиссию
     * @param id id пермиссии
     */
    @Override
    public void removePermission(int id)
    {
        final String SQL_REMOVE_PERMISSION = "DELETE FROM permissions WHERE id=:id";
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        int resultCount = jdbcTemplate.update(SQL_REMOVE_PERMISSION, params);
        if (resultCount == 0)
        {
            throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_PERMISSION_NOT_EXIST, id);
        }
    }

    /**
     * Возвращает данные пермиссии
     * @param idPermission id пермиссии
     * @return возвращает данные пермиссии
     */
    @Override
    public Permission getPermission(int idPermission)
    {
        try
        {
            final String SQL_GET_PERMISSION_BY_ID = "SELECT id, description, path, method, json_data FROM " +
                    "permissions WHERE id = :id";
            final SqlParameterSource params = new MapSqlParameterSource("id", idPermission);
            return jdbcTemplate.queryForObject(SQL_GET_PERMISSION_BY_ID, params, new RowMapper<Permission>()
            {
                @Override
                public Permission mapRow(ResultSet rs, int rowNum) throws SQLException
                {
                    Permission permission = new Permission();
                    permission.setId(rs.getInt("id"));
                    permission.setDescription(rs.getString("description"));
                    permission.setPath(rs.getString("path"));
                    permission.setMethod(rs.getString("method"));

                    String jsonString = rs.getString("json_data");
                    try
                    {
                        PermissionJsonObject jsonData = jsonMapper.readValue(jsonString, PermissionJsonObject.class);
                        permission.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    return permission;
                }
            });
        }
        catch (EmptyResultDataAccessException e)
        {
            throw notFoundException.applyParameters(ERROR_PERMISSION_NOT_EXIST, idPermission);
        }
    }

    /**
     * Возвращает список пермиссий
     * @return возвращает список пермиссий
     */
    @Override
    public List<Permission> getAllPermissions()
    {
        final String SQL_GET_ALL_PERMISSIONS = "SELECT id, description, path, method, json_data FROM permissions";
        return jdbcTemplate.query(SQL_GET_ALL_PERMISSIONS, new RowMapper<Permission>()
        {
            @Override
            public Permission mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                Permission permission = new Permission();
                permission.setId(rs.getInt("id"));
                permission.setDescription(rs.getString("description"));
                permission.setPath(rs.getString("path"));
                permission.setMethod(rs.getString("method"));

                String jsonString = rs.getString("json_data");
                if (jsonString != null)
                {
                    try
                    {
                        PermissionJsonObject jsonData = jsonMapper.readValue(jsonString, PermissionJsonObject.class);
                        permission.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                }
                return permission;
            }
        });
    }

    /**
     * Обновляет данные премиссии
     * Пермиссия с указанным id должна существовать в БД
     * @param idPermission id пермиссии
     * @param permission   данные пермиссии для обновления
     */
    @Override
    public void updatePermission(int idPermission, Permission permission)
    {
        String json = null;
        try
        {
            json = jsonMapper.writeValueAsString(permission.getJsonData());
        }
        catch (JsonProcessingException e)
        {
            logger.error(e);
        }

        try
        {
            final String SQL_UPDATE_PERMISSION =
                    "UPDATE permissions SET path = :path, description = :description, method = :method, " +
                            "json_data = cast(:jsonData AS JSON) WHERE id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", idPermission);
            params.addValue("description", permission.getDescription());
            params.addValue("path", permission.getPath());
            params.addValue("method", permission.getMethod(), Types.VARCHAR);
            params.addValue("jsonData", json);
            int resultCount = jdbcTemplate.update(SQL_UPDATE_PERMISSION, params);
            if (resultCount == 0)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_PERMISSION_NOT_EXIST, idPermission);
            }
        }
        catch (DuplicateKeyException e)
        {
            throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_PERMISSION_EXIST,
                    permission.getMethod(), permission.getPath());
        }
    }

    /**
     * Открепляет указанные роли от пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void removePermissionRoles(int idPermission, RoleNameList roles)
    {
        final String SQL_REMOVE_ROLES_PERMISSIONS = "DELETE FROM role_permissions WHERE role = :role AND " +
                "id_permission = :id";
        List<Map<String, Object>> batchValues = new ArrayList<>(roles.getRoles().size());
        for (String role : roles.getRoles())
        {
            batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("id", idPermission).getValues());
        }
        int[] result = jdbcTemplate.batchUpdate(SQL_REMOVE_ROLES_PERMISSIONS,
                batchValues.toArray(new Map[roles.getRoles().size()]));
        for (int i = 0; i < result.length; i++)
        {
            if (result[i] == 0)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_PERMISSION_ROLE_NOT_EXIST,
                        idPermission, roles.getRoles().get(i));
            }
        }
    }

    private void clearPermissionRoles(int idPermission)
    {
        final String SQL_REMOVE_PERMISSION_FROM_ROLES = "DELETE FROM role_permissions WHERE id_permission=:id";
        SqlParameterSource params = new MapSqlParameterSource("id", idPermission);
        jdbcTemplate.update(SQL_REMOVE_PERMISSION_FROM_ROLES, params);
    }

    /**
     * Устанавливает указанные роли для пермиссии
     * Все роли не указанные в списке и прикрепленные к пермиссии будут откреплены от указанной пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    @Override
    @Transactional
    public void setPermissionRoles(int idPermission, RoleNameList roles)
    {
        clearPermissionRoles(idPermission);
        addPermissionRoles(idPermission, roles);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void addPermissionRoles(int idPermission, RoleNameList roles)
    {
        List<String> existingRoles = getPermissionRoles(idPermission).stream().map(Role::getName).collect(toList());
        roles.getRoles().removeAll(existingRoles);
        if (!roles.getRoles().isEmpty())
        {
            try
            {
                List<Map<String, Object>> batchValues = new ArrayList<>(roles.getRoles().size());
                for (String role : roles.getRoles())
                {
                    batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("id", idPermission).getValues());
                }
                jdbcTemplate.batchUpdate(SQL_INSERT_PERMISSION_ROLES,
                        batchValues.toArray(new Map[roles.getRoles().size()]));
            }
            catch (DuplicateKeyException ex)
            {
                throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_PERMISSION_WITH_ROLES_EXIST,
                        idPermission, roles.getRoles().toString());
            }
            catch (DataIntegrityViolationException ex)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_PERMISSION_OR_ROLES_NOT_EXIST,
                        idPermission, roles.getRoles().toString());
            }
        }
    }

    /**
     * Удаляет указанные премиссии у роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void removeRolePermissions(String role, PermissionIdList permissions)
    {
        final String SQL_REMOVE_ROLES_PERMISSIONS = "DELETE FROM role_permissions WHERE role = :role AND " +
                "id_permission = :id";
        List<Map<String, Object>> batchValues = new ArrayList<>(permissions.getIds().size());
        for (int idPermission : permissions.getIds())
        {
            batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("id", idPermission).getValues());
        }
        int[] result = jdbcTemplate.batchUpdate(SQL_REMOVE_ROLES_PERMISSIONS,
                batchValues.toArray(new Map[permissions.getIds().size()]));
        for (int i = 0; i < result.length; i++)
        {
            if (result[i] == 0)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_ROLE_PERMISSION_NOT_EXIST, role,
                        permissions.getIds().get(i));
            }
        }
    }

    private void clearRolePermissions(String role)
    {
        final String SQL_REMOVE_PERMISSION_FROM_ROLES = "DELETE FROM role_permissions WHERE role=:role";
        SqlParameterSource params = new MapSqlParameterSource("role", role);
        jdbcTemplate.update(SQL_REMOVE_PERMISSION_FROM_ROLES, params);
    }

    /**
     * Устанавливает список пермиссий для роли
     * Все пермиссии привязанные к роли и не входящие в список добавления будут откреплены от роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    @Override
    @Transactional
    public void setRolePermissions(String role, PermissionIdList permissions)
    {
        clearRolePermissions(role);
        addRolePermissions(role, permissions);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void addRolePermissions(String role, PermissionIdList permissions)
    {
        List<Integer> existingPermissions = getRolePermissions(role).stream().map(Permission::getId).collect(toList());
        permissions.getIds().removeAll(existingPermissions);
        if (!permissions.getIds().isEmpty())
        {
            try
            {
                List<Map<String, Object>> batchValues = new ArrayList<>(permissions.getIds().size());
                for (int idPermission : permissions.getIds())
                {
                    batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("id", idPermission).getValues());
                }
                jdbcTemplate.batchUpdate(SQL_INSERT_PERMISSION_ROLES,
                        batchValues.toArray(new Map[permissions.getIds().size()]));
            }
            catch (DuplicateKeyException ex)
            {
                throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_ROLE_WITH_PERMISSIONS_EXIST, role,
                        permissions.getIds().toString());
            }
            catch (DataIntegrityViolationException ex)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_ROLE_OR_PERMISSIONS_NOT_EXIST,
                        role, permissions.getIds().toString());
            }
        }
    }

    /**
     * Возвращает список ролей, привязанных к указанной пермиссии
     * @param idPermission - id пермиссии
     * @return возвращает список ролей, привязанных к указанной пермиссии
     */
    @Override
    public List<Role> getPermissionRoles(int idPermission)
    {
        final String SQL_GET_ALL_ROLES_BY_PERMISSION = "SELECT r.name, r.description AS r_description,r.json_data AS "
                + "r_json " + "FROM roles AS r JOIN role_permissions AS rp ON r.name = rp.role JOIN permissions "
                + "AS p ON p.id = rp.id_permission WHERE p.id = :id";
        return jdbcTemplate.query(SQL_GET_ALL_ROLES_BY_PERMISSION, new MapSqlParameterSource("id", idPermission),
                (ResultSet rs) ->
        {
            Map<String, Role> map = new HashMap<>();
            Role role;
            while (rs.next())
            {
                String roleName = rs.getString("name");
                role = map.get(roleName);
                if (role == null)
                {
                    role = new Role();
                    role.setName(roleName);
                    role.setDescription(rs.getString("r_description"));
                    String jsonString = rs.getString("r_json");
                    try
                    {
                        RoleJsonObject jsonData = jsonMapper.readValue(jsonString, RoleJsonObject.class);
                        role.setJsonData(jsonData);
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                    map.put(roleName, role);
                }
            }
            return new ArrayList<>(map.values());
        });
    }

    /**
     * Возвращает список пермиссий, привязанных к указанной роли
     * @param role название роли
     * @return возвращает список пермиссий, привязанных к указанной роли
     */
    @Override
    public List<Permission> getRolePermissions(String role)
    {
        final String SQL_GET_PERMISSIONS_BY_ROLE = "SELECT p.id, p.method, p.path, p.description FROM " +
                "permissions AS p JOIN role_permissions AS rp ON p.id=rp.id_permission WHERE rp.role = :role";
        return jdbcTemplate.query(SQL_GET_PERMISSIONS_BY_ROLE, new MapSqlParameterSource("role", role),
                (ResultSet rs) ->
        {
            Map<Integer, Permission> map = new HashMap<>();
            Permission permission;
            while (rs.next())
            {
                int id = rs.getInt("id");
                permission = map.get(id);
                if (permission == null)
                {
                    permission = new Permission();
                    permission.setId(rs.getInt("id"));
                    permission.setDescription(rs.getString("description"));
                    permission.setPath(rs.getString("path"));
                    permission.setMethod(rs.getString("method"));
                    map.put(id, permission);
                }
            }
            return new ArrayList<>(map.values());
        });
    }

    /**
     * Возвращает список пользователей, имеющих указанную роль
     * @param role название роли
     * @return возвращает список пользователей, имеющих указанную роль
     */
    @Override
    public List<User> getRoleUsers(String role)
    {
        final String SQL_GET_USERS_BY_ROLE = "SELECT u.username, u.user_id, u.enabled, u.email, u.json_data, u.ldap "
                + "FROM users AS u JOIN user_roles AS ur ON u.username = ur.username JOIN roles AS r ON r.name = ur.role WHERE ur.role = :role";
        return jdbcTemplate.query(SQL_GET_USERS_BY_ROLE, new MapSqlParameterSource("role", role), new RowMapper<User>()
        {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setName(rs.getString("username"));
                user.setEnabled(rs.getBoolean("enabled"));
                user.setLdap(rs.getBoolean("ldap"));
                user.setEmail(rs.getString("email"));

                String jsonString = rs.getString("json_data");
                try
                {
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>(){};
                    Map<String, Object> jsonData = jsonMapper.readValue(jsonString, typeRef);
                    user.setJsonData(jsonData);
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
                return user;
            }
        });
    }

    /**
     * Удаляет указанные роли у пользователя
     * Указанные роли должны быть привязаны к пользователю
     * @param username логин пользователя
     * @param roles    список названий ролей
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void removeUserRoles(String username, RoleNameList roles)
    {
        final String SQL_REMOVE_ROLES_FROM_USER = "DELETE FROM user_roles WHERE role=:role and username = :username";
        List<Map<String, Object>> batchValues = new ArrayList<>(roles.getRoles().size());
        for (String role : roles.getRoles())
        {
            batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("username", username).getValues());
        }
        int[] result = jdbcTemplate.batchUpdate(SQL_REMOVE_ROLES_FROM_USER,
                batchValues.toArray(new Map[roles.getRoles().size()]));
        for (int i = 0; i < result.length; i++)
        {
            if (result[i] == 0)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_USER_ROLE_NOT_EXIST, username,
                        roles.getRoles().get(i));
            }
        }
    }

    private void clearUserRoles(String username)
    {
        final String SQL_REMOVE_ROLES_FROM_USER = "DELETE FROM user_roles WHERE username=:username";
        SqlParameterSource params = new MapSqlParameterSource("username", username);
        jdbcTemplate.update(SQL_REMOVE_ROLES_FROM_USER, params);
    }

    /**
     * Устанаваливает указанные роли пользователю
     * Все роли пользователя не указанные в списке будут удалены
     * @param username логин пользователя
     * @param roles
     */
    @Override
    @Transactional
    public void setUserRoles(String username, RoleNameList roles)
    {
        clearUserRoles(username);
        addUserRoles(username, roles);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void addUserRoles(String username, RoleNameList roles)
    {
        List<String> existingRoles = getUserRoles(username).stream().map(Role::getName).collect(toList());
        roles.getRoles().removeAll(existingRoles);
        if (!roles.getRoles().isEmpty())
        {
            try
            {
                final String SQL_INSERT_USER_ROLES = "INSERT INTO user_roles (username, role) VALUES (:username, :role);";
                List<Map<String, Object>> batchValues = new ArrayList<>(roles.getRoles().size());
                for (String role : roles.getRoles())
                {
                    batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("username", username).getValues());
                }
                jdbcTemplate.batchUpdate(SQL_INSERT_USER_ROLES, batchValues.toArray(new Map[roles.getRoles().size()]));
            }
            catch (DuplicateKeyException ex)
            {
                throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_USER_WITH_ROLES_EXIST, username,
                        roles.getRoles().toString());
            }
            catch (DataIntegrityViolationException ex)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_USER_OR_ROLES_NOT_EXIST, username,
                        roles.getRoles().toString());
            }
        }
    }

    /**
     * Возвращает список ролей указанного пользователя
     * @param username логин пользователя
     * @return возвращает список ролей пользователя
     */
    @Override
    public List<Role> getUserRoles(String username)
    {
        final String SQL_GET_ALL_ROLES_BY_USERNAME = "SELECT r.name, r.description, r.json_data FROM user_roles "
                + "AS ur JOIN roles AS r ON r.name = ur.role WHERE ur.username = :username";
        List<Role> roles = jdbcTemplate.query(SQL_GET_ALL_ROLES_BY_USERNAME, new MapSqlParameterSource("username",
                username), new RowMapper<Role>()
        {
            @Override
            public Role mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                Role role = new Role();
                role.setName(rs.getString("name"));
                role.setDescription(rs.getString("description"));

                String jsonString = rs.getString("json_data");
                try
                {
                    RoleJsonObject jsonData = jsonMapper.readValue(jsonString, RoleJsonObject.class);
                    role.setJsonData(jsonData);
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
                return role;
            }
        });
        roles.sort(comparing(Role::getName));
        return roles;
    }

    @Override
    public void setUserEnabled(int userId, boolean enabled)
    {
        final String SQL_UPDATE_USER_ENABLED = "UPDATE users SET enabled = :enabled WHERE user_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("enabled", enabled);
        params.addValue("id", userId);
        int resultCount = jdbcTemplate.update(SQL_UPDATE_USER_ENABLED, params);
        if (resultCount == 0)
        {
            throw notFoundException.applyParameters(HttpStatus.NOT_FOUND, ERROR_USER_ID_NOT_EXIST, userId);
        }
    }

    @Override
    public String getUserPassword(int userId)
    {
        try
        {
            final String SQL_GET_USER_PASSWORD = "SELECT password FROM users WHERE user_id = :id";
            final SqlParameterSource params = new MapSqlParameterSource("id", userId);
            return jdbcTemplate.queryForObject(SQL_GET_USER_PASSWORD, params, String.class);
        }
        catch (EmptyResultDataAccessException e)
        {
            throw notFoundException.applyParameters(ERROR_USER_ID_NOT_EXIST, userId);
        }
    }

    /**
     * Изменение пароля пользователя
     * @param userId   id пользователя
     * @param password хэш пароля пользователя
     */
    @Override
    public void setUserPassword(int userId, String password)
    {
        final String SQL_UPDATE_USER_PASSWORD = "UPDATE users SET password = :password WHERE user_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("password", password);
        params.addValue("id", userId);
        int resultCount = jdbcTemplate.update(SQL_UPDATE_USER_PASSWORD, params);
        if (resultCount == 0)
        {
            throw notFoundException.applyParameters(HttpStatus.NOT_FOUND, ERROR_USER_ID_NOT_EXIST, userId);
        }
    }

    @Override
    public LdapGroupsResult getLdapGroups(Integer pageNumber, Integer pageSize)
    {
        LdapGroupsResult result = new LdapGroupsResult();
        result.setGroups(getLdapGroupsPaging(pageNumber, pageSize));
        result.setCount(getLdapGroupsCount());
        return result;
    }

    private int getLdapGroupsCount()
    {
        final String SQL_COUNT_LDAP_GROUPS = "WITH gr AS (SELECT DISTINCT ldap_group FROM ldap_roles) SELECT COUNT(*) FROM gr";
        return jdbcTemplate.getJdbcOperations().queryForObject(SQL_COUNT_LDAP_GROUPS, Integer.class);
    }

    private List<LdapGroup> getLdapGroupsPaging(Integer pageNumber, Integer pageSize)
    {
        final String SQL_GET_ALL_LDAP_GROUPS =
                "SELECT ldap_group, role FROM ldap_roles WHERE ldap_group IN (SELECT DISTINCT ldap_group FROM ldap_roles LIMIT :limit OFFSET :offset)";
        return jdbcTemplate.query(SQL_GET_ALL_LDAP_GROUPS,
                new MapSqlParameterSource("limit", pageSize)
                        .addValue("offset", pageNumber * pageSize),
                (ResultSet rs) ->
        {
            Map<String, LdapGroup> map = new HashMap<>();
            LdapGroup ldapGroup;
            while (rs.next())
            {
                String group = rs.getString("ldap_group");
                ldapGroup = map.get(group);
                if (ldapGroup == null)
                {
                    ldapGroup = new LdapGroup();
                    ldapGroup.setGroup(group);
                    ldapGroup.setRoles(new ArrayList<>());
                    map.put(group, ldapGroup);
                }
                String role = rs.getString("role");
                ldapGroup.getRoles().add(role);
            }
            return new ArrayList<>(map.values());
        });
    }

    @Override
    public List<String> getRolesByLdapGroup(String group)
    {
        final String SQL_GET_ROLES_BY_LDAP_GROUP = "SELECT role FROM ldap_roles WHERE ldap_group = :group";
        return jdbcTemplate.queryForList(SQL_GET_ROLES_BY_LDAP_GROUP, new MapSqlParameterSource("group", group),
                String.class);
    }

    @Override
    public List<String> getLdapGroupsByRole(String role)
    {
        final String SQL_GET_LDAP_GROUPS_BY_ROLE = "SELECT ldap_group FROM ldap_roles WHERE  role= :role";
        return jdbcTemplate.queryForList(SQL_GET_LDAP_GROUPS_BY_ROLE, new MapSqlParameterSource("role", role),
                String.class);
    }

    @Transactional
    @Override
    @SuppressWarnings("unchecked")
    public void addRolesToLdapGroup(String group, List<String> roles)
    {
        List<String> existingRoles = getRolesByLdapGroup(group);
        roles.removeAll(existingRoles);
        if (!roles.isEmpty())
        {
            try
            {
                final String SQL_INSERT_LDAP_GROUP_ROLES =
                        "INSERT INTO ldap_roles (ldap_group, role) " + "VALUES " + "(:group, :role)";
                List<Map<String, Object>> batchValues = new ArrayList<>(roles.size());
                for (String role : roles)
                {
                    batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("group", group).getValues());
                }
                jdbcTemplate.batchUpdate(SQL_INSERT_LDAP_GROUP_ROLES, batchValues.toArray(new Map[roles.size()]));
            }
            catch (DuplicateKeyException ex)
            {
                // совпадение ролей в списке
                throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_LDAP_GROUP_WITH_ROLES_EXIST, group,
                        roles.toString());
            }
            catch (DataIntegrityViolationException ex)
            {
                // несуществующая в БД роль
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_ROLE_FROM_LIST_NOT__EXIST,
                        roles.toString());
            }
        }
    }

    @Transactional
    @Override
    public void setRolesToLdapGroup(String group, List<String> roles)
    {
        clearLdapGroup(group);
        addRolesToLdapGroup(group, roles);
    }

    @Transactional
    @Override
    public void clearLdapGroup(String group)
    {
        final String SQL_REMOVE_ROLES_FROM_LDAP_GROUPS = "DELETE FROM ldap_roles WHERE ldap_group=:group";
        SqlParameterSource params = new MapSqlParameterSource("group", group);
        jdbcTemplate.update(SQL_REMOVE_ROLES_FROM_LDAP_GROUPS, params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int addPermissions(List<Permission> permissions, List<String> roles)
    {
        if (permissions.size() == 0)
        {
            return 0;
        }
        if (roles == null || roles.size() == 0)
        {
            final String SQL_ADD_PERMISSION =
                    "INSERT INTO permissions(description, path, method, json_data) VALUES (:description, :path, :method, cast(:jsonData AS JSON))";

            List<Map<String, Object>> batchValues = new ArrayList<>(permissions.size());
            for (Permission permission : permissions)
            {
                String json = null;
                try
                {
                    json = jsonMapper.writeValueAsString(permission.getJsonData());
                }
                catch (JsonProcessingException e)
                {
                    logger.error(e);
                }
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("description", permission.getDescription());
                params.addValue("path", permission.getPath());
                params.addValue("method", permission
                        .getMethod()
                        .toString());
                params.addValue("jsonData", json);
                batchValues.add(params.getValues());
            }
            int[] result = jdbcTemplate.batchUpdate(SQL_ADD_PERMISSION,
                    batchValues.toArray(new Map[permissions.size()]));
            // Количество добавленных записей
            return (int) Arrays
                    .stream(result)
                    .filter(item -> item == 1)
                    .count();
        }
        else
        {
            StringBuilder query = new StringBuilder();
            permissions.forEach(p ->
            {
                query.append("INSERT INTO permissions (description,path,method,json_data) VALUES ('")
                     .append(p.getDescription())
                     .append("', '")
                     .append(p.getPath())
                     .append("', '")
                     .append(p.getMethod())
                     .append("', '")
                     .append(p.getJsonData() == null ? "{}" : p.getJsonData())
                     .append("');");
                roles.forEach(r ->
                {
                    query.append("INSERT INTO role_permissions (role, id_permission) VALUES ('")
                         .append(r)
                         .append("', currval(pg_get_serial_sequence('permissions', 'id')));");
                });
            });

            jdbcTemplate
                    .getJdbcOperations()
                    .execute(query.toString());
        }
        return permissions.size();
    }

    @Override
    public List<Permission> getUnlinkedPermissions()
    {
        final String SQL_GET_ALL_PERMISSIONS = "SELECT id, description, path, method, json_data FROM permissions AS p LEFT JOIN role_permissions AS rp ON p.id = rp.id_permission WHERE rp.role IS NULL";
        return jdbcTemplate.query(SQL_GET_ALL_PERMISSIONS, new RowMapper<Permission>()
        {
            @Override
            public Permission mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                Permission permission = new Permission();
                permission.setId(rs.getInt("id"));
                permission.setDescription(rs.getString("description"));
                permission.setPath(rs.getString("path"));
                permission.setMethod(rs.getString("method"));

                String jsonString = rs.getString("json_data");
                try
                {
                    PermissionJsonObject jsonData = jsonMapper.readValue(jsonString, PermissionJsonObject.class);
                    permission.setJsonData(jsonData);
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
                return permission;
            }
        });
    }

    @Override
    public List<UserShort> getShortUsersWithRole(String role)
    {
        final String SQL_GET_USER_BY_ROLE ="SELECT u.username, u.user_id, u.json_data FROM users AS u JOIN user_roles" +
                " AS ur ON u.username = ur.username AND ur.role = :role";
        final SqlParameterSource params = new MapSqlParameterSource("role", role);
        return jdbcTemplate.query(SQL_GET_USER_BY_ROLE, params, new RowMapper<UserShort>()
        {
            @Override
            public UserShort mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                UserShort user = new UserShort();
                user.setId(rs.getInt("user_id"));
                String username = rs.getString("username");
                user.setShortName(username);
                user.setFullName(username);

                String firstName = "";
                String lastName = "";
                String middleName = "";

                String jsonString = rs.getString("json_data");
                try
                {
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>(){};
                    Map<String, Object> jsonData = jsonMapper.readValue(jsonString, typeRef);
                    for (Map.Entry<String, Object> entry : jsonData.entrySet())
                    {
                        if (entry.getKey().equals(FIRST_NAME))
                        {
                            firstName = entry.getValue().toString();
                        }
                        else if (entry.getKey().equals(LAST_NAME))
                        {
                            lastName = entry.getValue().toString();
                        }
                        else if (entry.getKey().equals(MIDDLE_NAME))
                        {
                            middleName = entry.getValue().toString();
                        }
                    }
                    if (!lastName.isEmpty())
                    {
                        user.setFullName((
                                        (lastName.isEmpty() ? "" : (lastName + " ")) +
                                        (firstName.isEmpty() ? "" : (firstName + " ")) +
                                        (middleName.isEmpty() ? "" : (middleName + " "))
                                ).trim());
                        user.setShortName((
                                        (lastName.isEmpty() ? "" : (lastName + " ")) +
                                        (firstName.isEmpty() ? "" : (firstName.charAt(0)+ ".")) +
                                        (middleName.isEmpty() ? "" : (middleName.charAt(0) + "."))
                                ).trim());
                    }
                }
                catch (Exception e)
                {
                    logger.error(e);
                }
                return user;
            }
        });
    }

    @Transactional
    @Override
    @SuppressWarnings("unchecked")
    public void removeRolesFromLdapGroup(String group, List<String> roles)
    {
        final String SQL_REMOVE_ROLES_FROM_LDAP_GROUPS = "DELETE FROM ldap_roles WHERE role = :role AND ldap_group = :group";
        List<Map<String, Object>> batchValues = new ArrayList<>(roles.size());
        for (String role : roles)
        {
            batchValues.add(new MapSqlParameterSource().addValue("role", role).addValue("group", group).getValues());
        }
        int[] result = jdbcTemplate.batchUpdate(SQL_REMOVE_ROLES_FROM_LDAP_GROUPS,
                batchValues.toArray(new Map[roles.size()]));
        for (int i = 0; i < result.length; i++)
        {
            if (result[i] == 0)
            {
                throw serviceException.applyParameters(HttpStatus.NOT_FOUND, ERROR_ROLE_LDAP_GROUP_NOT_EXIST,
                        roles.get(i), group);
            }
        }
    }
}
