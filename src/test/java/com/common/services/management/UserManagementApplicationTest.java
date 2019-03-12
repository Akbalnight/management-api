package com.common.services.management;

import com.common.services.management.beans.management.model.*;
import com.common.services.management.datasource.DataSourceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserManagementApplicationTest.java
 * Date: 19 нояб. 2018 г.
 * Users: amatveev
 * Description: Тестирование API сервиса управления пользователями(UsersManagementController)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserManagementApplicationTest
{
    private final static String TABLE_USERS = "users";
    private final static String TABLE_ROLES = "roles";
    private final static String TABLE_PERMISSIONS = "permissions";
    private final static String TABLE_USER_ROLES = "user_roles";
    private final static String TABLE_ROLE_PERMISSIONS = "role_permissions";
    private final static String TABLE_LDAP_GROUP_ROLES = "ldap_roles";
    private final static String UTF8 = "UTF-8";

    @Autowired
    private MockMvc mockMvc;
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    public void setJdbcTempate(DataSourceManager dataManagement)
    {
        jdbcTemplate = new JdbcTemplate(dataManagement.getDataSource("auth"));
    }

    private User getTestUser()
    {
        User user = new User();
        user.setName("login");
        user.setEnabled(true);
        user.setLdap(false);
        user.setEmail("email");
        user.setPassword("password");
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("firstName", "firstName");
        user.setJsonData(jsonObject);
        return user;
    }

    private Role getTestRole()
    {
        Role role = new Role();
        role.setName("ROLE_TEST1");
        role.setDescription("descr");
        RoleJsonObject roleJsonObject = new RoleJsonObject();
        Map<String, String> map = new HashMap<>();
        map.put("service1", "data");
        roleJsonObject.setObjects(map);
        role.setJsonData(roleJsonObject);
        role.setPermissions(Collections.emptyList());
        return role;
    }

    private Permission getTestPermission()
    {
        Permission permission = new Permission();
        permission.setMethod(HttpMethod.GET);
        permission.setPath("/path");
        permission.setDescription("descr");
        return permission;
    }

    /**
     * Тестирование API методов добавления/изменения/получения/удаления пользователей
     * @throws Exception
     */
    @Test
    public void testAPIUsers() throws Exception
    {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_USERS);

        User user = getTestUser();
        String json = jsonMapper.writeValueAsString(user);
        // Добавление User
        String userId =
                mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        // User добавлен в базу
        assertEquals("Добавление пользователя", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_USERS));

        // Получение User
        user.setId(Integer.parseInt(userId));
        user.setPassword(null);
        json = jsonMapper.writeValueAsString(user);
        mockMvc.perform(get("/users/" + userId)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Получение списка пользователей
        User user2 = getTestUser();
        user2.setName("user2");
        json = jsonMapper.writeValueAsString(user2);
        userId =
                mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        user2.setId(Integer.parseInt(userId));
        user2.setPassword(null);
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);
        json = jsonMapper.writeValueAsString(users);
        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));
        assertEquals("Получение списка пользователей", 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_USERS));

        // Удаление пользователя
        mockMvc.perform(delete("/users/" + userId)).andExpect(status().isOk());
        mockMvc.perform(get("/users/" + userId)).andExpect(status().isNotFound());
        assertEquals("Удаление пользователя", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_USERS));

        // Изменение пользователя
        user.setName("rename_user1");
        user.setEmail("rename_email");
        user.setLdap(true);
        user.setEnabled(false);
        json = jsonMapper.writeValueAsString(user);
        mockMvc.perform(put("/users/" + user.getId()).contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        mockMvc.perform(get("/users/" + user.getId())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));
        assertEquals("Изменение пользователя", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_USERS));
    }

    /**
     * Тестирование API методов добавления/изменения/удаления/получения ролей
     * @throws Exception
     */
    @Test
    public void testAPIRoles() throws Exception
    {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ROLES);

        Role role = getTestRole();
        String json = jsonMapper.writeValueAsString(role);
        // Добавление роли
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        // Роль добавлена в базу
        assertEquals("Добавление роли", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_ROLES));

        // Получение роли
        mockMvc.perform(get("/roles/" + role.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Получение списка ролей
        Role role2 = getTestRole();
        role2.setName("ROLE_TEST2");
        json = jsonMapper.writeValueAsString(role2);
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());

        role.setPermissions(null);
        role2.setPermissions(null);
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        roles.add(role2);
        json = jsonMapper.writeValueAsString(roles);
        mockMvc.perform(get("/roles")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));
        assertEquals("Получение списка ролей", 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_ROLES));

        // Удаление роли
        mockMvc.perform(delete("/roles/" + role2.getName())).andExpect(status().isOk());
        mockMvc.perform(get("/roles/" + role2.getName())).andExpect(status().isNotFound());
        assertEquals("Удаление роли", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_ROLES));

        // Изменение роли
        String role_name = role.getName();
        role.setName("ROLE_TEST3");
        role.setDescription("rename descr");
        role.setPermissions(Collections.emptyList());
        json = jsonMapper.writeValueAsString(role);
        mockMvc.perform(put("/roles/" + role_name).contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        mockMvc.perform(get("/roles/" + role.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));
        assertEquals("Изменение роли", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_ROLES));
    }

    /**
     * Тестирование API методов добавления/изменения/удаления/получения пермиссий
     * @throws Exception
     */
    @Test
    public void testAPIPermissions() throws Exception
    {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_PERMISSIONS);

        Permission permission = getTestPermission();
        String json = jsonMapper.writeValueAsString(permission);
        // Добавление пермиссии
        String id =
                mockMvc.perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Пермиссия добавлена в базу
        assertEquals("Добавление пермиссии", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_PERMISSIONS));

        // Получение пермиссии
        permission.setId(Integer.parseInt(id));
        json = jsonMapper.writeValueAsString(permission);
        mockMvc.perform(get("/permissions/" + id)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Получение списка пермиссий
        Permission permission2 = getTestPermission();
        permission2.setPath("/path2");
        permission2.setMethod(HttpMethod.POST);
        json = jsonMapper.writeValueAsString(permission2);
        id = mockMvc.perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        permission2.setId(Integer.parseInt(id));
        List<Permission> permissions = new ArrayList<>();
        permissions.add(permission);
        permissions.add(permission2);
        json = jsonMapper.writeValueAsString(permissions);
        mockMvc.perform(get("/permissions")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));
        assertEquals("Получение списка пермиссий", 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_PERMISSIONS));

        // Удаление пермиссии
        mockMvc.perform(delete("/permissions/" + id)).andExpect(status().isOk());
        mockMvc.perform(get("/permissions/" + id)).andExpect(status().isNotFound());
        assertEquals("Удаление пермиссии", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_PERMISSIONS));

        // Изменение пермиссии
        permission.setDescription("rename descr");
        permission.setMethod(HttpMethod.DELETE);
        permission.setPath("path3");
        json = jsonMapper.writeValueAsString(permission);
        mockMvc.perform(put("/permissions/" + permission.getId()).contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        mockMvc.perform(get("/permissions/" + permission.getId())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));
        assertEquals("Изменение пермиссии", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_PERMISSIONS));
    }

    /**
     * Тестирование API методов добавления/изменения/получения/удаления связей пользователей и ролей
     * @throws Exception
     */
    @Test
    public void testAPIUserRoles() throws Exception
    {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_USER_ROLES);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_USERS);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ROLES);

        // Добавление пользователя
        User user = getTestUser();
        String json = jsonMapper.writeValueAsString(user);
        String userId =
                mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        user.setId(Integer.parseInt(userId));

        // Добавление роли
        Role role = getTestRole();
        json = jsonMapper.writeValueAsString(role);
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());

        RoleNameList roleNames = new RoleNameList();
        roleNames.setRoles(Collections.singletonList(role.getName()));
        json = jsonMapper.writeValueAsString(roleNames);
        // Добавление связи роли и пользователя
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("userid", userId)).andExpect(status().isOk());

        // Получение списка ролей пользователя
        role.setPermissions(null);
        List<Role> roles = Collections.singletonList(role);
        json = jsonMapper.writeValueAsString(roles);
        mockMvc.perform(get("/roles").param("userid", userId)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Получение списка пользователей с указанной ролью
        user.setPassword(null);
        json = jsonMapper.writeValueAsString(Collections.singletonList(user));
        mockMvc.perform(get("/users").param("role", role.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Получение списка пользователей без ролей
        mockMvc.perform(get("/users").param("with_roles", String.valueOf(false))).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));
        user.setRoles(Collections.singletonList(role));
        json = jsonMapper.writeValueAsString(Collections.singletonList(user));

        // Получение списка пользователей с ролями
        mockMvc.perform(get("/users").param("with_roles", String.valueOf(true))).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Изменение ролей пользователя
        role = getTestRole();
        role.setName("ROLE_TEST2");
        json = jsonMapper.writeValueAsString(role);
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        roleNames.setRoles(Collections.singletonList(role.getName()));
        json = jsonMapper.writeValueAsString(roleNames);
        mockMvc.perform(put("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("userid", userId)).andExpect(status().isOk());
        role.setPermissions(null);
        roles = Collections.singletonList(role);
        json = jsonMapper.writeValueAsString(roles);
        mockMvc.perform(get("/roles").param("userid", userId)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Удаление связи роли и пользователя
        roleNames.setRoles(Collections.singletonList(role.getName()));
        json = jsonMapper.writeValueAsString(roleNames);
        mockMvc.perform(delete("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("userid", userId)).andExpect(status().isOk());
        mockMvc.perform(get("/roles").param("userid", userId)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json("[]"));
        assertEquals("Удаление связи пользователя и роли", 0, JdbcTestUtils.countRowsInTable(jdbcTemplate,
                TABLE_USER_ROLES));
    }

    /**
     * Тестирование API методов добавления/изменения/получения/удаления связей роли и пермиссий
     * @throws Exception
     */
    @Test
    public void testAPIRolePermissions() throws Exception
    {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ROLE_PERMISSIONS);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ROLES);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_PERMISSIONS);

        // Добавление пермиссии
        Permission permission = getTestPermission();
        String json = jsonMapper.writeValueAsString(permission);
        String permissionId =
                mockMvc.perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        permission.setId(Integer.parseInt(permissionId));

        // Добавление роли
        Role role = getTestRole();
        json = jsonMapper.writeValueAsString(role);
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());

        PermissionIdList permissionIdList = new PermissionIdList();
        permissionIdList.setIds(Collections.singletonList(permission.getId()));
        json = jsonMapper.writeValueAsString(permissionIdList);
        // Добавление связи роли и пермиссии
        mockMvc.perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("role", role.getName())).andExpect(status().isOk());

        // Получение списка пермиссий роли
        List<Permission> permissions = Collections.singletonList(permission);
        json = jsonMapper.writeValueAsString(permissions);
        mockMvc.perform(get("/permissions").param("role", role.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Изменение пермиссий роли
        permission.setPath("/test2");
        permission.setMethod(HttpMethod.POST);
        permission.setDescription("rename_desc");
        json = jsonMapper.writeValueAsString(permission);
        permissionId =
                mockMvc.perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        permission.setId(Integer.parseInt(permissionId));
        permissionIdList.setIds(Collections.singletonList(permission.getId()));
        json = jsonMapper.writeValueAsString(permissionIdList);
        mockMvc.perform(put("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("role", role.getName())).andExpect(status().isOk());

        permissions = Collections.singletonList(permission);
        json = jsonMapper.writeValueAsString(permissions);
        mockMvc.perform(get("/permissions").param("role", role.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Удаление связи роли и пермиссии
        permissionIdList.setIds(Collections.singletonList(permission.getId()));
        json = jsonMapper.writeValueAsString(permissionIdList);
        mockMvc.perform(delete("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("role", role.getName())).andExpect(status().isOk());
        mockMvc.perform(get("/permissions").param("role", role.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json("[]"));
        assertEquals("Удаление связи роли и пермиссии", 0, JdbcTestUtils.countRowsInTable(jdbcTemplate,
                TABLE_USER_ROLES));
    }

    /**
     * Тестирование API методов добавления/изменения/получения/удаления связей пермисси и ролей
     * @throws Exception
     */
    @Test
    public void testAPIPermissionRoles() throws Exception
    {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ROLE_PERMISSIONS);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ROLES);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_PERMISSIONS);

        // Добавление пермиссии
        Permission permission = getTestPermission();
        String json = jsonMapper.writeValueAsString(permission);
        String permissionId =
                mockMvc.perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        permission.setId(Integer.parseInt(permissionId));

        // Добавление роли
        Role role = getTestRole();
        json = jsonMapper.writeValueAsString(role);
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());

        RoleNameList roleNameList = new RoleNameList();
        roleNameList.setRoles(Collections.singletonList(role.getName()));
        json = jsonMapper.writeValueAsString(roleNameList);
        // Добавление связи пермиссии и роли
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("idpermission", permissionId)).andExpect(status().isOk());

        // Получение списка ролей пермиссии
        role.setPermissions(null);
        List<Role> roles = Collections.singletonList(role);
        json = jsonMapper.writeValueAsString(roles);
        mockMvc.perform(get("/roles").param("idpermission", permissionId)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Изменение ролей пермиссий
        role.setName("ROLE_TEST2");
        role.setDescription("rename_desc");
        json = jsonMapper.writeValueAsString(role);
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        roleNameList.setRoles(Collections.singletonList(role.getName()));
        json = jsonMapper.writeValueAsString(roleNameList);
        mockMvc.perform(put("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("idpermission", permissionId)).andExpect(status().isOk());
        roles = Collections.singletonList(role);
        json = jsonMapper.writeValueAsString(roles);
        mockMvc.perform(get("/roles").param("idpermission", permissionId)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json(json));

        // Удаление связи роли и пермиссии
        roleNameList.setRoles(Collections.singletonList(role.getName()));
        json = jsonMapper.writeValueAsString(roleNameList);
        mockMvc.perform(delete("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json).param("idpermission", permissionId)).andExpect(status().isOk());
        mockMvc.perform(get("/roles").param("idpermission", permissionId)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().
                json("[]"));
        assertEquals("Удаление связи пермиссии и роли", 0, JdbcTestUtils.countRowsInTable(jdbcTemplate,
                TABLE_USER_ROLES));
    }

    /**
     * Тестирование API методов добавления/получения/удаления связей LDAP групп и ролей
     * @throws Exception
     */
    @Test
    public void testLdapGroupRoles() throws Exception
    {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_LDAP_GROUP_ROLES);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ROLES);

        Role role = getTestRole();
        String json = jsonMapper.writeValueAsString(role);
        // Добавление роли
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        // Роль добавлена в базу
        assertEquals("Добавление роли", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_ROLES));
        // Получение роли
        mockMvc.perform(get("/roles/" + role.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().json(json));

        Role role2 = getTestRole();
        role2.setName("ROLE_TEST2");
        json = jsonMapper.writeValueAsString(role2);
        // Добавление роли
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        // Роль добавлена в базу
        assertEquals("Добавление роли", 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_ROLES));
        // Получение роли
        mockMvc.perform(get("/roles/" + role2.getName())).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().json(json));

        // Добавление связей LDAP группы и ролей
        LdapGroup ldapGroup = new LdapGroup();
        ldapGroup.setGroup("TEST_LDAP_GROUP");
        ldapGroup.setRoles(Collections.singletonList(role.getName()));

        json = jsonMapper.writeValueAsString(ldapGroup);
        mockMvc.perform(post("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        // Связи добавлены в базу
        assertEquals("Добавление связей LDAP группы и ролей", 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_LDAP_GROUP_ROLES));

        // Получение списка названий LDAP групп связанных с ролью
        json = jsonMapper.writeValueAsString(Collections.singletonList(ldapGroup.getGroup()));
        mockMvc.perform(get("/ldap/roles/" + role.getName())).andExpect(status().isOk()).andExpect(content().json(json));

        // Изменение связей LDAP группы и ролей
        List<String> roles = new ArrayList<>();
        roles.add(role.getName());
        roles.add(role2.getName());
        ldapGroup.setRoles(roles);
        json = jsonMapper.writeValueAsString(ldapGroup);
        mockMvc.perform(put("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        // Связи изменены в базе
        assertEquals("Добавление связей LDAP группы и ролей", 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_LDAP_GROUP_ROLES));

        // Получение связей LDAP группы и ролей
        LdapGroup ldapGroup2 = new LdapGroup();
        ldapGroup2.setGroup("TEST_LDAP_GROUP2");
        ldapGroup2.setRoles(Collections.singletonList(role.getName()));
        json = jsonMapper.writeValueAsString(ldapGroup2);
        mockMvc.perform(post("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());

        List<LdapGroup> ldapGroups = new ArrayList<>();
        ldapGroups.add(ldapGroup);
        ldapGroups.add(ldapGroup2);
        LdapGroupsResult ldapGroupsResult = new LdapGroupsResult();
        ldapGroupsResult.setGroups(ldapGroups);
        ldapGroupsResult.setCount(2);
        json = jsonMapper.writeValueAsString(ldapGroupsResult);
        mockMvc.perform(get("/ldap/groups/all")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().json(json));

        // Получение связей LDAP группы и ролей(1 страница из 1 элемента)
        ldapGroups.remove(1);
        json = jsonMapper.writeValueAsString(ldapGroupsResult);
        mockMvc.perform(get("/ldap/groups/all").param("pageSize", "1").param("pageNumber", "1"))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().json(json));

        // Получение списка ролей по назавнию LDAP группы
        json = jsonMapper.writeValueAsString(roles);
        mockMvc.perform(get("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(ldapGroup.getGroup())).andExpect(status().isOk()).andExpect(content().json(json));

        // Удаление ролей из LDAP группы
        ldapGroup.setRoles(Collections.singletonList(role2.getName()));
        json = jsonMapper.writeValueAsString(ldapGroup);
        mockMvc.perform(delete("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());

        // Получение списка ролей по назавнию LDAP группы
        json = jsonMapper.writeValueAsString(Collections.singletonList(role.getName()));
        mockMvc.perform(get("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(ldapGroup.getGroup())).andExpect(status().isOk()).andExpect(content().json(json));

        // Очистка всех связей LDAP группы с ролями
        ldapGroup.setRoles(null);
        json = jsonMapper.writeValueAsString(ldapGroup);
        mockMvc.perform(delete("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());
        ldapGroup2.setRoles(null);
        json = jsonMapper.writeValueAsString(ldapGroup2);
        mockMvc.perform(delete("/ldap/groups").contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8).content(json)).andExpect(status().isOk());

        // Связи удалены
        assertEquals("Очистка всех связей LDAP группы с ролями", 0, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_LDAP_GROUP_ROLES));
    }
}