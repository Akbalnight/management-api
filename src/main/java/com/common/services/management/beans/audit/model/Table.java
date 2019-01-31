package com.common.services.management.beans.audit.model;

import java.util.List;

/**
 * Table.java
 * Date: 11 сент. 2018 г.
 * Users: vmeshkov
 * Description: Данные с сервиса в виде таблице.
 */
public class Table
{
    /**
     *  Названия столбцов
     */
    private String[] headers;
    /**
     * Строки
     */
    private List<String[]> rows;
    /**
     * Общее количество строк
     */
    private int countRows;
    /**
     * Определить список названий столбцов
     * @param заголовки
     */
    public Table setHeaders(String[] headers)
    {
        this.headers = headers;
        return this;
    }
    
    public Table setRows(List<String []> rows)
    {
        this.rows = rows;
        return this;
    }

    public String[] getHeaders()
    {
        return headers;
    }

    public List<String []> getRows()
    {
        return rows;
    }

    public int getCountRows()
    {
        return countRows;
    }

    public Table setCountRows(int countRows)
    {
        this.countRows = countRows;
        return this;
    }
}
