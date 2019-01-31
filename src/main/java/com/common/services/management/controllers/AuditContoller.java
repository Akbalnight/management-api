package com.common.services.management.controllers;

import com.common.services.management.beans.audit.AuditBean;
import com.common.services.management.beans.audit.model.Table;
import com.common.services.management.beans.audit.model.users.ActiveUser;
import com.common.services.management.beans.serv.settings.UserSettingsBean;
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
import java.util.Optional;

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
    
    @Autowired
    private UserSettingsBean settings;

    /**
     * Выполнение запроса по данным из таблицы аудита с фильтрами
     * @param request запрос с frontend'а
     * @return json c результатом
     */
    @GetMapping("/list")
    @ApiOperation(value = "Получение записей из таблицы аудита по фильтру")
    public Table list(HttpServletRequest request,
        @ApiParam(value ="Начало периода") @RequestParam(value = "timeFrom", required=false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime timeFrom,
        @ApiParam(value="Окончание периода") @RequestParam(value = "timeTo", required=false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime timeTo,
        @ApiParam(value="Фильтр для пользователя") @RequestParam(value = "user", required=false) String user,
        @ApiParam(value="Фильтр для идентификатора сессии") @RequestParam(value = "sessionid", required=false) String sessionId,
        @ApiParam(value="Признак запрос/ответ (Q - запрос, R - ответ)") @RequestParam(value = "rq", required=false) String rq,
        @ApiParam(value="Выводить только ошибочные ответы, не требует значения") @RequestParam(value = "errorstatus", required=false) String errorstatus,
        @ApiParam(value="Размер страницы") @RequestParam(value = "pageSize", required=false) Integer pageSize,
        @ApiParam(value="Номер страницы") @RequestParam(value = "pageNumber", required=false) Integer pageNumber)
    {
        pageSize = Optional.ofNullable(pageSize).orElse(settings.getPageSize());
        return audit.list(request, pageNumber, pageSize);
    }
    
    /**
     * Выполнение запроса по запросам с ответами с фильтрами
     * @param request запрос с frontend'а
     * @return json c результатом
     */
    @GetMapping("/requests")
    @ApiOperation(value = "Получение запросов с ответами таблицы аудита по фильтру")
    public Table requests(HttpServletRequest request,
        @ApiParam(value ="Начало периода") @RequestParam(value = "timeFrom", required=false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime timeFrom,
        @ApiParam(value="Окончание периода") @RequestParam(value = "timeTo", required=false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime timeTo,
        @ApiParam(value="Фильтр для id пользователя") @RequestParam(value = "userid", required=false) String userid,
        @ApiParam(value="Фильтр для метода запроса") @RequestParam(value = "method", required=false) String method,
        @ApiParam(value="Фильтр для пути в запросе") @RequestParam(value = "path", required=false) String path,
        @ApiParam(value="Фильтр для статуса ответа") @RequestParam(value = "status", required=false) String status,
        @ApiParam(value="Размер страницы") @RequestParam(value = "pageSize", required=false) Integer pageSize,
        @ApiParam(value="Номер страницы") @RequestParam(value = "pageNumber", required=false) Integer pageNumber)
    {
        pageSize = Optional.ofNullable(pageSize).orElse(settings.getPageSize());
        return audit.requests(request, pageNumber, pageSize);
    }
    
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
