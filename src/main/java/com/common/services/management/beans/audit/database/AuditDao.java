package com.common.services.management.beans.audit.database;

import com.common.services.management.beans.audit.model.Table;
import com.common.services.management.beans.audit.model.users.ActiveUser;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

/**
 * AuditDao.java
 * Date: 11 сент. 2018 г.
 * Users: vmeshkov
 * Description: DAO для аудита
 */
public interface AuditDao
{
    /**
     * Выполнить статический запрос из таблицы аудита
     * @param params параметры запроса
     * @param pageNumber номер страницы
     * @param pageSize размер страницы 
     * @return таблицу данных
     * @throws DataAccessException
     */
    public Table list(Map<String, Object> params, int pageNumber, int pageSize);

    /**
     * Получить активность пользователей за период
     * @param params параметры запроса
     * @return список активности пользователей
     * @throws DataAccessException
     */
    public List<ActiveUser> getActiveUsers(Map<String, Object> params);

    /**
     * Выполнить статический запрос запросов с ответами из таблицы аудита
     * @param params параметры запроса
     * @param pageNumber номер страницы
     * @param pageSize размер страницы 
     * @return таблицу данных
     * @throws DataAccessException
     */
    public Table requests(Map<String, Object> params, int pageNumber, int pageSize);
}
