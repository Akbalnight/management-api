package com.common.services.management.beans.serv.settings.database;

/**
 * UserSettingsDao.java
 * Date: 15 янв. 2019 г.
 * Users: vmeshkov
 * Description: Интерфейс для работы с настройками пользователя
 */
public interface UserSettingsDao
{

    /**
     * Получить размер страницы
     * @param user_id идентификатор пользователя
     * @return размер страницы 
     */
    Integer getPageSize(int user_id);
}
