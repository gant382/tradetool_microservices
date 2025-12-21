package com.saicon.games.callcard.resources;

import com.saicon.games.callcard.ws.ICallCardStatisticsService;
import com.saicon.games.callcard.ws.data.ResponseCallCardStats;
import com.saicon.games.callcard.ws.data.ResponseListTemplateUsage;
import com.saicon.games.callcard.ws.data.ResponseListUserEngagement;
import com.saicon.games.callcard.ws.dto.CallCardStatsDTO;
import com.saicon.games.callcard.ws.dto.TemplateUsageDTO;
import com.saicon.games.callcard.ws.dto.UserEngagementDTO;
import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.exception.ExceptionTypeTO;
import com.saicon.games.callcard.util.Assert;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * CallCard Statistics REST Resources
 *
 * Provides RESTful JSON endpoints for CallCard statistics and reporting.
 * All operations require userGroupId for multi-tenant isolation.
 *
 * Base path: /rest/callcard/statistics
 *
 * @author CallCard Microservice
 * @version 1.0
 */
@SwaggerDefinition(
        tags = {@Tag(name = "callcard-statistics", description = "CallCard Statistics and Reporting APIs")}
)
@Api(value = "/callcard/statistics")
@Path("/callcard/statistics")
@Service("callCardStatisticsResources")
public class CallCardStatisticsResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallCardStatisticsResources.class);
    private static final String X_TALOS_USER_GROUP_ID = "X-Talos-User-Group-Id";
    private static final String X_TALOS_ITEM_COUNT = "X-Talos-Item-Count";

    @Resource
    private ICallCardStatisticsService callCardStatisticsService;

    /**
     * Get overall CallCard statistics
     *
     * GET /rest/callcard/statistics/overall
     */
    @GET
    @Path("/overall")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get overall CallCard statistics",
            notes = "Returns comprehensive statistics including total cards, users, templates, and averages",
            response = CallCardStatsDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CallCardStatsDTO.class),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Bad Request", response = BusinessLayerException.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = BusinessLayerException.class)
    })
    public Response getCallCardStatistics(
            @ApiParam(name = X_TALOS_USER_GROUP_ID, value = "User Group ID (required)", required = true)
            @HeaderParam(X_TALOS_USER_GROUP_ID) String userGroupId,

            @ApiParam(name = "dateFrom", value = "Start date for statistics (optional)")
            @QueryParam("dateFrom") Date dateFrom,

            @ApiParam(name = "dateTo", value = "End date for statistics (optional)")
            @QueryParam("dateTo") Date dateTo
    ) throws BusinessLayerException {
        LOGGER.debug("REST: Getting CallCard statistics for userGroup: {}", userGroupId);

        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        ResponseCallCardStats response = callCardStatisticsService.getCallCardStatistics(userGroupId, dateFrom, dateTo);

        if (ResponseStatus.OK.equals(response.getStatus())) {
            if (response.getStats() != null) {
                return Response.ok(response.getStats()).build();
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new BusinessLayerException(response.getResult(), ExceptionTypeTO.valueOf(response.getErrorNumber()));
        }
    }

    /**
     * Get template usage statistics
     *
     * GET /rest/callcard/statistics/template/{templateId}
     */
    @GET
    @Path("/template/{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get template usage statistics",
            notes = "Returns usage metrics for a specific template",
            response = TemplateUsageDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TemplateUsageDTO.class),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Bad Request", response = BusinessLayerException.class),
            @ApiResponse(code = 404, message = "Template Not Found", response = BusinessLayerException.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = BusinessLayerException.class)
    })
    public Response getTemplateUsageStats(
            @ApiParam(name = "templateId", value = "Template ID (required)", required = true)
            @PathParam("templateId") String templateId,

            @ApiParam(name = X_TALOS_USER_GROUP_ID, value = "User Group ID (required)", required = true)
            @HeaderParam(X_TALOS_USER_GROUP_ID) String userGroupId,

            @ApiParam(name = "dateFrom", value = "Start date for statistics (optional)")
            @QueryParam("dateFrom") Date dateFrom,

            @ApiParam(name = "dateTo", value = "End date for statistics (optional)")
            @QueryParam("dateTo") Date dateTo
    ) throws BusinessLayerException {
        LOGGER.debug("REST: Getting template usage statistics for template: {}", templateId);

        Assert.notNullOrEmpty(templateId, "templateId must not be null or empty");
        Assert.isValidUUID(templateId, "templateId must be a valid UUID");
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        ResponseListTemplateUsage response = callCardStatisticsService.getTemplateUsageStats(
                templateId, userGroupId, dateFrom, dateTo
        );

        if (ResponseStatus.OK.equals(response.getStatus())) {
            if (response.getRecords() != null && !response.getRecords().isEmpty()) {
                return Response.ok(response.getRecords().get(0))
                        .header(X_TALOS_ITEM_COUNT, 1)
                        .build();
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new BusinessLayerException(response.getResult(), ExceptionTypeTO.valueOf(response.getErrorNumber()));
        }
    }

    /**
     * Get user engagement statistics
     *
     * GET /rest/callcard/statistics/user/{userId}
     */
    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get user engagement statistics",
            notes = "Returns engagement metrics for a specific user",
            response = UserEngagementDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserEngagementDTO.class),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Bad Request", response = BusinessLayerException.class),
            @ApiResponse(code = 404, message = "User Not Found", response = BusinessLayerException.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = BusinessLayerException.class)
    })
    public Response getUserEngagementStats(
            @ApiParam(name = "userId", value = "User ID (required)", required = true)
            @PathParam("userId") String userId,

            @ApiParam(name = X_TALOS_USER_GROUP_ID, value = "User Group ID (required)", required = true)
            @HeaderParam(X_TALOS_USER_GROUP_ID) String userGroupId,

            @ApiParam(name = "dateFrom", value = "Start date for statistics (optional)")
            @QueryParam("dateFrom") Date dateFrom,

            @ApiParam(name = "dateTo", value = "End date for statistics (optional)")
            @QueryParam("dateTo") Date dateTo
    ) throws BusinessLayerException {
        LOGGER.debug("REST: Getting user engagement statistics for user: {}", userId);

        Assert.notNullOrEmpty(userId, "userId must not be null or empty");
        Assert.isValidUUID(userId, "userId must be a valid UUID");
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        ResponseListUserEngagement response = callCardStatisticsService.getUserEngagementStats(
                userId, userGroupId, dateFrom, dateTo
        );

        if (ResponseStatus.OK.equals(response.getStatus())) {
            if (response.getRecords() != null && !response.getRecords().isEmpty()) {
                return Response.ok(response.getRecords().get(0))
                        .header(X_TALOS_ITEM_COUNT, 1)
                        .build();
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new BusinessLayerException(response.getResult(), ExceptionTypeTO.valueOf(response.getErrorNumber()));
        }
    }

    /**
     * Get top N most used templates
     *
     * GET /rest/callcard/statistics/templates/top
     */
    @GET
    @Path("/templates/top")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get top templates by usage",
            notes = "Returns ranked list of most used templates",
            response = TemplateUsageDTO.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TemplateUsageDTO.class, responseContainer = "List",
                    responseHeaders = @ResponseHeader(name = X_TALOS_ITEM_COUNT, response = Integer.class)),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Bad Request", response = BusinessLayerException.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = BusinessLayerException.class)
    })
    public Response getTopTemplates(
            @ApiParam(name = X_TALOS_USER_GROUP_ID, value = "User Group ID (required)", required = true)
            @HeaderParam(X_TALOS_USER_GROUP_ID) String userGroupId,

            @ApiParam(name = "limit", value = "Maximum number of templates to return (default: 10)")
            @QueryParam("limit") @DefaultValue("10") Integer limit,

            @ApiParam(name = "dateFrom", value = "Start date for statistics (optional)")
            @QueryParam("dateFrom") Date dateFrom,

            @ApiParam(name = "dateTo", value = "End date for statistics (optional)")
            @QueryParam("dateTo") Date dateTo
    ) throws BusinessLayerException {
        LOGGER.debug("REST: Getting top {} templates for userGroup: {}", limit, userGroupId);

        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        ResponseListTemplateUsage response = callCardStatisticsService.getTopTemplates(
                userGroupId, limit, dateFrom, dateTo
        );

        if (ResponseStatus.OK.equals(response.getStatus())) {
            List<TemplateUsageDTO> templates = response.getRecords();
            if (templates != null && !templates.isEmpty()) {
                return Response.ok(templates)
                        .header(X_TALOS_ITEM_COUNT, templates.size())
                        .build();
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new BusinessLayerException(response.getResult(), ExceptionTypeTO.valueOf(response.getErrorNumber()));
        }
    }

    /**
     * Get active users count
     *
     * GET /rest/callcard/statistics/users/active/count
     */
    @GET
    @Path("/users/active/count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get count of active users",
            notes = "Returns the number of unique users who created or modified CallCards",
            response = Long.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Long.class),
            @ApiResponse(code = 400, message = "Bad Request", response = BusinessLayerException.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = BusinessLayerException.class)
    })
    public Response getActiveUsersCount(
            @ApiParam(name = X_TALOS_USER_GROUP_ID, value = "User Group ID (required)", required = true)
            @HeaderParam(X_TALOS_USER_GROUP_ID) String userGroupId,

            @ApiParam(name = "dateFrom", value = "Start date for statistics (optional)")
            @QueryParam("dateFrom") Date dateFrom,

            @ApiParam(name = "dateTo", value = "End date for statistics (optional)")
            @QueryParam("dateTo") Date dateTo
    ) throws BusinessLayerException {
        LOGGER.debug("REST: Getting active users count for userGroup: {}", userGroupId);

        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        ResponseCallCardStats response = callCardStatisticsService.getActiveUsersCount(userGroupId, dateFrom, dateTo);

        if (ResponseStatus.OK.equals(response.getStatus())) {
            Long count = response.getStats() != null ? response.getStats().getTotalUsers() : 0L;
            return Response.ok().entity("{\"activeUsersCount\":" + count + "}").build();
        } else {
            throw new BusinessLayerException(response.getResult(), ExceptionTypeTO.valueOf(response.getErrorNumber()));
        }
    }

    /**
     * Get all user engagement statistics
     *
     * GET /rest/callcard/statistics/users/engagement
     */
    @GET
    @Path("/users/engagement")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get all user engagement statistics",
            notes = "Returns engagement metrics for all users in the user group",
            response = UserEngagementDTO.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserEngagementDTO.class, responseContainer = "List",
                    responseHeaders = @ResponseHeader(name = X_TALOS_ITEM_COUNT, response = Integer.class)),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Bad Request", response = BusinessLayerException.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = BusinessLayerException.class)
    })
    public Response getAllUserEngagementStats(
            @ApiParam(name = X_TALOS_USER_GROUP_ID, value = "User Group ID (required)", required = true)
            @HeaderParam(X_TALOS_USER_GROUP_ID) String userGroupId,

            @ApiParam(name = "limit", value = "Maximum number of users to return (default: 100)")
            @QueryParam("limit") @DefaultValue("100") Integer limit,

            @ApiParam(name = "dateFrom", value = "Start date for statistics (optional)")
            @QueryParam("dateFrom") Date dateFrom,

            @ApiParam(name = "dateTo", value = "End date for statistics (optional)")
            @QueryParam("dateTo") Date dateTo
    ) throws BusinessLayerException {
        LOGGER.debug("REST: Getting all user engagement statistics for userGroup: {}", userGroupId);

        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        ResponseListUserEngagement response = callCardStatisticsService.getAllUserEngagementStats(
                userGroupId, dateFrom, dateTo, limit
        );

        if (ResponseStatus.OK.equals(response.getStatus())) {
            List<UserEngagementDTO> engagementList = response.getRecords();
            if (engagementList != null && !engagementList.isEmpty()) {
                return Response.ok(engagementList)
                        .header(X_TALOS_ITEM_COUNT, engagementList.size())
                        .build();
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new BusinessLayerException(response.getResult(), ExceptionTypeTO.valueOf(response.getErrorNumber()));
        }
    }

    /**
     * Get all template usage statistics
     *
     * GET /rest/callcard/statistics/templates/usage
     */
    @GET
    @Path("/templates/usage")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get all template usage statistics",
            notes = "Returns usage metrics for all templates in the user group",
            response = TemplateUsageDTO.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TemplateUsageDTO.class, responseContainer = "List",
                    responseHeaders = @ResponseHeader(name = X_TALOS_ITEM_COUNT, response = Integer.class)),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Bad Request", response = BusinessLayerException.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = BusinessLayerException.class)
    })
    public Response getAllTemplateUsageStats(
            @ApiParam(name = X_TALOS_USER_GROUP_ID, value = "User Group ID (required)", required = true)
            @HeaderParam(X_TALOS_USER_GROUP_ID) String userGroupId,

            @ApiParam(name = "dateFrom", value = "Start date for statistics (optional)")
            @QueryParam("dateFrom") Date dateFrom,

            @ApiParam(name = "dateTo", value = "End date for statistics (optional)")
            @QueryParam("dateTo") Date dateTo
    ) throws BusinessLayerException {
        LOGGER.debug("REST: Getting all template usage statistics for userGroup: {}", userGroupId);

        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        ResponseListTemplateUsage response = callCardStatisticsService.getAllTemplateUsageStats(
                userGroupId, dateFrom, dateTo
        );

        if (ResponseStatus.OK.equals(response.getStatus())) {
            List<TemplateUsageDTO> templateList = response.getRecords();
            if (templateList != null && !templateList.isEmpty()) {
                return Response.ok(templateList)
                        .header(X_TALOS_ITEM_COUNT, templateList.size())
                        .build();
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new BusinessLayerException(response.getResult(), ExceptionTypeTO.valueOf(response.getErrorNumber()));
        }
    }
}
