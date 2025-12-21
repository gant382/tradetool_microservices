package com.saicon.games.callcard.ws;

import com.saicon.games.callcard.ws.data.ResponseCallCardStats;
import com.saicon.games.callcard.ws.data.ResponseListTemplateUsage;
import com.saicon.games.callcard.ws.data.ResponseListUserEngagement;
import org.apache.cxf.annotations.FastInfoset;
import org.apache.cxf.annotations.GZIP;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Date;

/**
 * CallCard Statistics Service Interface
 *
 * Provides statistical analysis and reporting APIs for CallCard usage:
 * - Overall statistics (total cards, users, templates)
 * - Template usage statistics
 * - User engagement statistics
 * - Top templates by usage
 * - Active users count
 *
 * All operations support:
 * - Multi-tenant isolation via userGroupId
 * - Date range filtering
 * - Efficient native SQL queries
 *
 * @author CallCard Microservice
 * @version 1.0
 */
@WebService(
        targetNamespace = "http://ws.callcard.statistics.saicon.com/",
        name = "CallCardStatisticsService"
)
@FastInfoset
@GZIP
public interface ICallCardStatisticsService {

    /**
     * Get overall CallCard statistics for a userGroup within a date range
     *
     * Returns comprehensive statistics including:
     * - Total CallCards (active, submitted)
     * - Total users and templates
     * - Average metrics (cards per user, users per card)
     * - Average completion time
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return ResponseCallCardStats containing aggregated statistics
     */
    @WebMethod(operationName = "getCallCardStatistics")
    ResponseCallCardStats getCallCardStatistics(
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );

    /**
     * Get usage statistics for a specific template
     *
     * Returns template-specific metrics:
     * - Usage count and unique users
     * - Completion rate
     * - Average completion time
     * - First and last usage dates
     *
     * @param templateId Required. The CallCardTemplate ID to analyze
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return ResponseListTemplateUsage with single template statistics
     */
    @WebMethod(operationName = "getTemplateUsageStats")
    ResponseListTemplateUsage getTemplateUsageStats(
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );

    /**
     * Get engagement statistics for a specific user
     *
     * Returns user-specific metrics:
     * - Total CallCards created (active, submitted)
     * - Completion rate
     * - Most used templates
     * - Average completion time
     * - Activity dates
     *
     * @param userId Required. The User ID to analyze
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return ResponseListUserEngagement with single user statistics
     */
    @WebMethod(operationName = "getUserEngagementStats")
    ResponseListUserEngagement getUserEngagementStats(
            @WebParam(name = "userId") String userId,
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );

    /**
     * Get top N most used templates within a userGroup
     *
     * Returns ranked list of templates by usage count
     * Useful for identifying popular templates
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param limit Optional. Maximum number of templates to return (default: 10)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return ResponseListTemplateUsage with top templates ordered by usage
     */
    @WebMethod(operationName = "getTopTemplates")
    ResponseListTemplateUsage getTopTemplates(
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "limit") Integer limit,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );

    /**
     * Get count of active users within a date range
     *
     * Returns the number of unique users who created or modified CallCards
     * within the specified date range
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return ResponseCallCardStats with activeUsers count populated
     */
    @WebMethod(operationName = "getActiveUsersCount")
    ResponseCallCardStats getActiveUsersCount(
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );

    /**
     * Get engagement statistics for all users in a userGroup
     *
     * Returns list of user engagement metrics for all users
     * Useful for generating user activity reports
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @param limit Optional. Maximum number of users to return (default: 100)
     * @return ResponseListUserEngagement with user statistics ordered by activity
     */
    @WebMethod(operationName = "getAllUserEngagementStats")
    ResponseListUserEngagement getAllUserEngagementStats(
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo,
            @WebParam(name = "limit") Integer limit
    );

    /**
     * Get usage statistics for all templates in a userGroup
     *
     * Returns list of template usage metrics for all templates
     * Useful for template performance analysis
     *
     * @param userGroupId Required. Filter by userGroup (multi-tenant isolation)
     * @param dateFrom Optional. Start date for statistics (inclusive)
     * @param dateTo Optional. End date for statistics (inclusive)
     * @return ResponseListTemplateUsage with all template statistics
     */
    @WebMethod(operationName = "getAllTemplateUsageStats")
    ResponseListTemplateUsage getAllTemplateUsageStats(
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "dateFrom") Date dateFrom,
            @WebParam(name = "dateTo") Date dateTo
    );
}
