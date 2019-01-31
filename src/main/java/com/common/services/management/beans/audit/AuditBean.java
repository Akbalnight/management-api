package com.common.services.management.beans.audit;

import com.common.services.management.beans.audit.database.AuditDao;
import com.common.services.management.beans.audit.database.AuditDaoImpl;
import com.common.services.management.beans.audit.model.Table;
import com.common.services.management.beans.audit.model.statementsparameter.SQLStatementParameter;
import com.common.services.management.beans.audit.model.users.ActiveUser;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.beans.serv.resourcemanager.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AuditBean.java
 * Date: 31 авг. 2018 г.
 * Users: vmeshkov
 * Description: Бин для аудита
 */
@Component
@EnableAutoConfiguration
@ImportResource("classpath*:applicationContext.xml")
public class AuditBean
{
    @Autowired
    private AuditDao auditDao;

    @Autowired
    private ServiceException serviceException;
    
    @Value("${audit.list.page.size:50}")
    protected int pageSize;
    
    /**
     * Выполнение запроса по данным из таблицы аудита с фильтрами
     * @param request запрос с frontend'а
     * @param pageNumber номер страницы
     * @param pageSize размер страницы
     * @return json c результатом
     */
    public Table list(HttpServletRequest request, Integer pageNumber, Integer pageSize)
    {
        return auditDao.list(getParameters(request),
            pageNumber == null ? 0 : pageNumber,
            pageSize == null ? this.pageSize : pageSize);
    }

    /**
     * Определим список параметров из листа параметров запроса 
     * @param request
     * @return
     */
    private Map<String, Object> getParameters(HttpServletRequest request)
    {
        // Определим список параметров по умолчанию, если параметров не было передано, то выведется
        // вся таблица audit
        Map<String, Object> params = AuditDaoImpl.getSelectParameters();
        // Переопределим параметры из запроса клиента
        request.getParameterMap().entrySet().forEach((p) -> {
            String key = p.getKey();
            String[] value= p.getValue();
            if (value != null && value.length > 0)
            {
                SQLStatementParameter selectParam = AuditDaoImpl.selectParameters.get(key);
                if (selectParam != null)
                {
                    try
                    {
                        params.put(key, selectParam.getObject(value[0]));
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw serviceException.applyParameters(HttpStatus.BAD_REQUEST,
                            ResourceManager.INVALID_PARAMETER_VALUE, value[0], key);
                    }
                    
                    // Исправим признак, чтобы учитывался параметр от клиента 
                    params.put(selectParam.getParamSign(), 0);
                }
            }
        });
        return params;
    }

    /**
     * Выполнение запроса по активным пользователям за данный период за каждый час
     * в указаный период. , если параметров не было передано, то возьмем за текущие сутки
     * @param request запрос с frontend'а
     * @return Список активных пользователей
     */
    public List<ActiveUser> activeUsers(HttpServletRequest request)
    {
        // Определим список параметров по умолчанию, если параметров не было передано, то возьмем
        // за текущие сутки
        LocalDate localDate = LocalDate.now();
        final Map<String, Object> params = new HashMap<String, Object>();

        // Переопределим параметры из запроса клиента
        request.getParameterMap().entrySet().forEach((p) -> {
            String key = p.getKey();
            String[] value= p.getValue();
            if (value != null && value.length > 0 && 
                Arrays.asList(AuditDaoImpl.TIME_FROM, AuditDaoImpl.TIME_TO).contains(key))
            {
                try
                {
                    params.put(key, Timestamp.valueOf(value[0]));
                }
                catch (IllegalArgumentException e)
                {
                    throw serviceException.applyParameters(HttpStatus.BAD_REQUEST,
                        ResourceManager.INVALID_PARAMETER_VALUE, value[0], key);
                }
            }
        });
        
        // Заполним параметры по умолчанию, те, которых нет в запросе
        if (params.size() == 0)
        {
            // В запросе нет параметров по датам.
            // Стартовая дата, это будет 0 часов текущего дня,
            // финальная дата - 0 часов следующего дня.
            // То есть будет выводится информация за текущет сутки
            params.putAll(Collections.unmodifiableMap(
                Stream.of(
                  new SimpleEntry<>(AuditDaoImpl.TIME_FROM, Timestamp.valueOf(localDate.atStartOfDay())),
                  new SimpleEntry<>(AuditDaoImpl.TIME_TO, Timestamp.valueOf(localDate.plusDays(1).atStartOfDay()))
                ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))));
        }
        else if (params.size() == 1)
        {
            Timestamp timeFrom = (Timestamp)params.get(AuditDaoImpl.TIME_FROM);
            if (timeFrom == null)
            {
                // Отсутствует стартовая дата, вставим минимальную дату (1 янв. 1970 года).
                params.put(AuditDaoImpl.TIME_FROM, Timestamp.valueOf("1970-01-01 00:00:00"));
            }
            else
            {
                // Отсутствует финальная дата, вставим текущее дату-время
                params.put(AuditDaoImpl.TIME_TO, new Timestamp(System.currentTimeMillis()));
            }
        }

        return auditDao.getActiveUsers(params);
    }

    /**
     * Выполнение запроса запросов с ответами с фильтрами
     * @param request запрос с frontend'а
     * @param pageNumber номер страницы
     * @param pageSize размер страницы  
     * @return json c результатом
     */
    public Table requests(HttpServletRequest request, Integer pageNumber, Integer pageSize)
    {
        return auditDao.requests(getParameters(request),
            pageNumber == null ? 0 : pageNumber,
            pageSize == null ? this.pageSize : pageSize);
    }
}
