package com.common.services.management.beans.audit.model.statementsparameter;

/**
 * SQLStatementParameters.java
 * Date: 13 сент. 2018 г.
 * Users: vmeshkov
 * Description: Класс, описывающий параметр для select запросов
 */
public class SQLStatementParameter
{
    /**
     * Название параметра
     */
    private String param;
    /**
     * Название признака, использовать ли параметр в select запросе
     */
    private String paramSign;
    /**
     * Значение параметра по умолчанию
     */
    protected String defaultValue = "";
    
    public SQLStatementParameter(String param, String paramSign)
    {
        super();
        this.param = param;
        this.paramSign = paramSign;
    }

    public String getParam()
    {
        return param;
    }

    public String getParamSign()
    {
        return paramSign;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }
    
    public Object getObject(String value)
    {
        return value;
    }
}
