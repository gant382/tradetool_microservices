package com.saicon.games.callcard.components;

import com.saicon.games.callcard.entity.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
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
}
