package com.common.services.management.beans.data;

import com.common.services.management.beans.data.database.DataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * DataBean.java
 * Date: 14 янв. 2019 г.
 * Users: vmeshkov
 * Description: Бин для работы с данными (тексты уведомлений...)
 */
@Component
public class DataBean
{
    @Autowired
    private DataDao dataDao;

    /**
     * Получение списка текстов для уведомлений, заголовков и текстов писем
     * @return Список текстов для уведомлений, заголовков и текстов писем
     */
    public Map<Integer, String[]> getNotifications()
    {
        return dataDao.getNotifications();
    }

    /**
     * Изменить данные
     * @param id идентификатор: 0 - досчеты, 1 - исключения, 2 - акты ...
     * @param data тексты - уведомление, заголовок письма, текст письма
     */
    public void putNotification(int id, String[] data)
    {
        dataDao.putNotification(id, data);
    }

}
