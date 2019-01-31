package com.common.services.management.beans.serv.settings;

import com.common.services.management.beans.serv.settings.database.UserSettingsDao;
import com.common.services.management.details.Details;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * UserSettingsBean.java
 * Date: 15 янв. 2019 г.
 * Users: vmeshkov
 * Description: Бин для работы с настройками пользователя
 */
@Component
public class UserSettingsBean
{
    @Autowired
    private UserSettingsDao userSettingsDao;

    /**
     * Получить размер страницы
     * @param user_id идентификатор пользователя
     * @return размер страницы
     */
    public Integer getPageSize()
    {
        return userSettingsDao.getPageSize(getUserId());
    }

    /**
     * Оределяет идентификатор пользователя
     * @return идентификатор пользователя
     */
    private int getUserId()
    {
        return Optional.ofNullable(Details.getDetails().getUserIntId()).orElse(-1);
    }
}
