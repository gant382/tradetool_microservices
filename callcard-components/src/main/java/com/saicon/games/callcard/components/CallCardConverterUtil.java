package com.saicon.games.callcard.components;

import com.saicon.games.callcard.entity.CallCard;
import com.saicon.games.callcard.entity.CallCardRefUser;
import com.saicon.games.callcard.entity.CallCardTemplate;
import com.saicon.games.callcard.ws.dto.CallCardSummaryDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardRefUserV2DTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardV2DTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Converter utility for CallCard entities to simplified V2 DTOs
 *
 * Features:
 * - Entity to DTO conversion with minimal overhead
 * - ISO 8601 date formatting
 * - Null-safe conversions
 * - Projection-friendly (can work with partial entities)
 */
public class CallCardConverterUtil {

    private static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Convert CallCard entity to SimplifiedCallCardV2DTO
     *
     * @param entity CallCard entity
     * @return Simplified V2 DTO
     */
    public static SimplifiedCallCardV2DTO toSimplifiedV2DTO(CallCard entity) {
        if (entity == null) {
            return null;
        }

        SimplifiedCallCardV2DTO dto = new SimplifiedCallCardV2DTO();
        dto.setId(entity.getCallCardId());

        // Template info
        if (entity.getCallCardTemplateId() != null) {
            dto.setTemplateId(entity.getCallCardTemplateId().getCallCardTemplateId());
            dto.setTemplateName(entity.getCallCardTemplateId().getDescription());
            dto.setName(entity.getCallCardTemplateId().getDescription());
        }

        // Status (derived from active flag)
        dto.setStatus(entity.isActive() ? "ACTIVE" : "INACTIVE");
        dto.setActive(entity.isActive());

        // Check if submitted (has end date)
        dto.setSubmitted(entity.getEndDate() != null);

        // Dates
        dto.setCreatedDate(formatDate(entity.getStartDate()));
        dto.setLastModified(formatDate(entity.getLastUpdated()));

        // User count (instead of full user list)
        if (entity.getCallCardIndices() != null) {
            dto.setAssignedUserCount(entity.getCallCardIndices().size());
        } else {
            dto.setAssignedUserCount(0);
        }

        // Optional fields
        dto.setComments(entity.getComments());
        dto.setInternalRefNo(entity.getInternalRefNo());

        return dto;
    }

    /**
     * Convert CallCard entity to CallCardSummaryDTO (ultra-minimal)
     *
     * @param entity CallCard entity
     * @return Summary DTO
     */
    public static CallCardSummaryDTO toSummaryDTO(CallCard entity) {
        if (entity == null) {
            return null;
        }

        CallCardSummaryDTO dto = new CallCardSummaryDTO();
        dto.setId(entity.getCallCardId());

        // Template name
        if (entity.getCallCardTemplateId() != null) {
            dto.setTemplateName(entity.getCallCardTemplateId().getDescription());
            dto.setName(entity.getCallCardTemplateId().getDescription());
        }

        // Status
        dto.setStatus(entity.isActive() ? "ACTIVE" : "INACTIVE");
        dto.setActive(entity.isActive());
        dto.setSubmitted(entity.getEndDate() != null);

        // User count
        if (entity.getCallCardIndices() != null) {
            dto.setUserCount(entity.getCallCardIndices().size());
        } else {
            dto.setUserCount(0);
        }

        // Last modified
        dto.setLastModified(formatDate(entity.getLastUpdated()));

        return dto;
    }

    /**
     * Convert CallCardRefUser entity to SimplifiedCallCardRefUserV2DTO
     *
     * @param entity CallCardRefUser entity
     * @return Simplified V2 DTO
     */
    public static SimplifiedCallCardRefUserV2DTO toSimplifiedRefUserV2DTO(CallCardRefUser entity) {
        if (entity == null) {
            return null;
        }

        SimplifiedCallCardRefUserV2DTO dto = new SimplifiedCallCardRefUserV2DTO();
        dto.setId(entity.getCallCardRefUserId());

        // User info (issuer and recipient)
        if (entity.getIssuerUserId() != null) {
            dto.setIssuerUserId(entity.getIssuerUserId().getUserId());
            dto.setIssuerUserName(entity.getIssuerUserId().getUsername());
        }

        if (entity.getRecipientUserId() != null) {
            dto.setRecipientUserId(entity.getRecipientUserId().getUserId());
            dto.setRecipientUserName(entity.getRecipientUserId().getUsername());
        }

        // Dates
        dto.setCreatedDate(formatDate(entity.getDateCreated()));
        dto.setLastModified(formatDate(entity.getDateUpdated()));

        // Status
        dto.setStatus(entity.getStatus() != null ? entity.getStatus() : 0);
        dto.setStatusLabel(getStatusLabel(entity.getStatus()));
        dto.setActive(entity.isActive());

        // Reference
        dto.setRefNo(entity.getRefNo());
        dto.setComment(entity.getComment());

        // Item count (instead of full item list)
        if (entity.getItems() != null) {
            dto.setItemCount(entity.getItems().size());
        } else {
            dto.setItemCount(0);
        }

        return dto;
    }

    /**
     * Convert list of CallCard entities to SimplifiedV2DTOs
     *
     * @param entities List of CallCard entities
     * @return List of simplified V2 DTOs
     */
    public static List<SimplifiedCallCardV2DTO> toSimplifiedV2DTOList(List<CallCard> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<SimplifiedCallCardV2DTO> dtos = new ArrayList<>(entities.size());
        for (CallCard entity : entities) {
            SimplifiedCallCardV2DTO dto = toSimplifiedV2DTO(entity);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }

    /**
     * Convert list of CallCard entities to SummaryDTOs
     *
     * @param entities List of CallCard entities
     * @return List of summary DTOs
     */
    public static List<CallCardSummaryDTO> toSummaryDTOList(List<CallCard> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<CallCardSummaryDTO> dtos = new ArrayList<>(entities.size());
        for (CallCard entity : entities) {
            CallCardSummaryDTO dto = toSummaryDTO(entity);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }

    /**
     * Format Date to ISO 8601 string
     *
     * @param date Date to format
     * @return ISO 8601 formatted string
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_FORMAT);
        return sdf.format(date);
    }

    /**
     * Get status label from status code
     *
     * @param status Status code
     * @return Status label
     */
    private static String getStatusLabel(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }

        switch (status) {
            case SimplifiedCallCardRefUserV2DTO.STATUS_PENDING:
                return "PENDING";
            case SimplifiedCallCardRefUserV2DTO.STATUS_APPROVED:
                return "APPROVED";
            case SimplifiedCallCardRefUserV2DTO.STATUS_REJECTED:
                return "REJECTED";
            case SimplifiedCallCardRefUserV2DTO.STATUS_COMPLETED:
                return "COMPLETED";
            default:
                return "UNKNOWN";
        }
    }
}
