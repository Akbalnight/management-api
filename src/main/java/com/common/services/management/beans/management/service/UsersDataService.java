package com.common.services.management.beans.management.service;

import com.common.services.management.beans.management.model.LdapGroupGenerationObject;
import com.common.services.management.beans.management.model.UserGenerationObject;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * UsersDataService.java
 * Date: 11 окт. 2018 г.
 * Users: vmeshkov
 * Description: Интерфейс сервиса управления данными (обьектами) пользователя
 */
public interface UsersDataService
{
    /**
     * Добавляет объекты пользователя
     * @param userId id пользователя
     * @param objects список объектов
     */
    void addUserObjects(Integer userId, List<Integer> objects);

    /**
     * Устанавливает объекты пользователя
     * @param userId id пользователя
     * @param objects список объектов
     */
    void setUserObjects(Integer userId, List<Integer> objects);

    /**
     * Возвращает список объектов пользователя
     * @param userId id пользователя
     * @return возвращает список объектов пользователя
     */
    List<UserGenerationObject> getUserObjects(Integer userId);

    /**
     * Устанавливает объекты сервисов для указанной роли
     * @param role    название роли
     * @param objects объекты сервисов
     */
    void setRoleObjects(String role, Map<String, String> objects);

    /**
     * Удаляет объекты сервисов указанной роли
     * @param role название роли
     */
    void removeRoleObjects(String role);

    /**
     * Удаляет указанные объекты у пользователя
     * @param userId  id пользователя
     * @param objects объекты пользователя
     */
    void removeUserObjects(Integer userId, List<Integer> objects);

    /**
     * Обновляет связь пользователя и объекта по указанной id записи
     * @param rowId id записи
     * @param userId id пользователя
     * @param object объект
     */
    void updateUserObjectByRowId(Integer rowId, Integer userId, Integer object);

    /**
     * Возвращает связь пользователя и объекта по указанной id записи
     * @param rowId id записи
     * @return возвращает связь пользователя и объекта по указанной id записи
     */
    UserGenerationObject getUserObjectByRowId(Integer rowId);

    /**
     * Возвращает настройки пользователя
     * @return Настройки пользователя
     */
    JsonNode getUserSettings();

    /**
     * Сохраняет настройки пользователя
     * @param settings настройки пользователя
     */
    void setUserSettings(JsonNode settings);

    /**
     * Получение всех связей LDAP групп с объектами генерации
     * @return возвращает все связи LDAP групп с объектами генерации
     */
    List<LdapGroupGenerationObject> getLdapGroupGenerationObjects();

    /**
     * Получение LDAP группы, связанной с указанным объектом генерации
     * @param generationObjectId id объекта генерации
     * @return возвращает название LDAP группы
     */
    String getLdapGroupByGenerationObjectId(int generationObjectId);

    /**
     * Получение id объекта генерации, связанного с указанной LDAP группой
     * @param group название LDAP группы
     * @return возвращает id объекта генерации
     */
    int getGenerationObjectIdByLdapGroup(String group);

    /**
     * Установка связи LDAP группы и объекта генерации
     * @param ldapGroup          название LDAP группы
     * @param generationObjectId id объекта генерации
     */
    void setGenerationObjectIdToLdapGroup(String ldapGroup, int generationObjectId);

    /**
     * Удаление связи объекта генерации с указанной LDAP группой
     * @param ldapGroup          название LDAP группы
     * @param generationObjectId id объекта генерации
     */
    void removeGenerationObjectFromLdapGroup(String ldapGroup, int generationObjectId);
}
