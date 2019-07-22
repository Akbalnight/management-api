package com.common.services.management.beans.management.service;

import com.common.services.management.beans.management.model.*;

import java.util.List;

/**
 * UsersManagementService.java
 * Date: 14 сент. 2018 г.
 * Users: amatveev
 * Description: Интерфейс сервиса управления пользователями, ролями и пермиссиями
 */
public interface UsersManagementService
{
    String FIRST_NAME = "firstName";
    String LAST_NAME = "lastName";
    String MIDDLE_NAME = "middleName";

    /**
     * Возвращает данные пользователя с указанным id
     * @param id id пользователя
     * @return возвращает данные пользователя с указанным id
     */
    User getUser(int id);

    /**
     * Добавляет пользователя
     * Пользователя с указанным логином не должно быть в БД
     * @param user данные пользователя
     * @return Возвращает данные добавленного пользователя
     */
    User addUser(User user);

    /**
     * Обновляет данные пользователя
     * Пользователь с указанным id должен быть записан в БД
     * @param id   id пользователя
     * @param user данные пользователя
     * @return Возвращает обновленные данные пользователя
     */
    User updateUser(int id, User user);

    /**
     * Удаляет пользователя
     * @param id id пользователя
     */
    void removeUser(int id);

    /**
     * Возвращает список пользователей с их данными
     * @param withRoles если флаг true данные пользователей будут содержать список их ролей
     * @return Возвращает список пользователей с их данными
     */
    List<User> getAllUsers(boolean withRoles);

    /**
     * Возвращает информацию об указанной роли
     * @param role название роли
     * @return возвращает информацию об указанной роли
     */
    Role getRole(String role);

    /**
     * Возвращает список всех ролей
     * @param withPermissions если флаг true роли будут содержать список назначенных им пермиссий
     * @return возвращает список всех ролей
     */
    List<Role> getAllRoles(boolean withPermissions);

    /**
     * Добавляет роль
     * Роль с указанным названием не должна существовать в БД
     * @param role данные роли
     * @return Возвращает данные добавленной роли
     */
    Role addRole(Role role);

    /**
     * Обновляет данные роли
     * Роль с указанным названием должна существовать в БД
     * @param roleName название роли
     * @param role     данные роли для обновления
     * @return Возвращает обновленные данные роли
     */
    Role updateRole(String roleName, Role role);

    /**
     * Удаляет роль
     * @param role название роли
     * @return Возвращает название удаленной роли
     */
    String removeRole(String role);

    /**
     * Возвращает данные пермиссии
     * @param idPermission id пермиссии
     * @return возвращает данные пермиссии
     */
    Permission getPermission(int idPermission);

    /**
     * Возвращает список пермиссий
     * @return возвращает список пермиссий
     */
    List<Permission> getAllPermissions();

    /**
     * Добавляет пермиссию
     * Пермиссия с укзанными path и method не должна существовать в БД
     * @param permission данные премиссии
     * @return Возвращает данные добавленной пермиссии
     */
    Permission addPermission(Permission permission);

    /**
     * Обновляет данные премиссии
     * Пермиссия с указанным id должна существовать в БД
     * @param idPermission id пермиссии
     * @param permission   данные пермиссии для обновления
     * @return Возвращает обновленные данные пермиссии
     */
    Permission updatePermission(int idPermission, Permission permission);

    /**
     * Удаляет пермиссию
     * @param idPermission id пермиссии
     */
    void removePermission(int idPermission);

    /**
     * Возвращает список ролей указанного пользователя
     * @param userId id пользователя
     * @return возвращает список ролей пользователя
     */
    List<Role> getUserRoles(int userId);

    /**
     * Добавляет указанные роли пользователю
     * @param userId id пользователя
     * @param roles  список названий ролей для добавления
     */
    void addUserRoles(int userId, RoleNameList roles);

    /**
     * Устанавливает указанные роли пользователю
     * Все роли пользователя не указанные в списке будут удалены
     * @param userid id пользователя
     * @param roles
     */
    void setUserRoles(int userid, RoleNameList roles);

    /**
     * Удаляет указанные роли у пользователя
     * Указанные роли должны быть привязаны к пользователю
     * @param userid if пользователя
     * @param roles  список названий ролей
     */
    void removeUserRoles(int userid, RoleNameList roles);

    /**
     * Возвращает список пользователей, имеющих указанную роль
     * @param role название роли
     * @return возвращает список пользователей, имеющих указанную роль
     */
    List<User> getRoleUsers(String role);

    /**
     * Возвращает список пермиссий, привязанных к указанной роли
     * @param role название роли
     * @return возвращает список пермиссий, привязанных к указанной роли
     */
    List<Permission> getRolePermissions(String role);

    /**
     * Возвращает список ролей, привязанных к указанной пермиссии
     * @param idPermission - id пермиссии
     * @return возвращает список ролей, привязанных к указанной пермиссии
     */
    List<Role> getPermissionRoles(int idPermission);

    /**
     * Добавляет пермисии для роли
     * @param role        название роли
     * @param permissions список id пермиссий для добавления
     */
    void addRolePermissions(String role, PermissionIdList permissions);

    /**
     * Устанавливает список пермиссий для роли
     * Все пермиссии привязанные к роли и не входящие в список добавления будут откреплены от роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    void setRolePermissions(String role, PermissionIdList permissions);

    /**
     * Удаляет указанные премиссии у роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    void removeRolePermissions(String role, PermissionIdList permissions);

    /**
     * Добавляет роли к пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    void addPermissionRoles(int idPermission, RoleNameList roles);

    /**
     * Устанавливает указанные роли для пермиссии
     * Все роли не указанные в списке и прикрепленные к пермиссии будут откреплены от указанной пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    void setPermissionRoles(int idPermission, RoleNameList roles);

    /**
     * Открепляет указанные роли от пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    void removePermissionRoles(int idPermission, RoleNameList roles);

    /**
     * Возвращает полную информацию о пользователе с перечислением его ролей и их пермиссий
     * @param id id пользователя
     * @return возвращает полную информацию о пользователе с перечислением его ролей и их пермиссий
     */
    User getFullUserInfo(int id);

    /**
     * Синхронизация БД с пользователями LDAP
     * @param usernames список логинов LDAP пользователей
     */
    void updateUsersFromLdap(List<String> usernames);

    /**
     * Изменение доступа пользователя к системе
     * @param userId  id пользователя
     * @param enabled значение доступа
     */
    void setUserEnabled(int userId, boolean enabled);

    /**
     * Изменение пароля пользователя
     * @param userId               id пользователя
     * @param changePasswordObject данные старого и нового паролей пользователя
     */
    void changeUserPassword(int userId, ChangePassword changePasswordObject);

    /**
     * Сброс пароля пользователя. Пользователю устанавливается указанный пароль
     * @param userId               id пользователя
     * @param changePasswordObject данные нового пароля пользователя
     */
    void resetUserPassword(int userId, ChangePassword changePasswordObject);

    /**
     * Возвращает список всех LDAP групп со списками соответствующих им ролей
     * @param pageNumber Номер страницы
     * @param pageSize Количество групп на странице
     * @return возвращает список всех LDAP групп со списками соответствующих им ролей
     */
    LdapGroupsResult getLdapGroups(Integer pageNumber, Integer pageSize);

    /**
     * Возвращает список ролей, соответствующих указанной LDAP группе
     * @param group название LDAP группы
     * @return возвращает список ролей, соответствующих указанной LDAP группе
     */
    List<String> getRolesByLdapGroup(String group);

    /**
     * Возвращает список названий LDAP групп, соответствующих указанной роли
     * @param role название роли
     * @return возвращает список названий LDAP групп, соответствующих указанной роли
     */
    List<String> getLdapGroupsByRole(String role);

    /**
     * Добавление ролей указанной LDAP группе
     * @param group название LDAP группы
     * @param roles список ролей
     */
    void addRolesToLdapGroup(String group, List<String> roles);

    /**
     * Установка ролей указанной LDAP группе
     * @param group название LDAP группы
     * @param roles список ролей
     */
    void setRolesToLdapGroup(String group, List<String> roles);

    /**
     * Удаляет связи указанных ролей и LDAP группы
     * @param group название LDAP группы
     * @param roles список ролей
     */
    void removeRolesFromLdapGroup(String group, List<String> roles);

    /**
     * Очищает список ролей указанной LDAP группы
     * @param group название LDAP группы
     */
    void clearLdapGroup(String group);

    /**
     * Добавляет список пермиссий в БД
     * @param permissions Список пермиссий для добавления
     * @param roles Список ролей пользователей
     * @return Возвращает количество добавленный пермиссий
     */
    int addPermissions(List<Permission> permissions, List<String> roles);

    /**
     * Возвращает все пермиссии не связанные с ролями пользователей
     * @return Возвращает все пермиссии не связанные с ролями пользователей
     */
    List<Permission> getUnlinkedPermissions();

    /**
     * Возвращает ФИО всех пользователей с ролью ROLE_PORTAL
     * @return Возвращает ФИО всех пользователей с ролью ROLE_PORTAL
     */
    List<UserShort> getPortalUsers();
}
