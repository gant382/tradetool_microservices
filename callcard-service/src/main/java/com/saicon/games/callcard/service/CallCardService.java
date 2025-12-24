package com.saicon.games.callcard.service;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import com.saicon.games.callcard.ws.dto.ItemStatisticsDTO;
import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.exception.ExceptionTypeTO;
import com.saicon.games.callcard.components.ICallCardManagement;
import com.saicon.games.callcard.components.external.IUserSessionManagement;
import com.saicon.games.callcard.ws.response.ResponseListItemStatistics;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import com.saicon.games.callcard.ws.response.WSResponse;
import com.saicon.games.callcard.ws.data.ResponseListCallCard;
import com.saicon.games.callcard.ws.data.ResponseListSimplifiedCallCard;
import com.saicon.games.callcard.ws.ICallCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.util.*;

/**
 * Created by user101 on 10/2/2016.
 */

@WebService(
        targetNamespace = "http://ws.callCard.saicon.com/",
        portName = "CallCardServicePort",
        endpointInterface = "com.saicon.games.callcard.ws.ICallCardService",
        serviceName = "CallCardService"
)
public class CallCardService implements ICallCardService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CallCardService.class);

    private ICallCardManagement callCardManagement;

    private IUserSessionManagement userSessionManagement;

    @Override
    public ResponseListCallCard addCallCardRecords(String userGroupId, String gameTypeId, String applicationId, String userId, List<CallCardDTO> callCards) {
        ResponseListCallCard toReturn;
        try {
            CallCardDTO callCardDTO = callCardManagement.updateCallCard(userGroupId, gameTypeId, applicationId, userId, callCards);

            toReturn = new ResponseListCallCard("", ResponseStatus.OK, callCardDTO != null ? Arrays.asList(callCardDTO) : null, callCardDTO != null ? 1 : 0);
        } catch (BusinessLayerException e) {
            return new ResponseListCallCard(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR, null, 0);
        }

        return toReturn;
    }

    @Override
    public WSResponse addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, SimplifiedCallCardDTO callCard) {
        WSResponse toReturn;
        try {
            callCardManagement.addOrUpdateSimplifiedCallCard(userGroupId, gameTypeId, applicationId, userId, callCard);

            toReturn = new WSResponse("", ResponseStatus.OK);
        } catch (BusinessLayerException e) {
            return new WSResponse(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR);
        }

        return toReturn;
    }

    @Override
    public ResponseListSimplifiedCallCard listSimplifiedCallCards(String callCardUserId, String sourceUserId, String refUserId, Date dateFrom, Date dateTo, int rangeFrom, int rangeTo) {

        try {
            List<SimplifiedCallCardDTO> simplifiedCallCardDTOs = callCardManagement.listSimplifiedCallCards(callCardUserId, sourceUserId, refUserId, dateFrom, dateTo, rangeFrom, rangeTo);

            int totalItemCount = 0;
            if (simplifiedCallCardDTOs != null && simplifiedCallCardDTOs.size() > 0)
                totalItemCount = callCardManagement.countSimplifiedCallCards(callCardUserId, sourceUserId, refUserId, dateFrom, dateTo);

            return new ResponseListSimplifiedCallCard("", ResponseStatus.OK, simplifiedCallCardDTOs, totalItemCount);
        } catch (BusinessLayerException e) {
            LOGGER.error("Could not list SimplifiedCallCards.", e);

            return new ResponseListSimplifiedCallCard(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR, null, 0);
        }
    }


    @Override
    public ResponseListCallCard getCallCardsFromTemplate(String userId, String userGroupId, String gameTypeId, String applicationId) {
        try {
            List<CallCardDTO> callCardDTOList;

            callCardDTOList = callCardManagement.getCallCardsFromTemplate(userId, userGroupId, gameTypeId, applicationId);

            return new ResponseListCallCard("", ResponseStatus.OK, callCardDTOList, callCardDTOList != null ? callCardDTOList.size() : 0);
        } catch (BusinessLayerException e) {
            return new ResponseListCallCard(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR, null, 0);
        }
    }

    @Override
    public ResponseListCallCard getPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId) {
        try {
            List<CallCardDTO> callCardDTOList = null;
            int totalCallCard = 0;

            CallCardDTO callCardDTO = callCardManagement.getPendingCallCard(userId, userGroupId, gameTypeId, applicationId);
            if (callCardDTO != null) {
                callCardDTOList = new ArrayList<CallCardDTO>();
                callCardDTOList.add(callCardDTO);
                totalCallCard++;
            }

            return new ResponseListCallCard("", ResponseStatus.OK, callCardDTOList, totalCallCard);
        } catch (BusinessLayerException e) {
            return new ResponseListCallCard(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR, null, 0);
        }
    }

    @Override
    public ResponseListCallCard getNewOrPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId, String callCardId, List<String> filterProperties) {
        try {
            List<CallCardDTO> callCardDTOList = null;
            int totalCallCard = 0;

            CallCardDTO callCardDTO = callCardManagement.getNewOrPendingCallCard(userId, userGroupId, gameTypeId, applicationId, callCardId, filterProperties);
            if (callCardDTO != null) {
                callCardDTOList = new ArrayList<CallCardDTO>();
                callCardDTOList.add(callCardDTO);
                totalCallCard++;
            }

            return new ResponseListCallCard("", ResponseStatus.OK, callCardDTOList, totalCallCard);
        } catch (BusinessLayerException e) {
            return new ResponseListCallCard(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR, null, 0);
        }
    }

    @Override
    public ResponseListCallCard listPendingCallCard(String userId, String userGroupId, String gameTypeId) {
        try {
            List<CallCardDTO> callCardDTOList = null;
            int totalCallCard = 0;

            CallCardDTO callCardDTO = callCardManagement.listPendingCallCard(userId, userGroupId, gameTypeId);
            if (callCardDTO != null) {
                callCardDTOList = new ArrayList<CallCardDTO>();
                callCardDTOList.add(callCardDTO);
                totalCallCard++;
            }

            return new ResponseListCallCard("", ResponseStatus.OK, callCardDTOList, totalCallCard);
        } catch (BusinessLayerException e) {
            return new ResponseListCallCard(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR, null, 0);
        }
    }

    @Override
    public WSResponse submitTransactions( String userId, String userGroupId, String gameTypeId, String applicationId, String indirectUserId, CallCardDTO callCardDTO) {

        try {
            callCardManagement.submitTransactions( userId, userGroupId, gameTypeId, applicationId, indirectUserId, callCardDTO);
            if (callCardDTO == null)
                return new WSResponse(ExceptionTypeTO.CMS_CONFIGURATION_ERROR, "", ResponseStatus.ERROR);

            return new WSResponse(ExceptionTypeTO.NONE, "", ResponseStatus.OK);
        } catch (BusinessLayerException e) {
            LOGGER.error("Could not submitTransactions.", e);

            return new WSResponse(Integer.parseInt(e.getErrorCode()), e.getMessage(), ResponseStatus.ERROR);
        }
    }

    public ICallCardManagement getCallCardManagement() {
        return callCardManagement;
    }

    public void setCallCardManagement(ICallCardManagement callCardManagement) {
        this.callCardManagement = callCardManagement;
    }

    public IUserSessionManagement getUserSessionManagement() {
        return userSessionManagement;
    }

    public void setUserSessionManagement(IUserSessionManagement userSessionManagement) {
        this.userSessionManagement = userSessionManagement;
    }
}
