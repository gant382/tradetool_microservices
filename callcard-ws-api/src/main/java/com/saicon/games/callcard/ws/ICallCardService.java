package com.saicon.games.callcard.ws;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
// import com.saicon.games.ecommerce.data.ResponseListItemStatistics; // Not needed - using local response class
import com.saicon.games.callcard.ws.response.WSResponse;
import com.saicon.games.callcard.ws.data.ResponseListCallCard;
import com.saicon.games.callcard.ws.data.ResponseListSimplifiedCallCard;
import com.saicon.games.callcard.ws.data.ResponseListItemStatistics;
import org.apache.cxf.annotations.FastInfoset;
import org.apache.cxf.annotations.GZIP;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Date;
import java.util.List;

/**
 * Created by user101 on 10/2/2016.
 */

@WebService(
        targetNamespace = "http://ws.callCard.saicon.com/",
        name = "CallCardService"
)
@FastInfoset
@GZIP
public interface ICallCardService {
    @WebMethod(operationName = "listSimplifiedCallCards")
    ResponseListSimplifiedCallCard listSimplifiedCallCards( @WebParam(name = "callCardUserId") String callCardUserId,
                                                            @WebParam(name = "fromUserId") String fromUserId,
                                                            @WebParam(name = "toUserId") String toUserId,
                                                            @WebParam(name = "dateFrom") Date dateFrom,
                                                            @WebParam(name = "dateTo") Date dateTo,
                                                            @WebParam(name = "rangeFrom") int rangeFrom,
                                                            @WebParam(name = "rangeTo") int rangeTo);

    @WebMethod(operationName = "getCallCardStatistics")
    ResponseListItemStatistics getCallCardStatistics(@WebParam(name = "userId") String userId,
                                                     @WebParam(name = "propertyId") String propertyId,
                                                     @WebParam(name = "types") List<Integer> types,
                                                     @WebParam(name = "dateFrom") Date dateFrom,
                                                     @WebParam(name = "dateTo") Date dateTo);

    @WebMethod(operationName = "submitTransactions")
    WSResponse submitTransactions(@WebParam(name = "userId") String userId,
                                  @WebParam(name = "userGroupId") String userGroupId,
                                  @WebParam(name = "gameTypeId") String gameTypeId,
                                  @WebParam(name = "applicationId") String applicationId,
                                  @WebParam(name = "indirectUserId") String indirectUserId,
                                  @WebParam(name = "callCardDTO") CallCardDTO callCardDTO);

    @WebMethod(operationName = "getPendingCallCard")
    ResponseListCallCard getPendingCallCard( @WebParam(name = "userId") String userId,
                                             @WebParam(name = "userGroupId") String userGroupId,
                                             @WebParam(name = "gameTypeId") String gameTypeId,
                                             @WebParam(name = "applicationId") String applicationId);

    @WebMethod(operationName = "getNewOrPendingCallCard")
    ResponseListCallCard getNewOrPendingCallCard( @WebParam(name = "userId") String userId,
                                                  @WebParam(name = "userGroupId") String userGroupId,
                                                  @WebParam(name = "gameTypeId") String gameTypeId,
                                                  @WebParam(name = "applicationId") String applicationId,
                                                  @WebParam(name = "callCardId") String callCardId,
                                                  @WebParam(name = "filterProperties") List<String> filterProperties);

    @WebMethod(operationName = "getCallCardsFromTemplate")
    ResponseListCallCard getCallCardsFromTemplate( @WebParam(name = "userId") String userId,
                                                   @WebParam(name = "userGroupId") String userGroupId,
                                                   @WebParam(name = "gameTypeId") String gameTypeId,
                                                   @WebParam(name = "applicationId") String applicationId);

    @WebMethod(operationName = "listPendingCallCard")
    ResponseListCallCard listPendingCallCard( @WebParam(name = "userId") String userId,
                                                     @WebParam(name = "userGroupId") String userGroupId,
                                                     @WebParam(name = "gameTypeId") String gameTypeId);

    @WebMethod(operationName = "addCallCardRecords")
    ResponseListCallCard addCallCardRecords(@WebParam(name = "userGroupId") String userGroupId,
                                            @WebParam(name = "gameTypeId") String gameTypeId,
                                            @WebParam(name = "applicationId") String applicationId,
                                            @WebParam(name = "userId") String userId,
                                            @WebParam(name = "callCard") List<CallCardDTO> callCard);

    @WebMethod(operationName = "addOrUpdateSimplifiedCallCard")
    WSResponse addOrUpdateSimplifiedCallCard(@WebParam(name = "userGroupId") String userGroupId,
                                             @WebParam(name = "gameTypeId") String gameTypeId,
                                             @WebParam(name = "applicationId") String applicationId,
                                             @WebParam(name = "userId") String userId,
                                             @WebParam(name = "callCard") SimplifiedCallCardDTO callCard);

}
