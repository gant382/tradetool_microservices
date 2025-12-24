package com.saicon.games.callcard.resources;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import com.saicon.games.callcard.ws.dto.ItemStatisticsDTO;
import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.exception.ExceptionTypeTO;
import com.saicon.games.callcard.util.Assert;
import com.saicon.games.callcard.ws.data.ResponseListItemStatistics;
import com.saicon.games.callcard.util.Constants;
import com.saicon.games.callcard.ws.ICallCardService;
import com.saicon.games.callcard.ws.external.IGameInternalService;
import com.saicon.games.callcard.ws.external.IGameService;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import com.saicon.games.callcard.ws.response.WSResponse;
import com.saicon.games.callcard.ws.data.ResponseListCallCard;
import com.saicon.games.callcard.ws.data.ResponseListSimplifiedCallCard;
import com.saicon.games.callcard.ws.external.UserSessionDTOS;
import com.saicon.games.callcard.util.TalosUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SwaggerDefinition(
        tags = {@Tag(name = "callcard", description = "Create, Get and Update Call Cards")}
)
@Api(value = "/callcard")
@Path("/callcard")
@Service("callCardResources")
public class CallCardResources {
    // Removed ICallCardResources interface (not needed for JAX-RS)
    private static final Logger LOGGER = LoggerFactory.getLogger("com.saicon.talos.services.audit_logger");

    @Resource
    private ICallCardService callCardService;

    @Resource
    private IGameService gameService;

    @Resource
    private IGameInternalService gameInternalService;

    public CallCardResources() {
    }

    public List<String> getUnsecuredMethods() {

        return Arrays.asList("getCallCardsFromTemplate", "getPendingCallCard0", "getNewOrPendingCallCard", "listPendingCallCard",
                "getCallCardStatistics");
    }

    @GET
    @Path("/template/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get list of empty CallCards assigned to user", notes = "User ID is required", response = CallCardDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class)}
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = TalosUtil.X_TALOS_FILL_RESOURCES, allowableValues = "true,metadata", dataType = "string", value = "Inline resources and/or metadata", paramType = "Header"),
    })
    public Response getCallCardsFromTemplate(@ApiParam(name = TalosUtil.X_TALOS_USER_GROUP_ID, value = "Get Call Cards for user with userGroupId.", required = true) @HeaderParam(TalosUtil.X_TALOS_USER_GROUP_ID) String userGroupId,
                                             @ApiParam(name = TalosUtil.X_TALOS_APPLICATION_ID, value = "Get Call Cards for user with applicationId.", required = true) @HeaderParam(TalosUtil.X_TALOS_APPLICATION_ID) String applicationId,
                                             @ApiParam(name = "userId", value = "Get Call Cards for user with userId.", required = true) @PathParam("userId") String userId,
                                             @ApiParam(name = "gameTypeId", value = "Query Call Cards by gameTypeId.", required = true) @QueryParam("gameTypeId") String gameTypeId,
                                             @ApiParam(name = "includePendingCallCard", value = "Includes the Pending CallCard if it exists") @DefaultValue("false") @QueryParam("includePendingCallCard") Boolean includePendingCallCard) throws BusinessLayerException {

        Assert.notNullOrEmpty(userId, "Provide a userId...");
        Assert.isValidUUID(userId, "Provide a valid userId...");
        Assert.notNullOrEmpty(gameTypeId, "Provide a gameTypeId...");
        Assert.isValidUUID(gameTypeId, "Provide a valid gameTypeId...");
        Assert.notNullOrEmpty(userGroupId, "Provide a userGroupId...");
        Assert.isValidUUID(userGroupId, "Provide a valid userGroupId...");
        Assert.notNullOrEmpty(applicationId, "Provide a applicationId...");
        Assert.isValidUUID(applicationId, "Provide a valid applicationId...");

        // get all call card templates for userId
        ResponseListCallCard rsp = callCardService.getCallCardsFromTemplate(userId, userGroupId, gameTypeId, applicationId);
        if (ResponseStatus.ERROR.equals(rsp.getStatus()))
            throw new BusinessLayerException(String.format("Error %d listing CallCard: %s", rsp.getErrorNumber(),
                    rsp.getResult()), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));

        List<CallCardDTO> callCardDTOs = new ArrayList<>();
        if (rsp.getRecords() != null && rsp.getRecords().size() > 0)
            callCardDTOs.addAll( rsp.getRecords());

        // get pending call card if exists for userId
        ResponseListCallCard rspPending = callCardService.getPendingCallCard( userId, userGroupId, gameTypeId, applicationId);
        if (ResponseStatus.ERROR.equals(rspPending.getStatus()))
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));

        if (rspPending.getRecords() != null && rspPending.getRecords().size() > 0)
            callCardDTOs.addAll( rspPending.getRecords());

        if (callCardDTOs != null && callCardDTOs.size() > 0)
            return Response.ok(callCardDTOs).header(TalosUtil.X_TALOS_ITEM_COUNT, callCardDTOs.size()).build();
        else
            return Response.noContent().build();
    }

    @GET
    @Path("/pending/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get user's pending CallCard (if it exists)", notes = "User ID is required", response = CallCardDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class)}
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = TalosUtil.X_TALOS_FILL_RESOURCES, allowableValues = "true,metadata", dataType = "string", value = "Inline resources and/or metadata", paramType = "Header"),
    })
    public Response getPendingCallCard(@ApiParam(name = TalosUtil.X_TALOS_USER_GROUP_ID, value = "Get Call Cards for user with userGroupId.", required = true) @HeaderParam(TalosUtil.X_TALOS_USER_GROUP_ID) String userGroupId,
                                            @ApiParam(name = TalosUtil.X_TALOS_APPLICATION_ID, value = "Get Call Cards for user with applicationId.", required = true) @HeaderParam(TalosUtil.X_TALOS_APPLICATION_ID) String applicationId,
                                            @ApiParam(name = "userId", value = "Get Call Cards for user with userId.", required = true) @PathParam("userId") String userId,
                                            @ApiParam(name = "gameTypeId", value = "Query Call Cards by gameTypeId.", required = true) @QueryParam("gameTypeId") String gameTypeId) throws BusinessLayerException {
        Assert.notNullOrEmpty(userId, "Provide a userId...");
        Assert.isValidUUID(userId, "Provide a valid userId...");
        Assert.notNullOrEmpty(gameTypeId, "Provide a gameTypeId...");
        Assert.isValidUUID(gameTypeId, "Provide a valid gameTypeId...");
        Assert.notNullOrEmpty(userGroupId, "Provide a userGroupId...");
        Assert.isValidUUID(userGroupId, "Provide a valid userGroupId...");
        Assert.notNullOrEmpty(applicationId, "Provide a applicationId...");
        Assert.isValidUUID(applicationId, "Provide a valid applicationId...");


        ResponseListCallCard rsp = callCardService.getPendingCallCard(userId, userGroupId, gameTypeId, applicationId);
        if (ResponseStatus.OK.equals(rsp.getStatus())) {
            if (rsp.getRecords() != null && rsp.getRecords().size() > 0)
                return Response.ok(rsp.getRecords()).header(TalosUtil.X_TALOS_ITEM_COUNT, rsp.getTotalRecords()).build();
            else
                return Response.noContent().build();
        } else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }

    @GET
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get pending CallCard or create a new one", notes = "User ID is required", response = CallCardDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class)}
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = TalosUtil.X_TALOS_FILL_RESOURCES, allowableValues = "true,metadata", dataType = "string", value = "Inline resources and/or metadata", paramType = "Header"),
    })
    public Response getNewOrPendingCallCard(@ApiParam(name = TalosUtil.X_TALOS_USER_GROUP_ID, value = "Get Call Cards for user with userGroupId.", required = true) @HeaderParam(TalosUtil.X_TALOS_USER_GROUP_ID) String userGroupId,
                                            @ApiParam(name = TalosUtil.X_TALOS_APPLICATION_ID, value = "Get Call Cards for user with applicationId.", required = true) @HeaderParam(TalosUtil.X_TALOS_APPLICATION_ID) String applicationId,
                                            @ApiParam(name = "userId", value = "Get Call Cards for user with userId.", required = true) @PathParam("userId") String userId,
                                            @ApiParam(name = "gameTypeId", value = "Query Call Cards by gameTypeId.", required = true) @QueryParam("gameTypeId") String gameTypeId,
                                            @ApiParam(name = "callCardId", value = "Query Call Cards by callCardId.", required = false) @QueryParam("callCardId") String callCardId,
                                            @ApiParam(name = "filterProperties", value = "Query Call Cards Templates by metadataKeys.", required = false) @QueryParam("filterProperties") List<String> filterProperties) throws BusinessLayerException {

        LOGGER.debug("listCallCard");

        Assert.notNullOrEmpty(userId, "Provide a userId...");
        Assert.isValidUUID(userId, "Provide a valid userId...");
        Assert.notNullOrEmpty(gameTypeId, "Provide a gameTypeId...");
        Assert.isValidUUID(gameTypeId, "Provide a valid gameTypeId...");
        Assert.notNullOrEmpty(userGroupId, "Provide a userGroupId...");
        Assert.isValidUUID(userGroupId, "Provide a valid userGroupId...");
        Assert.notNullOrEmpty(applicationId, "Provide a applicationId...");
        Assert.isValidUUID(applicationId, "Provide a valid applicationId...");

        if (filterProperties == null || filterProperties.size() == 0) {
            switch (gameTypeId) {
                case Constants.PMI_EGYPT_GAME_TYPE_ID:
                case Constants.PMI_SENEGAL_GAME_TYPE_ID:
                    filterProperties = new ArrayList<String>();
                    filterProperties.add(Constants.METADATA_KEY_PERSONAL_REGION);
                    break;
            }
        }

        ResponseListCallCard rsp = callCardService.getNewOrPendingCallCard(userId, userGroupId, gameTypeId, applicationId, callCardId, filterProperties);
        if (ResponseStatus.OK.equals(rsp.getStatus())) {
            if (rsp.getRecords() != null && rsp.getRecords().size() > 0)
                return Response.ok(rsp.getRecords()).header(TalosUtil.X_TALOS_ITEM_COUNT, rsp.getTotalRecords()).build();
            else
                return Response.noContent().build();
        } else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }

    @GET
    @Path("/{userId}/callcard")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List Pending CallCards", notes = "User ID is required", response = CallCardDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class)}
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = TalosUtil.X_TALOS_FILL_RESOURCES, allowableValues = "true", dataType = "string", value = "Inline resources", paramType = "Header"),
    })
    public Response listPendingCallCard(@ApiParam(name = TalosUtil.X_TALOS_USER_GROUP_ID, value = "Get Call Cards for user with userGroupId.", required = true) @HeaderParam(TalosUtil.X_TALOS_USER_GROUP_ID) String userGroupId,
                                        @ApiParam(name = "userId", value = "Get Call Cards for user with userId.", required = true) @PathParam("userId") String userId,
                                        @ApiParam(name = "gameTypeId", value = "Query Call Cards by gameTypeId.", required = true) @QueryParam("gameTypeId") String gameTypeId) throws BusinessLayerException {

        LOGGER.debug("listPendingCallCard");

        Assert.notNullOrEmpty(userId, "Provide a userId...");
        Assert.isValidUUID(userId, "Provide a valid userId...");
        Assert.notNullOrEmpty(gameTypeId, "Provide a gameTypeId...");
        Assert.isValidUUID(gameTypeId, "Provide a valid gameTypeId...");
        Assert.notNullOrEmpty(userGroupId, "Provide a userGroupId...");
        Assert.isValidUUID(userGroupId, "Provide a valid userGroupId...");

        ResponseListCallCard rsp = callCardService.listPendingCallCard(userId, userGroupId, gameTypeId);
        if (ResponseStatus.OK.equals(rsp.getStatus())) {
            if (rsp.getRecords() != null && rsp.getRecords().size() > 0)
                return Response.ok(rsp.getRecords()).header(TalosUtil.X_TALOS_ITEM_COUNT, rsp.getTotalRecords()).build();
            else
                return Response.noContent().build();
        } else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }

    @POST
    @Path("/update/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add CallCard records", notes = "User ID is required", response = CallCardDTO.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class),
            @ApiResponse(code = 1073, message = "Item belongs to other user", response = BusinessLayerException.class)
    })
    public Response addCallCardRecords(@ApiParam(name = TalosUtil.X_TALOS_SESSION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_SESSION_ID) String userSessionId,
                                       @ApiParam(name = TalosUtil.X_TALOS_APPLICATION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_APPLICATION_ID) String applicationId,
                                       @ApiParam(name = "userId", value = "Query Call Cards by userId.", required = true) @PathParam("userId") String userId,
                                       @ApiParam(value = "Value of 'mandatory' in message body must be set", required = true) CallCardDTO input) throws BusinessLayerException {

        Assert.notNullOrEmpty(userSessionId, "userSessionId shall not be null or empty");
        Assert.isValidUUID(userSessionId, "userSessionId shall be a valid UUID");

        UserSessionDTOS userSession = null;
        try {
            userSession = gameInternalService.getUserSession(userSessionId);
            if (userSession.getUser() != null) {
            } else {
                throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
            }
        } catch (Exception e) {
            throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
        }

        ResponseListCallCard rsp = callCardService.addCallCardRecords(userSession.getUser().getUserGroupId(), userSession.getGameTypeId(), userSession.getApplicationId(), userId, Arrays.asList(input));
        if (ResponseStatus.OK.equals(rsp.getStatus())) {
            if (rsp.getRecords() != null && rsp.getRecords().size() > 0)
                return Response.ok(rsp.getRecords()).header(TalosUtil.X_TALOS_ITEM_COUNT, rsp.getTotalRecords()).build();
            else
                return Response.noContent().build();
        } else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }

    @POST
    @Path("/update/{userId}/multiple")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add CallCard records", notes = "User ID is required", response = CallCardDTO.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class),
            @ApiResponse(code = 1073, message = "Item belongs to other user", response = BusinessLayerException.class)
    })
    public Response addMultipleCallCardRecords(@ApiParam(name = TalosUtil.X_TALOS_SESSION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_SESSION_ID) String userSessionId,
                                               @ApiParam(name = TalosUtil.X_TALOS_APPLICATION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_APPLICATION_ID) String applicationId,
                                               @ApiParam(name = "userId", value = "Query Call Cards by userId.", required = true) @PathParam("userId") String userId,
                                               @ApiParam(value = "Value of 'mandatory' in message body must be set", required = true) List<CallCardDTO> input) throws BusinessLayerException {

        Assert.notNullOrEmpty(userSessionId, "userSessionId shall not be null or empty");
        Assert.isValidUUID(userSessionId, "userSessionId shall be a valid UUID");

        UserSessionDTOS userSession = null;
        try {
            userSession = gameInternalService.getUserSession(userSessionId);
            if (userSession == null)
                throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
        } catch (Exception e) {
            throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
        }

        ResponseListCallCard rsp = callCardService.addCallCardRecords(userSession.getUser().getUserGroupId(), userSession.getGameTypeId(), userSession.getApplicationId(), userId, input);
        if (ResponseStatus.OK.equals(rsp.getStatus())) {
            if (rsp.getRecords() != null && rsp.getRecords().size() > 0)
                return Response.ok(rsp.getRecords()).header(TalosUtil.X_TALOS_ITEM_COUNT, rsp.getTotalRecords()).build();
            else
                return Response.noContent().build();
        } else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }

    @POST
    @Path("/transactions/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Submit individual CallCard records", notes = "User ID is required", response = CallCardDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class),
            @ApiResponse(code = 1073, message = "Item belongs to other user", response = BusinessLayerException.class)
    })
    public Response submitTransactions(@ApiParam(name = TalosUtil.X_TALOS_SESSION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_SESSION_ID) String userSessionId,
                                       @ApiParam(name = TalosUtil.X_TALOS_USER_GROUP_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_USER_GROUP_ID) String userGroupId,
                                       @ApiParam(name = TalosUtil.X_TALOS_APPLICATION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_APPLICATION_ID) String applicationId,
                                       @ApiParam(name = "userId", value = "Query Call Cards by userId.", required = true) @PathParam("userId") String userId,
                                       @ApiParam(value = "Value of 'mandatory' in message body must be set", required = true) CallCardDTO input) throws BusinessLayerException {

        Assert.notNullOrEmpty(userSessionId, "userSessionId shall not be null or empty");
        Assert.isValidUUID(userSessionId, "userSessionId shall be a valid UUID");
        Assert.notNull(input, "Provide an input...");

        UserSessionDTOS userSession = null;
        try {
            userSession = gameInternalService.getUserSession(userSessionId);
            if (userSession.getUser() != null) {
            } else {
                throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
            }
        } catch (Exception e) {
            throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
        }

        String gameTypeId = userSession.getGameTypeId();

        WSResponse rsp = callCardService.submitTransactions( userSession.getUser().getUserId(), userGroupId, gameTypeId, applicationId, userId, input);
        if (ResponseStatus.OK.equals(rsp.getStatus()))
            return Response.noContent().build();
        else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }

    @POST
    @Path("/update/simplified/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add Simplified CallCard records", response = SimplifiedCallCardDTO.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Invalid user ID supplied",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 404, message = "CallCard not found", response = BusinessLayerException.class),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = CallCardDTO.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class),
            @ApiResponse(code = 1070, message = "CMS Configuration Error", response = BusinessLayerException.class),
            @ApiResponse(code = 1073, message = "Item belongs to other user", response = BusinessLayerException.class)
    })
    public Response addSimplifiedCallCard(@ApiParam(name = TalosUtil.X_TALOS_SESSION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_SESSION_ID) String userSessionId,
                                          @ApiParam(name = TalosUtil.X_TALOS_APPLICATION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_APPLICATION_ID) String applicationId,
                                          @ApiParam(name = "userId", required = true) @PathParam("userId") String userId,
                                          @ApiParam(required = true) SimplifiedCallCardDTO input) throws BusinessLayerException {

        Assert.notNullOrEmpty(userSessionId, "userSessionId shall not be null or empty");
        Assert.isValidUUID(userSessionId, "userSessionId shall be a valid UUID");

        UserSessionDTOS userSession = null;
        try {
            userSession = gameInternalService.getUserSession(userSessionId);
            if (userSession.getUser() != null) {
            } else {
                throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
            }
        } catch (Exception e) {
            throw new BusinessLayerException("invalid userSession", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
        }

        WSResponse rsp = callCardService.addOrUpdateSimplifiedCallCard(userSession.getUser().getUserGroupId(), userSession.getGameTypeId(), userSession.getApplicationId(), userId, input);
        if (ResponseStatus.OK.equals(rsp.getStatus())) {
            return Response.noContent().build();
        } else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }


    @GET
    @Path("/list/simplified")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List simplified callCards records", response = SimplifiedCallCardDTO.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 400, message = "Bad request",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = ResponseListSimplifiedCallCard.class)),
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = @ResponseHeader(name = "X-Talos-Item-Count", response = ResponseListSimplifiedCallCard.class)),
            @ApiResponse(code = 204, message = "NoContent", response = Response.class)})
    public Response listSimplifiedCallCards(@ApiParam(name = TalosUtil.X_TALOS_SESSION_ID, required = true) @HeaderParam(TalosUtil.X_TALOS_SESSION_ID) String userSessionId,
                                            @QueryParam("sourceUserId") String sourceUserId,
                                            @QueryParam("refUserId") String refUserId,
                                            @QueryParam("dateFrom") Date dateFrom,
                                            @QueryParam("dateTo") Date dateTo,
                                            @QueryParam("rangeFrom") @DefaultValue("0") int rangeFrom,
                                            @QueryParam("rangeTo") @DefaultValue("10") int rangeTo) throws BusinessLayerException {

        Assert.notNullOrEmpty(userSessionId, "userSessionId shall not be null or empty");
        Assert.isValidUUID(userSessionId, "userSessionId shall be a valid UUID");

        UserSessionDTOS userSession = null;
        try {
            userSession = gameInternalService.getUserSession(userSessionId);
            if (userSession == null)
                throw new BusinessLayerException("Error while getting user's invoice history", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
        } catch (Exception e) {
            throw new BusinessLayerException("Error while getting user's invoice history ", ExceptionTypeTO.USER_SESSION_ID_NOT_VALID);
        }

        ResponseListSimplifiedCallCard rsp = callCardService.listSimplifiedCallCards(
                userSession.getUser().getUserId(),
                sourceUserId,
                refUserId,
                dateFrom,
                dateTo,
                rangeFrom,
                rangeTo);

        if (ResponseStatus.OK.equals(rsp.getStatus())) {
            if (rsp.getRecords() != null && rsp.getRecords().size() > 0)
                return Response.ok(rsp.getRecords()).header(TalosUtil.X_TALOS_ITEM_COUNT, rsp.getTotalRecords()).build();
            else
                return Response.noContent().build();
        } else
            throw new BusinessLayerException(rsp.getResult(), ExceptionTypeTO.valueOf(rsp.getErrorNumber()));
    }

}
