package com.common.services.management.libs;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;

/**
 * ValidateDao.java
 * Date: 23 янв. 2019 г.
 * Users: vmeshkov
 * Description: Абстрактный класс для DAO классов с валидацией базы данных
 */
public abstract class ValidateDao
{
    /**

     * ValidateDao.java
     * Date: 23 янв. 2019 г.
     * Users: vmeshkov
     * Description: Интерфейс, который описывает SQL ресурс
     */
    public static interface IResource
    {
        public String getSQL() throws IOException;
    }
    
    public static class SQLText implements IResource
    {
        /**
         * Текст запроса
         */
        private String sqlText;

        public SQLText(String sqlText)
        {
            this.sqlText = sqlText;
        }

        @Override
        public String getSQL()
        {
            return sqlText;
        }
    }
    
    public static class SQLScript implements IResource
    {
        /**
         * Файл со скриптом
         */
        private String sqlScript;

        public SQLScript(String sqlScript)
        {
            this.sqlScript = sqlScript;
        }

        @Override
        public String getSQL() throws IOException
        {
            InputStreamReader streamReader = new InputStreamReader(getClass().getResourceAsStream(sqlScript),
                StandardCharsets.UTF_8);
            LineNumberReader reader = new LineNumberReader(streamReader);
            return ScriptUtils.readScript(reader, "--", ";");
        }
        
    }
    
    public static class ValidateTable
    {
        /**
         * Ресурс, проверяющий целостность таблицы
         */
        private IResource validateResource;
        /**
         * Количество строк в таблице
         */
        private int validateRows;
        /**
         * Ресурс для создания таблицы
         */
        private IResource createResource;
        /**
         * Ресурс для заполнения таблицы
         */
        private IResource fillResource;

        public ValidateTable(IResource validateResource, int validateRows,
            IResource createResource, IResource fillResource)
        {
            this.validateResource = validateResource;
            this.validateRows = validateRows;
            this.createResource = createResource;
            this.fillResource = fillResource;
        }

        public IResource getValidateResource()
        {
            return validateResource;
        }

        public void setValidateResource(IResource validateResource)
        {
            this.validateResource = validateResource;
        }

        public int getValidateRows()
        {
            return validateRows;
        }

        public void setValidateRows(int validateRows)
        {
            this.validateRows = validateRows;
        }

        public IResource getCreateResource()
        {
            return createResource;
        }

        public void setCreateResource(IResource createResource)
        {
            this.createResource = createResource;
        }

        public IResource getFillResource()
        {
            return fillResource;
        }

        public void setFillResource(IResource fillResource)
        {
            this.fillResource = fillResource;
        }
    }
    
    /**
     * Темплейт для выполнения запросов
     * @return Темплейт для выполнения запросов
     */
    abstract protected NamedParameterJdbcTemplate getJDBCTemplate();

    /**
     * Набор для проверки
     */
    private ValidateTable[] validateElements;

    public void setValidateElements(ValidateTable[] validateElements) throws IOException
    {
        this.validateElements = validateElements;
        validate();
    }

    /**
     * Проверка целостности базы
     * @throws IOException
     */
    protected void validate() throws IOException
    {
        StringBuilder executeQuery = new StringBuilder();
        NamedParameterJdbcTemplate jdbcTemplate = getJDBCTemplate();
        if (validateElements != null)
        {
            for (ValidateTable v : validateElements)
            {
                try
                {
                    jdbcTemplate.getJdbcOperations().execute(v.getValidateResource().getSQL());
                }
                catch (DataAccessException e)
                {
                    String sql = v.getCreateResource().getSQL();
                    executeQuery.append(sql + (sql.endsWith(";") ? "" : ";"));
                }
            }
        }
        
        if (executeQuery.length() > 0)
        {
            jdbcTemplate.getJdbcOperations().execute(executeQuery.toString());
        }
    }
}
