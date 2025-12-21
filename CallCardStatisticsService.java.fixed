package com.saicon.games.callcard.service;

import com.saicon.games.callcard.ws.ICallCardStatisticsService;
import com.saicon.games.callcard.ws.data.ResponseCallCardStats;
import com.saicon.games.callcard.ws.data.ResponseListTemplateUsage;
import com.saicon.games.callcard.ws.data.ResponseListUserEngagement;
import com.saicon.games.callcard.ws.dto.CallCardStatsDTO;
import com.saicon.games.callcard.ws.dto.TemplateUsageDTO;
import com.saicon.games.callcard.ws.dto.UserEngagementDTO;
import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.exception.ExceptionTypeTO;
import com.saicon.games.callcard.components.ICallCardManagement;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CallCard Statistics Service Implementation
 *
 * Provides SOAP web service endpoints for CallCard statistics and reporting.
 * Implements ICallCardStatisticsService interface.
 *
 * All operations support:
 * - Multi-tenant isolation via userGroupId
 * - Date range filtering
 * - Error handling and logging
 *
 * @author CallCard Microservice
 * @version 1.0
 */
@WebService(
        targetNamespace = "http://ws.callcard.statistics.saicon.com/",
        portName = "CallCardStatisticsServicePort",
        endpointInterface = "com.saicon.games.callcard.ws.ICallCardStatisticsService",
        serviceName = "CallCardStatisticsService"
)
public class CallCardStatisticsService implements ICallCardStatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallCardStatisticsService.class);

    private ICallCardManagement callCardManagement;

    @Override
    public ResponseCallCardStats getCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) {
        try {
            LOGGER.debug("Getting CallCard statistics for userGroup: {}, dateFrom: {}, dateTo: {}",
                    userGroupId, dateFrom, dateTo);

            CallCardStatsDTO stats = callCardManagement.getOverallCallCardStatistics(userGroupId, dateFrom, dateTo);

            if (stats != null) {
                return new ResponseCallCardStats(
                        ExceptionTypeTO.NONE.getErrorCode(),
                        ResponseStatus.OK,
                        stats
                );
            } else {
                return new ResponseCallCardStats(
                        ExceptionTypeTO.NONE.getErrorCode(),
                        "No statistics found",
                        ResponseStatus.OK,
                        null
                );
            }

        } catch (BusinessLayerException e) {
            LOGGER.error("Business layer error getting CallCard statistics", e);
            return new ResponseCallCardStats(
                    e.getErrorCode(),
                    e.getMessage(),
                    ResponseStatus.ERROR,
                    null
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting CallCard statistics", e);
            return new ResponseCallCardStats(
                    ExceptionTypeTO.GENERIC_ERROR.getErrorCode(),
                    "Unexpected error: " + e.getMessage(),
                    ResponseStatus.ERROR,
                    null
            );
        }
    }

    @Override
    public ResponseListTemplateUsage getTemplateUsageStats(String templateId, String userGroupId,
                                                           Date dateFrom, Date dateTo) {
        try {
            LOGGER.debug("Getting template usage statistics for template: {}, userGroup: {}, dateFrom: {}, dateTo: {}",
                    templateId, userGroupId, dateFrom, dateTo);

            TemplateUsageDTO templateUsage = callCardManagement.getTemplateUsageStatistics(
                    templateId, userGroupId, dateFrom, dateTo
            );

            List<TemplateUsageDTO> results = new ArrayList<>();
            if (templateUsage != null) {
                results.add(templateUsage);
            }

            return new ResponseListTemplateUsage(
                    ExceptionTypeTO.NONE.getErrorCode(),
                    ResponseStatus.OK,
                    results,
                    results.size()
            );

        } catch (BusinessLayerException e) {
            LOGGER.error("Business layer error getting template usage statistics", e);
            return new ResponseListTemplateUsage(
                    e.getErrorCode(),
                    e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting template usage statistics", e);
            return new ResponseListTemplateUsage(
                    ExceptionTypeTO.GENERIC_ERROR.getErrorCode(),
                    "Unexpected error: " + e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        }
    }

    @Override
    public ResponseListUserEngagement getUserEngagementStats(String userId, String userGroupId,
                                                             Date dateFrom, Date dateTo) {
        try {
            LOGGER.debug("Getting user engagement statistics for user: {}, userGroup: {}, dateFrom: {}, dateTo: {}",
                    userId, userGroupId, dateFrom, dateTo);

            UserEngagementDTO userEngagement = callCardManagement.getUserEngagementStatistics(
                    userId, userGroupId, dateFrom, dateTo
            );

            List<UserEngagementDTO> results = new ArrayList<>();
            if (userEngagement != null) {
                results.add(userEngagement);
            }

            return new ResponseListUserEngagement(
                    ExceptionTypeTO.NONE.getErrorCode(),
                    ResponseStatus.OK,
                    results,
                    results.size()
            );

        } catch (BusinessLayerException e) {
            LOGGER.error("Business layer error getting user engagement statistics", e);
            return new ResponseListUserEngagement(
                    e.getErrorCode(),
                    e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting user engagement statistics", e);
            return new ResponseListUserEngagement(
                    ExceptionTypeTO.GENERIC_ERROR.getErrorCode(),
                    "Unexpected error: " + e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        }
    }

    @Override
    public ResponseListTemplateUsage getTopTemplates(String userGroupId, Integer limit,
                                                     Date dateFrom, Date dateTo) {
        try {
            LOGGER.debug("Getting top templates for userGroup: {}, limit: {}, dateFrom: {}, dateTo: {}",
                    userGroupId, limit, dateFrom, dateTo);

            List<TemplateUsageDTO> topTemplates = callCardManagement.getTopTemplates(
                    userGroupId, limit, dateFrom, dateTo
            );

            return new ResponseListTemplateUsage(
                    ExceptionTypeTO.NONE.getErrorCode(),
                    ResponseStatus.OK,
                    topTemplates,
                    topTemplates != null ? topTemplates.size() : 0
            );

        } catch (BusinessLayerException e) {
            LOGGER.error("Business layer error getting top templates", e);
            return new ResponseListTemplateUsage(
                    e.getErrorCode(),
                    e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting top templates", e);
            return new ResponseListTemplateUsage(
                    ExceptionTypeTO.GENERIC_ERROR.getErrorCode(),
                    "Unexpected error: " + e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        }
    }

    @Override
    public ResponseCallCardStats getActiveUsersCount(String userGroupId, Date dateFrom, Date dateTo) {
        try {
            LOGGER.debug("Getting active users count for userGroup: {}, dateFrom: {}, dateTo: {}",
                    userGroupId, dateFrom, dateTo);

            Long activeUsersCount = callCardManagement.getActiveUsersCount(userGroupId, dateFrom, dateTo);

            // Create a minimal stats DTO with just the active users count
            CallCardStatsDTO stats = new CallCardStatsDTO();
            stats.setUserGroupId(userGroupId);
            stats.setDateFrom(dateFrom);
            stats.setDateTo(dateTo);
            stats.setTotalUsers(activeUsersCount != null ? activeUsersCount : 0L);

            return new ResponseCallCardStats(
                    ExceptionTypeTO.NONE.getErrorCode(),
                    ResponseStatus.OK,
                    stats
            );

        } catch (BusinessLayerException e) {
            LOGGER.error("Business layer error getting active users count", e);
            return new ResponseCallCardStats(
                    e.getErrorCode(),
                    e.getMessage(),
                    ResponseStatus.ERROR,
                    null
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting active users count", e);
            return new ResponseCallCardStats(
                    ExceptionTypeTO.GENERIC_ERROR.getErrorCode(),
                    "Unexpected error: " + e.getMessage(),
                    ResponseStatus.ERROR,
                    null
            );
        }
    }

    @Override
    public ResponseListUserEngagement getAllUserEngagementStats(String userGroupId, Date dateFrom,
                                                                Date dateTo, Integer limit) {
        try {
            LOGGER.debug("Getting all user engagement statistics for userGroup: {}, limit: {}, dateFrom: {}, dateTo: {}",
                    userGroupId, limit, dateFrom, dateTo);

            List<UserEngagementDTO> engagementStats = callCardManagement.getAllUserEngagementStatistics(
                    userGroupId, dateFrom, dateTo, limit
            );

            return new ResponseListUserEngagement(
                    ExceptionTypeTO.NONE.getErrorCode(),
                    ResponseStatus.OK,
                    engagementStats,
                    engagementStats != null ? engagementStats.size() : 0
            );

        } catch (BusinessLayerException e) {
            LOGGER.error("Business layer error getting all user engagement statistics", e);
            return new ResponseListUserEngagement(
                    e.getErrorCode(),
                    e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting all user engagement statistics", e);
            return new ResponseListUserEngagement(
                    ExceptionTypeTO.GENERIC_ERROR.getErrorCode(),
                    "Unexpected error: " + e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        }
    }

    @Override
    public ResponseListTemplateUsage getAllTemplateUsageStats(String userGroupId, Date dateFrom, Date dateTo) {
        try {
            LOGGER.debug("Getting all template usage statistics for userGroup: {}, dateFrom: {}, dateTo: {}",
                    userGroupId, dateFrom, dateTo);

            List<TemplateUsageDTO> templateStats = callCardManagement.getAllTemplateUsageStatistics(
                    userGroupId, dateFrom, dateTo
            );

            return new ResponseListTemplateUsage(
                    ExceptionTypeTO.NONE.getErrorCode(),
                    ResponseStatus.OK,
                    templateStats,
                    templateStats != null ? templateStats.size() : 0
            );

        } catch (BusinessLayerException e) {
            LOGGER.error("Business layer error getting all template usage statistics", e);
            return new ResponseListTemplateUsage(
                    e.getErrorCode(),
                    e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting all template usage statistics", e);
            return new ResponseListTemplateUsage(
                    ExceptionTypeTO.GENERIC_ERROR.getErrorCode(),
                    "Unexpected error: " + e.getMessage(),
                    ResponseStatus.ERROR,
                    null,
                    0
            );
        }
    }

    // Getter and Setter
    public ICallCardManagement getCallCardManagement() {
        return callCardManagement;
    }

    public void setCallCardManagement(ICallCardManagement callCardManagement) {
        this.callCardManagement = callCardManagement;
    }
}
