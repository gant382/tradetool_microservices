package com.saicon.games.callcard.ws;

import com.saicon.games.callcard.ws.dto.CallCardBulkResponseDTO;
import com.saicon.games.callcard.ws.dto.CallCardSummaryDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import org.apache.cxf.annotations.FastInfoset;
import org.apache.cxf.annotations.GZIP;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

/**
 * Simplified CallCard Service V2 - Optimized for mobile clients
 *
 * Features:
 * - Reduced payload sizes (60-90% smaller)
 * - Pagination support
 * - Bulk operations with single query
 * - GZIP compression enabled
 * - FastInfoset binary XML support
 *
 * Target: Mobile applications with bandwidth constraints
 */
@WebService(
        targetNamespace = "http://ws.callcard.saicon.com/v2/",
        name = "SimplifiedCallCardService"
)
@FastInfoset
@GZIP
public interface ISimplifiedCallCardService {

    /**
     * Get a single simplified CallCard by ID
     *
     * @param callCardId The CallCard ID
     * @return Simplified CallCard V2 DTO
     */
    @WebMethod(operationName = "getSimplifiedCallCard")
    SimplifiedCallCardDTO getSimplifiedCallCard(
            @WebParam(name = "callCardId") String callCardId
    );

    /**
     * Get paginated list of simplified CallCards with optional filters
     *
     * @param userId       Filter by user ID (optional)
     * @param userGroupId  Filter by user group ID (optional)
     * @param templateId   Filter by template ID (optional)
     * @param status       Filter by status (active/inactive) (optional)
     * @param submitted    Filter by submitted flag (optional)
     * @param page         Page number (1-based)
     * @param pageSize     Number of items per page
     * @return Bulk response with pagination metadata
     */
    @WebMethod(operationName = "getSimplifiedCallCardList")
    CallCardBulkResponseDTO getSimplifiedCallCardList(
            @WebParam(name = "userId") String userId,
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "status") String status,
            @WebParam(name = "submitted") Boolean submitted,
            @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize
    );

    /**
     * Get ultra-minimal CallCard summaries for list views
     *
     * @param userGroupId Filter by user group ID
     * @param page        Page number (1-based)
     * @param pageSize    Number of items per page
     * @return List of summary DTOs
     */
    @WebMethod(operationName = "getCallCardSummaries")
    List<CallCardSummaryDTO> getCallCardSummaries(
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize
    );

    /**
     * Bulk fetch simplified CallCards by IDs (single query)
     *
     * @param callCardIds List of CallCard IDs
     * @return Bulk response with requested CallCards
     */
    @WebMethod(operationName = "bulkGetSimplifiedCallCards")
    CallCardBulkResponseDTO bulkGetSimplifiedCallCards(
            @WebParam(name = "callCardIds") List<String> callCardIds
    );

    /**
     * Get simplified CallCards by template ID with pagination
     *
     * @param templateId   Template ID to filter by
     * @param includeInactive Include inactive CallCards
     * @param page         Page number (1-based)
     * @param pageSize     Number of items per page
     * @return Bulk response with CallCards from template
     */
    @WebMethod(operationName = "getSimplifiedCallCardsByTemplate")
    CallCardBulkResponseDTO getSimplifiedCallCardsByTemplate(
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "includeInactive") boolean includeInactive,
            @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize
    );

    /**
     * Get simplified CallCards by user with pagination
     *
     * @param userId       User ID to filter by
     * @param includeInactive Include inactive CallCards
     * @param page         Page number (1-based)
     * @param pageSize     Number of items per page
     * @return Bulk response with user's CallCards
     */
    @WebMethod(operationName = "getSimplifiedCallCardsByUser")
    CallCardBulkResponseDTO getSimplifiedCallCardsByUser(
            @WebParam(name = "userId") String userId,
            @WebParam(name = "includeInactive") boolean includeInactive,
            @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize
    );

    /**
     * Search CallCards with minimal payload (summaries only)
     *
     * @param searchTerm   Search term (name, ref no, etc.)
     * @param userGroupId  Filter by user group ID
     * @param page         Page number (1-based)
     * @param pageSize     Number of items per page
     * @return List of matching CallCard summaries
     */
    @WebMethod(operationName = "searchCallCardSummaries")
    List<CallCardSummaryDTO> searchCallCardSummaries(
            @WebParam(name = "searchTerm") String searchTerm,
            @WebParam(name = "userGroupId") String userGroupId,
            @WebParam(name = "page") int page,
            @WebParam(name = "pageSize") int pageSize
    );
}
