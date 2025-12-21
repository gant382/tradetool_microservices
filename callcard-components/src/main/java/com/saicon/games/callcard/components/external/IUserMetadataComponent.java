package com.saicon.games.callcard.components.external;

import java.util.List;
import java.util.Map;

/**
 * Stub interface for User Metadata Component.
 * To be implemented when user metadata integration is required.
 */
public interface IUserMetadataComponent {
    /**
     * List user metadata by user IDs and metadata keys
     * @param userIds List of user IDs
     * @param metadataKeys List of metadata keys to retrieve
     * @param activeOnly Whether to return only active metadata
     * @return Map of user ID to metadata key-value pairs
     */
    Map<String, Map<String, String>> listUserMetadata(List<String> userIds, List<String> metadataKeys, boolean activeOnly);
}
