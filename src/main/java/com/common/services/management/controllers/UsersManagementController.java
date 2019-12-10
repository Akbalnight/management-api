package com.common.services.management.controllers;

import com.common.services.management.beans.management.model.*;
import com.common.services.management.beans.management.service.LdapService;
import com.common.services.management.beans.management.service.UsersManagementService;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.beans.serv.resourcemanager.ResourceManager;
import com.common.services.management.details.Details;
import com.common.services.management.filters.UsersFilter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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
        return user;
    }

    @ApiOperation(value = "Получение списка пользователей по фильтру")
    @PostMapping(value = "/users/filter")
    public List<User> getAllUsersByFilter(
            @RequestBody(required = false) UsersFilter filter,
            Pageable pageable)
    {
        return service.getAllUsers(false, filter, pageable);
    }

    @ApiOperation(value = "Получение списка пользователей с их данными")
    @GetMapping(value = "/users")
    public List<User> getAllUsersWithRoles(
            @ApiParam(value = "Если флаг true данные пользователей будут содержать список их ролей", required = false)
            @RequestParam(value = "with_roles", defaultValue = "false") boolean withRoles)
    {
        return service.getAllUsers(withRoles, null, null);
    }

    /**
     * Добавляет пользователя
     * Пользователя с указанным логином не должно быть в БД
     * @param user данные пользователя
     * @return возваращает id добавленного пользователя
     */
    @PostMapping(value = "/users")
    @ApiOperation(value = "Добавление пользователя", notes = "Вернет данные добавленного пользователя")
    public User addUser(@ApiParam(value = "Данные пользователя", required = true) @RequestBody User user)
    {
        return service.addUser(user);
    }

    /**
     * Обновляет данные пользователя
     * Пользователь с указанным id должен быть записан в БД
     * @param id   id пользователя
     * @param user данные пользователя
     * @return Возвращает обновленные данные пользователя
     */
    @PutMapping(value = "/users/{id}")
    @ApiOperation(value = "Обновление данных пользователя", notes = "Вернет обновленные данные пользователя")
    public User updateUser(@ApiParam(value = "Id пользователя", required = true) @PathVariable("id") int id,
                           @ApiParam(value = "Данные пользователя", required = true) @RequestBody User user)
    {
        return service.updateUser(id, user);
    }

    /**
     * Удаляет пользователя
     * @param id id пользователя
     * @return Возвращает id удаленного пользователя
     */
    @DeleteMapping(value = "/users/{id}")
    @ApiOperation(value = "Удаление пользователя", notes = "Вернет Id удаленного пользователя")
    public int removeUser(@ApiParam(value = "Id пользователя", required = true) @PathVariable("id") int id)
    {
        service.removeUser(id);
        return id;
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
     * @return Возвращает данные добавленной роли
     */
    @PostMapping(value = "/roles")
    @ApiOperation(value = "Добавление роли", notes = "Вернет данные добавленной роли")
    public Role addRole(@ApiParam(value = "Данные роли", required = true) @RequestBody Role role)
    {
        return service.addRole(role);
    }

    /**
     * Обновляет данные роли
     * Роль с указанным названием должна существовать в БД
     * @param roleName название роли
     * @param role     данные роли для обновления
     * @return Возвращает обновленные данные роли
     */
    @PutMapping(value = "/roles/{role}")
    @ApiOperation(value = "Обновление данных роли", notes = "Вернет обновленные данные роли")
    public Role updateRole(@ApiParam(value = "Название роли", required = true) @PathVariable("role") String roleName,
                           @ApiParam(value = "Данные роли для обновления") @RequestBody Role role)
    {
        return service.updateRole(roleName, role);
    }

    /**
     * Удаляет роль
     * @param role название роли
     */
    @DeleteMapping(value = "/roles/{role}")
    @ApiOperation(value = "Удаление роли", notes = "Вернет название удаленной роли")
    public String removeRole(@ApiParam(value = "Название роли", required = true) @PathVariable("role") String role)
    {
        return service.removeRole(role);
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
     * @return Возвращает данные добавленной пермиссии
     */
    @PostMapping(value = "/permissions")
    @ApiOperation(value = "Добавление пермиссии", notes = "Вернет данные добавленной пермиссии")
    public Permission addPermission(@ApiParam(value = "Данные пермиссии", required = true) @RequestBody Permission permission)
    {
        return service.addPermission(permission);
    }

    /**
     * Обновляет данные премиссии
     * Пермиссия с указанным id должна существовать в БД
     * @param idPermission id пермиссии
     * @param permission   данные пермиссии для обновления
     * @return Возвращает обновленные данные пермиссии
     */
    @PutMapping(value = "/permissions/{id}")
    @ApiOperation(value = "Обновление данных пермиссии", notes = "Вернет обновленные данные пермиссии")
    public Permission updatePermission(@ApiParam(value = "Id пермиссии", required = true) @PathVariable("id") int idPermission,
                                 @ApiParam(value = "Данные пермиссии", required = true) @RequestBody Permission permission)
    {
        return service.updatePermission(idPermission, permission);
    }

    /**
     * Удаляет пермиссию
     * @param idPermission id пермиссии
     */
    @DeleteMapping(value = "/permissions/{id}")
    @ApiOperation(value = "Удаление пермиссии", notes = "Вернет Id удаленной пермиссии")
    public int removePermission(@ApiParam(value = "Id пермиссии", required = true) @PathVariable("id") int idPermission)
    {
        service.removePermission(idPermission);
        return idPermission;
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
     * Устанавливает указанные роли пользователю
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
            "Указание ролей для пермиссии. Все роли не указанные в списке и прикрепленные к пермиссии будут " +
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

    @PostMapping(value = "/users/{id}/resetPassword")
    @ApiOperation(value = "Сброс пароля пользователя")
    public void resetPassword(@ApiParam(value = "id", required = true) @PathVariable("id") int userId,
                              @ApiParam(value = "Новый пароль пользователя", required = true) @RequestBody ChangePassword changePasswordObject)
            throws IOException
    {
        service.resetUserPassword(userId, changePasswordObject);
    }

    @GetMapping(value="/permissions/unlinked")
    @ApiOperation(value = "Получить все пермиссии не связанные с ролями пользователей")
    public List<Permission> getUnlinkedPermissions()
    {
        return service.getUnlinkedPermissions();
    }

    @GetMapping(value = "/users/portal")
    @ApiOperation(value = "Получить ФИО всех пользователей с ролью ROLE_PORTAL")
    public List<UserShort> getPortalUsers()
    {
        return service.getPortalUsers();
    }
}
