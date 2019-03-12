package com.common.services.management.beans.management.model;

import java.util.List;

/**
 * LdapGroupsResult.java
 * Date: 12 марта. 2019 г.
 * Users: amatveev
 * Description: Список LDAP групп и их ролей. Общее количество LDAP групп
 */
public class LdapGroupsResult
{
    private List<LdapGroup> groups;
    private int count;

    public List<LdapGroup> getGroups()
    {
        return groups;
    }

    public void setGroups(List<LdapGroup> groups)
    {
        this.groups = groups;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
}
