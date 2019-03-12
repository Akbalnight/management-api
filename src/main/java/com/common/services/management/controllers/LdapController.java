package com.common.services.management.controllers;


import com.common.services.management.beans.management.model.LdapGroup;
import com.common.services.management.beans.management.model.LdapGroupsResult;
import com.common.services.management.beans.management.service.UsersManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LdapController.java
 * Date: 11 янв. 2019 г.
 * Users: amatveev
 * Description: Контроллер для работы с группами LDAP
 */
@RestController
@RequestMapping(path = "ldap")
@Api(value = "Api for LdapController", description = "Контроллер для работы с группами LDAP.")
public class LdapController
{
    @Autowired
    private UsersManagementService usersManagementService;

    /**
     * Возвращает список ролей, соответствующих указанной LDAP группе
     * @param group название LDAP группы
     * @return возвращает список ролей, соответствующих указанной LDAP группе
     */
    @ApiOperation(value = "Получение списка ролей, соответствующих указанной LDAP группе")
    @GetMapping(value = "/groups")
    public List<String> getRolesByGroup(@ApiParam(value = "Название LDAP группы", required = true)
                                   @RequestBody(required = false) String group)
    {
        return usersManagementService.getRolesByLdapGroup(group);
    }

    /**
     * Получение LDAP групп со списками соответсвующих им ролей
     * @return Возвращает список LDAP групп со списками ролей
     */
    @ApiOperation(value = "Получение LDAP групп со списками соответсвующих им ролей")
    @GetMapping(value = "/groups/all")
    public LdapGroupsResult getAllGroups(@ApiParam(value = "Размер страницы")
                                   @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                            @ApiParam(value = "Номер страницы")
                                   @RequestParam(value = "pageNumber", required = false) Integer pageNumber)
    {
        return usersManagementService.getLdapGroups(pageNumber, pageSize);
    }

    /**
     * Возвращает список LDAP групп с указанной ролью
     */
    @ApiOperation(value = "Получение списка названий LDAP групп с указанной ролью")
    @GetMapping(value = "/roles/{role}")
    public List<String> getGroupsByRole(@ApiParam(value = "Название роли", required = true) @PathVariable String role)
    {
        return usersManagementService.getLdapGroupsByRole(role);
    }

    /**
     * Добавление ролей указанной LDAP группе
     */
    @PostMapping(value = "/groups")
    @ApiOperation(value = "Добавление ролей указанной LDAP группе")
    public void addRolesToGroup(@ApiParam(value = "Название LDAP группы и список ролей", required = true) @RequestBody LdapGroup ldapGroup)
    {
        usersManagementService.addRolesToLdapGroup(ldapGroup.getGroup(), ldapGroup.getRoles());
    }

    /**
     * Установка ролей указанной LDAP группе
     */
    @PutMapping(value = "/groups")
    @ApiOperation(value = "Установка ролей указанной LDAP группе", notes = "Не указанные в списке роли будут откреплены от LDAP группы")
    public void setRolesToGroup(@ApiParam(value = "Название LDAP группы и список ролей", required = true) @RequestBody LdapGroup ldapGroup)
    {
        usersManagementService.setRolesToLdapGroup(ldapGroup.getGroup(), ldapGroup.getRoles());
    }

    /**
     * Очищает список ролей указанной LDAP группы
     */
    @DeleteMapping(value = "/groups")
    @ApiOperation(value = "Удаление ролей из указанной LDAP группы", notes = "Если список roles не указан, будут удалены все связи LDAP группы с ролями")
    public void removeRolesFromGroup(@ApiParam(value = "Название LDAP группы и список ролей", required = true) @RequestBody LdapGroup ldapGroup)
    {
        if (ldapGroup.getRoles() == null)
        {
            usersManagementService.clearLdapGroup(ldapGroup.getGroup());
        }
        else
        {
            usersManagementService.removeRolesFromLdapGroup(ldapGroup.getGroup(), ldapGroup.getRoles());
        }
    }
}
