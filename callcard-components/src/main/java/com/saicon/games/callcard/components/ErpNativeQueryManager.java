package com.saicon.games.callcard.components;

import com.saicon.games.callcard.entity.CallCardRefUserIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * Native SQL query manager for CallCard entities.
 * Provides complex SQL queries that cannot be expressed easily with JPA or Criteria API.
 *
 * Adapted from gameserver_v3 ErpNativeQueryManager for CallCard microservice.
 * Contains only CallCard-related native queries from the original implementation.
 */
public class ErpNativeQueryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErpNativeQueryManager.class);

    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * List previous CallCardRefUserIndex values for given users.
     * Uses window functions to get the most recent N entries per user/item/property combination.
     *
     * @param userId         Filter by call card owner user ID
     * @param refUserIds     Filter by reference user IDs
     * @param limit          Maximum number of entries per partition
     * @param activeCallCards Filter by active call cards only
     * @return Map of refUserId to list of CallCardRefUserIndex entries
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<CallCardRefUserIndex>> listCallCardRefUserIndexesPreviousValues(
            String userId,
            List<String> refUserIds,
            Integer limit,
            Boolean activeCallCards) {

        Map<String, List<CallCardRefUserIndex>> results = new HashMap<>();

        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT * FROM ( ");
        queryStr.append("SELECT call_card_refuser.ref_user_id as refUserId, call_card_refuser_index.*, ");
        queryStr.append("ROW_NUMBER() OVER (PARTITION BY call_card_refuser.ref_user_id, call_card_refuser_index.item_id, call_card_refuser_index.property_id ORDER BY call_card_refuser_index.submit_date DESC) AS row ");
        queryStr.append("FROM call_card_refuser_index, call_card_refuser, call_card ");
        queryStr.append("WHERE call_card_refuser.call_card_id = call_card.call_card_id ");
        queryStr.append("AND call_card_refuser.call_card_refuser_id = call_card_refuser_index.call_card_refuser_id ");

        if (userId != null) {
            queryStr.append("AND call_card.user_id = '").append(userId).append("' ");
        }

        if (refUserIds != null && !refUserIds.isEmpty()) {
            queryStr.append("AND call_card_refuser.ref_user_id IN (");
            for (int i = 0; i < refUserIds.size(); i++) {
                if (i > 0) queryStr.append(", ");
                queryStr.append("'").append(refUserIds.get(i)).append("'");
            }
            queryStr.append(") ");
        }

        if (activeCallCards != null) {
            queryStr.append("AND call_card.active = ").append(activeCallCards ? "1" : "0").append(" ");
        }

        queryStr.append(") as res WHERE res.row <= ").append(limit);
        queryStr.append(" ORDER BY res.refUserId DESC, res.item_id DESC, res.property_id DESC, res.submit_date DESC");

        try {
            List<Object[]> resultList = entityManager.createNativeQuery(queryStr.toString(), "RefUserIndexMapping").getResultList();

            for (Object[] row : resultList) {
                CallCardRefUserIndex index = (CallCardRefUserIndex) row[0];
                String refUserId = (String) row[1];

                results.computeIfAbsent(refUserId, k -> new ArrayList<>()).add(index);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing native query for CallCardRefUserIndex previous values", e);
        }

        return results;
    }

    /**
     * List previous sales order details for given users.
     * Placeholder for future sales order integration.
     *
     * @param userId           Filter by call card owner user ID
     * @param refUserIds       Filter by reference user IDs
     * @param limit            Maximum number of entries per partition
     * @param activeCallCards  Filter by active call cards only
     * @param includeRevisions Include revised sales orders
     * @return Map of refUserId to list of SalesOrderDetails
     */
    public Map<String, List<Object>> listSalesOrderDetailsPreviousValues(
            String userId,
            List<String> refUserIds,
            Integer limit,
            Boolean activeCallCards,
            Boolean includeRevisions) {

        LOGGER.warn("listSalesOrderDetailsPreviousValues is not yet implemented - sales order integration pending");
        return new HashMap<>();
    }

    /**
     * List previous invoice details for given users.
     * Placeholder for future invoice integration.
     *
     * @param userId          Filter by call card owner user ID
     * @param refUserIds      Filter by reference user IDs
     * @param limit           Maximum number of entries per partition
     * @param activeCallCards Filter by active call cards only
     * @return Map of refUserId to list of InvoiceDetails
     */
    public Map<String, List<Object>> listInvoiceDetailsPreviousValues(
            String userId,
            List<String> refUserIds,
            Integer limit,
            Boolean activeCallCards) {

        LOGGER.warn("listInvoiceDetailsPreviousValues is not yet implemented - invoice integration pending");
        return new HashMap<>();
    }

    /**
     * Execute a native SQL query with named parameters.
     * Stub implementation for CallCard microservice.
     *
     * @param query       Native SQL query string
     * @param paramNames  Parameter names
     * @param paramValues Parameter values
     * @return Query results as Object arrays
     */
    @SuppressWarnings("unused")
    public List<Object[]> executeNativeQuery(String query, String[] paramNames, Object[] paramValues) {
        LOGGER.warn("executeNativeQuery is a stub implementation");
        return new ArrayList<>();
    }

    /**
     * List invoice details summaries with aggregation.
     * Stub implementation for CallCard microservice.
     *
     * @param userGroupId          Filter by user group
     * @param callCardIds          Filter by call card IDs
     * @param limit                Maximum results per partition
     * @param previousVisitsCounts Previous visits count filter
     * @return Summary results as Object arrays
     */
    @SuppressWarnings("unused")
    public List<Object[]> listInvoiceDetailsSummaries(
            String userGroupId,
            List<String> callCardIds,
            Integer limit,
            List<Integer> previousVisitsCounts) {
        LOGGER.warn("listInvoiceDetailsSummaries is a stub implementation");
        return new ArrayList<>();
    }

    /**
     * List CallCardRefUserIndex previous values summary with aggregation.
     * Stub implementation for CallCard microservice.
     *
     * @param userGroupId         Filter by user group
     * @param callCardIds         Filter by call card IDs
     * @param metadataKeys        Metadata keys to include
     * @param limit               Maximum results per partition
     * @param previousVisitsCounts Previous visits count filter
     * @param includeGeoInfo      Include geographical information
     * @return Summary results as Object arrays
     */
    @SuppressWarnings("unused")
    public List<Object[]> listCallCardRefUserIndexesPreviousValuesSummary(
            String userGroupId,
            List<String> callCardIds,
            List<String> metadataKeys,
            Integer limit,
            List<Integer> previousVisitsCounts,
            boolean includeGeoInfo) {
        LOGGER.warn("listCallCardRefUserIndexesPreviousValuesSummary is a stub implementation");
        return new ArrayList<>();
    }
}
