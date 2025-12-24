package com.saicon.games.callcard.components;

import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import com.saicon.games.callcard.ws.dto.CallCardStatsDTO;
import com.saicon.games.callcard.ws.dto.TemplateUsageDTO;
import com.saicon.games.callcard.ws.dto.UserEngagementDTO;
import com.saicon.games.callcard.ws.dto.CallCardSummaryDTO;
// TODO: stub needed
// import com.saicon.ecommerce.dto.ItemStatisticsDTO;

import java.util.Date;
import java.util.List;

/**
 * Created by user101 on 9/2/2016.
 */
public interface ICallCardManagement {
    // TODO: stub needed - ItemStatisticsDTO not available
    // List<ItemStatisticsDTO> getCallCardStatistics(String userId,
    //                                               String propertyId,
    //                                               List<Integer> types,
    //                                               Date dateFrom,
    //                                               Date dateTo);

    CallCardDTO listPendingCallCard(String userId, String userGroupId, String gameTypeId) throws BusinessLayerException;

    void submitTransactions(String userId, String userGroupId, String gameTypeId, String applicationId, String indirectUserId, CallCardDTO callCardDTO) throws BusinessLayerException;

    List<CallCardDTO> getCallCardsFromTemplate(String userId, String userGroupId, String gameTypeId, String applicationId) throws BusinessLayerException;

    CallCardDTO getPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId) throws BusinessLayerException;

    CallCardDTO getNewOrPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId, String callCardId, List<String> filterProperties) throws BusinessLayerException;

    CallCardDTO updateCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, List<CallCardDTO> callCards) throws BusinessLayerException;

    void addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, SimplifiedCallCardDTO callCard) throws BusinessLayerException;

    List<SimplifiedCallCardDTO> listSimplifiedCallCards(String callCardUserId, String sourceUserId, String refUserId, Date dateFrom, Date dateTo, int rangeFrom, int rangeTo) throws BusinessLayerException;

    Integer countSimplifiedCallCards(String callCardUserId, String sourceUserId, String refUserId, Date dateFrom, Date dateTo) throws BusinessLayerException;

    // ============================================================
    // Statistics Methods (User Story 2)
    // ============================================================

    /**
     * Get overall CallCard statistics for a userGroup within a date range
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return CallCardStatsDTO containing aggregated statistics
     */
    CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;

    /**
     * Get usage statistics for a specific template
     *
     * @param templateId Required. The CallCardTemplate ID to analyze
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return TemplateUsageDTO with template statistics
     */
    TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;

    /**
     * Get engagement statistics for a specific user
     *
     * @param userId Required. The User ID to analyze
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return UserEngagementDTO with user statistics
     */
    UserEngagementDTO getUserEngagementStatistics(String userId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;

    /**
     * Get top N most used templates within a userGroup
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param limit Optional. Maximum number of templates to return (default: 10)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return List of TemplateUsageDTO ordered by usage count
     */
    List<TemplateUsageDTO> getTopTemplates(String userGroupId, Integer limit, Date dateFrom, Date dateTo) throws BusinessLayerException;

    /**
     * Get count of active users within a date range
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return Count of unique active users
     */
    Long getActiveUsersCount(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;

    /**
     * Get engagement statistics for all users in a userGroup
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @param limit Optional. Maximum number of users to return (default: 100)
     * @return List of UserEngagementDTO ordered by activity
     */
    List<UserEngagementDTO> getAllUserEngagementStatistics(String userGroupId, Date dateFrom, Date dateTo, Integer limit) throws BusinessLayerException;

    /**
     * Get usage statistics for all templates in a userGroup
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return List of TemplateUsageDTO for all templates
     */
    List<TemplateUsageDTO> getAllTemplateUsageStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;


    // ============================================================
    // SimplifiedCallCard V2 Methods (User Story 3)
    // ============================================================

    SimplifiedCallCardDTO getSimplifiedCallCardV2(String callCardId);

    Integer countSimplifiedCallCardsV2(String userId, String sourceUserId, String refUserId, String templateId, Boolean includeDeleted);

    List<SimplifiedCallCardDTO> getSimplifiedCallCardListV2(String userId, String sourceUserId, String refUserId, String templateId, Boolean includeDeleted, int rangeFrom, int rangeTo);

    List<CallCardSummaryDTO> getCallCardSummaries(String userGroupId, int rangeFrom, int rangeTo);

    List<SimplifiedCallCardDTO> bulkGetSimplifiedCallCardsV2(List<String> callCardIds);

    Integer countCallCardsByTemplate(String templateId, boolean includeCompleted);

    List<SimplifiedCallCardDTO> getSimplifiedCallCardsByTemplate(String templateId, boolean includeCompleted, int rangeFrom, int rangeTo);

    Integer countCallCardsByUser(String userId, boolean includeCompleted);

    List<SimplifiedCallCardDTO> getSimplifiedCallCardsByUser(String userId, boolean includeCompleted, int rangeFrom, int rangeTo);

    List<CallCardSummaryDTO> searchCallCardSummaries(String userGroupId, String searchTerm, int rangeFrom, int rangeTo);

    }
