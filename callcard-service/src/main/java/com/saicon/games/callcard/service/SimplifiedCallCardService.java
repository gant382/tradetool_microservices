package com.saicon.games.callcard.service;

import com.saicon.games.callcard.components.ICallCardManagement;
import com.saicon.games.callcard.ws.ISimplifiedCallCardService;
import com.saicon.games.callcard.ws.dto.CallCardBulkResponseDTO;
import com.saicon.games.callcard.ws.dto.CallCardSummaryDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified CallCard Service Implementation V2
 *
 * Features:
 * - Optimized queries with projections
 * - Pagination support
 * - Bulk operations
 * - Minimal payload sizes
 * - GZIP compression (configured via @GZIP on interface)
 *
 * Performance:
 * - Uses JPA projection queries (SELECT NEW DTO)
 * - Avoids N+1 query problems
 * - Lazy loading prevention
 * - Query result caching where appropriate
 */
@WebService(
        targetNamespace = "http://ws.callcard.saicon.com/v2/",
        portName = "SimplifiedCallCardServicePort",
        endpointInterface = "com.saicon.games.callcard.ws.ISimplifiedCallCardService",
        serviceName = "SimplifiedCallCardService"
)
public class SimplifiedCallCardService implements ISimplifiedCallCardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedCallCardService.class);

    private ICallCardManagement callCardManagement;

    @Override
    public SimplifiedCallCardDTO getSimplifiedCallCard(String callCardId) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Getting simplified CallCard: {}", callCardId);

            SimplifiedCallCardDTO result = callCardManagement.getSimplifiedCallCardV2(callCardId);

            long executionTime = System.currentTimeMillis() - startTime;
            LOGGER.debug("Retrieved simplified CallCard in {}ms", executionTime);

            return result;
        } catch (Exception e) {
            LOGGER.error("Error getting simplified CallCard: {}", callCardId, e);
            return null;
        }
    }

    @Override
    public CallCardBulkResponseDTO getSimplifiedCallCardList(String userId, String userGroupId,
                                                               String templateId, String status,
                                                               Boolean submitted, int page, int pageSize) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Getting simplified CallCard list - page: {}, size: {}", page, pageSize);

            // Validate pagination parameters
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20; // Default to 20, max 100

            // Get total count first
            int totalCount = callCardManagement.countSimplifiedCallCardsV2(
                    userId, userGroupId, templateId, status, submitted
            );

            // Get paginated results
            List<SimplifiedCallCardDTO> callCards = callCardManagement.getSimplifiedCallCardListV2(
                    userId, userGroupId, templateId, status, submitted, page, pageSize
            );

            // Build response
            CallCardBulkResponseDTO response = new CallCardBulkResponseDTO(
                    callCards, totalCount, page, pageSize
            );

            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTimeMs(executionTime);
            response.setQueryTime(Instant.now().toString());

            LOGGER.debug("Retrieved {} CallCards (total: {}) in {}ms",
                    callCards.size(), totalCount, executionTime);

            return response;
        } catch (Exception e) {
            LOGGER.error("Error getting simplified CallCard list", e);

            CallCardBulkResponseDTO errorResponse = new CallCardBulkResponseDTO();
            errorResponse.addError("Error retrieving CallCards: " + e.getMessage());
            errorResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    @Override
    public List<CallCardSummaryDTO> getCallCardSummaries(String userGroupId, int page, int pageSize) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Getting CallCard summaries for userGroup: {}", userGroupId);

            // Validate pagination
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20;

            List<CallCardSummaryDTO> summaries = callCardManagement.getCallCardSummaries(
                    userGroupId, page, pageSize
            );

            long executionTime = System.currentTimeMillis() - startTime;
            LOGGER.debug("Retrieved {} summaries in {}ms", summaries.size(), executionTime);

            return summaries;
        } catch (Exception e) {
            LOGGER.error("Error getting CallCard summaries", e);
            return new ArrayList<>();
        }
    }

    @Override
    public CallCardBulkResponseDTO bulkGetSimplifiedCallCards(List<String> callCardIds) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Bulk getting {} CallCards", callCardIds.size());

            if (callCardIds == null || callCardIds.isEmpty()) {
                CallCardBulkResponseDTO response = new CallCardBulkResponseDTO();
                response.addError("CallCard IDs list is empty");
                return response;
            }

            // Limit bulk fetch to 100 items
            if (callCardIds.size() > 100) {
                CallCardBulkResponseDTO response = new CallCardBulkResponseDTO();
                response.addError("Bulk fetch limited to 100 items. Requested: " + callCardIds.size());
                return response;
            }

            List<SimplifiedCallCardDTO> callCards = callCardManagement.bulkGetSimplifiedCallCardsV2(
                    callCardIds
            );

            CallCardBulkResponseDTO response = new CallCardBulkResponseDTO(
                    callCards, callCards.size(), 1, callCards.size()
            );

            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTimeMs(executionTime);
            response.setQueryTime(Instant.now().toString());

            LOGGER.debug("Bulk retrieved {} CallCards in {}ms", callCards.size(), executionTime);

            return response;
        } catch (Exception e) {
            LOGGER.error("Error bulk getting CallCards", e);

            CallCardBulkResponseDTO errorResponse = new CallCardBulkResponseDTO();
            errorResponse.addError("Error bulk retrieving CallCards: " + e.getMessage());
            errorResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    @Override
    public CallCardBulkResponseDTO getSimplifiedCallCardsByTemplate(String templateId,
                                                                      boolean includeInactive,
                                                                      int page, int pageSize) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Getting CallCards by template: {}", templateId);

            // Validate pagination
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20;

            int totalCount = callCardManagement.countCallCardsByTemplate(
                    templateId, includeInactive
            );

            List<SimplifiedCallCardDTO> callCards = callCardManagement.getSimplifiedCallCardsByTemplate(
                    templateId, includeInactive, page, pageSize
            );

            CallCardBulkResponseDTO response = new CallCardBulkResponseDTO(
                    callCards, totalCount, page, pageSize
            );

            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTimeMs(executionTime);
            response.setQueryTime(Instant.now().toString());

            LOGGER.debug("Retrieved {} CallCards by template in {}ms", callCards.size(), executionTime);

            return response;
        } catch (Exception e) {
            LOGGER.error("Error getting CallCards by template", e);

            CallCardBulkResponseDTO errorResponse = new CallCardBulkResponseDTO();
            errorResponse.addError("Error retrieving CallCards by template: " + e.getMessage());
            errorResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    @Override
    public CallCardBulkResponseDTO getSimplifiedCallCardsByUser(String userId,
                                                                  boolean includeInactive,
                                                                  int page, int pageSize) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Getting CallCards by user: {}", userId);

            // Validate pagination
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20;

            int totalCount = callCardManagement.countCallCardsByUser(
                    userId, includeInactive
            );

            List<SimplifiedCallCardDTO> callCards = callCardManagement.getSimplifiedCallCardsByUser(
                    userId, includeInactive, page, pageSize
            );

            CallCardBulkResponseDTO response = new CallCardBulkResponseDTO(
                    callCards, totalCount, page, pageSize
            );

            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTimeMs(executionTime);
            response.setQueryTime(Instant.now().toString());

            LOGGER.debug("Retrieved {} CallCards by user in {}ms", callCards.size(), executionTime);

            return response;
        } catch (Exception e) {
            LOGGER.error("Error getting CallCards by user", e);

            CallCardBulkResponseDTO errorResponse = new CallCardBulkResponseDTO();
            errorResponse.addError("Error retrieving CallCards by user: " + e.getMessage());
            errorResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    @Override
    public List<CallCardSummaryDTO> searchCallCardSummaries(String searchTerm, String userGroupId,
                                                             int page, int pageSize) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Searching CallCards with term: {}", searchTerm);

            // Validate pagination
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20;

            List<CallCardSummaryDTO> summaries = callCardManagement.searchCallCardSummaries(
                    searchTerm, userGroupId, page, pageSize
            );

            long executionTime = System.currentTimeMillis() - startTime;
            LOGGER.debug("Found {} CallCards in {}ms", summaries.size(), executionTime);

            return summaries;
        } catch (Exception e) {
            LOGGER.error("Error searching CallCards", e);
            return new ArrayList<>();
        }
    }

    // Dependency injection
    public ICallCardManagement getCallCardManagement() {
        return callCardManagement;
    }

    public void setCallCardManagement(ICallCardManagement callCardManagement) {
        this.callCardManagement = callCardManagement;
    }
}
