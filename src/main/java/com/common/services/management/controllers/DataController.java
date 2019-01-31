package com.common.services.management.controllers;

import com.common.services.management.beans.data.DataBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DataController.java
 * Date: 14 янв. 2019 г.
 * Users: vmeshkov
 * Description: Контроллер для работы с прикладными данными
 */
@RestController
@Api(value = "Api for data", description = "Контроллер для работы с прикладными данными (тексты уведомлений...).")
@RequestMapping("/data")
public class DataController
{
    @Autowired
    private DataBean bean;

    /**
     * Получение списка текстов для уведомлений, заголовков и текстов писем
     * @return Список текстов для уведомлений, заголовков и текстов писем
     */
    @GetMapping(value = "/notifications")
    @ApiOperation(value = "Получение связанного списка текстов для уведомлений.",
        notes = "Key: 0 - досчеты, 1 - исключения, 2 - акты ..."
            + "value: String[], это массив строк, состоит из 4 строк: "
            + "1 - текст уведомления, 2 - заголовок письма, 3 - текст письма, 4 - свойства в виде json.")
    public Map<Integer, String[]> getNotifications()
    {
        return bean.getNotifications();
    }
    
    /**
     * Получение списка текстов для уведомлений, заголовков и текстов писем
     * @return Список текстов для уведомлений, заголовков и текстов писем
     */
    @PutMapping(value = "/notifications/{id}")
    @ApiOperation(value = "Вставка, изменение текстов для уведомлений, заголовков и текстов писем",
        notes = "Идентификатор: 0 - досчеты, 1 - исключения, 2 - акты ...")
    public void putNotifications(@ApiParam(value = "Id данных (0 - досчеты, 1 - исключения, 2 - акты)", required = true)
        @PathVariable("id") int id,
        @ApiParam(value = "Тексты для уведомлений, массив строк, состоит из 4 строк:"
            + " 1 - текст уведомления, 2 - заголовок письма, 3 - текст письма, 4 - свойства в виде json", required = true) @RequestBody String[] data)
    {
        bean.putNotification(id, data);
    }
}
