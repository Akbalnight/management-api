package com.common.services.management.beans.data.database;

import java.util.Map;

/**
 * dataDao.java
 * Date: 14 янв. 2019 г.
 * Users: vmeshkov
 * Description: DAO для данных (текстов уведомлений ...)
 */
public interface DataDao
{
    /**
     * Получить данные
     * @param id идентификатор: 0 - досчеты, 1 - исключения, 2 - акты ...
     * @param data тексты - уведомление, заголовок письма, текст письма, свойства
     */
    Map<Integer, String []> getNotifications();

    /**
     * Изменить данные 
     * @param id идентификатор: 0 - досчеты, 1 - исключения, 2 - акты ...
     * @param data тексты - уведомление, заголовок письма, текст письма, свойства
     */
    void putNotification(int id, String [] data);
}
