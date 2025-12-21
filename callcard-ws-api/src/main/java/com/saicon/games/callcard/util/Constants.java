package com.saicon.games.callcard.util;

/**
 * Constants for CallCard microservice.
 * Item type identifiers used for resource lookups.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public class Constants {

    /**
     * Item type for CallCard template
     */
    public static final int ITEM_TYPE_CALL_CARD_TEMPLATE = 1001;

    /**
     * Item type for CallCard instance
     */
    public static final int ITEM_TYPE_CALL_CARD = 1002;

    /**
     * Item type for CallCard POS group
     */
    public static final int ITEM_TYPE_CALL_CARD_POS_GROUP = 1003;

    /**
     * Item type for CallCard action item
     */
    public static final int ITEM_TYPE_CALL_CARD_ACTION_ITEM = 1004;

    // Game type IDs
    public static final String PMI_EGYPT_GAME_TYPE_ID = "pmi-egypt-game-type-id";
    public static final String PMI_SENEGAL_GAME_TYPE_ID = "pmi-senegal-game-type-id";

    // Metadata keys
    public static final String METADATA_KEY_PERSONAL_REGION = "personalRegion";

    private Constants() {
        // Prevent instantiation
    }
}
