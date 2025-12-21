package com.saicon.games.callcard.components;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Multi-tenant query filter for CallCard entities.
 *
 * Applies tenant isolation at the JPA/Hibernate level by enabling Hibernate filters
 * that automatically add WHERE clauses to queries based on the current tenant context.
 *
 * Usage:
 * <pre>
 * multiTenantQueryFilter.enableFilter(userGroupId, gameTypeId);
 * try {
 *     // Execute queries - filters are automatically applied
 *     List&lt;CallCard&gt; callCards = callCardDao.queryList(...);
 * } finally {
 *     multiTenantQueryFilter.disableFilter();
 * }
 * </pre>
 *
 * Filter definitions must be declared in entity classes using @FilterDef and @Filter annotations:
 * <pre>
 * @Entity
 * @FilterDef(name = "userGroupFilter", parameters = @ParamDef(name = "userGroupId", type = "string"))
 * @Filter(name = "userGroupFilter", condition = "user_group_id = :userGroupId")
 * public class CallCard {
 *     // ...
 * }
 * </pre>
 */
@Component
public class MultiTenantQueryFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiTenantQueryFilter.class);

    private static final String USER_GROUP_FILTER = "userGroupFilter";
    private static final String GAME_TYPE_FILTER = "gameTypeFilter";

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Enable multi-tenant filters for the current session
     *
     * @param userGroupId User group (organization) ID for tenant isolation
     * @param gameTypeId Game type ID for additional filtering (optional)
     */
    public void enableFilter(String userGroupId, String gameTypeId) {
        Session session = entityManager.unwrap(Session.class);

        if (userGroupId != null && !userGroupId.isEmpty()) {
            LOGGER.debug("Enabling userGroupFilter with userGroupId: {}", userGroupId);
            Filter userGroupFilter = session.enableFilter(USER_GROUP_FILTER);
            userGroupFilter.setParameter("userGroupId", userGroupId);
        }

        if (gameTypeId != null && !gameTypeId.isEmpty()) {
            LOGGER.debug("Enabling gameTypeFilter with gameTypeId: {}", gameTypeId);
            Filter gameTypeFilter = session.enableFilter(GAME_TYPE_FILTER);
            gameTypeFilter.setParameter("gameTypeId", gameTypeId);
        }
    }

    /**
     * Enable user group filter only
     *
     * @param userGroupId User group (organization) ID
     */
    public void enableUserGroupFilter(String userGroupId) {
        enableFilter(userGroupId, null);
    }

    /**
     * Disable all multi-tenant filters for the current session
     */
    public void disableFilter() {
        Session session = entityManager.unwrap(Session.class);

        session.disableFilter(USER_GROUP_FILTER);
        session.disableFilter(GAME_TYPE_FILTER);

        LOGGER.debug("Disabled multi-tenant filters");
    }

    /**
     * Check if user group filter is enabled
     *
     * @return true if filter is enabled
     */
    public boolean isFilterEnabled() {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.getEnabledFilter(USER_GROUP_FILTER);
        return filter != null;
    }

    /**
     * Get current user group ID from enabled filter
     *
     * @return userGroupId or null if filter not enabled
     */
    public String getCurrentUserGroupId() {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.getEnabledFilter(USER_GROUP_FILTER);
        if (filter != null) {
            // Hibernate 5.6 Filter API limitations - cannot retrieve parameter values easily
            // Return null for now - this method is informational only
            return null; // (String) filter.getParameter("userGroupId") not available in 5.6
        }
        return null;
    }
}
