package com.common.services.management.beans.management.service;

import com.common.services.management.beans.management.database.UsersManagementDao;
import com.common.services.management.beans.management.model.*;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import com.common.services.management.beans.serv.exceptions.UserNotFoundException;
import com.common.services.management.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.common.services.management.beans.management.service.UserManagementHelper.*;
import static java.util.Comparator.comparing;

/**
 * UsersManagementServiceImpl.java
 * Date: 14 сент. 2018 г.
 * Users: amatveev
 * Description: Сервис для управления пользователями, ролями и пермиссиями
 */
@Service
public class UsersManagementServiceImpl
        implements UsersManagementService
{
    /**
     * Сообщения для обработки исключений
     */
    private static final String ERROR_USER_LOGIN_EMPTY = "error.auth.userLoginEmpty";
    private static final String ERROR_ROLE_NAME_EMPTY = "error.auth.roleNameEmpty";
    private static final String ERROR_PERMISSION_PARAMS_EMPTY = "error.auth.permissionParamsEmpty";
    private static final String ERROR_ROLES_EMPTY = "error.auth.rolesEmpty";
    private static final String ERROR_GROUP_EMPTY = "error.auth.groupEmpty";
    private static final String ERROR_PERMISSIONS_EMPTY = "error.auth.permissionsEmpty";
    private static final String ERROR_EMPTY_USER_PASSWORD = "error.auth.emptyUserPassword";
    private static final String ERROR_NOT_VALID_USER_PASSWORD = "error.auth.notValidUserPassword";

    @Autowired
    private UsersManagementDao usersManagementDao;

    @Autowired
    private ServiceException serviceException;

    @Autowired
    private Logger logger;

    @Value("${ldap.groups.page.size:50}")
    protected int pageSize;

    @Override
    public User getUser(int id)
    {
        User user = usersManagementDao.getUser(id);
        if (user != null)
        {
            user.setRoles(usersManagementDao.getUserRoles(user.getName()));
        }
        return user;
    }

    @Override
    public User addUser(User user)
    {
        validationUserName(user.getName());
        user.setName(prepareUserName(user.getName()));
        if (user.getPassword() == null)
        {
            user.setPassword("");
        }
        user.setPassword(user.getPassword());
        parseFullUserName(user);
        int id = usersManagementDao.addUser(user);
        user.setId(id);
        user.setPassword(null);
        return user;
    }

    /**
     * Парсинг ФИО пользователя из поля firstName
     * Если lastName не указано, то поля firstName, lastName и middleName заполняются из поля firstName.
     * Разделитель - пробел
     * @param user Данные пользователя
     */
    private void parseFullUserName(User user)
    {
        Map<String, Object> jsonData = user.getJsonData();
        if (jsonData == null)
        {
            return;
        }
        String firstName = "";
        String lastName = "";
        String middleName = "";

        try
        {
            for (Map.Entry<String, Object> entry : jsonData.entrySet())
            {
                if (entry.getKey().equals(FIRST_NAME))
                {
                    firstName = entry.getValue().toString().trim();
                }
                else if (entry.getKey().equals(LAST_NAME))
                {
                    lastName = entry.getValue().toString().trim();
                }
                else if (entry.getKey().equals(MIDDLE_NAME))
                {
                    middleName = entry.getValue().toString().trim();
                }
            }
            if (lastName.isEmpty())// Если lastName не заполнено, распарсим поле firstName
            {
                int index = firstName.indexOf(" ");
                if (index > 0)
                {
                    if (firstName.lastIndexOf(" ") == index)// в формате "Фамилия Имя"
                    {
                        lastName = firstName.substring(0, index).trim();
                        firstName = firstName.substring(index).trim();
                    }
                    else // в формате "Фамилия Имя Отчество"
                    {
                        String fullName = firstName;
                        lastName = fullName.substring(0, index).trim();
                        fullName = fullName.substring(index).trim();

                        index = fullName.indexOf(" ");
                        firstName = fullName.substring(0, index).trim();
                        middleName = fullName.substring(index).trim();
                    }
                }
            }
            jsonData.put(FIRST_NAME, firstName);
            jsonData.put(LAST_NAME, lastName);
            jsonData.put(MIDDLE_NAME, middleName);
        }
        catch (Exception e)
        {
            logger.error("Ошибка парсинга ФИО пользователя", e);
        }
        user.setJsonData(jsonData);
    }

    @Override
    public User updateUser(int id, User user)
    {
        validationUserName(user.getName());
        user.setName(prepareUserName(user.getName()));
        parseFullUserName(user);
        usersManagementDao.updateUser(id, user);
        user.setId(id);
        return user;
    }

    @Override
    public void removeUser(int id)
    {
        usersManagementDao.removeUser(id);
    }

    @Override
    public List<User> getAllUsers(boolean withRoles)
    {
        List<User> users = usersManagementDao.getAllUsers(withRoles);
        if (withRoles)
        {
            users.forEach(user -> user.getRoles().sort(comparing(Role::getName)));
        }
        users.sort(comparing(User::getId));
        return users;
    }

    @Override
    public Role getRole(String rolename)
    {
        validationRole(rolename);
        rolename = prepareRoleName(rolename);
        Role role = usersManagementDao.getRole(rolename);
        role.setPermissions(usersManagementDao.getRolePermissions(role.getName()));
        return role;
    }

    @Override
    public List<Role> getAllRoles(boolean withPermissions)
    {
        List<Role> roles = usersManagementDao.getAllRoles(withPermissions);
        roles.sort(comparing(Role::getName));
        return roles;
    }

    @Override
    public Role addRole(Role role)
    {
        validationRole(role.getName());
        role.setName(prepareRoleName(role.getName()));
        usersManagementDao.addRole(role);
        return role;
    }

    @Override
    public Role updateRole(String roleName, Role role)
    {
        validationRole(roleName);
        validationRole(role.getName());
        roleName = prepareRoleName(roleName);
        role.setName(prepareRoleName(role.getName()));
        usersManagementDao.updateRole(roleName, role);
        return role;
    }

    @Override
    public String removeRole(String role)
    {
        validationRole(role);
        role = prepareRoleName(role);
        usersManagementDao.removeRole(role);
        return role;
    }

    @Override
    public Permission getPermission(int idPermission)
    {
        return usersManagementDao.getPermission(idPermission);
    }

    @Override
    public List<Permission> getAllPermissions()
    {
        return usersManagementDao.getAllPermissions();
    }

    @Override
    public Permission addPermission(Permission permission)
    {
        validationPermission(permission);
        int id = usersManagementDao.addPermission(permission);
        permission.setId(id);
        return permission;
    }

    @Override
    public Permission updatePermission(int idPermission, Permission permission)
    {
        validationPermission(permission);
        usersManagementDao.updatePermission(idPermission, permission);
        permission.setId(idPermission);
        return permission;
    }

    @Override
    public void removePermission(int idPermission)
    {
        usersManagementDao.removePermission(idPermission);
    }

    @Override
    public List<Role> getUserRoles(int userId)
    {
        User user = usersManagementDao.getUser(userId);
        return usersManagementDao.getUserRoles(user.getName());
    }

    @Override
    public void addUserRoles(int userId, RoleNameList roles)
    {
        validationRolesNamesList(roles, false);
        roles.setRoles(prepareRolesNames(roles.getRoles()));
        User user = usersManagementDao.getUser(userId);
        usersManagementDao.addUserRoles(user.getName(), roles);
    }

    @Override
    public void setUserRoles(int userid, RoleNameList roles)
    {
        validationRolesNamesList(roles, true);
        roles.setRoles(prepareRolesNames(roles.getRoles()));
        User user = usersManagementDao.getUser(userid);

        // Получим роли пользователя, назначенные из LDAP
        List<String> ldapRoles = usersManagementDao.getUserRoles(user.getName())
                                                   .stream()
                                                   .filter(role -> role.getJsonData() != null
                                                           && role.getJsonData().getLdap() != null
                                                           && role.getJsonData().getLdap())
                                                   .map(Role::getName)
                                                   .collect(Collectors.toList());
        roles.getRoles()
             .addAll(ldapRoles);
        usersManagementDao.setUserRoles(user.getName(), roles);
    }

    @Override
    public void removeUserRoles(int userid, RoleNameList roles)
    {
        validationRolesNamesList(roles, false);
        roles.setRoles(prepareRolesNames(roles.getRoles()));
        User user = usersManagementDao.getUser(userid);
        usersManagementDao.removeUserRoles(user.getName(), roles);
    }

    @Override
    public List<User> getRoleUsers(String role)
    {
        validationRole(role);
        role = prepareRoleName(role);
        List<User> users = usersManagementDao.getRoleUsers(role);
        users.sort(comparing(User::getId));
        return users;
    }

    @Override
    public List<Permission> getRolePermissions(String role)
    {
        validationRole(role);
        role = prepareRoleName(role);
        return usersManagementDao.getRolePermissions(role);
    }

    @Override
    public List<Role> getPermissionRoles(int idPermission)
    {
        List<Role> roles = usersManagementDao.getPermissionRoles(idPermission);
        roles.sort(comparing(Role::getName));
        return roles;
    }

    @Override
    public void addRolePermissions(String role, PermissionIdList permissions)
    {
        validationRole(role);
        validationPermissionsIdsList(permissions, false);
        role = prepareRoleName(role);
        usersManagementDao.addRolePermissions(role, permissions);
    }

    @Override
    public void setRolePermissions(String role, PermissionIdList permissions)
    {
        validationRole(role);
        validationPermissionsIdsList(permissions, true);
        role = prepareRoleName(role);
        usersManagementDao.setRolePermissions(role, permissions);
    }

    @Override
    public void removeRolePermissions(String role, PermissionIdList permissions)
    {
        validationRole(role);
        validationPermissionsIdsList(permissions, false);
        role = prepareRoleName(role);
        usersManagementDao.removeRolePermissions(role, permissions);
    }

    @Override
    public void addPermissionRoles(int idPermission, RoleNameList roles)
    {
        validationRolesNamesList(roles, false);
        roles.setRoles(prepareRolesNames(roles.getRoles()));
        usersManagementDao.addPermissionRoles(idPermission, roles);
    }

    @Override
    public void setPermissionRoles(int idPermission, RoleNameList roles)
    {
        validationRolesNamesList(roles, true);
        roles.setRoles(prepareRolesNames(roles.getRoles()));
        usersManagementDao.setPermissionRoles(idPermission, roles);
    }

    @Override
    public void removePermissionRoles(int idPermission, RoleNameList roles)
    {
        validationRolesNamesList(roles, false);
        roles.setRoles(prepareRolesNames(roles.getRoles()));
        usersManagementDao.removePermissionRoles(idPermission, roles);
    }

    @Override
    public User getFullUserInfo(int id)
    {
        User user = usersManagementDao.getUser(id);
        if (user != null)
        {
            user.setRoles(usersManagementDao.getUserRoles(user.getName()));
            user.getRoles().forEach(role -> role.setPermissions(usersManagementDao.getRolePermissions(role.getName())));
        }
        return user;
    }

    @Override
    public void updateUsersFromLdap(List<String> usernames)
    {
        for (String username : usernames)
        {
            try
            {
                User user = usersManagementDao.getUser(prepareUserName(username));
                if (user.getLdap())
                {
                    if (!user.isEnabled())
                    {
                        // откроем пользователю доступ
                        user.setEnabled(true);
                        updateUser(user.getId(), user);
                    }
                }
                else
                {
                    // установим пользователю метку ldap
                    user.setLdap(true);
                    user.setEnabled(true);
                    updateUser(user.getId(), user);
                }
            }
            catch (UserNotFoundException e)
            {
                // пользователь не найден
                User user = new User();
                user.setName(username);
                user.setEnabled(true);
                user.setLdap(true);
                addUser(user);
            }
        }
        disableMissingLdapUsers(usernames.stream().map(String::toLowerCase).collect(Collectors.toList()));
    }

    @Override
    public void setUserEnabled(int userId, boolean enabled)
    {
        usersManagementDao.setUserEnabled(userId, enabled);
    }

    @Override
    public void changeUserPassword(int userId, ChangePassword changePasswordObject)
    {
        if (changePasswordObject.getOldPassword() == null || changePasswordObject.getNewPassword() == null)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_EMPTY_USER_PASSWORD);
        }
        String dbPassword = usersManagementDao.getUserPassword(userId);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(new String(Base64
                .getDecoder()
                .decode(changePasswordObject.getOldPassword())), dbPassword))
        {
            throw serviceException.applyParameters(HttpStatus.CONFLICT, ERROR_NOT_VALID_USER_PASSWORD);
        }
        usersManagementDao.setUserPassword(userId, changePasswordObject.getNewPassword());
    }

    @Override
    public void resetUserPassword(int userId, ChangePassword changePasswordObject)
    {
        if (changePasswordObject.getNewPassword() == null)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_EMPTY_USER_PASSWORD);
        }
        usersManagementDao.setUserPassword(userId, changePasswordObject.getNewPassword());
    }

    @Override
    public LdapGroupsResult getLdapGroups(Integer pageNumber, Integer pageSize)
    {
        return usersManagementDao.getLdapGroups(
                pageNumber == null ? 0 : pageNumber - 1,
                pageSize == null ? this.pageSize : pageSize);
    }

    @Override
    public List<String> getRolesByLdapGroup(String group)
    {
        validationGroup(group);
        return usersManagementDao.getRolesByLdapGroup(group);
    }

    @Override
    public List<String> getLdapGroupsByRole(String role)
    {
        return usersManagementDao.getLdapGroupsByRole(prepareRoleName(role));
    }

    @Override
    public void addRolesToLdapGroup(String group, List<String> roles)
    {
        validationGroup(group);
        if (roles == null || roles.isEmpty())
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_ROLES_EMPTY);
        }
        usersManagementDao.addRolesToLdapGroup(group, prepareRolesNames(roles));
    }

    @Override
    public void setRolesToLdapGroup(String group, List<String> roles)
    {
        validationGroup(group);
        usersManagementDao.setRolesToLdapGroup(group, prepareRolesNames(roles));
    }

    @Override
    public void removeRolesFromLdapGroup(String group, List<String> roles)
    {
        validationGroup(group);
        usersManagementDao.removeRolesFromLdapGroup(group, prepareRolesNames(roles));
    }

    @Override
    public void clearLdapGroup(String group)
    {
        validationGroup(group);
        usersManagementDao.clearLdapGroup(group);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int addPermissions(List<Permission> permissions, List<String> roles)
    {
        return usersManagementDao.addPermissions(permissions
                .stream()
                // Очистим пермиссии с одинаковыми method и path
                .filter(distinctByKeys(Permission::getMethod, Permission::getPath))
                .collect(Collectors.toList()), roles);
    }

    @Override
    public List<Permission> getUnlinkedPermissions()
    {
        return usersManagementDao.getUnlinkedPermissions();
    }

    @Override
    public List<UserShort> getPortalUsers()
    {
        return usersManagementDao.getShortUsersWithRole("ROLE_PORTAL");
    }

    /**
     * Очищает дубликаты пермиссий в списке(по method, path)
     */
    @SuppressWarnings("unchecked")
    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors)
    {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();
        return t ->
        {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());
            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }

    /**
     * Отключает доступ у всех пользователей с меткой ldap которые есть в базе, но уже отсутсвуют в LDAP
     * @param usernames список имен текущих LDAP пользователей
     */
    private void disableMissingLdapUsers(List<String> usernames)
    {
        // Список пользователей которые есть в БД, но нет в LDAP
        List<User> users =
                usersManagementDao.getAllUsers(false).stream().filter(user -> !usernames.contains(user.getName())).collect(Collectors.toList());

        // Список пользователей которые были в ldap и были enabled
        users = users.stream().filter(user -> user.getLdap() && user.isEnabled()).collect(Collectors.toList());

        // Отключает доступ у пользователей
        users.forEach(user ->
        {
            try
            {
                user.setEnabled(false);
                usersManagementDao.updateUser(user.getId(), user);
            }
            catch (Exception ex)
            {
                logger.error(ex);
            }
        });
    }

    /**
     * Выбрасывает исключение {@link ServiceException} если имя пользователя не указано
     * @param name имя пользователя
     */
    private void validationUserName(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_USER_LOGIN_EMPTY);
        }
    }

    /**
     * Выбрасывает исключение {@link ServiceException} если название роли не указано
     * @param role название роли
     */
    private void validationRole(String role)
    {
        if (StringUtils.isEmpty(role) || ROLE_PREFIX.equals(role.toUpperCase()))
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_ROLE_NAME_EMPTY);
        }
    }

    /**
     * Выбрасывает исключение {@link ServiceException} если путь или метод пермиссии не указаны
     * @param permission пермиссия
     */
    private void validationPermission(Permission permission)
    {
        if (StringUtils.isEmpty(permission.getPath()) || permission.getMethod() == null)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_PERMISSION_PARAMS_EMPTY,
                    permission.getMethod(), permission.getPath());
        }
    }

    /**
     * Выбрасывает исключение {@link ServiceException} если список названий ролей не указан.
     * Если флаг canBeEmpty = true, то список ролей может быть пустым
     * @param roles      список названий ролей
     * @param canBeEmpty флаг - разрешен ли пустой список ролей
     */
    private void validationRolesNamesList(RoleNameList roles, boolean canBeEmpty)
    {
        if (roles == null || roles.getRoles() == null || !canBeEmpty && roles.getRoles().size() == 0)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_ROLES_EMPTY);
        }
    }

    /**
     * Выбрасывает исключение {@link ServiceException} если список id пермиссий не указан.
     * Если флаг canBeEmpty = true, то список id пермиссий может быть пустым
     * @param permissions список id пермиссий
     * @param canBeEmpty  флаг - разрешен ли пустой список id пермиссий
     */
    private void validationPermissionsIdsList(PermissionIdList permissions, boolean canBeEmpty)
    {
        if (permissions == null || permissions.getIds() == null || permissions.getIds().size() == 0)
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_PERMISSIONS_EMPTY);
        }
    }

    /**
     * Выбрасывает исключение {@link ServiceException} если название ldap группы не указано.
     * @param group Название группы
     */
    private void validationGroup(String group)
    {
        if (group == null || group.trim().isEmpty())
        {
            throw serviceException.applyParameters(HttpStatus.BAD_REQUEST, ERROR_GROUP_EMPTY);
        }
    }
}
