package com.saicon.games.callcard.components;

import com.saicon.games.callcard.entity.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dynamic query manager for CallCard entities using Hibernate Criteria API.
 * Provides complex query capabilities beyond JPA named queries.
 *
 * Adapted from gameserver_v3 ErpDynamicQueryManager for CallCard microservice.
 * Contains only CallCard-related queries from the original implementation.
 */
public class ErpDynamicQueryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErpDynamicQueryManager.class);

    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getHibernateSession() {
        return entityManager.unwrap(Session.class);
    }

    /**
     * List CallCardTemplatePOS entries with filters.
     */
    @SuppressWarnings("unchecked")
    public List<CallCardTemplatePOS> listCallCardTemplatePOS(
            List<String> callCardTemplatePOSIds,
            List<String> callCardTemplateIds,
            List<String> refUserIds,
            Boolean mandatory,
            Boolean active,
            int rangeFrom,
            int rangeTo) {

        Criteria criteria = getHibernateSession().createCriteria(CallCardTemplatePOS.class, "callCardTPOS");

        if (callCardTemplatePOSIds != null && !callCardTemplatePOSIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardTPOS.callCardTemplatePOSId", callCardTemplatePOSIds));
        }

        if (callCardTemplateIds != null && !callCardTemplateIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardTPOS.callCardTemplateId.callCardTemplateId", callCardTemplateIds));
        }

        if (refUserIds != null && !refUserIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardTPOS.refUserId.userId", refUserIds));
        }

        if (mandatory != null) {
            criteria.add(Restrictions.eq("callCardTPOS.mandatory", mandatory));
        }

        if (active != null) {
            criteria.add(Restrictions.eq("callCardTPOS.active", active));
        }

        if (!(rangeFrom == 0 && rangeTo == -1)) {
            criteria.setFirstResult(rangeFrom);
            criteria.setMaxResults(rangeTo - rangeFrom);
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return criteria.list();
    }

    /**
     * Count CallCardTemplatePOS entries with filters.
     */
    public int countCallCardTemplatePOS(
            List<String> callCardTemplatePOSIds,
            List<String> callCardTemplateIds,
            List<String> refUserIds,
            Boolean mandatory,
            Boolean active) {

        Criteria criteria = getHibernateSession().createCriteria(CallCardTemplatePOS.class, "callCardTPOS");

        if (callCardTemplatePOSIds != null && !callCardTemplatePOSIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardTPOS.callCardTemplatePOSId", callCardTemplatePOSIds));
        }

        if (callCardTemplateIds != null && !callCardTemplateIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardTPOS.callCardTemplateId.callCardTemplateId", callCardTemplateIds));
        }

        if (refUserIds != null && !refUserIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardTPOS.refUserId.userId", refUserIds));
        }

        if (mandatory != null) {
            criteria.add(Restrictions.eq("callCardTPOS.mandatory", mandatory));
        }

        if (active != null) {
            criteria.add(Restrictions.eq("callCardTPOS.active", active));
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        Object result = criteria.uniqueResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    /**
     * List CallCardTemplate entries with filters.
     */
    @SuppressWarnings("unchecked")
    public List<CallCardTemplate> listCallCardTemplates(
            String userGroupId,
            String gameTypeId,
            List<String> callCardTemplateIds,
            boolean currentlyActive,
            Boolean active,
            String assignedToUserId,
            int rangeFrom,
            int rangeTo) {

        Criteria criteria = getHibernateSession().createCriteria(CallCardTemplate.class, "callCardT");

        if (callCardTemplateIds != null && !callCardTemplateIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardT.callCardTemplateId", callCardTemplateIds));
        }

        if (userGroupId != null && !userGroupId.isEmpty()) {
            criteria.createAlias("callCardT.userGroupId", "userGroup");
            criteria.add(Restrictions.eq("userGroup.groupId", userGroupId));
        }

        if (gameTypeId != null && !gameTypeId.isEmpty()) {
            criteria.createAlias("callCardT.gameTypeId", "gameType");
            criteria.add(Restrictions.eq("gameType.gameTypeId", gameTypeId));
        }

        if (active != null) {
            criteria.add(Restrictions.eq("callCardT.active", active));
        }

        if (currentlyActive) {
            criteria.add(Restrictions.lt("callCardT.startDate", new Date()));
            criteria.add(Restrictions.gt("callCardT.endDate", new Date()));
        }

        if (!(rangeFrom == 0 && rangeTo == -1)) {
            criteria.setFirstResult(rangeFrom);
            criteria.setMaxResults(rangeTo - rangeFrom);
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return criteria.list();
    }

    /**
     * Count CallCardTemplate entries with filters.
     */
    public int countCallCardTemplates(
            String userGroupId,
            String gameTypeId,
            List<String> callCardTemplateIds,
            boolean currentlyActive,
            Boolean active,
            String assignedToUserId) {

        Criteria criteria = getHibernateSession().createCriteria(CallCardTemplate.class, "callCardT");

        if (callCardTemplateIds != null && !callCardTemplateIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardT.callCardTemplateId", callCardTemplateIds));
        }

        if (userGroupId != null && !userGroupId.isEmpty()) {
            criteria.createAlias("callCardT.userGroupId", "userGroup");
            criteria.add(Restrictions.eq("userGroup.groupId", userGroupId));
        }

        if (gameTypeId != null && !gameTypeId.isEmpty()) {
            criteria.createAlias("callCardT.gameTypeId", "gameType");
            criteria.add(Restrictions.eq("gameType.gameTypeId", gameTypeId));
        }

        if (active != null) {
            criteria.add(Restrictions.eq("callCardT.active", active));
        }

        if (currentlyActive) {
            criteria.add(Restrictions.lt("callCardT.startDate", new Date()));
            criteria.add(Restrictions.gt("callCardT.endDate", new Date()));
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        Object result = criteria.uniqueResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    /**
     * List CallCardTemplate entries with metadata filter.
     * Overload that accepts List<KeyValueDTO> metadataFilter parameter.
     */
    @SuppressWarnings("unchecked")
    public List<CallCardTemplate> listCallCardTemplates(
            String userGroupId,
            String gameTypeId,
            List<String> callCardTemplateIds,
            List<com.saicon.multiplayer.dto.KeyValueDTO> metadataFilter,
            boolean currentlyActive,
            Boolean active,
            String assignedToUserId,
            int rangeFrom,
            int rangeTo) {

        Criteria criteria = getHibernateSession().createCriteria(CallCardTemplate.class, "callCardT");

        if (callCardTemplateIds != null && !callCardTemplateIds.isEmpty()) {
            criteria.add(Restrictions.in("callCardT.callCardTemplateId", callCardTemplateIds));
        }

        if (userGroupId != null && !userGroupId.isEmpty()) {
            criteria.createAlias("callCardT.userGroupId", "userGroup");
            criteria.add(Restrictions.eq("userGroup.groupId", userGroupId));
        }

        if (gameTypeId != null && !gameTypeId.isEmpty()) {
            criteria.createAlias("callCardT.gameTypeId", "gameType");
            criteria.add(Restrictions.eq("gameType.gameTypeId", gameTypeId));
        }

        if (active != null) {
            criteria.add(Restrictions.eq("callCardT.active", active));
        }

        if (currentlyActive) {
            criteria.add(Restrictions.lt("callCardT.startDate", new Date()));
            criteria.add(Restrictions.gt("callCardT.endDate", new Date()));
        }

        // TODO: Implement metadata filter logic when metadata structure is known
        // For now, metadata filter is ignored as it requires additional joins

        if (!(rangeFrom == 0 && rangeTo == -1)) {
            criteria.setFirstResult(rangeFrom);
            criteria.setMaxResults(rangeTo - rangeFrom);
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return criteria.list();
    }


    /**
     * List SalesOrderDetails entries with filters.
     * Stub implementation for CallCard microservice.
     * Returns empty list as SalesOrderDetails is not part of CallCard microservice.
     */
    @SuppressWarnings("unused")
    public List<Object> listSalesOrderDetails(
            Object salesOrderId,
            String productId,
            Object itemTypeId,
            Object itemId,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * List SalesOrder entries with filters.
     * Stub implementation for CallCard microservice.
     * Returns empty list as SalesOrder is not part of CallCard microservice.
     */
    @SuppressWarnings("unused")
    public List<Object> listSalesOrders(
            String gameTypeId,
            String userGroupId,
            String userId,
            String salesOrderId,
            String productId,
            Date dateFrom,
            Date dateTo,
            List<String> statusList,
            int status,
            String orderBy,
            boolean ascending,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * List CallCardRefUser entries with mixed list-based filters (overload 1).
     * Stub implementation for CallCard microservice.
     */
    @SuppressWarnings("unused")
    public List<CallCardRefUser> listCallCardRefUsers(
            String gameTypeId,
            String userGroupId,
            List<String> issuerUserIds,
            List<String> recipientUserIds,
            List<String> callCardIndexIds,
            Date dateFrom,
            Date dateTo,
            String orderBy,
            boolean ascending,
            String searchTerm,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * List CallCardRefUser entries with array-based filters (overload 2).
     * Stub implementation for CallCard microservice.
     */
    @SuppressWarnings("unused")
    public List<CallCardRefUser> listCallCardRefUsers(
            String gameTypeId,
            String userGroupId,
            String[] issuerUserIds,
            String[] recipientUserIds,
            String[] callCardIndexIds,
            Date dateFrom,
            Date dateTo,
            String orderBy,
            boolean ascending,
            String searchTerm,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * List CallCardRefUser entries with list-based filters (overload 3).
     * Stub implementation for CallCard microservice.
     */
    @SuppressWarnings("unused")
    public List<CallCardRefUser> listCallCardRefUsers(
            String gameTypeId,
            List<String> issuerUserIdList,
            String recipientUserId,
            String callCardTemplateId,
            String callCardId,
            Date dateCreatedFrom,
            Date dateCreatedTo,
            Date dateClaimedFrom,
            Date dateClaimedTo,
            Integer status,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * Count CallCardRefUser entries with array-based filters.
     * Stub implementation for CallCard microservice.
     */
    @SuppressWarnings("unused")
    public long countCallCardRefUsers(
            String gameTypeId,
            String userGroupId,
            String[] issuerUserIds,
            String[] recipientUserIds,
            String[] callCardIndexIds,
            Date dateFrom,
            Date dateTo,
            String orderBy,
            boolean ascending,
            String searchTerm) {
        return 0L;
    }

    /**
     * List CallCardRefUserIndex entries (overload 1 - 14 params variant).
     * Stub implementation for CallCard microservice.
     * Used when filtering by userId and propertyId.
     */
    @SuppressWarnings("unused")
    public List<CallCardRefUserIndex> listCallCardRefUserIndexes(
            String gameTypeId,
            String userGroupId,
            String userId,
            List<String> callCardIndexIdList,
            String issuerUserId,
            String recipientUserId,
            String propertyId,
            String orderBy,
            Boolean ascending,
            Date dateFrom,
            Date dateTo,
            List<Integer> statusList,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * List CallCardRefUserIndex entries (overload 2 - 14 params variant).
     * Stub implementation for CallCard microservice.
     * Used when filtering by callCardId.
     */
    @SuppressWarnings("unused")
    public List<CallCardRefUserIndex> listCallCardRefUserIndexes(
            String gameTypeId,
            String userGroupId,
            List<String> callCardIdList,
            String param4,
            String param5,
            String param6,
            String param7,
            String param8,
            String param9,
            String param10,
            String param11,
            String param12,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * List CallCardRefUserIndex entries (overload 3 - 14 params variant).
     * Stub implementation for CallCard microservice.
     * Used when filtering by callCardRefUserId.
     */
    @SuppressWarnings("unused")
    public List<CallCardRefUserIndex> listCallCardRefUserIndexes(
            String gameTypeId,
            List<String> callCardRefUserIdList,
            String param3,
            String param4,
            String param5,
            String param6,
            String param7,
            String param8,
            String param9,
            String param10,
            String param11,
            String param12,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * Count CallCard entries with list-based filters.
     * Stub implementation for CallCard microservice.
     */
    @SuppressWarnings("unused")
    public long countCallCards(
            List<String> gameTypeIdList,
            List<String> callCardIdList,
            List<String> userIdList,
            List<String> callCardTemplateIdList,
            Date dateFrom,
            Boolean includeDeleted,
            boolean isLive,
            boolean isRestricted,
            String searchTerm) {
        return 0L;
    }

    /**
     * List CallCard entries with filters (returns Object[] for flexible projection).
     * Stub implementation for CallCard microservice.
     */
    @SuppressWarnings("unused")
    public List<Object[]> listCallCards(
            String gameTypeId,
            List<String> callCardTemplateIds,
            String callCardStatus,
            String userGroupId,
            String applicationId,
            boolean includeCallCardTemplateData,
            boolean includeCallCardMetadata,
            boolean includeCallCardReferencedUsers,
            String sortOrder,
            int offset,
            int limit) {
        return new ArrayList<>();
    }

    /**
     * List CallCard entities with filters (returns List<CallCard>).
     * This is the primary method used by CallCardManagement.
     * Signature matches calls at lines 132 and 1597 in CallCardManagement.java.
     */
    @SuppressWarnings("unchecked")
    public List<CallCard> listCallCards(
            String userGroupId,
            List<String> userIdList,
            List<String> callCardIdList,
            List<String> callCardTemplateIdList,
            Date dateFrom,
            Boolean includeDeleted,
            boolean isLive,
            boolean isRestricted,
            String gameTypeId,
            int offset,
            int limit) {

        Criteria criteria = getHibernateSession().createCriteria(CallCard.class, "callCard");

        if (userGroupId != null && !userGroupId.isEmpty()) {
            criteria.createAlias("callCard.userGroupId", "userGroup");
            criteria.add(Restrictions.eq("userGroup.groupId", userGroupId));
        }

        if (gameTypeId != null && !gameTypeId.isEmpty()) {
            criteria.createAlias("callCard.gameTypeId", "gameType");
            criteria.add(Restrictions.eq("gameType.gameTypeId", gameTypeId));
        }

        if (userIdList != null && !userIdList.isEmpty()) {
            criteria.createAlias("callCard.userId", "user");
            criteria.add(Restrictions.in("user.userId", userIdList));
        }

        if (callCardIdList != null && !callCardIdList.isEmpty()) {
            criteria.add(Restrictions.in("callCard.callCardId", callCardIdList));
        }

        if (callCardTemplateIdList != null && !callCardTemplateIdList.isEmpty()) {
            criteria.createAlias("callCard.callCardTemplateId", "template");
            criteria.add(Restrictions.in("template.callCardTemplateId", callCardTemplateIdList));
        }

        if (dateFrom != null) {
            criteria.add(Restrictions.ge("callCard.dateCreated", dateFrom));
        }

        if (includeDeleted != null && !includeDeleted) {
            criteria.add(Restrictions.eq("callCard.deleted", false));
        }

        if (isLive) {
            criteria.add(Restrictions.eq("callCard.live", true));
        }

        if (isRestricted) {
            criteria.add(Restrictions.eq("callCard.restricted", true));
        }

        if (!(offset == 0 && limit == -1)) {
            criteria.setFirstResult(offset);
            if (limit > 0) {
                criteria.setMaxResults(limit);
            }
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return criteria.list();
    }
}
