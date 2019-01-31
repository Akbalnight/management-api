package com.common.services.management.beans.audit.model.statementsparameter;

import java.sql.Timestamp;

/**
 * TimestampSQLStatementParameter.java
 * Date: 13 сент. 2018 г.
 * Users: vmeshkov
 * Description: Параметр для случая timestamp
 */
public class TimestampSQLStatementParameter
    extends SQLStatementParameter
{
    public TimestampSQLStatementParameter(String param, String paramSign)
    {
        super(param, paramSign);
        defaultValue = "1970-01-01 00:00:00";
    }

    @Override
    public Object getObject(String value)
    {
        return Timestamp.valueOf(value);
    }
}
