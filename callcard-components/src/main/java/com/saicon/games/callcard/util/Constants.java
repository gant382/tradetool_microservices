package com.saicon.games.callcard.util;

public class Constants {
    // Metadata keys
    public static final String METADATA_KEY_PERSONAL_REGION = "PERSONAL_REGION";
    public static final String METADATA_KEY_PERSONAL_ADDRESS = "PERSONAL_ADDRESS";
    public static final String METADATA_KEY_PERSONAL_CITY = "PERSONAL_CITY";
    public static final String METADATA_KEY_PERSONAL_COUNTRY = "PERSONAL_COUNTRY";
    public static final String METADATA_KEY_PERSONAL_STATE = "PERSONAL_STATE";
    public static final String METADATA_KEY_CALL_CARD_INDEX_SALES = "callCardIndexSales";

    // Item types
    public static final int ITEM_TYPE_CALL_CARD = 1000;
    public static final int ITEM_TYPE_CALL_CARD_INDEX = 1001;
    public static final int ITEM_TYPE_BRAND_PRODUCT = 1002;
    public static final int ITEM_TYPE_QUIZ = 1003;
    public static final int ITEM_TYPE_CALL_CARD_REFUSER = 1005;
    // PMI Game Type IDs (duplicated from ws-api for module independence)
    public static final String PMI_EGYPT_GAME_TYPE_ID = "pmi-egypt";
    public static final String PMI_SENEGAL_GAME_TYPE_ID = "pmi-senegal";
    public static final String PMI_IRAQ_GAME_TYPE_ID = "pmi-iraq";
}
