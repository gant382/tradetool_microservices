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
    public static final String PMI_EGYPT_GAME_TYPE_ID = "pmi-egypt";
    public static final String PMI_SENEGAL_GAME_TYPE_ID = "pmi-senegal";
    public static final String PMI_IRAQ_GAME_TYPE_ID = "pmi-iraq";

    // Metadata keys
    public static final String METADATA_KEY_PERSONAL_REGION = "personalRegion";
    public static final String METADATA_KEY_CALL_CARD_INDEX_SALES = "callCardIndexSales";
    public static final String METADATA_KEY_PERSONAL_ADDRESS = "personalAddress";
    public static final String METADATA_KEY_PERSONAL_CITY = "personalCity";
    public static final String METADATA_KEY_PERSONAL_COUNTRY = "personalCountry";
    public static final String METADATA_KEY_PERSONAL_STATE = "personalState";

    // Item type for call card refuser (legacy)
    public static final int ITEM_TYPE_CALL_CARD_REFUSER = 1005;

    // App Setting Keys
    public static final String APP_SETTING_KEY_PREVIOUS_VISITS_SUMMARY = "PREVIOUS_VISITS_SUMMARY";
    public static final String APP_SETTING_KEY_INCLUDE_VISITS_GEO_INFO = "INCLUDE_VISITS_GEO_INFO";
    public static final String APP_SETTING_KEY_PRODUCT_TYPE_CATEGORIES = "PRODUCT_TYPE_CATEGORIES";

    // Additional Item Types
    public static final int ITEM_TYPE_CALL_CARD_INDEX = 1001;
    public static final int ITEM_TYPE_BRAND_PRODUCT = 1002;
    public static final int ITEM_TYPE_QUIZ = 1003;

    private Constants() {
        // Prevent instantiation
    }
}
