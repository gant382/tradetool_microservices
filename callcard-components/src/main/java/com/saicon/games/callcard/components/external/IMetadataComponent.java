package com.saicon.games.callcard.components.external;

import java.util.List;

/**
 * Stub interface for Metadata Component.
 * To be implemented when metadata integration is required.
 */
public interface IMetadataComponent {
    /**
     * List metadata keys by item type
     * @param itemTypeId The item type ID
     * @param activeOnly Whether to return only active metadata keys
     * @return List of metadata keys
     */
    List<String> listMetadataKeysByItemType(int itemTypeId, boolean activeOnly);
}
