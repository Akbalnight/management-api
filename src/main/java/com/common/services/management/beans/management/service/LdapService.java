package com.common.services.management.beans.management.service;

import com.common.services.management.beans.management.model.LdapPermissions;
import com.common.services.management.beans.serv.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import javax.naming.directory.Attributes;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * LdapService.java
 * Date: 14 нояб. 2018 г.
 * Users: amatveev
 * Description: Сервис для работы с LDAP
 */
@Component
public class LdapService
{
    private static final String ERROR_LDAP_CONNECTION = "error.ldap.connectionError";
    @Value("${ldap.url:}")
    private String url;
    @Value("${ldap.base:}")
    private String base;
    @Autowired
    private ServiceException serviceException;

    /**
     * Подключение к LDAP
     * @param ldap параметры подключения
     * @return LdapTemplate объект
     */
    private LdapTemplate configureLdapTemplate(LdapPermissions ldap)
    {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setBase(base);
        contextSource.setUserDn(ldap.getUserDn());
        contextSource.setPassword(ldap.getPassword());
        contextSource.afterPropertiesSet();
        return new LdapTemplate(contextSource);
    }

    /**
     * Возвращает список пользователей
     * @param ldapPermissions параметры подключения
     * @return
     */
    public List<String> getAllUsers(LdapPermissions ldapPermissions)
    {
        try
        {
            LdapTemplate ldapTemplate = configureLdapTemplate(ldapPermissions);
            return ldapTemplate.search(query().where("objectclass").is("person"), new AttributesMapper<String>()
            {
                @Override
                public String mapFromAttributes(Attributes attributes) throws javax.naming.NamingException
                {
                    String username = attributes.get("uid").get().toString();
                    return username;
                }
            });
        }
        catch (Throwable e)
        {
            throw serviceException.applyParameters(ERROR_LDAP_CONNECTION, e);
        }
    }
}

