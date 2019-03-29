package com.common.services.management.beans.management.database;

import com.common.services.management.beans.management.model.*;

import java.util.List;

/**
 * UsersManagementDao.java
 * Date: 5 сент. 2018 г.
 * Users: amatveev
 * Description: Интерфейс для работы с базой пользователей
 */
public interface UsersManagementDao
{
    /**
     * Возвращает данные пользователя с указанным логином
     * @param username логин пользователя
     * @return возвращает данные пользователя с указанным логином
     */
    User getUser(String username);

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
     * @return возваращает id добавленного пользователя
     */
    int addUser(User user);

    /**
     * Обновляет данные пользователя
     * Пользователь с указанным id должен быть записан в БД
     * @param id   id пользователя
     * @param user данные пользователя
     */
    void updateUser(int id, User user);

    /**
     * Удаляет пользователя
     * @param id id пользователя
     */
    void removeUser(int id);

    /**
     * Добавляет пермиссию
     * Пермиссия с укзанными path и method не должна существовать в БД
     * @param permission данные премиссии
     * @return id добавленной пермиссии
     */
    int addPermission(Permission permission);

    /**
     * Удаляет пермиссию
     * @param id id пермиссии
     */
    void removePermission(int id);

    /**
     * Добавляет роль
     * Роль с указанным названием не должна существовать в БД
     * @param role данные роли
     */
    void addRole(Role role);

    /**
     * Удаляет роль
     * @param rolename название роли
     */
    void removeRole(String rolename);

    /**
     * Возвращает список пользователей с их данными
     * @param withRoles если флаг true данные пользователей будут содержать список их ролей
     * @return Возвращает список пользователей с их данными
     */
    List<User> getAllUsers(boolean withRoles);

    /**
     * Обновляет данные роли
     * Роль с указанным названием должна существовать в БД
     * @param roleName название роли
     * @param role     данные роли для обновления
     */
    void updateRole(String roleName, Role role);

    /**
     * Возвращает список всех ролей
     * @param withPermissions если флаг true роли будут содержать список назначенных им пермиссий
     * @return возвращает список всех ролей
     */
    List<Role> getAllRoles(boolean withPermissions);

    /**
     * Возвращает информацию об указанной роли
     * @param role название роли
     * @return возвращает информацию об указанной роли
     */
    Role getRole(String role);

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
     * Обновляет данные премиссии
     * Пермиссия с указанным id должна существовать в БД
     * @param idPermission id пермиссии
     * @param permission   данные пермиссии для обновления
     */
    void updatePermission(int idPermission, Permission permission);

    /**
     * Открепляет указанные роли от пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    void removePermissionRoles(int idPermission, RoleNameList roles);

    /**
     * Устанавливает указанные роли для пермиссии
     * Все роли не указанные в списке и прикрепленные к пермиссии будут откреплены от указанной пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    void setPermissionRoles(int idPermission, RoleNameList roles);

    /**
     * Добавляет роли к пермиссии
     * @param idPermission id пермиссии
     * @param roles        список названий ролей
     */
    void addPermissionRoles(int idPermission, RoleNameList roles);

    /**
     * Удаляет указанные премиссии у роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    void removeRolePermissions(String role, PermissionIdList permissions);

    /**
     * Устанавливает список пермиссий для роли
     * Все пермиссии привязанные к роли и не входящие в список добавления будут откреплены от роли
     * @param role        название роли
     * @param permissions список id пермиссий
     */
    void setRolePermissions(String role, PermissionIdList permissions);

    /**
     * Добавляет пермисии для роли
     * @param role        название роли
     * @param permissions список id пермиссий для добавления
     */
    void addRolePermissions(String role, PermissionIdList permissions);

    /**
     * Возвращает список ролей, привязанных к указанной пермиссии
     * @param idPermission - id пермиссии
     * @return возвращает список ролей, привязанных к указанной пермиссии
     */
    List<Role> getPermissionRoles(int idPermission);

    /**
     * Возвращает список пермиссий, привязанных к указанной роли
     * @param role название роли
     * @return возвращает список пермиссий, привязанных к указанной роли
     */
    List<Permission> getRolePermissions(String role);

    /**
     * Возвращает список пользователей, имеющих указанную роль
     * @param role название роли
     * @return возвращает список пользователей, имеющих указанную роль
     */
    List<User> getRoleUsers(String role);

    /**
     * Удаляет указанные роли у пользователя
     * Указанные роли должны быть привязаны к пользователю
     * @param username логин пользователя
     * @param roles    список названий ролей
     */
    void removeUserRoles(String username, RoleNameList roles);

    /**
     * Устанваливает указанные роли пользователю
     * Все роли пользователя не указанные в списке будут удалены
     * @param username логин пользователя
     * @param roles
     */
    void setUserRoles(String username, RoleNameList roles);

    /**
     * Добавляет указанные роли пользователю
     * @param username логин пользователя
     * @param roles    список названий ролей для добавления
     */
    void addUserRoles(String username, RoleNameList roles);

    /**
     * Возвращает список ролей указанного пользователя
     * @param username логин пользователя
     * @return возвращает список ролей пользователя
     */
    List<Role> getUserRoles(String username);

    /**
     * Изменение доступа пользователя к системе
     * @param userId  id пользователя
     * @param enabled значение доступа
     */
    void setUserEnabled(int userId, boolean enabled);

    /**
     * Возвращает пароль указанного пользователя
     * @param userId id пользователя
     * @return возвращает пароль указанного пользователя
     */
    String getUserPassword(int userId);

    /**
     * Изменение пароля пользователя
     * @param userId   id пользователя
     * @param password хэш пароля пользователя
     */
    void setUserPassword(int userId, String password);

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
     * Добавляет пермиссии в БД
     * @param permissions Список пермиссий для добавления
     * @return Возвращает количество добавленных пермиссий
     */
    int addPermissions(List<Permission> permissions);

    /**
     * Возвращает все пермиссии не связанные с ролями пользователей
     * @return Возвращает все пермиссии не связанные с ролями пользователей
     */
    List<Permission> getUnlinkedPermissions();

    /**
     * Возвращает ФИО всех пользователей с указанной ролью
     * @param role Название роли
     * @return Возвращает ФИО всех пользователей с указанной ролью
     */
    List<UserShort> getShortUsersWithRole(String role);
}