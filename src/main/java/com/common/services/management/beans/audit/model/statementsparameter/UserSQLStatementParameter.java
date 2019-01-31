package com.common.services.management.beans.audit.model.statementsparameter;

import com.common.services.management.beans.management.service.UserManagementHelper;

/**
 * UserSQLStatementParameter.java
 * Date: 17 янв. 2019 г.
 * Users: vmeshkov
 * Description: Параметр для случая логина пользователя
 */
public class UserSQLStatementParameter
    extends SQLStatementParameter
{

    public UserSQLStatementParameter(String param, String paramSign)
    {
        super(param, paramSign);
    }

    @Override
    public Object getObject(String value)
    {
        return UserManagementHelper.prepareUserName(value);
    }
}
