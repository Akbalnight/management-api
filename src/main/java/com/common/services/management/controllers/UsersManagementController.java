package com.common.services.management.controllers;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.services.management.beans.management.model.ChangePassword;
import com.common.services.management.beans.management.model.LdapPermissions;
import com.common.services.management.beans.management.model.Permission;
import com.common.services.management.beans.management.model.PermissionIdList;
import com.common.services.management.beans.management.model.Role;
import com.common.services.management.beans.management.model.RoleNameList;
import com.common.services.management.beans.management.model.User;
import com.common.services.management.beans.management.model.UserGenerationObject;
import com.common.services.management.beans.management.service.LdapService;
import com.common.services.management.beans.management.service.UsersDataService;
import com.common.services.management.beans.management.service.UsersManagementService;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.beans.serv.resourcemanager.ResourceManager;
import com.common.services.management.details.Details;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * UsersManagementController.java
 * Date: 14 сент. 2018 г.
 * Users: amatveev
 * Description: Контроллер для управления пользователями, ролями и пермиссиями
 */
@RestController
@RequestMapping(path = "")
@Api(value = "Api for UsersManagementController", description =
        "Контроллер для управления пользователями, данными пользователя, ролями и пермиссиями.")
public class UsersManagementController
{
    /**
     * Реализация сервиса
     */
    @Autowired
    private UsersManagementService service;

    @Autowired
    private UsersDataService serviceData;

    @Autowired
    private ServiceException serviceException;

    @Autowired
    private LdapService ldapService;

    /**
     * Возвращает данные пользователя с указанным id
     * @param id id пользователя
     * @return возвращает данные пользователя с указанным id
     */
    @GetMapping(value = "/users/{id}")
    @ApiOperation(value = "Получение данных пользователя", notes = "Возвращает данные пользователя со списком его ролей")
    public User getUser(@ApiParam(value = "Id пользователя", required = true) @PathVariable("id") int id)
    {
        User user = service.getUser(id);
        user.setObjects(serviceData.getUserObjects(id).stream().map(UserGenerationObject::getGenerationObjectId).collect(toList()));
        return user;
    }

    /**
     * Возвращает список пользователей с их данными
     * @param withRoles если флаг true данные пользователей будут содержать список их ролей
     * @return Возвращает список пользователей с их данными
     */
    @ApiOperation(value = "Получение списка пользователей с их данными")
    @GetMapping(value = "/users")
    public List<User> getAllUsersWithRoles(@ApiParam(value =
            "Если флаг true данные пользователей будут содержать список их ролей", required = false) @RequestParam(value = "with_roles", defaultValue = "false") boolean withRoles)
    {
        return service.getAllUsers(withRoles);
    }

    /**
     * Добавляет пользователя
     * Пользователя с указанным логином не должно быть в БД
     * @param user данные пользователя
     * @return возваращает id добавленного пользователя
     */
    @PostMapping(value = "/users")
    @ApiOperation(value = "Добавление пользователя", notes = "Возвращает id добавленного пользователя")
    public int addUser(@ApiParam(value = "Данные пользователя", required = true) @RequestBody User user)
    {
        return service.addUser(user);
    }

    /**
     * Обновляет данные пользователя
     * Пользователь с указанным id должен быть записан в БД
     * @param id   id пользователя
     * @param user данные пользователя
     */
    @PutMapping(value = "/users/{id}")
    @ApiOperation(value = "Обновление данных пользователя")
    public void updateUser(@ApiParam(value = "Id пользователя", required = true) @PathVariable("id") int id,
                           @ApiParam(value = "Данные пользователя", required = true) @RequestBody User user)
    {
        service.updateUser(id, user);
    }

    /**
     * Удаляет пользователя
     * @param id id пользователя
     */
    @DeleteMapping(value = "/users/{id}")
    @ApiOperation(value = "Удаление пользователя")
    public void removeUser(@ApiParam(value = "Id пользователя", required = true) @PathVariable("id") int id)
    {
        service.removeUser(id);
    }

    /**
     * Возвращает информацию об указанной роли
     * @param role название роли
     * @return возвращает информацию об указанной роли
     */
    @GetMapping(value = "/roles/{role}")
    @ApiOperation(value = "Получение информации о роли")
    public Role getRole(@ApiParam(value = "Название роли", required = true) @PathVariable("role") String role)
    {
        return service.getRole(role);
    }

    /**
     * Возвращает список всех ролей
     * @param withPermissions если флаг true роли будут содержать список назначенных им пермиссий
     * @return возвращает список всех ролей
     */
    @GetMapping(value = "/roles")
    @ApiOperation(value = "Получение списка ролей")
    public List<Role> getAllRolesWithPermissions(@ApiParam(value = "Если флаг true роли будут содержать список " +
            "назначенных им пермиссий", required = false) @RequestParam(value = "with_permissions", defaultValue =
            "false") boolean withPermissions)
    {
        return service.getAllRoles(withPermissions);
    }

    /**
     * Добавляет роль
     * Роль с указанным названием не должна существовать в БД
     * @param role данные роли
     */
    @PostMapping(value = "/roles")
    @ApiOperation(value = "Добавление роли")
    public void addRole(@ApiParam(value = "Данные роли", required = true) @RequestBody Role role)
    {
        service.addRole(role);
    }

    /**
     * Обновляет данные роли
     * Роль с указанным названием должна существовать в БД
     * @param roleName название роли
     * @param role     данные роли для обновления
     */
    @PutMapping(value = "/roles/{role}")
    @ApiOperation(value = "Обновление данных роли")
    public void updateRole(@ApiParam(value = "Название роли", required = true) @PathVariable("role") String roleName,
                           @ApiParam(value = "Данные роли для обновления") @RequestBody Role role)
    {
        service.updateRole(roleName, role);
    }

    /**
     * Удаляет роль
     * @param role название роли
     */
    @DeleteMapping(value = "/roles/{role}")
    @ApiOperation(value = "Удаление роли")
    public void removeRole(@ApiParam(value = "Название роли", required = true) @PathVariable("role") String role)
    {
        service.removeRole(role);
    }

    /**
     * Возвращает данные пермиссии
     * @param idPermission id пермиссии
     * @return возвращает данные пермиссии
     */
    @ApiOperation(value = "Получение данных пермиссии")
    @GetMapping(value = "/permissions/{id}")
    public Permission getPermission(@ApiParam(value = "Id пермиссии", required = true) @PathVariable("id") int idPermission)
    {
        return service.getPermission(idPermission);
    }

    /**
     * Возвращает список пермиссий
     * @return возвращает список пермиссий
     */
    @GetMapping(value = "/permissions")
    @ApiOperation(value = "Получение списка пермиссий")
    public List<Permission> getAllPermissions()
    {
        return service.getAllPermissions();
    }

    /**
     * Добавляет пермиссию
     * Пермиссия с укзанными path и method не должна существовать в БД
     * @param permission данные премиссии
     * @return id добавленной пермиссии
     */
    @PostMapping(value = "/permissions")
    @ApiOperation(value = "Добавление пермиссии", notes = "Возвращает id добавленной пермиссии")
    public int addPermission(@ApiParam(value = "Данные пермиссии", required = true) @RequestBody Permission permission)
    {
        return service.addPermission(permission);
    }

    /**
     * Обновляет данные премиссии
     * Пермиссия с указанным id должна существовать в БД
     * @param idPermission id пермиссии
     * @param permission   данные пермиссии для обновления
     */
    @PutMapping(value = "/permissions/{id}")
    @ApiOperation(value = "Обновление данных пермиссии")
    public void updatePermission(@ApiParam(value = "Id пермиссии", required = true) @PathVariable("id") int idPermission,
                                 @ApiParam(value = "Данные пермиссии", required = true) @RequestBody Permission permission)
    {
        service.updatePermission(idPermission, permission);
    }

    /**
     * Удаляет пермиссию
     * @param idPermission id пермиссии
     */
    @DeleteMapping(value = "/permissions/{id}")
    @ApiOperation(value = "Удаление пермиссии")
    public void removePermission(@ApiParam(value = "Id пермиссии", required = true) @PathVariable("id") int idPermission)
    {
        service.removePermission(idPermission);
    }

    /**
     * Возвращает список ролей указанного пользователя
     * @param userId id пользователя
     * @return возвращает список ролей пользователя
     */
    @GetMapping(value = "/roles", params = "userid")
    @ApiOperation(value = "Получение списка ролей пользователя")
    public List<Role> getUserRoles(@ApiParam(value = "Id пользователя", required = true) @RequestParam("userid") int userId)
    {
        return service.getUserRoles(userId);
    }

    /**
     * Добавляет указанные роли пользователю
     * @param userid id пользователя
     * @param roles    список названий ролей для добавления
     */
    @PostMapping(value = "/roles", params = "userid")
    @ApiOperation(value = "Добавление ролей пользователю")
    public void addUserRoles(@ApiParam(value = "Id пользователя", required = true) @RequestParam("userid") int userid,
                             @ApiParam(value = "Список ролей для добавления", required = true) @RequestBody RoleNameList roles)
    {
        service.addUserRoles(userid, roles);
    }

    /**
     * Устанваливает указанные роли пользователю
     * Все роли пользователя не указанные в списке будут удалены
     * @param userid id пользователя
     * @param roles
     */
    @PutMapping(value = "/roles", params = "userid")
    @ApiOperation(value = "Указание ролей пользователя. Все роли пользователя не указанные в списке будут удалены")
    public void setUserRoles(@ApiParam(value = "Id пользователя", required = true) @RequestParam("userid") int userid,
                             @ApiParam(value = "Список ролей", required = true) @RequestBody RoleNameList roles)
    {
        service.setUserRoles(userid, roles);
    }

    /**
     * Удаляет указанные роли у пользователя
     * Указанные роли должны быть привязаны к пользователю
     * @param userid id пользователя
     * @param roles    список названий ролей
     */
    @DeleteMapping(value = "/roles", params = "userid")
    @ApiOperation(value = "Удаление ролей у пользователя")
    public void removeUserRoles(@ApiParam(value = "Id пользователя", required = true) @RequestParam("userid") int userid,
                                @ApiParam(value = "Список ролей", required = true) @RequestBody RoleNameList roles)
    {
        service.removeUserRoles(userid, roles);
    }

    /**
     * Возвращает список пользователей, имеющих указанную роль
     * @param role название роли
     * @return возвращает список пользователей, имеющих указанную роль
     */
    @GetMapping(value = "/users", params = "role")
    @ApiOperation(value = "Получение списка пользователей с указанной ролью")
    public List<User> getRoleUsers(@ApiParam(value = "Название роли", required = true) @RequestParam("role") String role)
    {
        return service.getRoleUsers(role);
    }

    /**
     * Возвращает список пермиссий, привязанных к указанной роли
     * @param role название роли
     * @return возвращает список пермиссий, привязанных к указанной роли
     */
    @GetMapping(value = "/permissions", params = "role")
    @ApiOperation(value = "Получение списка пермиссий указанной роли")
    public List<Permission> getRolePermissions(@ApiParam(value = "Название роли", required = true) @RequestParam("role") String role)
    {
        return service.getRolePermissions(role);
    }

    /**
     * Добавляет пермисии для роли
     * @param role        название роли
     * @param permissions список id пермиссий для добавления
     */
    @PostMapping(value = "/permissions", params = "role")
    @ApiOperation(value = "Добавление пермиссий для роли")
    public void addRolePermissions(@ApiParam(value = "Название роли", required = true) @RequestParam("role") String role,
                                   @ApiParam(value = "Список пермиссий для добавления", required = true) @RequestBody PermissionIdList permissions)
    {
        service.addRolePermissions(role, permissions);
    }

    /**
     * Устанавливает список пермиссий для роли
     * Все пермиссии привязанные к роли и не входящие в список добавления будут откреплены от роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    @PutMapping(value = "/permissions", params = "role")
    @ApiOperation(value = "Указание пермиссий для роли. Все пермиссии привязанные к роли и не входящие в список " +
            "добавления будут откреплены от роли")
    public void setRolePermissions(@ApiParam(value = "Название роли", required = true) @RequestParam("role") String role,
                                   @ApiParam(value = "Список пермиссий", required = true) @RequestBody PermissionIdList permissions)
    {
        service.setRolePermissions(role, permissions);
    }

    /**
     * Удаляет указанные премиссии у роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    @DeleteMapping(value = "/permissions", params = "role")
    @ApiOperation(value = "Удаление пермиссий у роли")
    public void removeRolePermissions(@ApiParam(value = "Название роли", required = true) @RequestParam("role") String role,
                                      @ApiParam(value = "Список пермиссий", required = true) @RequestBody PermissionIdList permissions)
    {
        service.removeRolePermissions(role, permissions);
    }

    /**
     * Возвращает список ролей, привязанных к указанной пермиссии
     * @param idPermission - id пермиссии
     * @return возвращает список ролей, привязанных к указанной пермиссии
     */
    @GetMapping(value = "/roles", params = "idpermission")
    @ApiOperation(value = "Получение ролей с указанной пермиссией")
    public List<Role> getPermissionRoles(@ApiParam(value = "Id пермиссии", required = true) @RequestParam("idpermission") int idPermission)
    {
        return service.getPermissionRoles(idPermission);
    }

    /**
     * Добавляет роли к пермиссии
     * @param idPermission id пермиссии
     * @param roles        - список названий ролей
     */
    @PostMapping(value = "/roles", params = "idpermission")
    @ApiOperation(value = "Добавление ролей для пермиссии")
    public void addPermissionRoles(@ApiParam(value = "Id пермиссии", required = true) @RequestParam("idpermission") int idPermission,
                                   @ApiParam(value = "Список названий ролей", required = true) @RequestBody RoleNameList roles)
    {
        service.addPermissionRoles(idPermission, roles);
    }

    /**
     * Устанавливает указанные роли для пермиссии
     * Все роли не указанные в списке и прикрепленные к пермиссии будут откреплены от указанной пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    @PutMapping(value = "/roles", params = "idpermission")
    @ApiOperation(value =
            "Указание ролей для пермиссии. Все роли не указанные в списке и прикрепленные к пермиссии " + "будут " +
                    "откреплены от указанной пермиссии")
    public void setPermissionRoles(@ApiParam(value = "Id пермиссии", required = true) @RequestParam("idpermission") int idPermission,
                                   @ApiParam(value = "Список названий ролей", required = true) @RequestBody RoleNameList roles)
    {
        service.setPermissionRoles(idPermission, roles);
    }

    /**
     * Открепляет указанные роли от пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    @DeleteMapping(value = "/roles", params = "idpermission")
    @ApiOperation(value = "Удаление пермиссии из указанных ролей")
    public void removePermissionRoles(@ApiParam(value = "Id пермиссии", required = true) @RequestParam("idpermission") int idPermission,
                                      @ApiParam(value = "Список названий ролей", required = true) @RequestBody RoleNameList roles)
    {
        service.removePermissionRoles(idPermission, roles);
    }

    /**
     * Возвращает полную информацию о пользователе с перечислением его ролей и их пермиссий
     * id пользователя должно быть передано в заголовке запроса userId
     * @return возвращает полную информацию о пользователе с перечислением его ролей и их пермиссий
     */
    @GetMapping(value = "/auth/currentUser")
    @ApiOperation(value = "Получение информации о текущем пользователе. В заголовке запроса userId должно быть указано id пользователя.")
    public User getCurrentUser()
    {
        Integer userId = Details.getDetails().getUserIntId();
        if (userId == null)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ResourceManager.ERROR_USER_ID_EMPTY);
        }
        return service.getFullUserInfo(userId);
    }

    @PostMapping(value = "/roles/{rolename}/objects")
    @ApiOperation(value = "Указание объектов сервисов для роли")
    public void addRoleServiceObjects(@ApiParam(value = "Название роли", required = true) @PathVariable String rolename,
                                       @ApiParam(value = "Список сервисов и их объектов", required = true) @RequestBody Map<String, String> objects)
    {
        serviceData.setRoleObjects(rolename, objects);
    }

    @DeleteMapping(value = "/roles/{rolename}/objects")
    @ApiOperation(value = "Удаление объектов сервисов у роли")
    public void removeRoleServiceObjects(@ApiParam(value = "Название роли", required = true) @PathVariable String rolename)
    {
        serviceData.removeRoleObjects(rolename);
    }

    @PostMapping(value = "/synchronizeldap")
    @ApiOperation(value = "Синхронизация LDAP пользователей с БД")
    public void synchronizeLdap(@ApiParam(value = "Параметры подключения к LDAP", required = true) @RequestBody LdapPermissions ldap)
    {
        List<String> ldapUsers = ldapService.getAllUsers(ldap);
        service.updateUsersFromLdap(ldapUsers);
    }

    @PostMapping(value = "/users/{id}/enabled")
    @ApiOperation(value = "Изменение доступа пользователя к системе")
    public void setUserEnabled(@ApiParam(value = "id", required = true) @PathVariable("id") int userId,
                               @ApiParam(value = "Доступ к системе", required = true) @RequestParam("value") boolean enabled)
    {
        service.setUserEnabled(userId, enabled);
    }

    @PostMapping(value = "/users/{id}/password")
    @ApiOperation(value = "Изменение пароля пользователя")
    public void changePassword(@ApiParam(value = "id", required = true) @PathVariable("id") int userId,
                               @ApiParam(value = "Пароль пользователя", required = true) @RequestBody ChangePassword changePasswordObject, HttpServletResponse response) throws IOException
    {
        Integer userCurrentId = Details.getDetails().getUserIntId();
        if (userCurrentId == null)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ResourceManager.ERROR_USER_ID_EMPTY);
        }

        if (userCurrentId.intValue() != userId)
        {
            response.sendError(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
            return;
        }

        service.changeUserPassword(userId, changePasswordObject);
    }

    /**
     * Добавляет обьекты для пользователя
     * @param userId идентификатор пользователя
     * @param objects список идентификаторов обьектов
     */
    @PostMapping(value = "/users/objects", params = "userid")
    @ApiOperation(value = "Добавление объектов пользователя")
    public void addUserObjects(
            @ApiParam(value = "Идентификатор пользователя", required = true)
            @RequestParam(value="userid", required = true) Integer userId,
            @ApiParam(value = "Cписок идентификаторов обьектов", required = true)
            @RequestBody(required = true) List<Integer> objects)
    {
        serviceData.addUserObjects(userId, objects);
    }

    /**
     * Устанавливает объекты для пользователя
     * @param userId идентификатор пользователя
     * @param objects список идентификаторов объектов
     */
    @PutMapping(value = "/users/objects", params = "userid")
    @ApiOperation(value = "Указание объектов пользователя", notes = "Все связи пользователя и объектов которых нет в списке будут удалены")
    public void setUserObjects(
            @ApiParam(value = "Идентификатор пользователя", required = true)
            @RequestParam(value="userid", required = true) Integer userId,
            @ApiParam(value = "Cписок идентификаторов обьектов", required = true)
            @RequestBody(required = true) List<Integer> objects)
    {
        serviceData.setUserObjects(userId, objects);
    }

    /**
     * Удаляет объекты пользователя
     * @param userId идентификатор пользователя
     */
    @DeleteMapping(value = "/users/objects", params = "userid")
    @ApiOperation(value = "Удаление объектов пользователя",notes = "Если список объектов не указан, то будут удалены все объекты пользователя")
    public void removeUserObjects(
            @ApiParam(value = "Идентификатор пользователя", required = true)
            @RequestParam(value="userid", required = true) Integer userId,
    @ApiParam(value = "Cписок идентификаторов обьектов", required = false)
    @RequestBody(required = false) List<Integer> objects)
    {
        serviceData.removeUserObjects(userId, objects);
    }

    /**
     * Возвращает объекты пользователя
     */
    @GetMapping(value = "/users/{id}/objects")
    @ApiOperation(value = "Получение объектов пользователя")
    public List<UserGenerationObject> getUserObjects(
            @ApiParam(value = "Id пользователя", required = true) @PathVariable("id") int userId)
    {
        return serviceData.getUserObjects(userId);
    }

    /**
     * Возвращает связь пользователя и объекта по id записи
     */
    @GetMapping(value = "/users/objects/{rowid}")
    @ApiOperation(value = "Получение связи пользователя и объекта по id записи")
    public UserGenerationObject getUserObjectsByRowId(@ApiParam(value = "Id записи связи пользователя и объекта",
            required = true) @PathVariable("rowid") int rowId)
    {
        return serviceData.getUserObjectByRowId(rowId);
    }

    /**
     * Обновляет связь пользователя и объекта по указанной id записи
     */
    @PostMapping(value = "/users/objects/{rowid}")
    @ApiOperation(value = "Изменяет связь пользователя и объекта по id записи")
    public void updateUserObjectByRowId(@ApiParam(value = "Id записи связи пользователя и объекта", required = true)
                                          @PathVariable("rowid") int rowId, @ApiParam(value = "Связь пользователя и объекта", required = true)
                                          @RequestBody UserGenerationObject userGenerationObject)
    {
        serviceData.updateUserObjectByRowId(rowId, userGenerationObject.getUserId(),
                userGenerationObject.getGenerationObjectId());
    }
    
    /**
     * Возвращает настройки пользователя
     */
    @GetMapping(value = "/users/settings")
    @ApiOperation(value = "Получение настроек пользователя")
    public JsonNode getUserSettings()
    {
        return serviceData.getUserSettings();
    }
    
    /**
     * Сохраняет настройки пользователя
     */
    @PutMapping(value = "/users/settings")
    @ApiOperation(value = "Сохраняет настройки пользователя")
    public void setUserSettings(
            @ApiParam(value = "Настройки пользователя", required = true) @RequestBody JsonNode settings)
    {
        serviceData.setUserSettings(settings);
    }
}
