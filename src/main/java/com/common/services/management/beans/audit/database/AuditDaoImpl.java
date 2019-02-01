package com.common.services.management.beans.audit.database;

import com.common.services.management.beans.audit.model.Table;
import com.common.services.management.beans.audit.model.statementsparameter.SQLStatementParameter;
import com.common.services.management.beans.audit.model.statementsparameter.TimestampSQLStatementParameter;
import com.common.services.management.beans.audit.model.statementsparameter.UserSQLStatementParameter;
import com.common.services.management.beans.audit.model.users.ActiveUser;
import com.common.services.management.beans.serv.resourcemanager.ResourceManager;
import com.common.services.management.datasource.DataSourceManager;
import com.common.services.management.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AuditDaoImpl.java
 * Date: 11 сент. 2018 г.
 * Users: vmeshkov
 * Description: Реализация методов работы с базой данных аудита
 */
@Repository
public class AuditDaoImpl
        implements AuditDao
{
    /**
     * Название конфигурации базы данных
     */
    public static final String DB_CONFIG_NAME = "audit";
    /**
     * Путь к файлу с SQL скриптами создания таблиц для аудита
     */
    private static final String RESOURCE_CREATE_TABLES = "/db_audit_create_tables.sql";
    /**
     * SQL для проверки существования таблицы audit в БД
     */
    private static final String SQL_VALIDATE_AUDIT = "SELECT sessionid FROM audit LIMIT 1";
    /**
     * Параметры для SQL получения данных по аудиту
     */
    /**
     * Начальная дата
     */
    public static final String TIME_FROM = "timeFrom";
    /**
     * Конечная дата
     */
    public static final String TIME_TO = "timeTo";
    /**
     * Имя пользователя (логин)
     */
    public static final String USER = "user";
    /**
     * Идентификатор сессии
     */
    public static final String SESSIONID = "sessionid";
    /**
     * Признак запрос/ответ (запрос - Q, ответ - R)
     */
    public static final String RQ = "rq";
    /**
     * Ответы с ошибками
     */
    public static final String ERRORSTATUS = "errorstatus";
    /**
     * Идентификатор пользователя
     */
    public static final String USERID = "userid";
    /**
     * Статус ответа
     */
    public static final String STATUS = "status";
    /**
     * Метод запроса
     */
    public static final String METHOD = "method";
    /**
     * Путь в запросе
     */
    public static final String PATH = "path";

    /**
     * Список полей для select запроса
     */
    public static final Map<String, SQLStatementParameter> selectParameters =
        Collections.unmodifiableMap(Stream.of(
                new SimpleEntry<>(TIME_FROM, new TimestampSQLStatementParameter(TIME_FROM, "timeFromSign")),
                new SimpleEntry<>(TIME_TO, new TimestampSQLStatementParameter(TIME_TO, "timeToSign")),
                new SimpleEntry<>(USER, new UserSQLStatementParameter(USER, "userSign")),
                new SimpleEntry<>(USERID, new SQLStatementParameter(USERID, "useridSign")),
                new SimpleEntry<>(SESSIONID, new SQLStatementParameter(SESSIONID, "sessionidSign")),
                new SimpleEntry<>(RQ, new SQLStatementParameter(RQ, "rqSign")),
                new SimpleEntry<>(ERRORSTATUS, new SQLStatementParameter(ERRORSTATUS, "esSign")),
                new SimpleEntry<>(STATUS, new SQLStatementParameter(STATUS, "statusSign")),
                new SimpleEntry<>(PATH, new SQLStatementParameter(PATH, "pathSign")),
                new SimpleEntry<>(METHOD, new SQLStatementParameter(METHOD, "methodSign")))
           .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

    /**
     * SQL для получения данных по аудиту
     */
    private static final String SQL_SELECT = "select \"time\", \"user\", sessionid, rq, data from audit " +
        "where " + 
            "((\"time\" >= :" + selectParameters.get(TIME_FROM).getParam() + " or 1 = :" + selectParameters.get(TIME_FROM).getParamSign() + ") and " +
            "(\"time\" <= :" + selectParameters.get(TIME_TO).getParam() + " or 1 = :" + selectParameters.get(TIME_TO).getParamSign() + ") and " +
            "(\"user\" like :" + selectParameters.get(USER).getParam() + " or 1 = :" + selectParameters.get(USER).getParamSign() + ") and " +
            "(sessionid like :" + selectParameters.get(SESSIONID).getParam() + " or 1 = :" + selectParameters.get(SESSIONID).getParamSign() + ") and " +
            "(rq = :" + selectParameters.get(RQ).getParam() + " or 1 = :" + selectParameters.get(RQ).getParamSign() + ") and " +
            "(data->>'code' != '200' or 1 = :" + selectParameters.get(ERRORSTATUS).getParamSign() + ")) " +
            "order by \"time\" " +
            "limit %d offset %d";
    
    /**
     * SQL для получения количества строк по аудиту
     */
    private static final String SQL_COUNT_SELECT = "select count(\"time\") from audit " +
        "where " + 
            "((\"time\" >= :" + selectParameters.get(TIME_FROM).getParam() + " or 1 = :" + selectParameters.get(TIME_FROM).getParamSign() + ") and " +
            "(\"time\" <= :" + selectParameters.get(TIME_TO).getParam() + " or 1 = :" + selectParameters.get(TIME_TO).getParamSign() + ") and " +
            "(\"user\" like :" + selectParameters.get(USER).getParam() + " or 1 = :" + selectParameters.get(USER).getParamSign() + ") and " +
            "(sessionid like :" + selectParameters.get(SESSIONID).getParam() + " or 1 = :" + selectParameters.get(SESSIONID).getParamSign() + ") and " +
            "(rq = :" + selectParameters.get(RQ).getParam() + " or 1 = :" + selectParameters.get(RQ).getParamSign() + ") and " +
            "(data->>'code' != '200' or 1 = :" + selectParameters.get(ERRORSTATUS).getParamSign() + "))";
    
    /**
     * SQL для получения запросов с ответами
     */
    private static final String SQL_REQUESTS = "select " + 
        "data->'requestJson'->>'time' as timereq," + 
        "\"time\" as timeres," + 
        "userid," + 
        "sessionid," +
        "data->'requestJson'->>'method' as method," + 
        "data->'requestJson'->>'path' as path," + 
        "data->>'code' as status " + 
        "from audit where rq='R' " +
            "and ((cast(data->'requestJson'->>'time' as timestamp) >= to_timestamp(:" + selectParameters.get(TIME_FROM).getParam() + ", 'YYYY-MM-DD HH24:MI:SS.MS') or 1 = :" + selectParameters.get(TIME_FROM).getParamSign() + ") " +
            "and  (cast(data->'requestJson'->>'time' as timestamp) <= to_timestamp(:" + selectParameters.get(TIME_TO).getParam() + ", 'YYYY-MM-DD HH24:MI:SS.MS') or 1 = :" + selectParameters.get(TIME_TO).getParamSign() + ") " + 
            "and  (userid = :" + selectParameters.get(USERID).getParam() + " or 1 = :" + selectParameters.get(USERID).getParamSign() + ") " +
            "and  (data->'requestJson'->>'method' = :" + selectParameters.get(METHOD).getParam() + " or 1 = :" + selectParameters.get(METHOD).getParamSign() + ") " +
            "and  (data->'requestJson'->>'path' like :" + selectParameters.get(PATH).getParam() + " or 1 = :" + selectParameters.get(PATH).getParamSign() + ") " +
            "and  (cast(data->>'code' as integer) = any(regexp_split_to_array(:" + selectParameters.get(STATUS).getParam() + ",',')::int[]) or 1 = :" + selectParameters.get(STATUS).getParamSign() + ")" +
            ") " +
            "order by timereq " +
            "limit %d offset %d";
    
    /**
     * SQL для получения количества строк запросов с ответами
     */
    private static final String SQL_COUNT_REQUESTS = "select " + 
        "count(data) " + 
        "from audit where rq='R' " +
            "and ((cast(data->'requestJson'->>'time' as timestamp) >= to_timestamp(:" + selectParameters.get(TIME_FROM).getParam() + ", 'YYYY-MM-DD HH24:MI:SS.MS') or 1 = :" + selectParameters.get(TIME_FROM).getParamSign() + ") " +
            "and  (cast(data->'requestJson'->>'time' as timestamp) <= to_timestamp(:" + selectParameters.get(TIME_TO).getParam() + ", 'YYYY-MM-DD HH24:MI:SS.MS') or 1 = :" + selectParameters.get(TIME_TO).getParamSign() + ") " + 
            "and  (userid = :" + selectParameters.get(USERID).getParam() + " or 1 = :" + selectParameters.get(USERID).getParamSign() + ") " +
            "and  (data->'requestJson'->>'method' = :" + selectParameters.get(METHOD).getParam() + " or 1 = :" + selectParameters.get(METHOD).getParamSign() + ") " +
            "and  (data->'requestJson'->>'path' like :" + selectParameters.get(PATH).getParam() + " or 1 = :" + selectParameters.get(PATH).getParamSign() + ") " +
            "and  (cast(data->>'code' as integer) = any(regexp_split_to_array(:" + selectParameters.get(STATUS).getParam() + ",',')::int[]) or 1 = :" + selectParameters.get(STATUS).getParamSign() + ")" +
            ")";

    public static final String SQL_SELECT_ACTIVE_USERS = "select "
        + "data->'requestJson'->>'time',"
        + "\"user\","
        + "userid,"
        + "sessionid,"
        + "data->'requestJson'->>'adress' "
        +   "from audit "
            + "where "
                + "cast(data->'requestJson'->>'time' as timestamp) "
                + "between "
                + ":" + TIME_FROM
                + " and "
                + ":" + TIME_TO
                + " and "
                + "rq='R'";

    /**
     * Шаблон для выполнения SQL запросов
     */
    private NamedParameterJdbcTemplate jdbcTemplate;
    /**
     * Менеджер ресурсов
     */
    @Autowired
    private ResourceManager resourceManager;

    /**
     * Логгер
     */
    @Autowired
    private Logger logger;

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
        initTables();
    }

    /**
     * Проверяет базу данных на наличие таблиц для аудита
     * @return возращает false если одна из таблиц не существует
     */
    boolean validateDataBaseInit()
    {
        try
        {
            jdbcTemplate.getJdbcOperations().queryForList(SQL_VALIDATE_AUDIT, String.class);
        }
        catch (DataAccessException ex)
        {
            return false;
        }
        return true;
    }

    /**
     * Создание таблиц в БД если они не существуют
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
                logger.error("Ошибка инициализации базы данных аудита.", e);
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
        InputStreamReader streamReader = new InputStreamReader(getClass().getResourceAsStream(path), "UTF-8");
        LineNumberReader reader = new LineNumberReader(streamReader);
        String query = ScriptUtils.readScript(reader, "--", ";");
        jdbcTemplate.getJdbcOperations().execute(query);
    }

    @Override
    public Table list(Map<String, Object> params, int pageNumber, int pageSize)
    {
        return executeSQL(SQL_SELECT, params, pageNumber, pageSize).setCountRows((Integer)jdbcTemplate.queryForObject(
            SQL_COUNT_SELECT, params, Integer.class));
    }

    /**
     * @param query Текст select запроса
     * @param params параметры запроса
     * @param pageNumber номер страницы
     * @param pageSize размер страницы
     * @return
     */
    private Table executeSQL(String query, Map<String, Object> params, int pageNumber, int pageSize)
    {
        query = String.format(query, pageSize, pageSize * pageNumber);
        final Table table = new Table();
        // Получим имена колонок из запроса
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(query, params);
        SqlRowSetMetaData metaData = rowSet.getMetaData();

        int columns = metaData.getColumnCount();
        String[] headers = new String[columns];
        // Сохраним имена колонок
        for (int i = 1; i <= columns; ++i)
        {
            headers[i - 1] = Optional.ofNullable(metaData.getColumnLabel(i)).
                    orElse(metaData.getColumnName(i));
        }

        table.setHeaders(headers);

        // Получим значения в строках
        List<String[]> rows = new ArrayList<>();        
        while (rowSet.next())
        {
            String[] row = new String[columns];
            for (int i = 1; i <= columns; ++i)
            {
                row[i - 1] = Optional.ofNullable(rowSet.getString(i)).orElse("");
            }
            rows.add(row);
        } ;

        table.setRows(rows);
        return table;
    }

    /**
     * Сформировать список параметров для статического запроса
     * @return список параметров
     */
    public static Map<String, Object> getSelectParameters()
    {
        Map<String, Object> params = new HashMap<>();
        selectParameters.forEach((k, v) ->
        {
            params.put(v.getParam(), v.getObject(v.getDefaultValue()));
            params.put(v.getParamSign(), 1);
        });

        return params;
    }

    @Override
    public List<ActiveUser> getActiveUsers(Map<String, Object> params)
    {
        final Map<String, ActiveUser> map = new HashMap<>();
        jdbcTemplate.query(AuditDaoImpl.SQL_SELECT_ACTIVE_USERS, params, (ResultSet rs) ->
        {
            Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            if (rs.isBeforeFirst())
            {
                return;
            }
            do
            {
                Timestamp timestamp = rs.getTimestamp(1, utc);
                Instant date = timestamp.toInstant();
                String sessionId = rs.getString(4);
                ActiveUser activeUser = map.get(sessionId);
                if (activeUser == null)
                {
                    map.put(sessionId, new ActiveUser(resourceManager).
                            setLogin(rs.getString(2)).
                            setId(rs.getString(3)).
                            setStartTime(date).
                            setAdress(rs.getString(5)));
                }
                else
                {
                    activeUser.setDuration(Duration.between(activeUser.getStartTime(), date).toMillis());
                }
            } while (rs.next());
        });

        return map.values().stream().sorted(Comparator.comparing(ActiveUser::getStartTime)).
                collect(Collectors.toList());
    }

    @Override
    public Table requests(Map<String, Object> params, int pageNumber, int pageSize)
    {
        return executeSQL(SQL_REQUESTS, params, pageNumber, pageSize).setCountRows((Integer)jdbcTemplate.queryForObject(
            SQL_COUNT_REQUESTS, params, Integer.class));
    }
}
