package com.common.services.management.controllers;

import com.common.services.management.beans.audit.AuditBean;
import com.common.services.management.beans.audit.model.users.ActiveUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditBean.java
 * Date: 31 авг. 2018 г.
 * Users: vmeshkov
 * Description: Контроллер для выполнения запросов по аудиту 
 */
@RestController
@Api(value = "Api for audit", description = "Контроллер для получения данных по аудиту.")
@RequestMapping("/audit")
public class AuditContoller
{
    @Autowired
    private AuditBean audit;
    
    /**
     * Выполнение запроса по активным пользователям за данный период
     * @param request запрос с frontend'а
     * @return json c результатом
     */
    @GetMapping("/activeusers")
    @ApiOperation(value = "Получение списка активных пользователе за заданный  промежуток времени")
    public List<ActiveUser> activeUsers(HttpServletRequest request,
        @ApiParam(value ="Начало периода") @RequestParam(value = "timeFrom", required=false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime timeFrom,
        @ApiParam(value="Окончание периода") @RequestParam(value = "timeTo", required=false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime timeTo)
    {
        return audit.activeUsers(request);
    }
}
