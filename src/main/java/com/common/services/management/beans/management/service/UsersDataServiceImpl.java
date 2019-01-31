package com.common.services.management.beans.management.service;

import com.common.services.management.beans.management.database.UsersDataDao;
import com.common.services.management.beans.management.database.UsersManagementDao;
import com.common.services.management.beans.management.model.LdapGroupGenerationObject;
import com.common.services.management.beans.management.model.UserGenerationObject;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.beans.serv.resourcemanager.ResourceManager;
import com.common.services.management.details.Details;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.common.services.management.beans.management.service.UserManagementHelper.prepareRoleName;

/**
 * UsersDataServiceImpl.java
 * Date: 11 окт. 2018 г.
 * Users: vmeshkov
 * Description: Сервис для управления данными(обьектами) пользователя
 */
@Service
public class UsersDataServiceImpl
        implements UsersDataService
{
    @Autowired
    private UsersDataDao usersDataDao;

    @Autowired
    private UsersManagementDao usersManagementDao;
    
    @Autowired
    private ServiceException serviceException;

    @Override
    public void addUserObjects(Integer userId, List<Integer> objects)
    {
        // Проверим, что такой пользователь есть
        usersManagementDao.getUser(userId);
        usersDataDao.addUserObjects(userId, objects);
    }

    @Override
    public void setUserObjects(Integer userId, List<Integer> objects)
    {
        // Проверим, что такой пользователь есть
        usersManagementDao.getUser(userId);
        usersDataDao.setUserObjects(userId, objects);
    }

    @Override
    public List<UserGenerationObject> getUserObjects(Integer userId)
    {
        return usersDataDao.getUserObjects(userId);
    }

    @Override
    public void setRoleObjects(String role, Map<String, String> objects)
    {
        role = prepareRoleName(role);
        usersManagementDao.getRole(role);
        usersDataDao.setRoleObjects(role, objects);
    }

    @Override
    public void removeRoleObjects(String role)
    {
        role = prepareRoleName(role);
        usersDataDao.removeRoleObjects(role);
    }

    @Override
    public void removeUserObjects(Integer userId, List<Integer> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            usersDataDao.clearUserObjects(userId);
        }
        else
        {
            usersDataDao.removeUserObjects(userId, objects);
        }
    }

    @Override
    public void updateUserObjectByRowId(Integer rowId, Integer userId, Integer object)
    {
        // Проверим, что такой пользователь есть
        usersManagementDao.getUser(userId);
        usersDataDao.updateUserObjectByRowId(rowId, userId, object);
    }

    @Override
    public UserGenerationObject getUserObjectByRowId(Integer rowId)
    {
        return usersDataDao.getUserObjectByRowId(rowId);
    }

    /**
     * Оределяет идентификатор пользователя
     * @return идентификатор пользователя
     */
    private int getUserId()
    {
        Integer userId = Details.getDetails().getUserIntId();
        if (userId == null)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ResourceManager.ERROR_USER_ID_EMPTY);
        }
        
        return userId;
    }
    
    @Override
    public JsonNode getUserSettings()
    {
        return usersDataDao.getUserSettings(getUserId());
    }

    @Override
    public void setUserSettings(JsonNode settings)
    {
        usersDataDao.setUserSettings(getUserId(), settings);
    }

    @Override
    public List<LdapGroupGenerationObject> getLdapGroupGenerationObjects()
    {
        return usersDataDao.getLdapGroupGenerationObjects();
    }

    @Override
    public String getLdapGroupByGenerationObjectId(int generationObjectId)
    {
        return usersDataDao.getLdapGroupByGenerationObjectId(generationObjectId);
    }

    @Override
    public int getGenerationObjectIdByLdapGroup(String group)
    {
        return usersDataDao.getGenerationObjectIdByLdapGroup(group);
    }

    @Override
    public void setGenerationObjectIdToLdapGroup(String ldapGroup, int generationObjectId)
    {
        usersDataDao.setGenerationObjectIdToLdapGroup(ldapGroup, generationObjectId);
    }

    @Override
    public void removeGenerationObjectFromLdapGroup(String ldapGroup, int generationObjectId)
    {
        usersDataDao.removeGenerationObjectFromLdapGroup(ldapGroup, generationObjectId);
    }
}
