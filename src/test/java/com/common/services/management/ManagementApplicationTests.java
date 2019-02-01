package com.common.services.management;

import com.common.services.management.datasource.DataSourceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = ManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ManagementApplicationTests
{
    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private DataSourceManager dataManagement;
    
    @Before
    public void setUp() throws IOException
    {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataManagement.getDataSource("audit"));
        // Создание таблиц в БД
        executeScript(jdbcTemplate, "/db_audit_delete_tables.sql");
        executeScript(jdbcTemplate, "/db_audit_create_tables.sql");
        executeScript(jdbcTemplate, "/db_audit_fill_test_tables.sql");
    }

    /**
     * Выполнить SQL скрипт
     * @param jdbcTemplate соединение с базой
     * @throws IOException
     */
    private void executeScript(NamedParameterJdbcTemplate jdbcTemplate, String script)
        throws IOException
    {
        InputStreamReader streamReader =
            new InputStreamReader(getClass().getResourceAsStream(script),
                StandardCharsets.UTF_8);
        LineNumberReader reader = new LineNumberReader(streamReader);
        String query = toCurrentDate(ScriptUtils.readScript(reader, "--", ";"));
        jdbcTemplate.getJdbcOperations().execute(query);
    }
    
    /**
     * Подменим на текущую дату CURRDATE -> текущая дата
     * @param content текст для замены
     * @return
     */
    private String toCurrentDate(String content)
    {
        return content.replaceAll("CURRDATE", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .replaceAll("CURRHOUR", Integer.toString(LocalTime.now().getHour() - 1));
    }

    @Test
    public void auditTest() throws Exception
    {
        // Получим лист запросов
        mvc.perform(get("/audit/requests")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"timereq\",\"timeres\",\"userid\",\"sessionid\",\"method\",\"path\",\"status\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.000\",\"CURRDATE CURRHOUR:00:00.001\",\"2\",\"SESSION1\",\"GET\",\"path\",\"200\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.000\",\"CURRDATE CURRHOUR:40:00.001\",\"3\",\"SESSION2\",\"GET\",\"path\",\"404\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.000\",\"CURRDATE CURRHOUR:41:00.001\",\"3\",\"SESSION2\",\"GET\",\"path\",\"404\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.000\",\"CURRDATE CURRHOUR:50:00.001\",\"4\",\"SESSION3\",\"GET\",\"path\",\"200\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.000\",\"CURRDATE CURRHOUR:50:10.001\",\"4\",\"SESSION3\",\"GET\",\"path\",\"404\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.000\",\"CURRDATE CURRHOUR:50:20.001\",\"4\",\"SESSION3\",\"GET\",\"path\",\"500\"]],"
                + "\"countRows\":6}")));
        
        // Получим лист запросов
        // Зададим размер страницы
        mvc.perform(get("/audit/requests?pageSize=2")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"timereq\",\"timeres\",\"userid\",\"sessionid\",\"method\",\"path\",\"status\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.000\",\"CURRDATE CURRHOUR:00:00.001\",\"2\",\"SESSION1\",\"GET\",\"path\",\"200\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.000\",\"CURRDATE CURRHOUR:40:00.001\",\"3\",\"SESSION2\",\"GET\",\"path\",\"404\"]],"
                + "\"countRows\":6}")));
        
        // Получим лист запросов
        // Зададим размер и номер страницы
        mvc.perform(get("/audit/requests?pageSize=2&pageNumber=1")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"timereq\",\"timeres\",\"userid\",\"sessionid\",\"method\",\"path\",\"status\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:41:00.000\",\"CURRDATE CURRHOUR:41:00.001\",\"3\",\"SESSION2\",\"GET\",\"path\",\"404\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.000\",\"CURRDATE CURRHOUR:50:00.001\",\"4\",\"SESSION3\",\"GET\",\"path\",\"200\"]],"
                + "\"countRows\":6}")));
        
        // Получим лист запросов с фильтром от
        mvc.perform(get("/audit/requests?timeFrom=" + toCurrentDate("CURRDATE CURRHOUR:40:00.000"))
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"timereq\",\"timeres\",\"userid\",\"sessionid\",\"method\",\"path\",\"status\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:41:00.000\",\"CURRDATE CURRHOUR:41:00.001\",\"3\",\"SESSION2\",\"GET\",\"path\",\"404\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.000\",\"CURRDATE CURRHOUR:50:00.001\",\"4\",\"SESSION3\",\"GET\",\"path\",\"200\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.000\",\"CURRDATE CURRHOUR:50:10.001\",\"4\",\"SESSION3\",\"GET\",\"path\",\"404\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.000\",\"CURRDATE CURRHOUR:50:20.001\",\"4\",\"SESSION3\",\"GET\",\"path\",\"500\"]],"
                + "\"countRows\":4}")));
        
        // Получим лист запросов с фильтром до
        mvc.perform(get("/audit/requests?timeTo=" + toCurrentDate("CURRDATE CURRHOUR:49:00.000"))
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"timereq\",\"timeres\",\"userid\",\"sessionid\",\"method\",\"path\",\"status\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.000\",\"CURRDATE CURRHOUR:00:00.001\",\"2\",\"SESSION1\",\"GET\",\"path\",\"200\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.000\",\"CURRDATE CURRHOUR:40:00.001\",\"3\",\"SESSION2\",\"GET\",\"path\",\"404\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.000\",\"CURRDATE CURRHOUR:41:00.001\",\"3\",\"SESSION2\",\"GET\",\"path\",\"404\"]],"
                + "\"countRows\":3}")));
        
        // Получим список активных пользователей
        mvc.perform(get("/audit/activeusers")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("["
                + "{\"login\":\"user1\",\"id\":\"2\",\"adress\":\"0:0:0:0:0:0:0:1\",\"startTime\":\"CURRDATETCURRHOUR:00:00Z\",\"duration\":\"меньше секунды\"},"
                + "{\"login\":\"user2\",\"id\":\"3\",\"adress\":\"0:0:0:0:0:0:0:1\",\"startTime\":\"CURRDATETCURRHOUR:40:00Z\",\"duration\":\"1мин.\"},"
                + "{\"login\":\"user3\",\"id\":\"4\",\"adress\":\"0:0:0:0:0:0:0:1\",\"startTime\":\"CURRDATETCURRHOUR:50:00Z\",\"duration\":\"20сек.\"}]")));
        
        // Получим список активных пользователей для даты от
        mvc.perform(get("/audit/activeusers?timeFrom=" + toCurrentDate("CURRDATE CURRHOUR:40:00.000"))
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("["
                + "{\"login\":\"user2\",\"id\":\"3\",\"adress\":\"0:0:0:0:0:0:0:1\",\"startTime\":\"CURRDATETCURRHOUR:40:00Z\",\"duration\":\"1мин.\"},"
                + "{\"login\":\"user3\",\"id\":\"4\",\"adress\":\"0:0:0:0:0:0:0:1\",\"startTime\":\"CURRDATETCURRHOUR:50:00Z\",\"duration\":\"20сек.\"}]")));
        
        // Получим список активных пользователей для даты до
        mvc.perform(get("/audit/activeusers?timeTo=" + toCurrentDate("CURRDATE CURRHOUR:49:00.000"))
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("["
                + "{\"login\":\"user1\",\"id\":\"2\",\"adress\":\"0:0:0:0:0:0:0:1\",\"startTime\":\"CURRDATETCURRHOUR:00:00Z\",\"duration\":\"меньше секунды\"},"
                + "{\"login\":\"user2\",\"id\":\"3\",\"adress\":\"0:0:0:0:0:0:0:1\",\"startTime\":\"CURRDATETCURRHOUR:40:00Z\",\"duration\":\"1мин.\"}]")));
        
        // Получим лист запросов ответов
        mvc.perform(get("/audit/list")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.0\",\"user1\",\"SESSION1\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:00:00.001\",\"user1\",\"SESSION1\",\"R\",\"{\\\"code\\\":200,\\\"json\\\":[],\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:00:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.001\",\"user2\",\"SESSION2\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:40:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.001\",\"user2\",\"SESSION2\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:41:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":200,\\\"json\\\":[],\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:10.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":500,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:20.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"]],"
                + "\"countRows\":12}")));
        
        // Получим лист запросов ответов
        // Зададим размер страницы
        mvc.perform(get("/audit/list?pageSize=2")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.0\",\"user1\",\"SESSION1\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:00:00.001\",\"user1\",\"SESSION1\",\"R\",\"{\\\"code\\\":200,\\\"json\\\":[],\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:00:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"]],"
                + "\"countRows\":12}")));
        
        // Получим лист запросов ответов
        // Зададим размер и номер страницы
        mvc.perform(get("/audit/list?pageSize=2&pageNumber=1")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:40:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.001\",\"user2\",\"SESSION2\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:40:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"]],"
                + "\"countRows\":12}")));
        
        // Получим лист запросов ответов для пользователя
        mvc.perform(get("/audit/list?user=user3")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:50:00.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":200,\\\"json\\\":[],\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:10.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":500,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:20.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"]],"
                + "\"countRows\":6}")));
        
        // Получим лист запросов ответов от времени
        mvc.perform(get("/audit/list?timeFrom=" + toCurrentDate("CURRDATE CURRHOUR:40:00.000"))
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:40:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.001\",\"user2\",\"SESSION2\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:40:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.001\",\"user2\",\"SESSION2\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:41:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":200,\\\"json\\\":[],\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:10.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":500,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:20.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"]],"
                + "\"countRows\":10}")));
        
        // Получим лист запросов ответов до времени
        mvc.perform(get("/audit/list?timeTo=" + toCurrentDate("CURRDATE CURRHOUR:40:00.000"))
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.0\",\"user1\",\"SESSION1\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:00:00.001\",\"user1\",\"SESSION1\",\"R\",\"{\\\"code\\\":200,\\\"json\\\":[],\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:00:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"]],"
                + "\"countRows\":3}")));
        
        // Получим лист запросов ответов за текущие сутки для сессии
        mvc.perform(get("/audit/list?sessionid=SESSION1")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.0\",\"user1\",\"SESSION1\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:00:00.001\",\"user1\",\"SESSION1\",\"R\",\"{\\\"code\\\":200,\\\"json\\\":[],\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:00:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"]],"
                + "\"countRows\":2}")));
        
        // Получим лист запросов за текущие сутки
        mvc.perform(get("/audit/list?rq=Q")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:00:00.0\",\"user1\",\"SESSION1\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:40:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.0\",\"user2\",\"SESSION2\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:00.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.0\",\"user3\",\"SESSION3\",\"Q\",\"{\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\",\\\"params\\\":{},\\\"json\\\":null}\"]],"
                + "\"countRows\":6}")));
        
        // Получим лист ответов с ошибками
        mvc.perform(get("/audit/list?errorstatus")
            .header("userid", "0")
            .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(toCurrentDate("{\"headers\":[\"time\",\"user\",\"sessionid\",\"rq\",\"data\"],"
                + "\"rows\":["
                +    "[\"CURRDATE CURRHOUR:40:00.001\",\"user2\",\"SESSION2\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:40:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:41:00.001\",\"user2\",\"SESSION2\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:41:00.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:10.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":404,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:10.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"],"
                +    "[\"CURRDATE CURRHOUR:50:20.001\",\"user3\",\"SESSION3\",\"R\",\"{\\\"code\\\":500,\\\"json\\\":{},\\\"requestJson\\\":{\\\"time\\\":\\\"CURRDATE CURRHOUR:50:20.000\\\",\\\"adress\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"path\\\"}}\"]],"
                + "\"countRows\":4}")));
    }
}
