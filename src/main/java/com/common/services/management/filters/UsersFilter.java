package com.common.services.management.filters;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * @author asmatveev
 */
@ApiModel(description = "Фильтр пользователей")
public class UsersFilter
{
    @ApiModelProperty("Список id пользователей")
    private List<Integer> userIds;

    @ApiModelProperty(value = "Флаг включения/исключения пользователей из списка userIds", notes = "Если флаг true " +
            "или не указан, то будут найдены только пользователи из списка userIds. Если флаг false, то будут найдены" +
            " все пользователи, не входящие в список userIds")
    private Boolean isIncludedUsers;

    @ApiModelProperty(value = "Фильтр по полям из jsonData", notes = "Ключ - название поля в jsonData. Значение - " +
            "список значений указанного поля по которым будут фильтроваться пользователи")
    private Map<String, List<Object>> jsonDataFilters;

    public List<Integer> getUserIds()
    {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds)
    {
        this.userIds = userIds;
    }

    public Boolean getIsIncludedUsers()
    {
        return isIncludedUsers;
    }

    public void setIsIncludedUsers(Boolean includedUsers)
    {
        isIncludedUsers = includedUsers;
    }

    public Map<String, List<Object>> getJsonDataFilters()
    {
        return jsonDataFilters;
    }

    public void setJsonDataFilters(Map<String, List<Object>> jsonDataFilters)
    {
        this.jsonDataFilters = jsonDataFilters;
    }

    public boolean isEmpty()
    {
        return CollectionUtils.isEmpty(userIds) && CollectionUtils.isEmpty(jsonDataFilters);
    }
}
