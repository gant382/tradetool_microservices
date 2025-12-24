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

@Configuration
public class ComponentConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    // Component Layer Beans
    @Bean
    public ICallCardManagement callCardManagement() {
        CallCardManagement management = new CallCardManagement();
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
        management.setErpDynamicQueryManager(erpDynamicQueryManager());
        management.setErpNativeQueryManager(erpNativeQueryManager());
        return management;
    }

    // Service Layer Beans
    @Bean
    public ICallCardService callCardService() {
        CallCardService service = new CallCardService();
        service.setCallCardManagement(callCardManagement());
        return service;
    }

    @Bean
    public com.saicon.games.callcard.ws.ICallCardStatisticsService callCardStatisticsService() {
        com.saicon.games.callcard.service.CallCardStatisticsService service =
            new com.saicon.games.callcard.service.CallCardStatisticsService();
        service.setCallCardManagement(callCardManagement());
        return service;
    }

    @Bean
    public com.saicon.games.callcard.ws.ISimplifiedCallCardService simplifiedCallCardService() {
        com.saicon.games.callcard.service.SimplifiedCallCardService service =
            new com.saicon.games.callcard.service.SimplifiedCallCardService();
        service.setCallCardManagement(callCardManagement());
        return service;
    }

    // Query Manager Beans
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

    // DAO Beans - CallCard Entities
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

    // DAO Beans - Supporting Entities
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

    // Stub Beans for External Dependencies
    @Bean
    public com.saicon.games.callcard.components.external.IMetadataComponent metadataComponent() {
        return (itemTypeId, activeOnly) -> java.util.Collections.emptyList();
    }

    @Bean
    public com.saicon.games.callcard.components.external.IUserMetadataComponent userMetadataComponent() {
        return (userIds, metadataKeys, activeOnly) -> java.util.Collections.emptyMap();
    }

    @Bean
    public com.saicon.games.callcard.components.external.SolrClient solrClient() {
        return new com.saicon.games.callcard.components.external.SolrClient();
    }

    @Bean
    public com.saicon.games.callcard.components.external.GeneratedEventsDispatcher generatedEventsDispatcher() {
        return new com.saicon.games.callcard.components.external.GeneratedEventsDispatcher();
    }

    // Security Beans
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        return username -> {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                "User authentication not yet implemented in microservice"
            );
        };
    }
    // External Service Stubs
    @Bean
    public com.saicon.games.callcard.ws.external.IGameService gameService() {
        return () -> "PONG - Stub Service";
    }

    @Bean
    public com.saicon.games.callcard.ws.external.IGameInternalService gameInternalService() {
        return new com.saicon.games.callcard.ws.external.IGameInternalService() {
            @Override
            public com.saicon.games.callcard.ws.external.UserSessionDTOS getUserSession(String userSessionId) {
                return null; // Stub - returns null session
            }
            @Override
            public com.saicon.games.callcard.ws.external.UserSessionDTOS getActiveUserSession(String userSessionId) {
                return null; // Stub - returns null session
            }
        };
    }

    // Transaction Service Beans
    @Bean
    public com.saicon.games.callcard.components.ICallCardTransactionManagement transactionManagement() {
        return new com.saicon.games.callcard.components.ICallCardTransactionManagement() {
            @Override
            public com.saicon.games.callcard.entity.CallCardTransaction recordTransaction(String callCardId, com.saicon.games.callcard.entity.CallCardTransactionType transactionType, Integer userId, Integer userGroupId, String oldValue, String newValue, String description, String ipAddress, String sessionId) { return null; }
            @Override
            public com.saicon.games.callcard.entity.CallCardTransaction recordCreate(com.saicon.games.callcard.entity.CallCard callCard, Integer userId, String ipAddress, String sessionId) { return null; }
            @Override
            public com.saicon.games.callcard.entity.CallCardTransaction recordUpdate(com.saicon.games.callcard.entity.CallCard oldCallCard, com.saicon.games.callcard.entity.CallCard newCallCard, Integer userId, String ipAddress, String sessionId) { return null; }
            @Override
            public com.saicon.games.callcard.entity.CallCardTransaction recordDelete(com.saicon.games.callcard.entity.CallCard callCard, Integer userId, String ipAddress, String sessionId) { return null; }
            @Override
            public java.util.List<com.saicon.games.callcard.entity.CallCardTransaction> findByCallCardId(String callCardId, Integer userGroupId, Integer pageNumber, Integer pageSize) { return java.util.Collections.emptyList(); }
            @Override
            public Long countByCallCardId(String callCardId, Integer userGroupId) { return 0L; }
            @Override
            public java.util.List<com.saicon.games.callcard.entity.CallCardTransaction> findByUserId(Integer userId, Integer userGroupId, java.util.Date dateFrom, java.util.Date dateTo, Integer pageNumber, Integer pageSize) { return java.util.Collections.emptyList(); }
            @Override
            public Long countByUserId(Integer userId, Integer userGroupId, java.util.Date dateFrom, java.util.Date dateTo) { return 0L; }
            @Override
            public java.util.List<com.saicon.games.callcard.entity.CallCardTransaction> findByType(com.saicon.games.callcard.entity.CallCardTransactionType transactionType, Integer userGroupId, java.util.Date dateFrom, java.util.Date dateTo, Integer pageNumber, Integer pageSize) { return java.util.Collections.emptyList(); }
            @Override
            public Long countByType(com.saicon.games.callcard.entity.CallCardTransactionType transactionType, Integer userGroupId, java.util.Date dateFrom, java.util.Date dateTo) { return 0L; }
            @Override
            public java.util.List<com.saicon.games.callcard.entity.CallCardTransaction> searchTransactions(com.saicon.games.callcard.ws.dto.TransactionSearchCriteriaDTO criteria) { return java.util.Collections.emptyList(); }
            @Override
            public Long countSearchResults(com.saicon.games.callcard.ws.dto.TransactionSearchCriteriaDTO criteria) { return 0L; }
            @Override
            public com.saicon.games.callcard.entity.CallCardTransaction findById(String transactionId, Integer userGroupId) { return null; }
            @Override
            public java.util.List<com.saicon.games.callcard.entity.CallCardTransaction> findRecent(Integer userGroupId, Integer limit) { return java.util.Collections.emptyList(); }
            @Override
            public java.util.List<com.saicon.games.callcard.entity.CallCardTransaction> findBySessionId(String sessionId, Integer userGroupId) { return java.util.Collections.emptyList(); }
            @Override
            public String serializeCallCard(com.saicon.games.callcard.entity.CallCard callCard) { return "{}"; }
            @Override
            public String detectChanges(com.saicon.games.callcard.entity.CallCard oldCallCard, com.saicon.games.callcard.entity.CallCard newCallCard) { return ""; }
        };
    }

    @Bean
    public com.saicon.games.callcard.ws.ICallCardTransactionService callCardTransactionService() {
        com.saicon.games.callcard.service.CallCardTransactionService service = new com.saicon.games.callcard.service.CallCardTransactionService();
        service.setTransactionManagement(transactionManagement());
        return service;
    }
}
