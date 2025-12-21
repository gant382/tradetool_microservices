package com.saicon.callcard.config;

import com.saicon.games.callcard.components.ICallCardManagement;
import com.saicon.games.callcard.components.impl.CallCardManagement;
import com.saicon.games.callcard.components.ErpDynamicQueryManager;
import com.saicon.games.callcard.components.ErpNativeQueryManager;
import com.saicon.games.callcard.dao.GenericDAO;
import com.saicon.games.callcard.dao.IGenericDAO;
import com.saicon.games.callcard.entity.*;
import com.saicon.games.entities.shared.*;
import com.saicon.games.callcard.service.CallCardService;
import com.saicon.games.callcard.ws.ICallCardService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Spring Bean Configuration for CallCard microservice components.
 *
 * Defines:
 * - Component layer beans (CallCardManagement)
 * - Service layer beans (CallCardService)
 * - DAO beans for all entities
 * - Query managers (Dynamic and Native)
 */
@Configuration
public class ComponentConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    // ============================================================
    // Component Layer Beans
    // ============================================================

    @Bean
    public ICallCardManagement callCardManagement() {
        CallCardManagement management = new CallCardManagement();

        // Inject DAOs
        management.setCallCardDao(callCardDao());
        management.setCallCardTemplateDao(callCardTemplateDao());
        management.setCallCardTemplatePOSDao(callCardTemplatePOSDao());
        management.setCallCardTemplateEntryDao(callCardTemplateEntryDao());
        management.setCallCardTemplateUserReferencesDao(callCardTemplateUserReferencesDao());
        management.setCallCardRefUserDao(callCardRefUserDao());
        management.setCallCardRefUserIndexDao(callCardRefUserIndexDao());
        management.setUsersDao(usersDao());
        management.setItemTypesDao(itemTypesDao());
        management.setApplicationDao(applicationDao());
        management.setPostcodeDao(postcodeDao());

        // Inject query managers
        management.setErpDynamicQueryManager(erpDynamicQueryManager());
        management.setErpNativeQueryManager(erpNativeQueryManager());

        // Note: Other dependencies (salesOrderManagement, addressbookManagement, etc.)
        // will need to be configured when those modules are integrated

        return management;
    }

    // ============================================================
    // Service Layer Beans
    // ============================================================

    @Bean
    public ICallCardService callCardService() {
        CallCardService service = new CallCardService();
        service.setCallCardManagement(callCardManagement());
        return service;
    }

    // ============================================================
    // Query Manager Beans
    // ============================================================

    @Bean
    public ErpDynamicQueryManager erpDynamicQueryManager() {
        ErpDynamicQueryManager manager = new ErpDynamicQueryManager();
        manager.setEntityManager(entityManager);
        return manager;
    }

    @Bean
    public ErpNativeQueryManager erpNativeQueryManager() {
        ErpNativeQueryManager manager = new ErpNativeQueryManager();
        manager.setEntityManager(entityManager);
        return manager;
    }

    // ============================================================
    // DAO Beans - CallCard Entities
    // ============================================================

    @Bean
    public IGenericDAO<CallCard, String> callCardDao() {
        return new GenericDAO<>(CallCard.class, entityManager);
    }

    @Bean
    public IGenericDAO<CallCardTemplate, String> callCardTemplateDao() {
        return new GenericDAO<>(CallCardTemplate.class, entityManager);
    }

    @Bean
    public IGenericDAO<CallCardTemplatePOS, String> callCardTemplatePOSDao() {
        return new GenericDAO<>(CallCardTemplatePOS.class, entityManager);
    }

    @Bean
    public IGenericDAO<CallCardTemplateEntry, String> callCardTemplateEntryDao() {
        return new GenericDAO<>(CallCardTemplateEntry.class, entityManager);
    }

    @Bean
    public IGenericDAO<CallCardTemplateUserReferences, String> callCardTemplateUserReferencesDao() {
        return new GenericDAO<>(CallCardTemplateUserReferences.class, entityManager);
    }

    @Bean
    public IGenericDAO<CallCardRefUser, String> callCardRefUserDao() {
        return new GenericDAO<>(CallCardRefUser.class, entityManager);
    }

    @Bean
    public IGenericDAO<CallCardRefUserIndex, String> callCardRefUserIndexDao() {
        return new GenericDAO<>(CallCardRefUserIndex.class, entityManager);
    }

    // ============================================================
    // DAO Beans - Supporting Entities
    // ============================================================

    @Bean
    public IGenericDAO<Users, String> usersDao() {
        return new GenericDAO<>(Users.class, entityManager);
    }

    @Bean
    public IGenericDAO<ItemTypes, Integer> itemTypesDao() {
        return new GenericDAO<>(ItemTypes.class, entityManager);
    }

    @Bean
    public IGenericDAO<Application, String> applicationDao() {
        return new GenericDAO<>(Application.class, entityManager);
    }

    @Bean
    public IGenericDAO<Postcode, Integer> postcodeDao() {
        return new GenericDAO<>(Postcode.class, entityManager);
    }
}
