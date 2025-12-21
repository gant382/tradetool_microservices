package com.saicon.games.callcard.resources;

import com.saicon.games.callcard.ws.ICallCardTransactionService;
import com.saicon.games.callcard.ws.dto.CallCardTransactionDTO;
import com.saicon.games.callcard.ws.dto.TransactionListResponseDTO;
import com.saicon.games.callcard.ws.dto.TransactionSearchCriteriaDTO;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * CallCard Transaction History REST Resources.
 * Provides RESTful API for transaction history and audit trail operations.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
@SwaggerDefinition(
        tags = {@Tag(name = "callcard-transactions", description = "CallCard Transaction History and Audit Trail")}
)
@Api(value = "/callcard/transactions")
@Path("/callcard/transactions")
@Service("callCardTransactionResources")
public class CallCardTransactionResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallCardTransactionResources.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private ICallCardTransactionService transactionService;

    /**
     * Get transaction history for a CallCard.
     * GET /callcard/transactions/callcard/{callCardId}
     */
    @GET
    @Path("/callcard/{callCardId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get transaction history for a CallCard",
            notes = "Returns all audit trail entries for the specified CallCard",
            response = TransactionListResponseDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TransactionListResponseDTO.class),
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getTransactionHistory(
            @ApiParam(value = "CallCard ID", required = true)
            @PathParam("callCardId") String callCardId,

            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId,

            @ApiParam(value = "Page number (0-based)", defaultValue = "0")
            @QueryParam("page") @DefaultValue("0") Integer pageNumber,

            @ApiParam(value = "Page size", defaultValue = "50")
            @QueryParam("size") @DefaultValue("50") Integer pageSize
    ) {
        try {
            LOGGER.info("GET /callcard/transactions/callcard/{} - UserGroup: {}", callCardId, userGroupId);

            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            TransactionListResponseDTO response = transactionService.getTransactionHistoryPaginated(
                    callCardId, userGroupId, pageNumber, pageSize);

            return Response.ok(response)
                    .header("X-Total-Count", response.getTotalRecords())
                    .header("X-Total-Pages", response.getTotalPages())
                    .build();

        } catch (Exception e) {
            LOGGER.error("Error getting transaction history", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get transactions by user.
     * GET /callcard/transactions/user/{userId}
     */
    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get transactions by user",
            notes = "Returns all transactions performed by the specified user within date range",
            response = TransactionListResponseDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TransactionListResponseDTO.class),
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getTransactionsByUser(
            @ApiParam(value = "User ID", required = true)
            @PathParam("userId") Integer userId,

            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId,

            @ApiParam(value = "Start date (yyyy-MM-dd)", required = true)
            @QueryParam("dateFrom") String dateFromStr,

            @ApiParam(value = "End date (yyyy-MM-dd)", required = true)
            @QueryParam("dateTo") String dateToStr,

            @ApiParam(value = "Page number (0-based)", defaultValue = "0")
            @QueryParam("page") @DefaultValue("0") Integer pageNumber,

            @ApiParam(value = "Page size", defaultValue = "50")
            @QueryParam("size") @DefaultValue("50") Integer pageSize
    ) {
        try {
            LOGGER.info("GET /callcard/transactions/user/{} - UserGroup: {}", userId, userGroupId);

            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            Date dateFrom = DATE_FORMAT.parse(dateFromStr);
            Date dateTo = DATE_FORMAT.parse(dateToStr);

            TransactionListResponseDTO response = transactionService.getTransactionsByUserPaginated(
                    userId, userGroupId, dateFrom, dateTo, pageNumber, pageSize);

            return Response.ok(response)
                    .header("X-Total-Count", response.getTotalRecords())
                    .build();

        } catch (Exception e) {
            LOGGER.error("Error getting transactions by user", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get transactions by type.
     * GET /callcard/transactions/type/{transactionType}
     */
    @GET
    @Path("/type/{transactionType}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get transactions by type",
            notes = "Returns all transactions of the specified type within date range",
            response = TransactionListResponseDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TransactionListResponseDTO.class),
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getTransactionsByType(
            @ApiParam(value = "Transaction Type (CREATE, UPDATE, DELETE, etc.)", required = true)
            @PathParam("transactionType") String transactionType,

            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId,

            @ApiParam(value = "Start date (yyyy-MM-dd)", required = true)
            @QueryParam("dateFrom") String dateFromStr,

            @ApiParam(value = "End date (yyyy-MM-dd)", required = true)
            @QueryParam("dateTo") String dateToStr,

            @ApiParam(value = "Page number (0-based)", defaultValue = "0")
            @QueryParam("page") @DefaultValue("0") Integer pageNumber,

            @ApiParam(value = "Page size", defaultValue = "50")
            @QueryParam("size") @DefaultValue("50") Integer pageSize
    ) {
        try {
            LOGGER.info("GET /callcard/transactions/type/{} - UserGroup: {}", transactionType, userGroupId);

            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            Date dateFrom = DATE_FORMAT.parse(dateFromStr);
            Date dateTo = DATE_FORMAT.parse(dateToStr);

            TransactionListResponseDTO response = transactionService.getTransactionsByTypePaginated(
                    transactionType, userGroupId, dateFrom, dateTo, pageNumber, pageSize);

            return Response.ok(response)
                    .header("X-Total-Count", response.getTotalRecords())
                    .build();

        } catch (Exception e) {
            LOGGER.error("Error getting transactions by type", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Advanced transaction search.
     * POST /callcard/transactions/search
     */
    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Search transactions with advanced criteria",
            notes = "Supports filtering by CallCard, user, type, date range, session, IP",
            response = TransactionListResponseDTO.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TransactionListResponseDTO.class),
            @ApiResponse(code = 400, message = "Invalid criteria"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response searchTransactions(
            @ApiParam(value = "Search criteria", required = true)
            TransactionSearchCriteriaDTO criteria,

            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId
    ) {
        try {
            LOGGER.info("POST /callcard/transactions/search - UserGroup: {}", userGroupId);

            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            if (criteria == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Search criteria is required\"}")
                        .build();
            }

            // Ensure tenant isolation
            criteria.setUserGroupId(userGroupId);

            TransactionListResponseDTO response = transactionService.searchTransactions(criteria);

            return Response.ok(response)
                    .header("X-Total-Count", response.getTotalRecords())
                    .build();

        } catch (Exception e) {
            LOGGER.error("Error searching transactions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get transaction count for a CallCard.
     * GET /callcard/transactions/callcard/{callCardId}/count
     */
    @GET
    @Path("/callcard/{callCardId}/count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get transaction count for a CallCard",
            notes = "Returns the total number of transactions for the specified CallCard",
            response = Long.class
    )
    public Response getTransactionCount(
            @ApiParam(value = "CallCard ID", required = true)
            @PathParam("callCardId") String callCardId,

            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId
    ) {
        try {
            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            Long count = transactionService.getTransactionCount(callCardId, userGroupId);
            return Response.ok("{\"count\": " + count + "}").build();

        } catch (Exception e) {
            LOGGER.error("Error getting transaction count", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get transaction by ID.
     * GET /callcard/transactions/{transactionId}
     */
    @GET
    @Path("/{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get transaction by ID",
            notes = "Returns a single transaction by its ID",
            response = CallCardTransactionDTO.class
    )
    public Response getTransactionById(
            @ApiParam(value = "Transaction ID", required = true)
            @PathParam("transactionId") String transactionId,

            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId
    ) {
        try {
            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            CallCardTransactionDTO transaction = transactionService.getTransactionById(transactionId, userGroupId);

            if (transaction == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Transaction not found\"}")
                        .build();
            }

            return Response.ok(transaction).build();

        } catch (Exception e) {
            LOGGER.error("Error getting transaction by ID", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get available transaction types.
     * GET /callcard/transactions/types
     */
    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get available transaction types",
            notes = "Returns list of all available transaction types",
            response = String.class,
            responseContainer = "List"
    )
    public Response getTransactionTypes() {
        try {
            List<String> types = transactionService.getTransactionTypes();
            return Response.ok(types).build();
        } catch (Exception e) {
            LOGGER.error("Error getting transaction types", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get recent transactions.
     * GET /callcard/transactions/recent
     */
    @GET
    @Path("/recent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get recent transactions",
            notes = "Returns recent transactions for audit dashboard",
            response = TransactionListResponseDTO.class
    )
    public Response getRecentTransactions(
            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId,

            @ApiParam(value = "Limit", defaultValue = "100")
            @QueryParam("limit") @DefaultValue("100") Integer limit
    ) {
        try {
            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            TransactionListResponseDTO response = transactionService.getRecentTransactions(userGroupId, limit);
            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.error("Error getting recent transactions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get transactions by session ID.
     * GET /callcard/transactions/session/{sessionId}
     */
    @GET
    @Path("/session/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get transactions by session",
            notes = "Returns all transactions in a user session",
            response = TransactionListResponseDTO.class
    )
    public Response getTransactionsBySession(
            @ApiParam(value = "Session ID", required = true)
            @PathParam("sessionId") String sessionId,

            @ApiParam(value = "User Group ID (tenant)", required = true)
            @HeaderParam("X-Talos-User-Group-Id") Integer userGroupId
    ) {
        try {
            if (userGroupId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"X-Talos-User-Group-Id header is required\"}")
                        .build();
            }

            TransactionListResponseDTO response = transactionService.getTransactionsBySession(sessionId, userGroupId);
            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.error("Error getting transactions by session", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
