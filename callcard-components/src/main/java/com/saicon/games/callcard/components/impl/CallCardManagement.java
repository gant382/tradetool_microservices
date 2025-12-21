package com.saicon.games.callcard.components.impl;

// TODO: Addressbook entities stub needed
// import com.saicon.addressbook.entities.*;
import com.saicon.games.entities.shared.Application;
import com.saicon.games.callcard.util.ScopeType;
import com.saicon.games.callcard.ws.dto.*;
import com.saicon.games.callcard.ws.dto.CallCardStatsDTO;
import com.saicon.games.callcard.ws.dto.TemplateUsageDTO;
import com.saicon.games.callcard.ws.dto.UserEngagementDTO;
import com.saicon.games.callcard.ws.dto.ItemStatisticsDTO;
import com.saicon.games.solr.dto.SolrBrandProductDTO;
import com.saicon.games.appsettings.dto.AppSettingsDTO;
import com.saicon.games.client.data.DecimalDTO;
import com.saicon.games.metadata.dto.MetadataKeyDTO;
import com.saicon.games.callcard.util.EventType;
import com.saicon.games.callcard.util.SortOrderTypes;
import com.saicon.games.callcard.exception.BusinessLayerException;
import com.saicon.games.callcard.exception.ExceptionTypeTO;
import com.saicon.games.callcard.util.Assert;
import com.saicon.games.callcard.util.UUIDUtilities;
import com.saicon.games.callcard.components.ErpDynamicQueryManager;
import com.saicon.games.callcard.components.ErpNativeQueryManager;
import com.saicon.games.callcard.components.ICallCardManagement;
import com.saicon.games.callcard.components.external.ISalesOrderManagement;
import com.saicon.games.callcard.components.external.IAddressbookManagement;
import com.saicon.games.callcard.components.external.IAppSettingsComponent;
import com.saicon.games.callcard.components.external.GeneratedEventsDispatcher;
import com.saicon.games.callcard.components.external.IMetadataComponent;
import com.saicon.games.callcard.components.external.IUserMetadataComponent;
import com.saicon.games.callcard.components.external.SolrClient;
import com.saicon.games.callcard.entity.*;
import com.saicon.games.callcard.dao.IGenericDAO;
import com.saicon.games.callcard.components.util.CallCardTemplateEntryComparator;
import com.saicon.games.callcard.components.external.InvoiceDetails;
// TODO: import com.saicon.games.callcard.ws.dto.MetadataDTO;
import com.saicon.games.callcard.components.external.SalesOrder;
import com.saicon.games.callcard.components.external.SalesOrderDetails;
import com.saicon.games.callcard.components.external.State;
import com.saicon.games.callcard.components.external.Addressbook;
import com.saicon.games.callcard.components.external.InvoiceDTO;
import com.saicon.games.callcard.components.external.Postcode;
// SalesOrderStatus enum stub needed
import com.saicon.games.callcard.util.Constants;
import com.saicon.games.callcard.util.EventTO;
import com.saicon.games.entities.shared.ItemTypes;
// TODO: InvoiceDTO stub
import com.saicon.multiplayer.dto.KeyValueDTO;
// TODO: SalesOrderDTO stub
// TODO: SalesOrderDetailsDTO stub
import com.saicon.games.entities.shared.Users;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class CallCardManagement implements ICallCardManagement {
    private final static Logger LOGGER = LoggerFactory.getLogger(CallCardManagement.class);

    private IGenericDAO<CallCard, String> callCardDao;
    private IGenericDAO<CallCardTemplate, String> callCardTemplateDao;
    private IGenericDAO<CallCardTemplatePOS, String> callCardTemplatePOSDao;
    private IGenericDAO<CallCardTemplateEntry, String> callCardTemplateEntryDao;
    private IGenericDAO<CallCardTemplateUserReferences, String> callCardTemplateUserReferencesDao;
    private IGenericDAO<CallCardRefUser, String> callCardRefUserDao;
    private IGenericDAO<CallCardRefUserIndex, String> callCardRefUserIndexDao;
    private IGenericDAO<Users, String> usersDao;
    private IGenericDAO<ItemTypes, Integer> itemTypesDao;
    private IGenericDAO<Application, String> applicationDao;
    private IGenericDAO<Postcode, Integer> postcodeDao;

    private ErpDynamicQueryManager erpDynamicQueryManager;
    private ErpNativeQueryManager erpNativeQueryManager;

    private ISalesOrderManagement salesOrderManagement;
    private IAddressbookManagement addressbookManagement;
    private IAppSettingsComponent appSettingsComponent;

    @Autowired
    private IMetadataComponent metadataComponent;

    @Autowired
    private IUserMetadataComponent userMetadataComponent;

    @Autowired
    private SolrClient solrClient;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private GeneratedEventsDispatcher generatedEventsDispatcher;

    private static final List<String> userAddressMetadataKeys = new ArrayList<String>() {{
        add(Constants.METADATA_KEY_PERSONAL_REGION);
        add(Constants.METADATA_KEY_PERSONAL_ADDRESS);
        add(Constants.METADATA_KEY_PERSONAL_CITY);
        add(Constants.METADATA_KEY_PERSONAL_COUNTRY);
        add(Constants.METADATA_KEY_PERSONAL_STATE);
    }};

    private static final List<Integer> callCardVisitStatuses = new ArrayList<Integer>() {{
        add(CallCardRefUserDTO.SELL);
        add(CallCardRefUserDTO.UNSCHEDULED_SELL);
        add(CallCardRefUserDTO.ORDER);
        add(CallCardRefUserDTO.UNSCHEDULED_ORDER);
    }};

    private static final List<Integer> salesOrderStatuses = new ArrayList<Integer>() {{
        add(CallCardRefUserDTO.ORDER);
        add(CallCardRefUserDTO.UNSCHEDULED_ORDER);
    }};

    public CallCardManagement() {
    }

    @Override
    @Transactional
    public CallCardDTO listPendingCallCard(String userId, String userGroupId, String gameTypeId) {
        LOGGER.info("-- listPendingCallCard : userId={} userGroupId={} gameTypeId={}", userId, userGroupId, gameTypeId);

        CallCard callCard = checkIfActiveCallCard(userId, userGroupId, gameTypeId);
        if (callCard != null)
            return new CallCardDTO(callCard.getCallCardId(), callCard.getStartDate(), callCard.getEndDate(), null, !callCard.isActive(), callCard.getComments(), callCard.getLastUpdated(), callCard.getCallCardTemplateId().getCallCardTemplateId());

        return null;
    }

    @Transactional
    private CallCard checkIfActiveCallCard(String userId, String userGroupId, String gameTypeId) {
        LOGGER.info("-- checkIfActiveCallCard : userId={} userGroupId={} gameTypeId={}", userId, userGroupId, gameTypeId);

        List<CallCard> callCards = erpDynamicQueryManager.listCallCards(null, Arrays.asList(userId), null, null, null, true, true, true, gameTypeId, 0, -1);
        if (callCards != null && callCards.size() > 0)
            return callCards.get(0);

        return null;
    }

    private void dispatchEvent(EventType eventType, String userId, String gameTypeId, String applicationId, String itemId, int itemTypeId, int quantity, String additionalEventProperties, Boolean rollBackEvent) {
        EventTO eventTO = new EventTO();
        eventTO.setUserId(userId);
        eventTO.setApplicationId(applicationId);
        eventTO.setGameTypeId(gameTypeId);

        String eventProperties = "";

        if (StringUtils.isNotBlank(itemId))
            eventProperties += (EventTO.PROPERTY_ITEM_ID + "=" + itemId + "\n");

        if (itemTypeId != 0)
            eventProperties += (EventTO.PROPERTY_ITEM_TYPE_ID + "=" + itemTypeId + "\n");

        if (quantity != 0)
            eventProperties += (EventTO.PROPERTY_QUANTITY + "=" + quantity + "\n");

        if (StringUtils.isNotBlank(additionalEventProperties))
            eventProperties += additionalEventProperties;

        if (!rollBackEvent)
            eventTO.setRunInNewDBTransaction(true);

        eventTO.setEventProperties(eventProperties);

        eventTO.setClientTypeId(applicationDao.read(applicationId).getClientType().toInt());

        eventTO.setEventTypeId(eventType.toInt());
        generatedEventsDispatcher.dispatch(eventTO);
    }

    @Transactional
    private List<CallCardTemplate> getCallCardTemplateByMetadataKeys(String userId, String userGroupId, String gameTypeId, String callCardTemplateId, List<String> metadataKeys) {
        List<KeyValueDTO> metadataFilter = new ArrayList<>();
        if (metadataKeys != null && metadataKeys.size() > 0) {
            List<MetadataDTO> metadataDTOs = userMetadataComponent.listUserMetadata(Collections.singletonList(userId), metadataKeys, false);
            if (metadataDTOs == null || metadataDTOs.size() == 0)
                throw new BusinessLayerException("", ExceptionTypeTO.CMS_CONFIGURATION_ERROR);

            LOGGER.info("-- Get Call card Template by metadata keys : \n");
            for (MetadataDTO metadataDTO : metadataDTOs) {
                LOGGER.info("metadata key : " + metadataDTO.getKey() + " value : " + metadataDTO.getValue() + "\n");
                metadataFilter.add(new KeyValueDTO(metadataDTO.getKey(), metadataDTO.getValue()));
            }
        }

        return erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, callCardTemplateId != null ? Arrays.asList(callCardTemplateId) : null, metadataFilter, true, true, null, 0, -1);
    }

    @Override
    @Transactional
    public List<CallCardDTO> getCallCardsFromTemplate(String userId, String userGroupId, String gameTypeId, String applicationId) {
        LOGGER.info("-- CallCardManagement.getCallCardsFromTemplate : userId={} userGroupId={} gameTypeId={}", userId, userGroupId, gameTypeId);

        // Read GameType's CallCard configuration from application settings
        int previousValues = 0;
        boolean includeGeoInfo = false;
        List<String> productTypeCategories = new ArrayList<String>();
        List<AppSettingsDTO> appSettings = appSettingsComponent.get(null, applicationId, Collections.singletonList(ScopeType.GAME_TYPE));
        if (appSettings != null || appSettings.size() > 0) {
            for (AppSettingsDTO appSetting : appSettings) {
                switch (appSetting.getKey()) {
                    case Constants.APP_SETTING_KEY_PREVIOUS_VISITS_SUMMARY:
                        try {
                            previousValues = Integer.parseInt(appSetting.getValue());
                        } catch (NumberFormatException e) {
                            LOGGER.error("Invalid application setting format for GameTypeId=" + gameTypeId + " AppSettingKey=" + appSetting.getKey() + " Value=" + appSetting.getValue(), ExceptionTypeTO.GENERIC);
                        }
                        break;
                    case Constants.APP_SETTING_KEY_INCLUDE_VISITS_GEO_INFO:
                        includeGeoInfo = Boolean.parseBoolean(appSetting.getValue());
                        break;
                    case Constants.APP_SETTING_KEY_PRODUCT_TYPE_CATEGORIES:
                        try {
                            StringTokenizer tokenizer = new StringTokenizer(appSetting.getValue(), ",");
                            while (tokenizer.hasMoreElements()) {
                                productTypeCategories.add((String) tokenizer.nextElement());
                            }
                        } catch (NumberFormatException e) {
                            LOGGER.error("Invalid application setting format for GameTypeId=" + gameTypeId + " AppSettingKey=" + appSetting.getKey() + " Value=" + appSetting.getValue(), ExceptionTypeTO.GENERIC);
                        }
                        break;
                }
            }
        }

        LOGGER.info("CallCard configuration: \n" +
                "- Number of previous values to summarize: " + previousValues + "\n" +
                "- Include Geographical info for CallCardRefUsers: " + includeGeoInfo);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Read list of CallCard properties
        List<MetadataKeyDTO> metadataKeyDTOs = metadataComponent.listMetadataKeysByItemType(Constants.ITEM_TYPE_CALL_CARD_INDEX, false);
        Map<String, String> metadataKeysTypeMap = new HashMap<>();
        Map<String, String> metadataKeysIdMap = new HashMap<>();
        if (metadataKeyDTOs != null && metadataKeyDTOs.size() > 0) {
            for (MetadataKeyDTO metadataKeyDTO : metadataKeyDTOs) {
                metadataKeysTypeMap.put(metadataKeyDTO.getMetadataKeyName(), metadataKeyDTO.getDataTypeName());
                metadataKeysIdMap.put(metadataKeyDTO.getMetadataKeyName(), metadataKeyDTO.getMetadataKeyId());
            }
        }

        //get Brand product Type Categories
        Map<String, Integer> brandProductTypeCategoriesMap = new HashMap<String, Integer>();
        Map<Integer, List<SolrBrandProductDTO>> solrBrandProducts = solrClient.getMultipleBrandProducts(gameTypeId,
                null,
                "",
                null,
                null,
                new String[]{},
                new Integer[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                0,
                -1,
                false,
                true,
                null,
                null,
                null,
                false,
                null,
                null,
                false,
                null,
                SortOrderTypes.BY_ORDERING_ASC);

        List<SolrBrandProductDTO> brandProducts = solrBrandProducts != null ? solrBrandProducts.values().iterator().next() : null;

        if (brandProducts != null && brandProducts.size() > 0) {
            for (SolrBrandProductDTO brandProduct : brandProducts) {
                if (brandProduct != null && brandProduct.getSubcategoryIds() != null) {
                    List<String> categories = new ArrayList<String>(Arrays.asList(brandProduct.getSubcategoryIds()));

                    categories.retainAll(productTypeCategories);
                    if (categories != null && categories.size() > 0)
                        brandProductTypeCategoriesMap.put(brandProduct.getBrandProductId(), Integer.parseInt(categories.get(0)));
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        List<CallCardTemplate> assignedCallCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, null, true, true, userId, 0, -1);
        List<CallCardDTO> callCards = new ArrayList<CallCardDTO>();

        if (assignedCallCardTemplates != null && assignedCallCardTemplates.size() > 0) {
            for (CallCardTemplate assignedCallCardTemplate : assignedCallCardTemplates) {
                CallCardDTO callCardDTO = new CallCardDTO();
                callCardDTO.setCallCardTemplateId(assignedCallCardTemplate.getCallCardTemplateId());

                List<CallCardActionItemDTO> items = null;

                int itemsItemTypeId = 0;
                if (assignedCallCardTemplate.getEntries() != null && assignedCallCardTemplate.getEntries().size() > 0) {
                    List<CallCardTemplateEntry> templateEntries = assignedCallCardTemplate.getEntries();
                    Collections.sort(templateEntries, new CallCardTemplateEntryComparator());

                    items = new ArrayList<>();
                    for (CallCardTemplateEntry templateEntry : templateEntries) {
                        LOGGER.debug("----- List of CallCardTemplateEntries for template= {}  : size={}", templateEntry.getCallCardTemplateId(), assignedCallCardTemplate.getPos().size());

                        List<CallCardActionItemAttributesDTO> attributes = new ArrayList<>();
                        if (StringUtils.isNotBlank(templateEntry.getProperties())) {
                            LOGGER.debug("------ Properties for CallCardTemplate with ID={} : properties={}", templateEntry.getCallCardTemplateId(), templateEntry.getProperties());
                            StringTokenizer properties = new StringTokenizer(templateEntry.getProperties(), ","); //split properties

                            while (properties.hasMoreElements()) {
                                String attribute = (String) properties.nextElement();

                                attributes.add(new CallCardActionItemAttributesDTO(
                                        null,
                                        metadataKeysIdMap.get(attribute),
                                        attribute,
                                        metadataKeysTypeMap.get(attribute),
                                        null,
                                        null,
                                        null,
                                        CallCardRefUserDTO.SELL,
                                        null,
                                        null));
                            }

                            itemsItemTypeId = templateEntry.getItemTypeId().getItemTypeId();
                            int categoryId = brandProductTypeCategoriesMap.get(templateEntry.getId()) != null ? brandProductTypeCategoriesMap.get(templateEntry.getId()) : 0;
                            items.add(new CallCardActionItemDTO(templateEntry.getItemId(), templateEntry.getItemTypeId().getItemTypeId(), attributes, categoryId, false));
                        }
                    }
                }

                // add other references
                Map<String, List<CallCardActionItemDTO>> referencesMap = null;

                List<CallCardTemplateUserReferences> references = callCardTemplateUserReferencesDao.queryList("listByCallCardTemplateId", assignedCallCardTemplate.getCallCardTemplateId());
                if (references != null && references.size() > 0) {
                    referencesMap = new HashMap<String, List<CallCardActionItemDTO>>(); //< RefUserId, List of ActionItems>

                    for (CallCardTemplateUserReferences reference : references) {
                        List<CallCardActionItemDTO> referencesList = null;
                        if (referencesMap.containsKey(reference.getRefUserId().getUserId()))
                            referencesList = referencesMap.get(reference.getRefUserId().getUserId());
                        else
                            referencesList = new ArrayList<>();

                        int categoryId = 0;
                        if (reference.getItemTypeId().getItemTypeId() == Constants.ITEM_TYPE_BRAND_PRODUCT)
                            categoryId = brandProductTypeCategoriesMap.get(reference.getItemId()) != null ? brandProductTypeCategoriesMap.get(reference.getItemId()) : 0;

                        referencesList.add(new CallCardActionItemDTO(reference.getItemId(), reference.getItemTypeId().getItemTypeId(), null, categoryId, reference.isMandatory()));
                        referencesMap.put(reference.getRefUserId().getUserId(), referencesList);
                    }
                }


                Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> refUserItemAttributeSummariesMap = new HashMap<>();//< RefUserId, < BrandProductId, Attributes>>
                Map<String, List<KeyValueDTO>> refUserAdditionalInfoMap = new HashMap<>();//< RefUserId, Additional Info>
                List<String> templateRefUsers = new ArrayList<String>();
                List<CallCardTemplatePOS> callCardTemplatePOSs = callCardTemplatePOSDao.queryList("listByCallCardTemplateId", assignedCallCardTemplate.getCallCardTemplateId());
                if (callCardTemplatePOSs != null && callCardTemplatePOSs.size() > 0) {
                    Map<Integer, List<CallCardRefUserDTO>> groupUserDTOs = new HashMap<>();

                    // Create list of visit targets from template,that need to be filled with additional data
                    if (includeGeoInfo || previousValues != 0) {
                        for (CallCardTemplatePOS pos : callCardTemplatePOSs) {
                            if (!templateRefUsers.contains(pos.getRefUserId().getUserId()))
                                templateRefUsers.add(pos.getRefUserId().getUserId());
                        }
                    }

                    // Collect additional CallCard Info
                    if (previousValues > 0)
                        refUserItemAttributeSummariesMap = summarizeCallCardProperties(templateRefUsers, userId, previousValues, callCardVisitStatuses, metadataKeysTypeMap, false);

                    if (includeGeoInfo)
                        refUserAdditionalInfoMap = getAdditionalRefUserInfo(templateRefUsers);

                    LOGGER.info("-- List of POS : size= {}", assignedCallCardTemplate.getPos().size());
                    for (CallCardTemplatePOS pos : callCardTemplatePOSs) {

                        List<CallCardRefUserDTO> userDTOs;

                        if (groupUserDTOs.containsKey(pos.getGroupId()))
                            userDTOs = groupUserDTOs.get(pos.getGroupId());
                        else if (pos.getGroupId() == null && groupUserDTOs.containsKey(CallCardGroupDTO.UNASSIGNED_POS_GROUP))
                            userDTOs = groupUserDTOs.get(CallCardGroupDTO.UNASSIGNED_POS_GROUP);
                        else
                            userDTOs = new ArrayList<CallCardRefUserDTO>();

                        List<CallCardActionsDTO> userActions = new ArrayList<>();
                        if (items != null && items.size() > 0) {
                            List<CallCardActionItemDTO> actionItems = new ArrayList<CallCardActionItemDTO>();

                            for (CallCardActionItemDTO item : items) {
                                List<CallCardActionItemAttributesDTO> actionItemsAttributes = new ArrayList<CallCardActionItemAttributesDTO>();

                                List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;

                                if (refUserItemAttributeSummariesMap != null) {
                                    Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = refUserItemAttributeSummariesMap.get(pos.getRefUserId().getUserId());
                                    if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                        itemAttributeSummaries = itemAttributeSummariesMap.get(item.getItemId());
                                }

                                for (CallCardActionItemAttributesDTO itemAttribute : item.getAttributes()) {
                                    CallCardActionItemAttributesDTO summary = new CallCardActionItemAttributesDTO();

                                    if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                                        for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                            if (itemAttributeSummary != null && itemAttributeSummary.getPropertyName().equalsIgnoreCase(itemAttribute.getPropertyName())) {
                                                summary = itemAttributeSummary;
                                                break;
                                            }
                                        }
                                    }

                                    actionItemsAttributes.add(new CallCardActionItemAttributesDTO(
                                            itemAttribute.getCallCardRefUserIndexId(),
                                            itemAttribute.getPropertyId(),
                                            itemAttribute.getPropertyName(),
                                            itemAttribute.getPropertyTypeId(),
                                            itemAttribute.getPropertyValue(),
                                            itemAttribute.getDateSubmitted(),
                                            itemAttribute.getStatus(),
                                            itemAttribute.getType(),
                                            null,
                                            summary != null ? summary.getRefPropertyValue() : null));
                                }

                                int categoryId = 0;
                                switch (item.getItemTypeId()) {
                                    case Constants.ITEM_TYPE_BRAND_PRODUCT:
                                        categoryId = brandProductTypeCategoriesMap.get(item.getItemId()) != null ? brandProductTypeCategoriesMap.get(item.getItemId()) : 0;
                                        break;
                                }
                                actionItems.add(new CallCardActionItemDTO(item.getItemId(), item.getItemTypeId(), actionItemsAttributes, categoryId, item.isMandatory()));
                            }

                            userActions.add(new CallCardActionsDTO(actionItems, itemsItemTypeId, pos.isMandatory()));
                        }

                        if (referencesMap != null && referencesMap.containsKey(pos.getRefUserId().getUserId())) {

                            boolean mandatorySurveyExists = false; // Mark if there is at list one mandatory Survey

                            List<CallCardActionItemDTO> actionItems = referencesMap.get(pos.getRefUserId().getUserId());
                            for (CallCardActionItemDTO item : actionItems) {
                                if ((item.getItemTypeId() == Constants.ITEM_TYPE_QUIZ) && item.isMandatory()) {
                                    mandatorySurveyExists = true;
                                    break;
                                }
                            }

                            int itemTypeId = referencesMap.get(pos.getRefUserId().getUserId()).get(0).getItemTypeId();

                            userActions.add(new CallCardActionsDTO(referencesMap.get(pos.getRefUserId().getUserId()), itemTypeId, mandatorySurveyExists));
                        }

                        List<KeyValueDTO> additionalRefUserInfo = refUserItemAttributeSummariesMap != null ? refUserAdditionalInfoMap.get(pos.getRefUserId().getUserId()) : null;

                        CallCardRefUserDTO refUserDTO = new CallCardRefUserDTO(null, pos.getRefUserId().getUserId(), userActions, null, null, pos.isMandatory(), null, CallCardRefUserDTO.SELL, null, null, userId, additionalRefUserInfo, true);

                        userDTOs.add(refUserDTO);

                        groupUserDTOs.put(pos.getGroupId() != null ? pos.getGroupId() : CallCardGroupDTO.UNASSIGNED_POS_GROUP, userDTOs); // Out Of Route POS are in group 0
                    }

                    List<CallCardGroupDTO> groupDTOs = new ArrayList<>();
                    for (Map.Entry<Integer, List<CallCardRefUserDTO>> groupUserDTO : groupUserDTOs.entrySet()) {
                        groupDTOs.add(new CallCardGroupDTO(groupUserDTO.getKey(), groupUserDTO.getValue(), assignedCallCardTemplate.getCallCardTemplateId()));
                    }

                    callCardDTO.setGroupIds(groupDTOs);

                }

                callCards.add(callCardDTO);
            }
        }

        return callCards;
    }

    @Override
    @Transactional
    public CallCardDTO getPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId) {
        LOGGER.info("-- CallCardManagement.getPendingCallCard : userId={} userGroupId={} gameTypeId={}", userId, userGroupId, gameTypeId);

        String additionalEventProperties = "";

        String callCardTemplateId = null;
        CallCard callCard = null;
        CallCardTemplate template = null;

        // Check if there is a pending CallCard
        callCard = checkIfActiveCallCard(userId, userGroupId, gameTypeId);
        if (callCard != null) {
            callCardTemplateId = callCard.getCallCardTemplateId().getCallCardTemplateId();
            template = callCardTemplateDao.read(callCardTemplateId);
        } else {
            return null;
        }

        LOGGER.info("-- CallCardTemplate returned : {}", callCardTemplateId);

        // Read GameType's CallCard configuration from application settings
        int previousValues = 0;
        boolean includeGeoInfo = false;
        List<String> productTypeCategories = new ArrayList<String>();
        List<AppSettingsDTO> appSettings = appSettingsComponent.get(null, applicationId, Collections.singletonList(ScopeType.GAME_TYPE));
        if (appSettings != null || appSettings.size() > 0) {
            for (AppSettingsDTO appSetting : appSettings) {
                switch (appSetting.getKey()) {
                    case Constants.APP_SETTING_KEY_PREVIOUS_VISITS_SUMMARY:
                        try {
                            previousValues = Integer.parseInt(appSetting.getValue());
                        } catch (NumberFormatException e) {
                            LOGGER.error("Invalid application setting format for GameTypeId=" + gameTypeId + " AppSettingKey=" + appSetting.getKey() + " Value=" + appSetting.getValue(), ExceptionTypeTO.GENERIC);
                        }
                        break;
                    case Constants.APP_SETTING_KEY_INCLUDE_VISITS_GEO_INFO:
                        includeGeoInfo = Boolean.parseBoolean(appSetting.getValue());
                        break;
                    case Constants.APP_SETTING_KEY_PRODUCT_TYPE_CATEGORIES:
                        try {
                            StringTokenizer tokenizer = new StringTokenizer(appSetting.getValue(), ",");
                            while (tokenizer.hasMoreElements()) {
                                productTypeCategories.add((String) tokenizer.nextElement());
                            }
                        } catch (NumberFormatException e) {
                            LOGGER.error("Invalid application setting format for GameTypeId=" + gameTypeId + " AppSettingKey=" + appSetting.getKey() + " Value=" + appSetting.getValue(), ExceptionTypeTO.GENERIC);
                        }
                        break;
                }
            }
        }

        LOGGER.info("CallCard configuration: \n" +
                "- Number of previous values to summarize: " + previousValues + "\n" +
                "- Include Geographical info for CallCardRefUsers: " + includeGeoInfo);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Read list of CallCard properties
        List<MetadataKeyDTO> metadataKeyDTOs = metadataComponent.listMetadataKeysByItemType(Constants.ITEM_TYPE_CALL_CARD_INDEX, false);
        Map<String, String> metadataKeysTypeMap = new HashMap<>();
        Map<String, String> metadataKeysIdMap = new HashMap<>();
        if (metadataKeyDTOs != null && metadataKeyDTOs.size() > 0) {
            for (MetadataKeyDTO metadataKeyDTO : metadataKeyDTOs) {
                metadataKeysTypeMap.put(metadataKeyDTO.getMetadataKeyName(), metadataKeyDTO.getDataTypeName());
                metadataKeysIdMap.put(metadataKeyDTO.getMetadataKeyName(), metadataKeyDTO.getMetadataKeyId());
            }
        }

        //get Brand product Type Categories
        Map<String, Integer> brandProductTypeCategoriesMap = new HashMap<String, Integer>();
        Map<Integer, List<SolrBrandProductDTO>> solrBrandProducts = solrClient.getMultipleBrandProducts(gameTypeId,
                null,
                "",
                null,
                null,
                new String[]{},
                new Integer[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                0,
                -1,
                false,
                true,
                null,
                null,
                null,
                false,
                null,
                null,
                false,
                null,
                SortOrderTypes.BY_ORDERING_ASC);

        List<SolrBrandProductDTO> brandProducts = solrBrandProducts != null ? solrBrandProducts.values().iterator().next() : null;

        if (brandProducts != null && brandProducts.size() > 0) {
            for (SolrBrandProductDTO brandProduct : brandProducts) {
                if (brandProduct != null && brandProduct.getSubcategoryIds() != null) {
                    List<String> categories = new ArrayList<String>(Arrays.asList(brandProduct.getSubcategoryIds()));

                    categories.retainAll(productTypeCategories);
                    if (categories != null && categories.size() > 0)
                        brandProductTypeCategoriesMap.put(brandProduct.getBrandProductId(), Integer.parseInt(categories.get(0)));
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        CallCardDTO callCardDTO = new CallCardDTO();
        callCardDTO.setCallCardTemplateId(template.getCallCardTemplateId());

        List<CallCardActionItemDTO> items = null;

        int itemsItemTypeId = 0;
        if (template.getEntries() != null && template.getEntries().size() > 0) {
            List<CallCardTemplateEntry> templateEntries = template.getEntries();
            Collections.sort(templateEntries, new CallCardTemplateEntryComparator());

            items = new ArrayList<>();
            for (CallCardTemplateEntry templateEntry : templateEntries) {
                LOGGER.debug("----- List of CallCardTemplateEntries for template= {}  : size={}", templateEntry.getCallCardTemplateId(), template.getPos().size());

                List<CallCardActionItemAttributesDTO> attributes = new ArrayList<>();
                if (StringUtils.isNotBlank(templateEntry.getProperties())) {
                    LOGGER.debug("------ Properties for CallCardTemplate with ID={} : properties={}", templateEntry.getCallCardTemplateId(), templateEntry.getProperties());
                    StringTokenizer properties = new StringTokenizer(templateEntry.getProperties(), ","); //split properties

                    while (properties.hasMoreElements()) {
                        String attribute = (String) properties.nextElement();

                        attributes.add(new CallCardActionItemAttributesDTO(
                                null,
                                metadataKeysIdMap.get(attribute),
                                attribute,
                                metadataKeysTypeMap.get(attribute),
                                null,
                                null,
                                null,
                                CallCardRefUserDTO.SELL,
                                null,
                                null));
                    }

                    itemsItemTypeId = templateEntry.getItemTypeId().getItemTypeId();
                    int categoryId = brandProductTypeCategoriesMap.get(templateEntry.getId()) != null ? brandProductTypeCategoriesMap.get(templateEntry.getId()) : 0;
                    items.add(new CallCardActionItemDTO(templateEntry.getItemId(), templateEntry.getItemTypeId().getItemTypeId(), attributes, categoryId, false));
                }
            }
        }

        // add other references
        Map<String, List<CallCardActionItemDTO>> referencesMap = null;

        List<CallCardTemplateUserReferences> references = callCardTemplateUserReferencesDao.queryList("listByCallCardTemplateId", template.getCallCardTemplateId());
        if (references != null && references.size() > 0) {
            referencesMap = new HashMap<String, List<CallCardActionItemDTO>>(); //< RefUserId, List of ActionItems>

            for (CallCardTemplateUserReferences reference : references) {
                List<CallCardActionItemDTO> referencesList = null;
                if (referencesMap.containsKey(reference.getRefUserId().getUserId()))
                    referencesList = referencesMap.get(reference.getRefUserId().getUserId());
                else
                    referencesList = new ArrayList<>();

                int categoryId = 0;
                if (reference.getItemTypeId().getItemTypeId() == Constants.ITEM_TYPE_BRAND_PRODUCT)
                    categoryId = brandProductTypeCategoriesMap.get(reference.getItemId()) != null ? brandProductTypeCategoriesMap.get(reference.getItemId()) : 0;

                referencesList.add(new CallCardActionItemDTO(reference.getItemId(), reference.getItemTypeId().getItemTypeId(), null, categoryId, reference.isMandatory()));
                referencesMap.put(reference.getRefUserId().getUserId(), referencesList);
            }
        }


        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> refUserItemAttributeSummariesMap = new HashMap<>();//< RefUserId, < BrandProductId, Attributes>>
        Map<String, List<KeyValueDTO>> refUserAdditionalInfoMap = new HashMap<>();//< RefUserId, Additional Info>
        List<String> templateRefUsers = new ArrayList<String>();
        List<CallCardTemplatePOS> callCardTemplatePOSs = callCardTemplatePOSDao.queryList("listByCallCardTemplateId", template.getCallCardTemplateId());
        if (callCardTemplatePOSs != null && callCardTemplatePOSs.size() > 0) {
            Map<Integer, List<CallCardRefUserDTO>> groupUserDTOs = new HashMap<>();

            // Create list of visit targets from template,that need to be filled with additional data
            if (includeGeoInfo || previousValues != 0) {
                for (CallCardTemplatePOS pos : callCardTemplatePOSs) {
                    if (!templateRefUsers.contains(pos.getRefUserId().getUserId()))
                        templateRefUsers.add(pos.getRefUserId().getUserId());
                }
            }

            // Collect additional CallCard Info
            if (previousValues > 0)
                refUserItemAttributeSummariesMap = summarizeCallCardProperties(templateRefUsers, userId, previousValues, callCardVisitStatuses, metadataKeysTypeMap, false);

            if (includeGeoInfo)
                refUserAdditionalInfoMap = getAdditionalRefUserInfo(templateRefUsers);

            LOGGER.info("-- List of POS : size= {}", template.getPos().size());
            for (CallCardTemplatePOS pos : callCardTemplatePOSs) {

                List<CallCardRefUserDTO> userDTOs;

                if (groupUserDTOs.containsKey(pos.getGroupId()))
                    userDTOs = groupUserDTOs.get(pos.getGroupId());
                else if (pos.getGroupId() == null && groupUserDTOs.containsKey(CallCardGroupDTO.UNASSIGNED_POS_GROUP))
                    userDTOs = groupUserDTOs.get(CallCardGroupDTO.UNASSIGNED_POS_GROUP);
                else
                    userDTOs = new ArrayList<CallCardRefUserDTO>();

                List<CallCardActionsDTO> userActions = new ArrayList<>();
                if (items != null && items.size() > 0) {
                    List<CallCardActionItemDTO> actionItems = new ArrayList<CallCardActionItemDTO>();

                    for (CallCardActionItemDTO item : items) {
                        List<CallCardActionItemAttributesDTO> actionItemsAttributes = new ArrayList<CallCardActionItemAttributesDTO>();

                        List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;

                        if (refUserItemAttributeSummariesMap != null) {
                            Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = refUserItemAttributeSummariesMap.get(pos.getRefUserId().getUserId());
                            if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                itemAttributeSummaries = itemAttributeSummariesMap.get(item.getItemId());
                        }

                        for (CallCardActionItemAttributesDTO itemAttribute : item.getAttributes()) {
                            CallCardActionItemAttributesDTO summary = new CallCardActionItemAttributesDTO();

                            if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                                for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                    if (itemAttributeSummary != null && itemAttributeSummary.getPropertyName().equalsIgnoreCase(itemAttribute.getPropertyName())) {
                                        summary = itemAttributeSummary;
                                        break;
                                    }
                                }
                            }

                            actionItemsAttributes.add(new CallCardActionItemAttributesDTO(
                                    itemAttribute.getCallCardRefUserIndexId(),
                                    itemAttribute.getPropertyId(),
                                    itemAttribute.getPropertyName(),
                                    itemAttribute.getPropertyTypeId(),
                                    itemAttribute.getPropertyValue(),
                                    itemAttribute.getDateSubmitted(),
                                    itemAttribute.getStatus(),
                                    itemAttribute.getType(),
                                    null,
                                    summary != null ? summary.getRefPropertyValue() : null));
                        }

                        int categoryId = 0;
                        switch (item.getItemTypeId()) {
                            case Constants.ITEM_TYPE_BRAND_PRODUCT:
                                categoryId = brandProductTypeCategoriesMap.get(item.getItemId()) != null ? brandProductTypeCategoriesMap.get(item.getItemId()) : 0;
                                break;
                        }
                        actionItems.add(new CallCardActionItemDTO(item.getItemId(), item.getItemTypeId(), actionItemsAttributes, categoryId, item.isMandatory()));
                    }

                    userActions.add(new CallCardActionsDTO(actionItems, itemsItemTypeId, pos.isMandatory()));
                }

                if (referencesMap != null && referencesMap.containsKey(pos.getRefUserId().getUserId())) {

                    boolean mandatorySurveyExists = false; // Mark if there is at list one mandatory Survey

                    List<CallCardActionItemDTO> actionItems = referencesMap.get(pos.getRefUserId().getUserId());
                    for (CallCardActionItemDTO item : actionItems) {
                        if ((item.getItemTypeId() == Constants.ITEM_TYPE_QUIZ) && item.isMandatory()) {
                            mandatorySurveyExists = true;
                            break;
                        }
                    }

                    int itemTypeId = referencesMap.get(pos.getRefUserId().getUserId()).get(0).getItemTypeId();

                    userActions.add(new CallCardActionsDTO(referencesMap.get(pos.getRefUserId().getUserId()), itemTypeId, mandatorySurveyExists));
                }

                List<KeyValueDTO> additionalRefUserInfo = refUserItemAttributeSummariesMap != null ? refUserAdditionalInfoMap.get(pos.getRefUserId().getUserId()) : null;

                CallCardRefUserDTO refUserDTO = new CallCardRefUserDTO(null, pos.getRefUserId().getUserId(), userActions, null, null, pos.isMandatory(), null, CallCardRefUserDTO.SELL, null, null, userId, additionalRefUserInfo, true);

                userDTOs.add(refUserDTO);

                groupUserDTOs.put(pos.getGroupId() != null ? pos.getGroupId() : CallCardGroupDTO.UNASSIGNED_POS_GROUP, userDTOs);
            }

            List<CallCardGroupDTO> groupDTOs = new ArrayList<>();
            for (Map.Entry<Integer, List<CallCardRefUserDTO>> groupUserDTO : groupUserDTOs.entrySet()) {
                groupDTOs.add(new CallCardGroupDTO(groupUserDTO.getKey(), groupUserDTO.getValue(), template.getCallCardTemplateId()));
            }

            callCardDTO.setGroupIds(groupDTOs);

        }

        callCardDTO.setCallCardId(callCard.getCallCardId());
        callCardDTO.setStartDate(callCard.getStartDate());
        callCardDTO.setEndDate(callCard.getEndDate());
        callCardDTO.setComments(callCard.getComments());
        callCardDTO.setLastUpdated(callCard.getLastUpdated());

        List<CallCardRefUser> additionalRefUserIndexes = new ArrayList<>();

        dispatchEvent(EventType.CALL_CARD_DOWNLOADED, userId, gameTypeId, applicationId, callCardDTO.getCallCardId(), Constants.ITEM_TYPE_CALL_CARD, 0, additionalEventProperties, true);

        // Read call card from DB
        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> callCardValuesFromSalesOrder = new HashMap<String, Map<String, List<CallCardActionItemAttributesDTO>>>();
        List<CallCardRefUser> callCardRefUsers = callCardRefUserDao.queryList("listByCallCardId", callCard.getCallCardId());

        Boolean callCardWithSalesOrder = false;
        LOGGER.debug("-- Most recent pending CallCard - refUserIndex : size={}", callCardRefUsers.size());
        if (callCardRefUsers != null && callCardRefUsers.size() > 0) {

            for (CallCardRefUser refUserIndex : callCardRefUsers) {
                boolean refUserFound = false;

                if (!callCardWithSalesOrder && salesOrderStatuses.contains(refUserIndex.getStatus())) { // if CallCard uses Sales Orders
                    callCardValuesFromSalesOrder = getCallCardValuesFromSalesOrder(callCard);
                    callCardWithSalesOrder = true;
                }

                if (refUserIndex.getStatus() == CallCardRefUserDTO.UNSCHEDULED_SELL || refUserIndex.getStatus() == CallCardRefUserDTO.UNSCHEDULED_ORDER) {
                    additionalRefUserIndexes.add(refUserIndex);
                    continue;
                }

                if (callCardDTO.getGroupIds() == null || callCardDTO.getGroupIds().size() == 0)
                    continue;

                for (CallCardGroupDTO group : callCardDTO.getGroupIds()) {

                    if (group.getRefUserIds() == null || group.getRefUserIds().size() == 0)
                        continue;

                    for (CallCardRefUserDTO refUser : group.getRefUserIds()) {
                        if (refUser.getRefUserId().equalsIgnoreCase(refUserIndex.getRefUserId().getUserId())) {
                            refUser.setCallCardRefUserId(refUserIndex.getCallCardRefUserId());
                            refUser.setStartDate(refUserIndex.getStartDate());
                            refUser.setEndDate(refUserIndex.getEndDate());
                            refUser.setComment(refUserIndex.getComment());
                            refUser.setStatus(refUserIndex.getStatus());
                            refUser.setLastUpdated(refUserIndex.getLastUpdated());
                            refUser.setActive(refUserIndex.isActive());

                            refUserFound = true;

                            List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                            if (indexes != null && indexes.size() > 0) {

                                assignPendingCallCardAttributes:
                                for (CallCardRefUserIndex index : indexes) {
                                    for (CallCardActionsDTO action : refUser.getActions()) {
                                        if (index.getItemTypeId().getItemTypeId() == action.getItemTypeId()) {
                                            for (CallCardActionItemDTO item : action.getActionItems()) {
                                                if (index.getItemId().equalsIgnoreCase(item.getItemId())) {
                                                    for (CallCardActionItemAttributesDTO attribute : item.getAttributes()) {
                                                        if (index.getPropertyId().equalsIgnoreCase(attribute.getPropertyName())) {

                                                            if (refUser.getStatus() == null)
                                                                refUser.setStatus(index.getType());

                                                            attribute.setCallCardRefUserIndexId(index.getCallCardRefUserIndexId());
                                                            attribute.setPropertyValue(index.getPropertyValue());
                                                            attribute.setType(index.getType());
                                                            attribute.setStatus(index.getStatus());
                                                            attribute.setDateSubmitted(index.getSubmitDate());
                                                            attribute.setAmount(index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null);

                                                            continue assignPendingCallCardAttributes;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // load pending values for .sales attributes from SalesOrder, when sales orders are used
                            if (callCardWithSalesOrder && refUser != null && refUser.getActions().size() > 0) {
                                for (CallCardActionsDTO action : refUser.getActions()) {
                                    if (action.getActionItems() != null && action.getActionItems().size() > 0) {
                                        Map<String, List<CallCardActionItemAttributesDTO>> salesAttributesMap = null;

                                        for (CallCardActionItemDTO item : action.getActionItems()) {
                                            if (item.getAttributes() != null && item.getAttributes().size() > 0) {
                                                List<CallCardActionItemAttributesDTO> salesAttributes = null;

                                                if (salesAttributesMap == null)
                                                    salesAttributesMap = callCardValuesFromSalesOrder.get(refUserIndex.getCallCardRefUserId());

                                                if (salesAttributesMap != null && salesAttributesMap.size() > 0)
                                                    salesAttributes = salesAttributesMap.get(item.getItemId());

                                                for (CallCardActionItemAttributesDTO attribute : item.getAttributes()) {
                                                    if (salesOrderStatuses.contains(refUserIndex.getStatus()) &&
                                                            attribute.getPropertyName().equals(Constants.METADATA_KEY_CALL_CARD_INDEX_SALES) && attribute.getPropertyValue() == null) {
                                                        if (salesAttributes != null && salesAttributes.size() > 0) {
                                                            for (CallCardActionItemAttributesDTO salesAttribute : salesAttributes) {
                                                                if (salesAttribute.getPropertyName().equalsIgnoreCase(attribute.getPropertyName())) {
                                                                    attribute.setPropertyValue(salesAttribute.getPropertyValue());
                                                                    attribute.setType(salesAttribute.getType());
                                                                    attribute.setStatus(salesAttribute.getStatus());
                                                                    attribute.setDateSubmitted(salesAttribute.getDateSubmitted());
                                                                    attribute.setAmount(salesAttribute.getAmount());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (refUserFound == false) // Record refUsers Not in template eg. WS, POS missing from template, etc.
                    additionalRefUserIndexes.add(refUserIndex);
            }
        }

        // process refUsers Not in template eg POS, WS, etc
        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> additionalRefUserItemAttributesMap = new HashMap<>(); // <CallCardRefUserID , List of Attributes>

        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> additionalRefUserItemAttributeSummariesMap = new HashMap<>(); // < RefUserId, < BrandProductId, Attributes>>
        Map<String, List<KeyValueDTO>> additionalRefUserAdditionalInfoMap = new HashMap<>(); // < RefUserId, Additional Info>
        List<String> additionalVisitUsers = new ArrayList<String>();

        // Separate CallCardRefUsers that need to be filled with additional Info
        if (includeGeoInfo || previousValues != 0) {
            for (CallCardRefUser refUser : additionalRefUserIndexes) {
                if (!additionalVisitUsers.contains(refUser.getRefUserId().getUserId()) &&
                        (callCardVisitStatuses.contains(refUser.getStatus()))) {
                    additionalVisitUsers.add(refUser.getRefUserId().getUserId());
                }
            }
        }

        // Collect additional CallCard Info
        if (previousValues > 0)
            additionalRefUserItemAttributeSummariesMap = summarizeCallCardProperties(additionalVisitUsers, userId, previousValues, callCardVisitStatuses, metadataKeysTypeMap, false);

        if (includeGeoInfo)
            additionalRefUserAdditionalInfoMap = getAdditionalRefUserInfo(additionalVisitUsers);

        // itemId, property list
        Map<String, List<CallCardActionItemAttributesDTO>> defaultRefUserItemsMap = new HashMap<>();

        if (additionalRefUserIndexes != null && additionalRefUserIndexes.size() > 0) {
            for (CallCardRefUser refUserIndex : additionalRefUserIndexes) {

                if (refUserIndex.getStatus() != null && (refUserIndex.getStatus() == CallCardRefUserDTO.BUY || refUserIndex.getStatus() == CallCardRefUserDTO.INDIRECT_BUY)) {
                    List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                    if (indexes == null || indexes.size() == 0)
                        continue;

                    // Aggregate attributes for the defaultRefUser
                    for (CallCardRefUserIndex index : indexes) {

                        List<CallCardActionItemAttributesDTO> callCardActionItemAttributesDTOs = defaultRefUserItemsMap.get(index.getItemId());
                        if (defaultRefUserItemsMap.containsKey(index.getItemId()) &&
                                callCardActionItemAttributesDTOs != null && callCardActionItemAttributesDTOs.size() > 0) {
                            boolean propertyFound = false;

                            for (CallCardActionItemAttributesDTO itemAttribute : callCardActionItemAttributesDTOs) {
                                if (itemAttribute.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {

                                    Integer attributeValue = Integer.parseInt(itemAttribute.getPropertyValue()) + Integer.parseInt(index.getPropertyValue());
                                    itemAttribute.setPropertyValue(attributeValue.toString());

                                    propertyFound = true;
                                    break;
                                }
                            }

                            if (!propertyFound) { //Add attribute to Product
                                CallCardActionItemAttributesDTO newAttribute = new CallCardActionItemAttributesDTO(
                                        null,
                                        metadataKeysIdMap.get(index.getPropertyId()),
                                        index.getPropertyId(),
                                        metadataKeysTypeMap.get(index.getPropertyId()),
                                        index.getPropertyValue(),
                                        index.getSubmitDate(),
                                        index.getStatus(),
                                        index.getType(),
                                        index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                        null);

                                List<CallCardActionItemAttributesDTO> itemAttributes = defaultRefUserItemsMap.get(index.getItemId());
                                itemAttributes.add(newAttribute);

                                defaultRefUserItemsMap.put(index.getItemId(), itemAttributes);
                            }
                        } else { // Add Product and Attribute
                            CallCardActionItemAttributesDTO itemAttribute = new CallCardActionItemAttributesDTO(
                                    null,
                                    metadataKeysIdMap.get(index.getPropertyId()),
                                    index.getPropertyId(),
                                    metadataKeysTypeMap.get(index.getPropertyId()),
                                    index.getPropertyValue(),
                                    index.getSubmitDate(),
                                    index.getStatus(),
                                    index.getType(),
                                    index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                    null);

                            List<CallCardActionItemAttributesDTO> itemAttributes = new ArrayList<CallCardActionItemAttributesDTO>();
                            itemAttributes.add(itemAttribute);

                            defaultRefUserItemsMap.put(index.getItemId(), itemAttributes);
                        }
                    }

                    continue;
                }

                // already recorded RefUser
                if (additionalRefUserItemAttributesMap.containsKey(refUserIndex.getCallCardRefUserId())) {
                    Map<String, List<CallCardActionItemAttributesDTO>> refUserItemsMap = additionalRefUserItemAttributesMap.get(refUserIndex.getCallCardRefUserId());

                    List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                    if (indexes == null || indexes.size() == 0)
                        continue;

                    for (CallCardRefUserIndex index : indexes) {
                        for (Map.Entry<String, List<CallCardActionItemAttributesDTO>> refUserItem : refUserItemsMap.entrySet()) {
                            if (refUserItem.getKey().equalsIgnoreCase(index.getItemId())) {

                                List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;
                                if (additionalRefUserItemAttributeSummariesMap != null) {
                                    Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUserIndex.getRefUserId().getUserId());
                                    if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                        itemAttributeSummaries = itemAttributeSummariesMap.get(index.getItemId());
                                }

                                CallCardActionItemAttributesDTO summary = null;
                                if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                                    for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                        if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {
                                            summary = itemAttributeSummary;
                                            break;
                                        }
                                    }
                                }

                                List<CallCardActionItemAttributesDTO> additionalRefUserItemAttributes = refUserItemsMap.get(index.getItemId());

                                additionalRefUserItemAttributes.add(new CallCardActionItemAttributesDTO(
                                        index.getCallCardRefUserIndexId(),
                                        metadataKeysIdMap.get(index.getPropertyId()),
                                        index.getPropertyId(),
                                        metadataKeysTypeMap.get(index.getPropertyId()),
                                        index.getPropertyValue(),
                                        index.getSubmitDate(),
                                        index.getStatus(),
                                        index.getType(),
                                        index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                        summary != null ? summary.getRefPropertyValue() : null));

                            } else {
                                List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;
                                if (additionalRefUserItemAttributeSummariesMap != null) {
                                    Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUserIndex.getRefUserId().getUserId());
                                    if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                        itemAttributeSummaries = itemAttributeSummariesMap.get(index.getItemId());
                                }

                                CallCardActionItemAttributesDTO summary = null;
                                if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                                    for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                        if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {
                                            summary = itemAttributeSummary;
                                            break;
                                        }
                                    }
                                }

                                List<CallCardActionItemAttributesDTO> list = new ArrayList<>();
                                list.add(new CallCardActionItemAttributesDTO(
                                        index.getCallCardRefUserIndexId(),
                                        metadataKeysIdMap.get(index.getPropertyId()),
                                        index.getPropertyId(),
                                        metadataKeysTypeMap.get(index.getPropertyId()),
                                        index.getPropertyValue(),
                                        index.getSubmitDate(),
                                        index.getStatus(),
                                        index.getType(),
                                        index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                        summary != null ? summary.getRefPropertyValue() : null));

                                refUserItemsMap.put(index.getItemId(), list);
                            }
                        }
                    }
                }
                // new RefUser
                else {
                    Map<String, List<CallCardActionItemAttributesDTO>> additionalItemAttributesMap = new HashMap<>();
                    List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                    if (indexes == null || indexes.size() == 0)
                        continue;

                    List<CallCardActionItemAttributesDTO> list = null;
                    for (CallCardRefUserIndex index : indexes) {
                        List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;
                        if (additionalRefUserItemAttributeSummariesMap != null) {
                            Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUserIndex.getRefUserId().getUserId());
                            if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                itemAttributeSummaries = itemAttributeSummariesMap.get(index.getItemId());
                        }

                        CallCardActionItemAttributesDTO summary = null;
                        if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                            for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {
                                    summary = itemAttributeSummary;
                                    break;
                                }
                            }
                        }

                        if (additionalItemAttributesMap.containsKey(index.getItemId()))
                            list = additionalItemAttributesMap.get(index.getItemId());
                        else
                            list = new ArrayList<>();

                        list.add(new CallCardActionItemAttributesDTO(
                                index.getCallCardRefUserIndexId(),
                                metadataKeysIdMap.get(index.getPropertyId()),
                                index.getPropertyId(),
                                metadataKeysTypeMap.get(index.getPropertyId()),
                                index.getPropertyValue(),
                                index.getSubmitDate(),
                                index.getStatus(),
                                index.getType(),
                                index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                summary != null ? summary.getRefPropertyValue() : null));

                        additionalItemAttributesMap.put(index.getItemId(), list);
                    }
                    additionalRefUserItemAttributesMap.put(refUserIndex.getCallCardRefUserId(), additionalItemAttributesMap);
                }
            }

            // Add pending values from sales orders
            for (CallCardRefUser refUser : additionalRefUserIndexes) {
                Map<String, List<CallCardActionItemAttributesDTO>> salesOrderItemAttributesMap = new HashMap<>();

                if (callCardValuesFromSalesOrder != null)
                    salesOrderItemAttributesMap = callCardValuesFromSalesOrder.get(refUser.getCallCardRefUserId());

                if (salesOrderItemAttributesMap != null && salesOrderItemAttributesMap.size() > 0) {
                    for (Map.Entry<String, List<CallCardActionItemAttributesDTO>> itemAttributes : salesOrderItemAttributesMap.entrySet()) {

                        Map<String, List<CallCardActionItemAttributesDTO>> attributesMap = additionalRefUserItemAttributesMap.get(refUser.getCallCardRefUserId());
                        List<CallCardActionItemAttributesDTO> attributes = null;
                        if (attributesMap != null && attributesMap.size() > 0)
                            attributes = attributesMap.get(itemAttributes.getKey());

                        // Read summaries
                        List<CallCardActionItemAttributesDTO> itemAttributeSummaries = new ArrayList<CallCardActionItemAttributesDTO>();
                        Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = new HashMap<>();
                        if (additionalRefUserItemAttributeSummariesMap != null) {
                            itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUser.getRefUserId().getUserId());
                            if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                itemAttributeSummaries = itemAttributeSummariesMap.get(itemAttributes.getKey());
                        }

                        if (attributes != null) {

                            // find attribute summary
                            for (CallCardActionItemAttributesDTO itemAttribute : itemAttributes.getValue()) {
                                CallCardActionItemAttributesDTO summary = null;
                                for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                    if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(itemAttribute.getPropertyName())) {
                                        summary = itemAttributeSummary;
                                        break;
                                    }
                                }

                                // Append
                                attributes.add(new CallCardActionItemAttributesDTO(
                                        itemAttribute.getCallCardRefUserIndexId(),
                                        metadataKeysIdMap.get(itemAttribute.getPropertyName()),
                                        itemAttribute.getPropertyName(),
                                        metadataKeysTypeMap.get(itemAttribute.getPropertyName()),
                                        itemAttribute.getPropertyValue(),
                                        itemAttribute.getDateSubmitted(),
                                        itemAttribute.getStatus(),
                                        itemAttribute.getType(),
                                        itemAttribute.getAmount() != null ? itemAttribute.getAmount() : null,
                                        summary != null ? summary.getRefPropertyValue() : null));


                            }

                        }
                    }
                }
            }

        }

        if (defaultRefUserItemsMap != null && defaultRefUserItemsMap.size() > 0)
            additionalRefUserItemAttributesMap.put(CallCardRefUserDTO.DEFAULT_REF_USER_ID, defaultRefUserItemsMap);

        if (additionalRefUserItemAttributesMap != null && additionalRefUserItemAttributesMap.size() > 0) {
            List<CallCardRefUserDTO> refUserDTOtoAdd = new ArrayList<>();
            for (Map.Entry<String, Map<String, List<CallCardActionItemAttributesDTO>>> additionalEntry : additionalRefUserItemAttributesMap.entrySet()) {

                List<CallCardActionItemDTO> additionalActionItems = new ArrayList<>();

                Map<String, List<CallCardActionItemAttributesDTO>> additional = additionalEntry.getValue();
                for (Map.Entry<String, List<CallCardActionItemAttributesDTO>> additionalAction : additional.entrySet()) {
                    int categoryId = brandProductTypeCategoriesMap.get(additionalAction.getKey()) != null ? brandProductTypeCategoriesMap.get(additionalAction.getKey()) : 0;
                    additionalActionItems.add(new CallCardActionItemDTO(additionalAction.getKey(), Constants.ITEM_TYPE_BRAND_PRODUCT, additionalAction.getValue(), categoryId, false));
                }

                List<CallCardActionsDTO> list = new ArrayList<>();
                list.add(new CallCardActionsDTO(additionalActionItems, Constants.ITEM_TYPE_BRAND_PRODUCT, false));

                Date startDate = new Date();
                Date endDate = new Date();
                Date lastUpdated = new Date();
                String comment = null;
                Integer status = null;
                String refUserId = null;
                String refNo = null;
                Boolean active = true;
                if (additionalRefUserIndexes != null) {
                    for (CallCardRefUser additionalRefUserIndex : additionalRefUserIndexes) {
                        if (additionalEntry.getKey().equalsIgnoreCase(additionalRefUserIndex.getCallCardRefUserId())) {
                            startDate = additionalRefUserIndex.getStartDate();
                            endDate = additionalRefUserIndex.getEndDate();
                            lastUpdated = additionalRefUserIndex.getLastUpdated();
                            comment = additionalRefUserIndex.getComment();
                            refUserId = additionalRefUserIndex.getRefUserId().getUserId();
                            status = additionalRefUserIndex.getStatus();
                            active = additionalRefUserIndex.isActive();
                            refNo = additionalRefUserIndex.getRefNo();
                            break;
                        }
                    }
                    refUserId = additionalEntry.getKey().equalsIgnoreCase(CallCardRefUserDTO.DEFAULT_REF_USER_ID) ? additionalEntry.getKey() : refUserId;
                }

                List<KeyValueDTO> additionalRefUserInfo = null;
                if (additionalRefUserAdditionalInfoMap != null && additionalRefUserAdditionalInfoMap.containsKey(refUserId))
                    additionalRefUserInfo = additionalRefUserAdditionalInfoMap.get(refUserId);

                CallCardRefUserDTO additionalRefUserDTO = new CallCardRefUserDTO(
                        additionalEntry.getKey(),
                        refUserId,
                        list,
                        startDate,
                        endDate,
                        false,
                        comment,
                        status,
                        lastUpdated,
                        refNo,
                        userId,
                        additionalRefUserInfo,
                        active);

                refUserDTOtoAdd.add(additionalRefUserDTO);
            }

            CallCardGroupDTO additionalGroupDTO = new CallCardGroupDTO(CallCardGroupDTO.STOCK_DATA_GROUP, refUserDTOtoAdd, "");
            callCardDTO.getGroupIds().add(additionalGroupDTO);
        }

        return callCardDTO;
    }

    @Override
    @Transactional
    public CallCardDTO getNewOrPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId, String callCardId, List<String> filterProperties) {
        LOGGER.info("-- CallCardManagement.getNewOrPendingCallCard : userId={} userGroupId={} gameTypeId={}", userId, userGroupId, gameTypeId);

        String additionalEventProperties = "";

        String callCardTemplateId = null;
        CallCard callCard = null;
        CallCardTemplate template = null;

        if (StringUtils.isNotBlank(callCardId)) {
            callCard = callCardDao.read(callCardId);

            callCardTemplateId = callCard.getCallCardTemplateId().getCallCardTemplateId();

            template = callCardTemplateDao.read(callCardTemplateId);

            additionalEventProperties = (EventTO.PROPERTY_STATUS + "=existed\n");
        } else {

            callCard = checkIfActiveCallCard(userId, userGroupId, gameTypeId);
            if (callCard != null) {
                callCardTemplateId = callCard.getCallCardTemplateId().getCallCardTemplateId();

                template = callCardTemplateDao.read(callCardTemplateId);
            } else {
                // check in CALL_CARD_TEMPLATE_USERS for related template
                additionalEventProperties = (EventTO.PROPERTY_STATUS + "=fromTemplate\n");

                // check for associated Template
                template = getCallCardTemplate(userGroupId, gameTypeId, applicationId, userId);

            }
        }

        LOGGER.info("-- CallCardTemplate returned : {}", template.getCallCardTemplateId());

        // Read GameType's CallCard configuration from application settings
        int previousValues = 0;
        boolean includeGeoInfo = false;
        List<String> productTypeCategories = new ArrayList<String>();
        List<AppSettingsDTO> appSettings = appSettingsComponent.get(null, applicationId, Collections.singletonList(ScopeType.GAME_TYPE));
        if (appSettings != null || appSettings.size() > 0) {
            for (AppSettingsDTO appSetting : appSettings) {
                switch (appSetting.getKey()) {
                    case Constants.APP_SETTING_KEY_PREVIOUS_VISITS_SUMMARY:
                        try {
                            previousValues = Integer.parseInt(appSetting.getValue());
                        } catch (NumberFormatException e) {
                            LOGGER.error("Invalid application setting format for GameTypeId=" + gameTypeId + " AppSettingKey=" + appSetting.getKey() + " Value=" + appSetting.getValue(), ExceptionTypeTO.GENERIC);
                        }
                        break;
                    case Constants.APP_SETTING_KEY_INCLUDE_VISITS_GEO_INFO:
                        includeGeoInfo = Boolean.parseBoolean(appSetting.getValue());
                        break;
                    case Constants.APP_SETTING_KEY_PRODUCT_TYPE_CATEGORIES:
                        try {
                            StringTokenizer tokenizer = new StringTokenizer(appSetting.getValue(), ",");
                            while (tokenizer.hasMoreElements()) {
                                productTypeCategories.add((String) tokenizer.nextElement());
                            }
                        } catch (NumberFormatException e) {
                            LOGGER.error("Invalid application setting format for GameTypeId=" + gameTypeId + " AppSettingKey=" + appSetting.getKey() + " Value=" + appSetting.getValue(), ExceptionTypeTO.GENERIC);
                        }
                        break;
                }
            }
        }

        LOGGER.info("CallCard configuration: \n" +
                "- Number of previous values to summarize: " + previousValues + "\n" +
                "- Include Geographical info for CallCardRefUsers: " + includeGeoInfo + "\n");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Read list of CallCard properties
        List<MetadataKeyDTO> metadataKeyDTOs = metadataComponent.listMetadataKeysByItemType(Constants.ITEM_TYPE_CALL_CARD_INDEX, false);
        Map<String, String> metadataKeysTypeMap = new HashMap<>();
        Map<String, String> metadataKeysIdMap = new HashMap<>();
        if (metadataKeyDTOs != null && metadataKeyDTOs.size() > 0) {
            for (MetadataKeyDTO metadataKeyDTO : metadataKeyDTOs) {
                metadataKeysTypeMap.put(metadataKeyDTO.getMetadataKeyName(), metadataKeyDTO.getDataTypeName());
                metadataKeysIdMap.put(metadataKeyDTO.getMetadataKeyName(), metadataKeyDTO.getMetadataKeyId());
            }
        }

        //get Brand product Type Categories
        Map<String, Integer> brandProductTypeCategoriesMap = new HashMap<String, Integer>();
        Map<Integer, List<SolrBrandProductDTO>> solrBrandProducts = solrClient.getMultipleBrandProducts(gameTypeId,
                null,
                "",
                null,
                null,
                new String[]{},
                new Integer[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                0,
                -1,
                false,
                true,
                null,
                null,
                null,
                false,
                null,
                null,
                false,
                null,
                SortOrderTypes.BY_ORDERING_ASC);

        List<SolrBrandProductDTO> brandProducts = solrBrandProducts != null ? solrBrandProducts.values().iterator().next() : null;

        if (brandProducts != null && brandProducts.size() > 0) {
            for (SolrBrandProductDTO brandProduct : brandProducts) {
                if (brandProduct != null && brandProduct.getSubcategoryIds() != null) {
                    List<String> categories = new ArrayList<String>(Arrays.asList(brandProduct.getSubcategoryIds()));

                    categories.retainAll(productTypeCategories);
                    if (categories != null && categories.size() > 0)
                        brandProductTypeCategoriesMap.put(brandProduct.getBrandProductId(), Integer.parseInt(categories.get(0)));
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        CallCardDTO callCardDTO = new CallCardDTO();
        callCardDTO.setCallCardTemplateId(template.getCallCardTemplateId());

        List<CallCardActionItemDTO> items = null;

        int itemsItemTypeId = 0;
        if (template.getEntries() != null && template.getEntries().size() > 0) {
            List<CallCardTemplateEntry> templateEntries = template.getEntries();
            Collections.sort(templateEntries, new CallCardTemplateEntryComparator());

            items = new ArrayList<>();
            for (CallCardTemplateEntry templateEntry : templateEntries) {
                LOGGER.debug("----- List of CallCardTemplateEntries for template= {}  : size={}", templateEntry.getCallCardTemplateId(), template.getPos().size());

                List<CallCardActionItemAttributesDTO> attributes = new ArrayList<>();
                if (StringUtils.isNotBlank(templateEntry.getProperties())) {
                    LOGGER.debug("------ Properties for CallCardTemplate with ID={} : properties={}", templateEntry.getCallCardTemplateId(), templateEntry.getProperties());
                    StringTokenizer properties = new StringTokenizer(templateEntry.getProperties(), ","); //split properties

                    while (properties.hasMoreElements()) {
                        String attribute = (String) properties.nextElement();

                        attributes.add(new CallCardActionItemAttributesDTO(
                                null,
                                metadataKeysIdMap.get(attribute),
                                attribute,
                                metadataKeysTypeMap.get(attribute),
                                null,
                                null,
                                null,
                                CallCardRefUserDTO.SELL,
                                null,
                                null));
                    }

                    itemsItemTypeId = templateEntry.getItemTypeId().getItemTypeId();
                    int categoryId = brandProductTypeCategoriesMap.get(templateEntry.getId()) != null ? brandProductTypeCategoriesMap.get(templateEntry.getId()) : 0;
                    items.add(new CallCardActionItemDTO(templateEntry.getItemId(), templateEntry.getItemTypeId().getItemTypeId(), attributes, categoryId, false));
                }
            }
        }

        // add other references
        Map<String, List<CallCardActionItemDTO>> referencesMap = null;

        List<CallCardTemplateUserReferences> references = callCardTemplateUserReferencesDao.queryList("listByCallCardTemplateId", template.getCallCardTemplateId());
        if (references != null && references.size() > 0) {
            referencesMap = new HashMap<String, List<CallCardActionItemDTO>>(); //< RefUserId, List of ActionItems>

            for (CallCardTemplateUserReferences reference : references) {
                List<CallCardActionItemDTO> referencesList = null;
                if (referencesMap.containsKey(reference.getRefUserId().getUserId()))
                    referencesList = referencesMap.get(reference.getRefUserId().getUserId());
                else
                    referencesList = new ArrayList<>();

                int categoryId = 0;
                if (reference.getItemTypeId().getItemTypeId() == Constants.ITEM_TYPE_BRAND_PRODUCT)
                    categoryId = brandProductTypeCategoriesMap.get(reference.getItemId()) != null ? brandProductTypeCategoriesMap.get(reference.getItemId()) : 0;

                referencesList.add(new CallCardActionItemDTO(reference.getItemId(), reference.getItemTypeId().getItemTypeId(), null, categoryId, reference.isMandatory()));
                referencesMap.put(reference.getRefUserId().getUserId(), referencesList);
            }
        }


        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> refUserItemAttributeSummariesMap = new HashMap<>();//< RefUserId, < BrandProductId, Attributes>>
        Map<String, List<KeyValueDTO>> refUserAdditionalInfoMap = new HashMap<>();//< RefUserId, Additional Info>
        List<String> templateRefUsers = new ArrayList<String>();
        List<CallCardTemplatePOS> callCardTemplatePOSs = callCardTemplatePOSDao.queryList("listByCallCardTemplateId", template.getCallCardTemplateId());
        if (callCardTemplatePOSs != null && callCardTemplatePOSs.size() > 0) {
            Map<Integer, List<CallCardRefUserDTO>> groupUserDTOs = new HashMap<>();

            // Create list of visit targets from template,that need to be filled with additional data
            if (includeGeoInfo || previousValues != 0) {
                for (CallCardTemplatePOS pos : callCardTemplatePOSs) {
                    if (!templateRefUsers.contains(pos.getRefUserId().getUserId()))
                        templateRefUsers.add(pos.getRefUserId().getUserId());
                }
            }

            // Collect additional CallCard Info
            if (previousValues > 0)
                refUserItemAttributeSummariesMap = summarizeCallCardProperties(templateRefUsers, userId, previousValues, callCardVisitStatuses, metadataKeysTypeMap, false);

            if (includeGeoInfo)
                refUserAdditionalInfoMap = getAdditionalRefUserInfo(templateRefUsers);

            LOGGER.info("-- List of POS : size= {}", template.getPos().size());
            for (CallCardTemplatePOS pos : callCardTemplatePOSs) {

                List<CallCardRefUserDTO> userDTOs;

                if (groupUserDTOs.containsKey(pos.getGroupId()))
                    userDTOs = groupUserDTOs.get(pos.getGroupId());
                else if (pos.getGroupId() == null && groupUserDTOs.containsKey(CallCardGroupDTO.UNASSIGNED_POS_GROUP))
                    userDTOs = groupUserDTOs.get(CallCardGroupDTO.UNASSIGNED_POS_GROUP);
                else
                    userDTOs = new ArrayList<CallCardRefUserDTO>();

                List<CallCardActionsDTO> userActions = new ArrayList<>();
                if (items != null && items.size() > 0) {
                    List<CallCardActionItemDTO> actionItems = new ArrayList<CallCardActionItemDTO>();

                    for (CallCardActionItemDTO item : items) {
                        List<CallCardActionItemAttributesDTO> actionItemsAttributes = new ArrayList<CallCardActionItemAttributesDTO>();

                        List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;

                        if (refUserItemAttributeSummariesMap != null) {
                            Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = refUserItemAttributeSummariesMap.get(pos.getRefUserId().getUserId());
                            if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                itemAttributeSummaries = itemAttributeSummariesMap.get(item.getItemId());
                        }

                        for (CallCardActionItemAttributesDTO itemAttribute : item.getAttributes()) {
                            CallCardActionItemAttributesDTO summary = new CallCardActionItemAttributesDTO();

                            if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                                for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                    if (itemAttributeSummary != null && itemAttributeSummary.getPropertyName().equalsIgnoreCase(itemAttribute.getPropertyName())) {
                                        summary = itemAttributeSummary;
                                        break;
                                    }
                                }
                            }

                            actionItemsAttributes.add(new CallCardActionItemAttributesDTO(
                                    itemAttribute.getCallCardRefUserIndexId(),
                                    itemAttribute.getPropertyId(),
                                    itemAttribute.getPropertyName(),
                                    itemAttribute.getPropertyTypeId(),
                                    itemAttribute.getPropertyValue(),
                                    itemAttribute.getDateSubmitted(),
                                    itemAttribute.getStatus(),
                                    itemAttribute.getType(),
                                    null,
                                    summary != null ? summary.getRefPropertyValue() : null));
                        }

                        int categoryId = 0;
                        switch (item.getItemTypeId()) {
                            case Constants.ITEM_TYPE_BRAND_PRODUCT:
                                categoryId = brandProductTypeCategoriesMap.get(item.getItemId()) != null ? brandProductTypeCategoriesMap.get(item.getItemId()) : 0;
                                break;
                        }
                        actionItems.add(new CallCardActionItemDTO(item.getItemId(), item.getItemTypeId(), actionItemsAttributes, categoryId, item.isMandatory()));
                    }

                    userActions.add(new CallCardActionsDTO(actionItems, itemsItemTypeId, pos.isMandatory()));
                }

                if (referencesMap != null && referencesMap.containsKey(pos.getRefUserId().getUserId())) {

                    boolean mandatorySurveyExists = false; // Mark if there is at list one mandatory Survey

                    List<CallCardActionItemDTO> actionItems = referencesMap.get(pos.getRefUserId().getUserId());
                    for (CallCardActionItemDTO item : actionItems) {
                        if ((item.getItemTypeId() == Constants.ITEM_TYPE_QUIZ) && item.isMandatory()) {
                            mandatorySurveyExists = true;
                            break;
                        }
                    }

                    int itemTypeId = referencesMap.get(pos.getRefUserId().getUserId()).get(0).getItemTypeId();

                    userActions.add(new CallCardActionsDTO(referencesMap.get(pos.getRefUserId().getUserId()), itemTypeId, mandatorySurveyExists));
                }

                List<KeyValueDTO> additionalRefUserInfo = refUserItemAttributeSummariesMap != null ? refUserAdditionalInfoMap.get(pos.getRefUserId().getUserId()) : null;

                CallCardRefUserDTO refUserDTO = new CallCardRefUserDTO(null, pos.getRefUserId().getUserId(), userActions, null, null, pos.isMandatory(), null, CallCardRefUserDTO.SELL, null, null, userId, additionalRefUserInfo, true);

                userDTOs.add(refUserDTO);

                groupUserDTOs.put(pos.getGroupId() != null ? pos.getGroupId() : CallCardGroupDTO.UNASSIGNED_POS_GROUP, userDTOs);
            }

            List<CallCardGroupDTO> groupDTOs = new ArrayList<>();
            for (Map.Entry<Integer, List<CallCardRefUserDTO>> groupUserDTO : groupUserDTOs.entrySet()) {
                groupDTOs.add(new CallCardGroupDTO(groupUserDTO.getKey(), groupUserDTO.getValue(), template.getCallCardTemplateId()));
            }

            callCardDTO.setGroupIds(groupDTOs);

        }

        if (callCard == null) {
            // Check if callCard with content already pending
            List<CallCard> callCards = erpDynamicQueryManager.listCallCards(null, Arrays.asList(userId), null, Arrays.asList(template.getCallCardTemplateId()), null, true, true, true, gameTypeId, 0, -1);
            if (callCards == null || callCards.size() != 1) {
                callCard = addCallCard(template.getCallCardTemplateId(), userId, new Date(), null, true, null, null);

                callCardDTO.setCallCardId(callCard.getCallCardId());
                callCardDTO.setStartDate(callCard.getStartDate());

                if (!includeGeoInfo && previousValues == 0)
                    return callCardDTO;
            }

            if (callCards != null && callCards.size() > 0) {
                LOGGER.info("-- erpDynamicQueryManager.listCallCards - List of returned pending CallCards : size={}", callCards.size());

                LOGGER.info("-- Most recent pending CallCard={}", callCards.get(0));

                // get the latest one
                callCard = callCards.get(0);
            }
        }

        callCardDTO.setCallCardId(callCard.getCallCardId());
        callCardDTO.setStartDate(callCard.getStartDate());
        callCardDTO.setEndDate(callCard.getEndDate());
        callCardDTO.setComments(callCard.getComments());
        callCardDTO.setLastUpdated(callCard.getLastUpdated());

        List<CallCardRefUser> additionalRefUserIndexes = new ArrayList<>();

        dispatchEvent(EventType.CALL_CARD_DOWNLOADED, userId, gameTypeId, applicationId, callCardDTO.getCallCardId(), Constants.ITEM_TYPE_CALL_CARD, 0, additionalEventProperties, true);

        // Read call card from DB
        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> callCardValuesFromSalesOrder = new HashMap<String, Map<String, List<CallCardActionItemAttributesDTO>>>();
        List<CallCardRefUser> callCardRefUsers = callCardRefUserDao.queryList("listByCallCardId", callCard.getCallCardId());

        Boolean callCardWithSalesOrder = false;
        LOGGER.debug("-- Most recent pending CallCard - refUserIndex : size={}", callCardRefUsers.size());
        if (callCardRefUsers != null && callCardRefUsers.size() > 0) {

            for (CallCardRefUser refUserIndex : callCardRefUsers) {
                boolean refUserFound = false;

                if (!callCardWithSalesOrder && salesOrderStatuses.contains(refUserIndex.getStatus())) { // if CallCard uses Sales Orders
                    callCardValuesFromSalesOrder = getCallCardValuesFromSalesOrder(callCard);
                    callCardWithSalesOrder = true;
                }

                if (refUserIndex.getStatus() == CallCardRefUserDTO.UNSCHEDULED_SELL || refUserIndex.getStatus() == CallCardRefUserDTO.UNSCHEDULED_ORDER) {
                    additionalRefUserIndexes.add(refUserIndex);
                    continue;
                }

                if (callCardDTO.getGroupIds() == null || callCardDTO.getGroupIds().size() == 0)
                    continue;

                for (CallCardGroupDTO group : callCardDTO.getGroupIds()) {

                    if (group.getRefUserIds() == null || group.getRefUserIds().size() == 0)
                        continue;

                    for (CallCardRefUserDTO refUser : group.getRefUserIds()) {
                        if (refUser.getRefUserId().equalsIgnoreCase(refUserIndex.getRefUserId().getUserId())) {
                            refUser.setCallCardRefUserId(refUserIndex.getCallCardRefUserId());
                            refUser.setStartDate(refUserIndex.getStartDate());
                            refUser.setEndDate(refUserIndex.getEndDate());
                            refUser.setComment(refUserIndex.getComment());
                            refUser.setStatus(refUserIndex.getStatus());
                            refUser.setLastUpdated(refUserIndex.getLastUpdated());
                            refUser.setActive(refUserIndex.isActive());

                            refUserFound = true;

                            List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                            if (indexes != null && indexes.size() > 0) {

                                assignPendingCallCardAttributes:
                                for (CallCardRefUserIndex index : indexes) {
                                    for (CallCardActionsDTO action : refUser.getActions()) {
                                        if (index.getItemTypeId().getItemTypeId() == action.getItemTypeId()) {
                                            for (CallCardActionItemDTO item : action.getActionItems()) {
                                                if (index.getItemId().equalsIgnoreCase(item.getItemId())) {
                                                    for (CallCardActionItemAttributesDTO attribute : item.getAttributes()) {
                                                        if (index.getPropertyId().equalsIgnoreCase(attribute.getPropertyName())) {

                                                            if (refUser.getStatus() == null)
                                                                refUser.setStatus(index.getType());

                                                            attribute.setCallCardRefUserIndexId(index.getCallCardRefUserIndexId());
                                                            attribute.setPropertyValue(index.getPropertyValue());
                                                            attribute.setType(index.getType());
                                                            attribute.setStatus(index.getStatus());
                                                            attribute.setDateSubmitted(index.getSubmitDate());
                                                            attribute.setAmount(index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null);

                                                            continue assignPendingCallCardAttributes;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // load pending values for .sales attributes from SalesOrder, when sales orders are used
                            if (callCardWithSalesOrder && refUser != null && refUser.getActions().size() > 0) {
                                for (CallCardActionsDTO action : refUser.getActions()) {
                                    if (action.getActionItems() != null && action.getActionItems().size() > 0) {
                                        Map<String, List<CallCardActionItemAttributesDTO>> salesAttributesMap = null;

                                        for (CallCardActionItemDTO item : action.getActionItems()) {
                                            if (item.getAttributes() != null && item.getAttributes().size() > 0) {
                                                List<CallCardActionItemAttributesDTO> salesAttributes = null;

                                                if (salesAttributesMap == null)
                                                    salesAttributesMap = callCardValuesFromSalesOrder.get(refUserIndex.getCallCardRefUserId());

                                                if (salesAttributesMap != null && salesAttributesMap.size() > 0)
                                                    salesAttributes = salesAttributesMap.get(item.getItemId());

                                                for (CallCardActionItemAttributesDTO attribute : item.getAttributes()) {
                                                    if (salesOrderStatuses.contains(refUserIndex.getStatus()) &&
                                                            attribute.getPropertyName().equals(Constants.METADATA_KEY_CALL_CARD_INDEX_SALES) && attribute.getPropertyValue() == null) {
                                                        if (salesAttributes != null && salesAttributes.size() > 0) {
                                                            for (CallCardActionItemAttributesDTO salesAttribute : salesAttributes) {
                                                                if (salesAttribute.getPropertyName().equalsIgnoreCase(attribute.getPropertyName())) {
                                                                    attribute.setPropertyValue(salesAttribute.getPropertyValue());
                                                                    attribute.setType(salesAttribute.getType());
                                                                    attribute.setStatus(salesAttribute.getStatus());
                                                                    attribute.setDateSubmitted(salesAttribute.getDateSubmitted());
                                                                    attribute.setAmount(salesAttribute.getAmount());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (refUserFound == false) // Record refUsers Not in template eg. WS, POS missing from template, etc.
                    additionalRefUserIndexes.add(refUserIndex);
            }
        }

        // process refUsers Not in template eg POS, WS, etc
        // CallCardRefUserID , List of Attributes
        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> additionalRefUserItemAttributesMap = new HashMap<>();

        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> additionalRefUserItemAttributeSummariesMap = new HashMap<>();//< RefUserId, < BrandProductId, Attributes>>
        Map<String, List<KeyValueDTO>> additionalRefUserAdditionalInfoMap = new HashMap<>();//< RefUserId, Additional Info>
        List<String> additionalVisitUsers = new ArrayList<String>();

        // Separate CallCardRefUsers that need to be filled with additional Info
        if (includeGeoInfo || previousValues != 0) {
            for (CallCardRefUser refUser : additionalRefUserIndexes) {
                if (!additionalVisitUsers.contains(refUser.getRefUserId().getUserId()) &&
                        (callCardVisitStatuses.contains(refUser.getStatus()))) {
                    additionalVisitUsers.add(refUser.getRefUserId().getUserId());
                }
            }
        }

        // Collect additional CallCard Info
        if (previousValues > 0)
            additionalRefUserItemAttributeSummariesMap = summarizeCallCardProperties(additionalVisitUsers, userId, previousValues, callCardVisitStatuses, metadataKeysTypeMap, false);

        if (includeGeoInfo)
            additionalRefUserAdditionalInfoMap = getAdditionalRefUserInfo(additionalVisitUsers);

        // itemId, property list
        Map<String, List<CallCardActionItemAttributesDTO>> defaultRefUserItemsMap = new HashMap<>();

        if (additionalRefUserIndexes != null && additionalRefUserIndexes.size() > 0) {
            for (CallCardRefUser refUserIndex : additionalRefUserIndexes) {

                if (refUserIndex.getStatus() != null && (refUserIndex.getStatus() == CallCardRefUserDTO.BUY || refUserIndex.getStatus() == CallCardRefUserDTO.INDIRECT_BUY)) {
                    List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                    if (indexes == null || indexes.size() == 0)
                        continue;

                    // Aggregate attributes for the defaultRefUser
                    for (CallCardRefUserIndex index : indexes) {

                        List<CallCardActionItemAttributesDTO> callCardActionItemAttributesDTOs = defaultRefUserItemsMap.get(index.getItemId());
                        if (defaultRefUserItemsMap.containsKey(index.getItemId()) &&
                                callCardActionItemAttributesDTOs != null && callCardActionItemAttributesDTOs.size() > 0) {
                            boolean propertyFound = false;

                            for (CallCardActionItemAttributesDTO itemAttribute : callCardActionItemAttributesDTOs) {
                                if (itemAttribute.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {

                                    Integer attributeValue = Integer.parseInt(itemAttribute.getPropertyValue()) + Integer.parseInt(index.getPropertyValue());
                                    itemAttribute.setPropertyValue(attributeValue.toString());

                                    propertyFound = true;
                                    break;
                                }
                            }

                            if (!propertyFound) { //Add attribute to Product
                                CallCardActionItemAttributesDTO newAttribute = new CallCardActionItemAttributesDTO(
                                        null,
                                        metadataKeysIdMap.get(index.getPropertyId()),
                                        index.getPropertyId(),
                                        metadataKeysTypeMap.get(index.getPropertyId()),
                                        index.getPropertyValue(),
                                        index.getSubmitDate(),
                                        index.getStatus(),
                                        index.getType(),
                                        index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                        null);

                                List<CallCardActionItemAttributesDTO> itemAttributes = defaultRefUserItemsMap.get(index.getItemId());
                                itemAttributes.add(newAttribute);

                                defaultRefUserItemsMap.put(index.getItemId(), itemAttributes);
                            }
                        } else { // Add Product and Attribute
                            CallCardActionItemAttributesDTO itemAttribute = new CallCardActionItemAttributesDTO(
                                    null,
                                    metadataKeysIdMap.get(index.getPropertyId()),
                                    index.getPropertyId(),
                                    metadataKeysTypeMap.get(index.getPropertyId()),
                                    index.getPropertyValue(),
                                    index.getSubmitDate(),
                                    index.getStatus(),
                                    index.getType(),
                                    index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                    null);

                            List<CallCardActionItemAttributesDTO> itemAttributes = new ArrayList<CallCardActionItemAttributesDTO>();
                            itemAttributes.add(itemAttribute);

                            defaultRefUserItemsMap.put(index.getItemId(), itemAttributes);
                        }
                    }

                    continue;
                }

                // already recorded RefUser
                if (additionalRefUserItemAttributesMap.containsKey(refUserIndex.getCallCardRefUserId())) {
                    Map<String, List<CallCardActionItemAttributesDTO>> refUserItemsMap = additionalRefUserItemAttributesMap.get(refUserIndex.getCallCardRefUserId());

                    List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                    if (indexes == null || indexes.size() == 0)
                        continue;

                    for (CallCardRefUserIndex index : indexes) {
                        for (Map.Entry<String, List<CallCardActionItemAttributesDTO>> refUserItem : refUserItemsMap.entrySet()) {
                            if (refUserItem.getKey().equalsIgnoreCase(index.getItemId())) {

                                List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;
                                if (additionalRefUserItemAttributeSummariesMap != null) {
                                    Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUserIndex.getRefUserId().getUserId());
                                    if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                        itemAttributeSummaries = itemAttributeSummariesMap.get(index.getItemId());
                                }

                                CallCardActionItemAttributesDTO summary = null;
                                if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                                    for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                        if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {
                                            summary = itemAttributeSummary;
                                            break;
                                        }
                                    }
                                }

                                List<CallCardActionItemAttributesDTO> additionalRefUserItemAttributes = refUserItemsMap.get(index.getItemId());

                                additionalRefUserItemAttributes.add(new CallCardActionItemAttributesDTO(
                                        index.getCallCardRefUserIndexId(),
                                        metadataKeysIdMap.get(index.getPropertyId()),
                                        index.getPropertyId(),
                                        metadataKeysTypeMap.get(index.getPropertyId()),
                                        index.getPropertyValue(),
                                        index.getSubmitDate(),
                                        index.getStatus(),
                                        index.getType(),
                                        index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                        summary != null ? summary.getRefPropertyValue() : null));

                            } else {
                                List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;
                                if (additionalRefUserItemAttributeSummariesMap != null) {
                                    Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUserIndex.getRefUserId().getUserId());
                                    if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                        itemAttributeSummaries = itemAttributeSummariesMap.get(index.getItemId());
                                }

                                CallCardActionItemAttributesDTO summary = null;
                                if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                                    for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                        if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {
                                            summary = itemAttributeSummary;
                                            break;
                                        }
                                    }
                                }

                                List<CallCardActionItemAttributesDTO> list = new ArrayList<>();
                                list.add(new CallCardActionItemAttributesDTO(
                                        index.getCallCardRefUserIndexId(),
                                        metadataKeysIdMap.get(index.getPropertyId()),
                                        index.getPropertyId(),
                                        metadataKeysTypeMap.get(index.getPropertyId()),
                                        index.getPropertyValue(),
                                        index.getSubmitDate(),
                                        index.getStatus(),
                                        index.getType(),
                                        index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                        summary != null ? summary.getRefPropertyValue() : null));

                                refUserItemsMap.put(index.getItemId(), list);
                            }
                        }
                    }
                }
                // new RefUser
                else {
                    Map<String, List<CallCardActionItemAttributesDTO>> additionalItemAttributesMap = new HashMap<>();
                    List<CallCardRefUserIndex> indexes = callCardRefUserIndexDao.queryList("listByCallCardRefUserId", refUserIndex.getCallCardRefUserId());
                    if (indexes == null || indexes.size() == 0)
                        continue;

                    List<CallCardActionItemAttributesDTO> list = null;
                    for (CallCardRefUserIndex index : indexes) {
                        List<CallCardActionItemAttributesDTO> itemAttributeSummaries = null;
                        if (additionalRefUserItemAttributeSummariesMap != null) {
                            Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUserIndex.getRefUserId().getUserId());
                            if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                itemAttributeSummaries = itemAttributeSummariesMap.get(index.getItemId());
                        }

                        CallCardActionItemAttributesDTO summary = null;
                        if (itemAttributeSummaries != null && itemAttributeSummaries.size() > 0) {
                            for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(index.getPropertyId())) {
                                    summary = itemAttributeSummary;
                                    break;
                                }
                            }
                        }

                        if (additionalItemAttributesMap.containsKey(index.getItemId()))
                            list = additionalItemAttributesMap.get(index.getItemId());
                        else
                            list = new ArrayList<>();

                        list.add(new CallCardActionItemAttributesDTO(
                                index.getCallCardRefUserIndexId(),
                                metadataKeysIdMap.get(index.getPropertyId()),
                                index.getPropertyId(),
                                metadataKeysTypeMap.get(index.getPropertyId()),
                                index.getPropertyValue(),
                                index.getSubmitDate(),
                                index.getStatus(),
                                index.getType(),
                                index.getAmount() != null ? new DecimalDTO(index.getAmount()) : null,
                                summary != null ? summary.getRefPropertyValue() : null));

                        additionalItemAttributesMap.put(index.getItemId(), list);
                    }
                    additionalRefUserItemAttributesMap.put(refUserIndex.getCallCardRefUserId(), additionalItemAttributesMap);
                }
            }

            // Add pending values from sales orders
            for (CallCardRefUser refUser : additionalRefUserIndexes) {
                Map<String, List<CallCardActionItemAttributesDTO>> salesOrderItemAttributesMap = new HashMap<>();

                if (callCardValuesFromSalesOrder != null)
                    salesOrderItemAttributesMap = callCardValuesFromSalesOrder.get(refUser.getCallCardRefUserId());

                if (salesOrderItemAttributesMap != null && salesOrderItemAttributesMap.size() > 0) {
                    for (Map.Entry<String, List<CallCardActionItemAttributesDTO>> itemAttributes : salesOrderItemAttributesMap.entrySet()) {

                        Map<String, List<CallCardActionItemAttributesDTO>> attributesMap = additionalRefUserItemAttributesMap.get(refUser.getCallCardRefUserId());
                        List<CallCardActionItemAttributesDTO> attributes = null;
                        if (attributesMap != null && attributesMap.size() > 0)
                            attributes = attributesMap.get(itemAttributes.getKey());

                        // Read summaries
                        List<CallCardActionItemAttributesDTO> itemAttributeSummaries = new ArrayList<CallCardActionItemAttributesDTO>();
                        Map<String, List<CallCardActionItemAttributesDTO>> itemAttributeSummariesMap = new HashMap<>();
                        if (additionalRefUserItemAttributeSummariesMap != null) {
                            itemAttributeSummariesMap = additionalRefUserItemAttributeSummariesMap.get(refUser.getRefUserId().getUserId());
                            if (itemAttributeSummariesMap != null && itemAttributeSummariesMap.size() > 0)
                                itemAttributeSummaries = itemAttributeSummariesMap.get(itemAttributes.getKey());
                        }

                        if (attributes != null) {

                            // find attribute summary
                            for (CallCardActionItemAttributesDTO itemAttribute : itemAttributes.getValue()) {
                                CallCardActionItemAttributesDTO summary = null;
                                for (CallCardActionItemAttributesDTO itemAttributeSummary : itemAttributeSummaries) {
                                    if (itemAttributeSummary.getPropertyName().equalsIgnoreCase(itemAttribute.getPropertyName())) {
                                        summary = itemAttributeSummary;
                                        break;
                                    }
                                }

                                // Append
                                attributes.add(new CallCardActionItemAttributesDTO(
                                        itemAttribute.getCallCardRefUserIndexId(),
                                        metadataKeysIdMap.get(itemAttribute.getPropertyName()),
                                        itemAttribute.getPropertyName(),
                                        metadataKeysTypeMap.get(itemAttribute.getPropertyName()),
                                        itemAttribute.getPropertyValue(),
                                        itemAttribute.getDateSubmitted(),
                                        itemAttribute.getStatus(),
                                        itemAttribute.getType(),
                                        itemAttribute.getAmount() != null ? itemAttribute.getAmount() : null,
                                        summary != null ? summary.getRefPropertyValue() : null));


                            }

                        }
                    }
                }
            }

        }

        if (defaultRefUserItemsMap != null && defaultRefUserItemsMap.size() > 0)
            additionalRefUserItemAttributesMap.put(CallCardRefUserDTO.DEFAULT_REF_USER_ID, defaultRefUserItemsMap);

        if (additionalRefUserItemAttributesMap != null && additionalRefUserItemAttributesMap.size() > 0) {
            List<CallCardRefUserDTO> refUserDTOtoAdd = new ArrayList<>();
            for (Map.Entry<String, Map<String, List<CallCardActionItemAttributesDTO>>> additionalEntry : additionalRefUserItemAttributesMap.entrySet()) {

                List<CallCardActionItemDTO> additionalActionItems = new ArrayList<>();

                Map<String, List<CallCardActionItemAttributesDTO>> additional = additionalEntry.getValue();
                for (Map.Entry<String, List<CallCardActionItemAttributesDTO>> additionalAction : additional.entrySet()) {
                    int categoryId = brandProductTypeCategoriesMap.get(additionalAction.getKey()) != null ? brandProductTypeCategoriesMap.get(additionalAction.getKey()) : 0;
                    additionalActionItems.add(new CallCardActionItemDTO(additionalAction.getKey(), Constants.ITEM_TYPE_BRAND_PRODUCT, additionalAction.getValue(), categoryId, false));
                }

                List<CallCardActionsDTO> list = new ArrayList<>();
                list.add(new CallCardActionsDTO(additionalActionItems, Constants.ITEM_TYPE_BRAND_PRODUCT, false));

                Date startDate = new Date();
                Date endDate = new Date();
                Date lastUpdated = new Date();
                String comment = null;
                Integer status = null;
                String refUserId = null;
                String refNo = null;
                Boolean active = true;
                if (additionalRefUserIndexes != null) {
                    for (CallCardRefUser additionalRefUserIndex : additionalRefUserIndexes) {
                        if (additionalEntry.getKey().equalsIgnoreCase(additionalRefUserIndex.getCallCardRefUserId())) {
                            startDate = additionalRefUserIndex.getStartDate();
                            endDate = additionalRefUserIndex.getEndDate();
                            lastUpdated = additionalRefUserIndex.getLastUpdated();
                            comment = additionalRefUserIndex.getComment();
                            refUserId = additionalRefUserIndex.getRefUserId().getUserId();
                            status = additionalRefUserIndex.getStatus();
                            active = additionalRefUserIndex.isActive();
                            refNo = additionalRefUserIndex.getRefNo();
                            break;
                        }
                    }
                    refUserId = additionalEntry.getKey().equalsIgnoreCase(CallCardRefUserDTO.DEFAULT_REF_USER_ID) ? additionalEntry.getKey() : refUserId;
                }

                List<KeyValueDTO> additionalRefUserInfo = null;
                if (additionalRefUserAdditionalInfoMap != null && additionalRefUserAdditionalInfoMap.containsKey(refUserId))
                    additionalRefUserInfo = additionalRefUserAdditionalInfoMap.get(refUserId);

                CallCardRefUserDTO additionalRefUserDTO = new CallCardRefUserDTO(
                        additionalEntry.getKey(),
                        refUserId,
                        list,
                        startDate,
                        endDate,
                        false,
                        comment,
                        status,
                        lastUpdated,
                        refNo,
                        userId,
                        additionalRefUserInfo,
                        active);

                refUserDTOtoAdd.add(additionalRefUserDTO);
            }

            CallCardGroupDTO additionalGroupDTO = new CallCardGroupDTO(CallCardGroupDTO.STOCK_DATA_GROUP, refUserDTOtoAdd, "");
            callCardDTO.getGroupIds().add(additionalGroupDTO);
        }

        return callCardDTO;
    }

    @Transactional
    public void addCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<CallCardGroupDTO> groups) {
        LOGGER.info("in addCallCardIndexes with userId {}, gameTypeId {}, applicationId {}, callCardId {}, Using SalesOrders {}", userId, gameTypeId, applicationId, callCard.getCallCardId());
        Assert.notNull(callCard, "callCard is null");
        Assert.notNull(groups, "groups is null");

        Map<String, Map<Integer, Integer>> totalQuantityPerPackagingUnitPerProductMap = new HashMap<>(); //< ProductId, < Packaging Unit, Quantity>>
        Map<Integer, Integer> totalVisitsPerTypeMap = new HashMap<>(); //< CallCardRefUSer Status, Number Of Visits>

        for (CallCardGroupDTO group : groups) {
            if (group.getRefUserIds() != null) {

                for (CallCardRefUserDTO refUser : group.getRefUserIds()) {
                    CallCardRefUser callCardRefUser = null;
                    String existingCallCardRefUserId = null;
                    SalesOrder revisedSalesOrder = null;
                    boolean isSalesOrderRevision = false;
                    SalesOrderDTO salerOrderToCreate = null;

                    //Skip defaultRefUser record And empty CallCard refUsers
                    if (refUser.getRefUserId().equalsIgnoreCase(CallCardRefUserDTO.DEFAULT_REF_USER_ID) || StringUtils.isBlank(refUser.getCallCardRefUserId()))
                        continue;

                    if (refUser.getStatus() == null)
                        throw new BusinessLayerException("CallCardRefUser Status cannot be null or empty", ExceptionTypeTO.GENERIC);

                    if (UUIDUtilities.isValidUUID(refUser.getCallCardRefUserId())) {
                        // Update CallCardRefUser contents
                        callCardRefUser = callCardRefUserDao.read(refUser.getCallCardRefUserId());
                        if (callCardRefUser == null)
                            throw new BusinessLayerException("CallCardRefUser with id:" + refUser.getCallCardRefUserId() + "does not exist", ExceptionTypeTO.ITEM_NOT_FOUND);

                        callCardRefUser.setActive(refUser.isActive());
                        resetIndexEntries(callCardRefUser);

                        existingCallCardRefUserId = callCardRefUser.getCallCardRefUserId();
                    } else {
                        /** Client-generated ID - search by Client-generated ID for existing CallCardRefUser
                         * existing => Overwrite existing
                         * new => create CallCardRefUser
                         * */

                        List<CallCardRefUser> existedRefUsers = callCardRefUserDao.queryList("listByCallCardIdInternalRefNo", callCard.getCallCardId(), refUser.getCallCardRefUserId());
                        if (existedRefUsers != null && existedRefUsers.size() > 0) {
                            if (existedRefUsers.size() == 1) {
                                callCardRefUser = existedRefUsers.get(0);
                                callCardRefUser.setActive(refUser.isActive());
                                resetIndexEntries(callCardRefUser);

                                existingCallCardRefUserId = callCardRefUser.getCallCardRefUserId();
                            } else
                                throw new BusinessLayerException("10-Digit random number found in multiple entries", ExceptionTypeTO.GENERIC);
                        } else
                            callCardRefUser = addCallCardRefUser(callCard, refUser.getSourceUserId(), refUser.getRefUserId(), refUser.getStartDate(), refUser.getEndDate(), null, refUser.getComment(), refUser.getStatus(), refUser.getCallCardRefUserId(), refUser.getRefNo(), refUser.isActive());
                    }

                    callCardRefUser.setComment(StringUtils.isNotBlank(refUser.getComment()) ? refUser.getComment() : null);
                    callCardRefUser.setLastUpdated(refUser.getLastUpdated() != null ? refUser.getLastUpdated() : new Date());
                    callCardRefUser.setEndDate(refUser.getEndDate() != null ? refUser.getEndDate() : new Date());
                    callCardRefUserDao.update(callCardRefUser);

                    if (refUser.getAdditionalRefUserInfo() != null)
                        createOrUpdateAdditionalRefUserInfo(refUser.getRefUserId(), refUser.getAdditionalRefUserInfo());

                    if (salesOrderStatuses.contains(callCardRefUser.getStatus())
                            && existingCallCardRefUserId != null) {
                        List<SalesOrder> salesOrders = null;
                        salesOrders = erpDynamicQueryManager.listSalesOrders(null, null, null, null, null, null, null, Arrays.asList(callCardRefUser.getCallCardRefUserId()), Constants.ITEM_TYPE_CALL_CARD_REFUSER, null, true, 0, -1);
                        if (salesOrders != null && salesOrders.size() > 1)
                            throw new BusinessLayerException("Error while creating Sales Order revision", ExceptionTypeTO.MORE_THAN_1_ITEM_FOUND_WITH_SPECIFIED_PROPERTIES);
                        else if (salesOrders != null && salesOrders.size() == 1) { //create SalesOrder revision
                            salerOrderToCreate = new SalesOrderDTO(null, userId, callCardRefUser.getSourceUserId().getUserId(), callCardRefUser.getRefUserId().getUserId(), callCardRefUser.getStartDate(), callCardRefUser.getLastUpdated(), null, 0, null, callCardRefUser.getComment(), null, null, null, null, salesOrders.get(0).getSalesOrderStatus().getStatusId(), callCardRefUser.getEndDate(), null, null, null, new ArrayList<SalesOrderDetailsDTO>(), callCardRefUser.getCallCardRefUserId(), Constants.ITEM_TYPE_CALL_CARD_REFUSER);
                            isSalesOrderRevision = true;
                            revisedSalesOrder = salesOrders.get(0);
                        }
                    }

                    if (refUser.getActions() != null) {
                        for (CallCardActionsDTO action : refUser.getActions()) {
                            if (action.getActionItems() != null && action.getActionItems().size() > 0) {
                                for (CallCardActionItemDTO item : action.getActionItems()) {
                                    if (item.getAttributes() != null && item.getAttributes().size() > 0) {
                                        boolean sell = false;
                                        Integer quantity = 0;
                                        Integer packagingUnit = 0;
                                        for (CallCardActionItemAttributesDTO attribute : item.getAttributes()) {
                                            if (StringUtils.isNotBlank(attribute.getPropertyValue()) && StringUtils.isNotBlank(attribute.getPropertyName())) {
                                                if (attribute.getType() == CallCardRefUserDTO.SELL || attribute.getType() == CallCardRefUserDTO.UNSCHEDULED_SELL || attribute.getType() == CallCardRefUserDTO.CREDIT_SELL)
                                                    sell = true;
                                                if (sell && attribute.getPropertyName().equals("CallCardIndex.sales")) {
                                                    quantity = Integer.parseInt(attribute.getPropertyValue());
                                                    quantity = quantity > 0 && attribute.getType() == CallCardRefUserDTO.CREDIT_SELL ? -quantity : quantity;
                                                }
                                                if (sell && attribute.getPropertyName().equals("CallCardIndex.salesUnit"))
                                                    packagingUnit = Integer.parseInt(attribute.getPropertyValue());

                                                if (salesOrderStatuses.contains(callCardRefUser.getStatus()) && attribute.getPropertyName().equals(Constants.METADATA_KEY_CALL_CARD_INDEX_SALES)) { // Save sales on a SalesOrder

                                                    if (salerOrderToCreate == null) {// create new Sales order
                                                        salerOrderToCreate = new SalesOrderDTO(null, userId, callCardRefUser.getSourceUserId().getUserId(), callCardRefUser.getRefUserId().getUserId(), callCardRefUser.getStartDate(), callCardRefUser.getLastUpdated(), null, 0, null, callCardRefUser.getComment(), null, null, null, null, SalesOrderStatus.SUBMITTED.getStatusId(), callCardRefUser.getEndDate(), null, null, null, new ArrayList<SalesOrderDetailsDTO>(), callCardRefUser.getCallCardRefUserId(), Constants.ITEM_TYPE_CALL_CARD_REFUSER);
                                                        isSalesOrderRevision = false;
                                                    }

                                                    SalesOrderDetailsDTO salesOrderDetailsDTOToAdd = new SalesOrderDetailsDTO(
                                                            null,
                                                            null,
                                                            item.getItemId(),
                                                            item.getItemTypeId(),
                                                            null,
                                                            attribute.getAmount(),
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            Integer.parseInt(attribute.getPropertyValue()),
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null);
                                                    salerOrderToCreate.getSalesOrderDetailsDTOList().add(salesOrderDetailsDTOToAdd);

                                                } else {    // Save attribute on CallCardIndex
                                                    addCallCardRefUserIndex(
                                                            callCardRefUser,
                                                            item.getItemId(),
                                                            item.getItemTypeId(),
                                                            attribute.getPropertyName(),
                                                            attribute.getPropertyValue(),
                                                            attribute.getStatus() != null ? attribute.getStatus() : 1,
                                                            attribute.getDateSubmitted() != null ? attribute.getDateSubmitted() : new Date(),
                                                            attribute.getAmount() != null ? attribute.getAmount().getValue() : null,
                                                            attribute.getType());
                                                }
                                            }
                                        }

                                        if (sell) {
                                            if (totalQuantityPerPackagingUnitPerProductMap.containsKey(item.getItemId())) {
                                                if (totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).containsKey(packagingUnit)) {
                                                    Integer updatedQuantity = totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).get(packagingUnit) + quantity;
                                                    totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).put(packagingUnit, updatedQuantity);
                                                } else
                                                    totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).put(packagingUnit, quantity);
                                            } else {
                                                Map<Integer, Integer> newQuantityPerPackagingUnit = new HashMap<>();
                                                newQuantityPerPackagingUnit.put(packagingUnit, quantity);
                                                totalQuantityPerPackagingUnitPerProductMap.put(item.getItemId(), newQuantityPerPackagingUnit);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Check if SalesOrder or Sales Order Revision should be created
                    boolean createRevision = false;
                    SalesOrder salesOrder = null;
                    if (salesOrderStatuses.contains(callCardRefUser.getStatus()) && !isSalesOrderRevision && salerOrderToCreate != null) { //create new SalesOrder
                        salesOrder = salesOrderManagement.addSalesOrder(salerOrderToCreate.getCreatedByUserId(), salerOrderToCreate.getFromUserId(), salerOrderToCreate.getToUserId(), salerOrderToCreate.getDateCreated(), salerOrderToCreate.getDateUpdated(), null, null, null, salerOrderToCreate.getComments(), null, null, null, null, salerOrderToCreate.getSalesOrderStatus(), salerOrderToCreate.getDateSubmitted(), null, null, null, salerOrderToCreate.getRefItemId(), salerOrderToCreate.getRefItemTypeId());
                    } else if (salesOrderStatuses.contains(callCardRefUser.getStatus()) && isSalesOrderRevision && salerOrderToCreate != null) {  // create SalesOrder revision

                        if (revisedSalesOrder.getSalesOrderDetails().size() == salerOrderToCreate.getSalesOrderDetailsDTOList().size()) {
                            searchForDifference:
                            for (SalesOrderDetails detail : revisedSalesOrder.getSalesOrderDetails()) {
                                for (SalesOrderDetailsDTO detailDTO : salerOrderToCreate.getSalesOrderDetailsDTOList()) {
                                    if (detail.getItemId().equalsIgnoreCase(detailDTO.getItemId())) {
                                        BigDecimal detailPrice = detail.getItemPrice();
                                        BigDecimal dtoPrice = null;
                                        if (detailDTO != null && detailDTO.getItemPrice() != null)
                                            dtoPrice = detailDTO.getItemPrice().toBigDecimal();

                                        if ((detail.getQuantity() != detailDTO.getQuantity())) {
                                            createRevision = true;
                                            break searchForDifference;
                                        }

                                        if (detailPrice == null && dtoPrice == null)
                                            break searchForDifference;

                                        if ((detailPrice != null && dtoPrice == null) || (detailPrice == null && dtoPrice != null) ||
                                                (detailPrice != null && dtoPrice != null && detailPrice.compareTo(dtoPrice) != 0)) {
                                            createRevision = true;
                                            break searchForDifference;
                                        }
                                    }
                                }
                            }
                        } else {
                            createRevision = true;
                        }

                        if (createRevision) {
                            salesOrder = salesOrderManagement.createSalesOrderRevision(salerOrderToCreate.getCreatedByUserId(), salerOrderToCreate.getDateCreated(), salerOrderToCreate.getDateUpdated(), null, null, null, salerOrderToCreate.getComments(), null, null, null, null, salerOrderToCreate.getDateSubmitted(), revisedSalesOrder.getSalesOrderId(), salerOrderToCreate.getRefItemId(), salerOrderToCreate.getRefItemTypeId());
                        }
                    }

                    if (salesOrder != null) {
                        for (SalesOrderDetailsDTO detailDTO : salerOrderToCreate.getSalesOrderDetailsDTOList()) {
                            SalesOrderDetailsDTO salesOrderDetailsDTO = salesOrderManagement.addSalesOrderDetails(
                                    salesOrder.getSalesOrderId(),
                                    detailDTO.getItemId(),
                                    detailDTO.getItemTypeId(),
                                    null,
                                    detailDTO.getItemPrice() != null ? new BigDecimal(BigInteger.valueOf(detailDTO.getItemPrice().getValue()), detailDTO.getItemPrice().getScale()) : null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    detailDTO.getQuantity(),
                                    null,
                                    null,
                                    null,
                                    salesOrder.getDateCreated(),
                                    null,
                                    null);
                        }
                    }

                    if (totalVisitsPerTypeMap.containsKey(refUser.getStatus())) {
                        totalVisitsPerTypeMap.put(refUser.getStatus(), totalVisitsPerTypeMap.get(refUser.getStatus()) + 1);
                    } else
                        totalVisitsPerTypeMap.put(refUser.getStatus(), 1);
                }
            }
        }

        if (!callCard.isActive() && totalVisitsPerTypeMap != null && totalVisitsPerTypeMap.size() > 0) {
            for (Map.Entry<Integer, Integer> totalVisitsPerType : totalVisitsPerTypeMap.entrySet()) {
                String additionalEventProperties = EventTO.PROPERTY_TYPE + "=" + totalVisitsPerType.getKey() + "\n";

                dispatchEvent(EventType.CALL_CARD_STATISTICS, userId, gameTypeId, applicationId, null, 0, totalVisitsPerType.getValue(), additionalEventProperties, true);
            }
        }

        if (!callCard.isActive() && totalQuantityPerPackagingUnitPerProductMap != null && totalQuantityPerPackagingUnitPerProductMap.size() > 0) {
            for (Map.Entry<String, Map<Integer, Integer>> quantityPerPackagingUnitPerProduct : totalQuantityPerPackagingUnitPerProductMap.entrySet()) {
                for (Map.Entry<Integer, Integer> quantityPerPackagingUnit : quantityPerPackagingUnitPerProduct.getValue().entrySet()) {
                    String additionalEventProperties = EventTO.PROPERTY_UNIT_TYPE_ID + "=" + quantityPerPackagingUnit.getKey() + "\n";
                    additionalEventProperties += EventTO.PROPERTY_REF_ITEM_ID + "=" + callCard.getCallCardId() + "\n";

                    dispatchEvent(EventType.CALL_CARD_STATISTICS, userId, gameTypeId, applicationId, quantityPerPackagingUnitPerProduct.getKey(), Constants.ITEM_TYPE_BRAND_PRODUCT, quantityPerPackagingUnit.getValue(), additionalEventProperties, true);
                }
            }
        }
    }

    @Transactional
    public void createOrUpdateSimplifiedCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<SimplifiedCallCardRefUserDTO> refUserIds) {
        Assert.notNull(callCard, "callCard is null");
        Assert.notNull(refUserIds, "refUserIds is null");

        Map<String, Map<Integer, Integer>> totalQuantityPerPackagingUnitPerProductMap = new HashMap<>(); //< ProductId, < Packaging Unit, Quantity>>

        for (SimplifiedCallCardRefUserDTO refUser : refUserIds) {
            CallCardRefUser callCardRefUser = null;

            //Skip defaultRefUser record And empty CallCard refUsers
            if (refUser.getRecipientUserId().equalsIgnoreCase(SimplifiedCallCardRefUserDTO.DEFAULT_REF_USER_ID) || StringUtils.isBlank(refUser.getCallCardRefUserId()))
                continue;

            if (refUser.getStatus() == null)
                throw new BusinessLayerException("CallCardRefUser Status cannot be null or empty", ExceptionTypeTO.INTERFACE_ERROR);

            if (UUIDUtilities.isValidUUID(refUser.getCallCardRefUserId())) {
                // Update CallCardRefUser contents
                callCardRefUser = callCardRefUserDao.read(refUser.getCallCardRefUserId());
                if (callCardRefUser == null)
                    throw new BusinessLayerException("CallCardRefUser with id:" + refUser.getCallCardRefUserId() + "does not exist", ExceptionTypeTO.ITEM_NOT_FOUND);

                callCardRefUser.setActive(refUser.isActive());
                resetIndexEntries(callCardRefUser);
            } else {
                /** Client-generated ID - search by Client-generated ID for existing CallCardRefUser
                 * existing => Overwrite existing
                 * new => create CallCardRefUser
                 * */

                List<CallCardRefUser> existedRefUsers = callCardRefUserDao.queryList("listByCallCardIdInternalRefNo", callCard.getCallCardId(), refUser.getCallCardRefUserId());
                if (existedRefUsers != null && existedRefUsers.size() > 0) {
                    if (existedRefUsers.size() == 1) {
                        callCardRefUser = existedRefUsers.get(0);
                        callCardRefUser.setActive(refUser.isActive());
                        resetIndexEntries(callCardRefUser);
                    } else
                        throw new BusinessLayerException("Internal Ref No found in multiple entries", ExceptionTypeTO.GENERIC);
                } else
                    callCardRefUser = addCallCardRefUser(callCard, refUser.getIssuerUserId(), refUser.getRecipientUserId(), refUser.getDateCreated(), new Date(), refUser.getDateUpdated(), refUser.getComment(), refUser.getStatus(), refUser.getCallCardRefUserId(), refUser.getRefNo(), refUser.isActive());
            }

            callCardRefUser.setComment(StringUtils.isNotBlank(refUser.getComment()) ? refUser.getComment() : null);
            callCardRefUser.setLastUpdated(refUser.getDateUpdated() != null ? refUser.getDateUpdated() : new Date());
            callCardRefUserDao.update(callCardRefUser);

            if (refUser.getItems() != null && refUser.getItems().size() > 0) {
                for (CallCardActionItemDTO item : refUser.getItems()) {
                    if (item.getAttributes() != null && item.getAttributes().size() > 0) {
                        boolean sell = false;
                        Integer quantity = 0;
                        Integer packagingUnit = 0;
                        for (CallCardActionItemAttributesDTO attribute : item.getAttributes()) {
                            if (StringUtils.isNotBlank(attribute.getPropertyValue()) && StringUtils.isNotBlank(attribute.getPropertyName())) {
                                if (attribute.getType() == CallCardRefUserDTO.SELL || attribute.getType() == CallCardRefUserDTO.CREDIT_SELL)
                                    sell = true;
                                if (sell && attribute.getPropertyName().equals("CallCardIndex.sales")) {
                                    quantity = Integer.parseInt(attribute.getPropertyValue());
                                    quantity = quantity > 0 && attribute.getType() == CallCardRefUserDTO.CREDIT_SELL ? -quantity : quantity;
                                }
                                if (sell && attribute.getPropertyName().equals("CallCardIndex.salesUnit"))
                                    packagingUnit = Integer.parseInt(attribute.getPropertyValue());

                                addCallCardRefUserIndex(
                                        callCardRefUser,
                                        item.getItemId(),
                                        item.getItemTypeId(),
                                        attribute.getPropertyName(),
                                        attribute.getPropertyValue(),
                                        attribute.getStatus() != null ? attribute.getStatus() : 1,
                                        attribute.getDateSubmitted() != null ? attribute.getDateSubmitted() : new Date(),
                                        attribute.getAmount() != null ? attribute.getAmount().getValue() : null,
                                        attribute.getType());
                            }
                        }

                        if (sell) {
                            if (totalQuantityPerPackagingUnitPerProductMap.containsKey(item.getItemId())) {
                                if (totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).containsKey(packagingUnit)) {
                                    Integer updatedQuantity = totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).get(packagingUnit) + quantity;
                                    totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).put(packagingUnit, updatedQuantity);
                                } else
                                    totalQuantityPerPackagingUnitPerProductMap.get(item.getItemId()).put(packagingUnit, quantity);
                            } else {
                                Map<Integer, Integer> newQuantityPerPackagingUnit = new HashMap<>();
                                newQuantityPerPackagingUnit.put(packagingUnit, quantity);
                                totalQuantityPerPackagingUnitPerProductMap.put(item.getItemId(), newQuantityPerPackagingUnit);
                            }
                        }
                    }
                }
            }
        }

        if (!callCard.isActive() && totalQuantityPerPackagingUnitPerProductMap != null && totalQuantityPerPackagingUnitPerProductMap.size() > 0) {
            for (Map.Entry<String, Map<Integer, Integer>> quantityPerPackagingUnitPerProduct : totalQuantityPerPackagingUnitPerProductMap.entrySet()) {
                for (Map.Entry<Integer, Integer> quantityPerPackagingUnit : quantityPerPackagingUnitPerProduct.getValue().entrySet()) {
                    String additionalEventProperties = EventTO.PROPERTY_UNIT_TYPE_ID + "=" + quantityPerPackagingUnit.getKey() + "\n";
                    additionalEventProperties += EventTO.PROPERTY_REF_ITEM_ID + "=" + callCard.getCallCardId() + "\n";

                    dispatchEvent(EventType.CALL_CARD_STATISTICS, userId, gameTypeId, applicationId, quantityPerPackagingUnitPerProduct.getKey(), Constants.ITEM_TYPE_BRAND_PRODUCT, quantityPerPackagingUnit.getValue(), additionalEventProperties, true);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimplifiedCallCardDTO> listSimplifiedCallCards(String callCardUserId, String sourceUserId, String refUserId, Date dateFrom, Date dateTo, int rangeFrom, int rangeTo) {

        List<SimplifiedCallCardDTO> simplifiedCallCardDTOs = new LinkedList<SimplifiedCallCardDTO>();

        List<CallCardRefUser> callCardRefUsers = erpDynamicQueryManager.listCallCardRefUsers(null,
                null,
                StringUtils.isNotBlank(sourceUserId) ? Arrays.asList(sourceUserId) : null,
                StringUtils.isNotBlank(refUserId) ? Arrays.asList(refUserId) : null,
                StringUtils.isNotBlank(callCardUserId) ? Arrays.asList(callCardUserId) : null,
                dateFrom,
                dateTo,
                null,
                true,
                null,
                rangeFrom,
                rangeTo);

        if (callCardRefUsers == null || callCardRefUsers.size() == 0)
            return simplifiedCallCardDTOs;

        //<CallCardId, SimplifiedCallCardDTO>
        Map<String, SimplifiedCallCardDTO> simplifiedCallCardDTOsMap = new LinkedHashMap<String, SimplifiedCallCardDTO>();
        for (CallCardRefUser callCardRefUser : callCardRefUsers) {

            List<CallCardRefUserIndex> callCardRefUserIndices = callCardRefUser.getCallCardRefUserIndexes();

            if (callCardRefUserIndices != null && callCardRefUserIndices.size() > 0) {
                List<CallCardActionItemDTO> actionItems = new ArrayList<CallCardActionItemDTO>();

                // itemId, properties
                Map<String, List<CallCardActionItemAttributesDTO>> attributesMap = new HashMap<String, List<CallCardActionItemAttributesDTO>>();

                for (CallCardRefUserIndex callCardRefUserIndex : callCardRefUserIndices) {
                    if (attributesMap.containsKey(callCardRefUserIndex.getItemId()) == false) {
                        CallCardActionItemDTO actionItem = new CallCardActionItemDTO(callCardRefUserIndex.getItemId(),
                                callCardRefUserIndex.getItemTypeId().getItemTypeId(),
                                null,
                                0,
                                false);

                        actionItems.add(actionItem);
                        attributesMap.put(callCardRefUserIndex.getItemId(), new ArrayList<CallCardActionItemAttributesDTO>());
                    }

                    CallCardActionItemAttributesDTO attributeRecord = new CallCardActionItemAttributesDTO(callCardRefUserIndex.getCallCardRefUserIndexId(),
                            callCardRefUserIndex.getPropertyId(),
                            callCardRefUserIndex.getPropertyId(),
                            String.valueOf(Constants.ITEM_TYPE_CALL_CARD_INDEX),
                            callCardRefUserIndex.getPropertyValue(),
                            callCardRefUserIndex.getSubmitDate(),
                            callCardRefUserIndex.getStatus(),
                            callCardRefUserIndex.getType(),
                            callCardRefUserIndex.getAmount() != null ? new DecimalDTO(callCardRefUserIndex.getAmount()) : null,
                            null);

                    // add actionItemAttribute
                    List<CallCardActionItemAttributesDTO> callCardActionItemAttributes = attributesMap.get(callCardRefUserIndex.getItemId());
                    callCardActionItemAttributes.add(attributeRecord);

                    attributesMap.put(callCardRefUserIndex.getItemId(), callCardActionItemAttributes);
                }

                for (CallCardActionItemDTO actionItem : actionItems) {
                    actionItem.setAttributes(attributesMap.get(actionItem.getItemId()));
                }

                SimplifiedCallCardRefUserDTO refUserRecord = new SimplifiedCallCardRefUserDTO(
                        callCardRefUser.getCallCardRefUserId(),
                        callCardRefUser.getSourceUserId().getUserId(),
                        actionItems,
                        callCardRefUser.getStartDate(),
                        callCardRefUser.getLastUpdated(),
                        callCardRefUser.getRefUserId().getUserId(),
                        callCardRefUser.getComment(),
                        callCardRefUser.getStatus(),
                        callCardRefUser.getRefNo(),
                        callCardRefUser.isActive());

                if (simplifiedCallCardDTOsMap.containsKey(callCardRefUser.getCallCardId().getCallCardId())) {
                    List<SimplifiedCallCardRefUserDTO> refUserDTOs = simplifiedCallCardDTOsMap.get(callCardRefUser.getCallCardId().getCallCardId()).getRefUserIds();
                    refUserDTOs.add(refUserRecord);
                } else {
                    List<SimplifiedCallCardRefUserDTO> refUserDTOs = new ArrayList<SimplifiedCallCardRefUserDTO>();
                    refUserDTOs.add(refUserRecord);

                    SimplifiedCallCardDTO callCardRecord = new SimplifiedCallCardDTO(
                            callCardRefUser.getCallCardId().getCallCardId(),
                            callCardRefUser.getCallCardId().getStartDate(),
                            callCardRefUser.getCallCardId().getLastUpdated(),
                            refUserDTOs,
                            !callCardRefUser.getCallCardId().isActive(),
                            callCardRefUser.getCallCardId().getEndDate());

                    simplifiedCallCardDTOsMap.put(callCardRefUser.getCallCardId().getCallCardId(), callCardRecord);
                }
            }
        }

        for (Map.Entry<String, SimplifiedCallCardDTO> simplifiedCallCard : simplifiedCallCardDTOsMap.entrySet())
            simplifiedCallCardDTOs.add(simplifiedCallCard.getValue());

        return simplifiedCallCardDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countSimplifiedCallCards(String callCardUserId, String sourceUserId, String refUserId, Date dateFrom, Date dateTo) {
        return erpDynamicQueryManager.countCallCardRefUsers(null,
                null,
                StringUtils.isNotBlank(sourceUserId) ? Arrays.asList(sourceUserId) : null,
                StringUtils.isNotBlank(refUserId) ? Arrays.asList(refUserId) : null,
                StringUtils.isNotBlank(callCardUserId) ? Arrays.asList(callCardUserId) : null,
                dateFrom,
                dateTo,
                null,
                true,
                null);
    }

    @Transactional
    public CallCardRefUser addCallCardRefUser(CallCard callCard,
                                              String sourceUserId,
                                              String refUserId,
                                              Date startDate,
                                              Date endDate,
                                              Date lastUpdated,
                                              String comment,
                                              Integer status,
                                              String internalRefNo,
                                              String refNo,
                                              boolean active) {

        Assert.notNull(callCard, "callCard is null");
        Assert.notNullOrEmpty(refUserId, "refUserId is null or empty");
        Assert.isValidUUID(refUserId, "refUserId is not valid");
//            Assert.notNull(startDate, "startDate is null");
//            Assert.notNull(endDate, "endDate is null");

        CallCardRefUser callCardRefUser = new CallCardRefUser();
        if (sourceUserId != null) {
            Users sourceUser = usersDao.read(sourceUserId);
            callCardRefUser.setSourceUserId(sourceUser);
        }

        Users refUser = usersDao.read(refUserId);
        callCardRefUser.setCallCardId(callCard);
        callCardRefUser.setRefUserId(refUser);
        callCardRefUser.setStartDate(startDate);
        callCardRefUser.setEndDate(endDate);
        callCardRefUser.setLastUpdated(lastUpdated);
        callCardRefUser.setActive(active);

        if (status != null)
            callCardRefUser.setStatus(status);

        if (StringUtils.isNotBlank(comment))
            callCardRefUser.setComment(comment);

        if (StringUtils.isNotBlank(internalRefNo))
            callCardRefUser.setInternalRefNo(internalRefNo);

        if (StringUtils.isNotBlank(refNo))
            callCardRefUser.setRefNo(refNo);

        callCardRefUserDao.create(callCardRefUser);

        return callCardRefUser;
    }

    @Transactional
    public void addCallCardRefUserIndex(CallCardRefUser callCardRefUser,
                                        String itemId,
                                        Integer itemTypeId,
                                        String propertyName,
                                        String propertyValue,
                                        Integer status,
                                        Date submitDate,
                                        BigDecimal amount,
                                        Integer type) {

        Assert.notNull(callCardRefUser, "callCardRefUser is null");
        Assert.isTrue(itemTypeId > 0, "itemTypeId is null or empty");
        Assert.notNullOrEmpty(propertyName, "propertyName is null");
        Assert.notNullOrEmpty(propertyValue, "propertyValue is null");
        Assert.notNull(status, "status is null");
        Assert.notNull(type, "type is null");
        Assert.notNull(submitDate, "submitDate is null");

        ItemTypes itemType = itemTypesDao.read(itemTypeId);

        CallCardRefUserIndex callCardRefUserIndex = new CallCardRefUserIndex();
        callCardRefUserIndex.setCallCardRefUserId(callCardRefUser);
        callCardRefUserIndex.setItemId(itemId);
        callCardRefUserIndex.setItemTypeId(itemType);
        callCardRefUserIndex.setPropertyId(propertyName);
        callCardRefUserIndex.setPropertyValue(propertyValue);
        callCardRefUserIndex.setStatus(status);
        callCardRefUserIndex.setSubmitDate(submitDate);
        callCardRefUserIndex.setAmount(amount);
        callCardRefUserIndex.setType(type);

        callCardRefUserIndexDao.create(callCardRefUserIndex);
    }

    @Transactional(readOnly = true)
    public int countCallCards(List<String> callCardIds, List<String> userIds, List<String> refUserIds, List<String> callCardTemplateIds, Date startDate, Boolean active, boolean currentlyActive, boolean isNotCompleted, String gameTypeId) {
        return erpDynamicQueryManager.countCallCards(callCardIds, userIds, refUserIds, callCardTemplateIds, startDate, active, currentlyActive, true, gameTypeId);
    }

    @Override
    @Transactional
    public void addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, SimplifiedCallCardDTO callCard) {
        /**
         * SimplifiedCallCardDTO.getCallCardId()
         * null => create new, temporaryId is stored in InternalRefNo
         * UUID => update related
         */

        CallCard processingCallCard = null;
        if (StringUtils.isBlank(callCard.getCallCardId()))
            throw new BusinessLayerException("CallCardId is null or empty", ExceptionTypeTO.GENERIC);

        // search for existing callCard with the Internal Ref No sent by client
        if (UUIDUtilities.isValidUUID(callCard.getCallCardId())) {
            processingCallCard = callCardDao.read(callCard.getCallCardId());
            if (processingCallCard == null)
                throw new BusinessLayerException("No callCard found with callcardId:" + callCard.getCallCardId(), ExceptionTypeTO.NO_ITEM_FOUND_WITH_SPECIFIED_PROPERTIES);

            if (processingCallCard.isActive() == false)
                LOGGER.info("\n\n\n\n Call card is closed but client tries SYNC, callCardId " + callCard.getCallCardId());

            if (!processingCallCard.getUserId().getUserId().equalsIgnoreCase(userId))
                throw new BusinessLayerException("Could not update callCard with ID:" + processingCallCard.getCallCardId(), ExceptionTypeTO.ITEM_BELONGS_TO_OTHER_USER);
        } else {
            List<CallCard> callCardsByTemporaryId = callCardDao.queryList("listByUserInternalRefNo", userId, callCard.getCallCardId());
            if (callCardsByTemporaryId != null && callCardsByTemporaryId.size() > 0) {
                if (callCardsByTemporaryId.size() != 1)
                    throw new BusinessLayerException("More than one callCards found with reference ID:" + callCard.getCallCardId(), ExceptionTypeTO.MORE_THAN_1_ITEM_FOUND_WITH_SPECIFIED_PROPERTIES);

                callCard.setCallCardId(callCardsByTemporaryId.get(0).getCallCardId());
            } else {
                // check for associated Template
                CallCardTemplate callCardTemplate = getCallCardTemplate(userGroupId, gameTypeId, applicationId, userId);
                if (callCardTemplate != null)
                    processingCallCard = addCallCard(callCardTemplate.getCallCardTemplateId(), userId, new Date(), null, true, null, callCard.getCallCardId());
                else
                    throw new BusinessLayerException("No call card template could be mapped to user :", ExceptionTypeTO.CMS_CONFIGURATION_ERROR);

                callCard.setCallCardId(processingCallCard.getCallCardId());
                callCard.setDateCreated(processingCallCard.getStartDate());
            }
        }

        // call card is finally submitted
        if (callCard.isSubmitted())
            processingCallCard.setActive(false);

        if (callCard.getRefUserIds() != null && callCard.getRefUserIds().size() > 0)
            createOrUpdateSimplifiedCallCardIndexes(userId, gameTypeId, applicationId, processingCallCard, callCard.getRefUserIds());

        processingCallCard.setEndDate(new Date());
        processingCallCard.setLastUpdated(callCard.getDateCreated() != null ? callCard.getDateCreated() : new Date());

        callCardDao.update(processingCallCard);

        return;
    }

    @Override
    @Transactional
    public CallCardDTO updateCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, List<CallCardDTO> callCards) throws BusinessLayerException {
        List<String> pendingCallCardIds = new ArrayList<>();

        if (callCards == null || callCards.size() == 0)
            throw new BusinessLayerException("CallCards list cannot be null or empty!", ExceptionTypeTO.GENERIC);

        Collections.sort(callCards, new CallCardDTO.ExistingCallCardFirst());   // existing CallCards first

        CallCardTemplate inheritedCallCardTemplateId = null;
        for (CallCardDTO callCardDTO : callCards) {
            if (StringUtils.isBlank(callCardDTO.getCallCardId()))
                throw new BusinessLayerException("CallCardId cannot be null or empty !", ExceptionTypeTO.GENERIC);

            // search for existing callCard with the external Id sent by client
            if (UUIDUtilities.isValidUUID(callCardDTO.getCallCardId())) {
                CallCard requestedCallCard = callCardDao.read(callCardDTO.getCallCardId());
                inheritedCallCardTemplateId = requestedCallCard.getCallCardTemplateId();

                if (requestedCallCard == null)
                    throw new BusinessLayerException("No callCard found with callcardId:" + callCardDTO.getCallCardId(), ExceptionTypeTO.NO_ITEM_FOUND_WITH_SPECIFIED_PROPERTIES);

                if (requestedCallCard.isActive() == false)
                    LOGGER.info("\n\n\n\n Call card is closed but client tries SYNC, callCardId " + callCardDTO.getCallCardId());
            } else {
                List<CallCard> callCardsByTemporaryId = callCardDao.queryList("listByUserInternalRefNo", userId, callCardDTO.getCallCardId());
                if (callCardsByTemporaryId != null && callCardsByTemporaryId.size() > 0) {
                    if (callCardsByTemporaryId.size() != 1)
                        throw new BusinessLayerException("More than one callCards found with reference ID:" + callCardDTO.getCallCardId(), ExceptionTypeTO.MORE_THAN_1_ITEM_FOUND_WITH_SPECIFIED_PROPERTIES);

                    callCardDTO.setCallCardId(callCardsByTemporaryId.get(0).getCallCardId());
                } else {
                    callCardDTO.setInternalRefNo(callCardDTO.getCallCardId());
                    callCardDTO.setCallCardId(null);
                }
            }

            CallCardDTO storedCallCard = updateCallCardInternal(userGroupId, gameTypeId, applicationId, userId, callCardDTO, inheritedCallCardTemplateId);
            if (storedCallCard.isSubmitted() == false)
                pendingCallCardIds.add(storedCallCard.getCallCardId());
        }

        if (pendingCallCardIds != null && pendingCallCardIds.size() > 1)
            throw new BusinessLayerException("pendingCallCardIds more than 1 in DB!", ExceptionTypeTO.GENERIC);

        if (pendingCallCardIds.size() == 1) {
            List<String> filterProperties = new ArrayList<>();
            switch (gameTypeId) {
                case Constants.PMI_EGYPT_GAME_TYPE_ID:
                case Constants.PMI_SENEGAL_GAME_TYPE_ID:
                    filterProperties = new ArrayList<String>();
                    filterProperties.add(Constants.METADATA_KEY_PERSONAL_REGION);
                    break;
                case Constants.PMI_IRAQ_GAME_TYPE_ID:
                    break;
            }

            return getNewOrPendingCallCard(
                    userId,
                    userGroupId,
                    gameTypeId,
                    applicationId,
                    pendingCallCardIds.get(0),
                    filterProperties);
        }

        return null;
    }

    @Transactional
    private CallCardTemplate getCallCardTemplateByMetadataProperty(String userGroupId, String gameTypeId, String userId) {
        List<String> metadataKeys = new ArrayList<>();
        switch (gameTypeId) {
            case Constants.PMI_EGYPT_GAME_TYPE_ID:
            case Constants.PMI_SENEGAL_GAME_TYPE_ID:
                metadataKeys = new ArrayList<String>();
                metadataKeys.add(Constants.METADATA_KEY_PERSONAL_REGION);
                break;
            case Constants.PMI_IRAQ_GAME_TYPE_ID:
                break;
        }

        List<MetadataDTO> metadataDTOs = userMetadataComponent.listUserMetadata(Collections.singletonList(userId), metadataKeys, false);
        if (metadataDTOs == null || metadataDTOs.size() == 0)
            throw new BusinessLayerException("", ExceptionTypeTO.CMS_CONFIGURATION_ERROR);

        List<KeyValueDTO> metadataFilter = new ArrayList<>();
        LOGGER.info("-- Get Call card Template by metadata keys : \n");
        for (MetadataDTO metadataDTO : metadataDTOs) {
            LOGGER.info("metadata key : " + metadataDTO.getKey() + " value : " + metadataDTO.getValue() + "\n");
            metadataFilter.add(new KeyValueDTO(Constants.METADATA_KEY_PERSONAL_REGION, metadataDTO.getValue()));
        }

        List<CallCardTemplate> templates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, metadataFilter, true, true, null, 0, -1);
        if (templates == null || templates.size() == 0)
            throw new BusinessLayerException("Could not list listCallCardTemplate", ExceptionTypeTO.CMS_CONFIGURATION_ERROR);

        return templates.get(0);
    }

    @Transactional
    private CallCardTemplate getCallCardTemplate(String userGroupId, String gameTypeId, String applicationId, String userId) {
        // check for associated Template
        List<CallCardTemplate> callCardTemplates = erpDynamicQueryManager.listCallCardTemplates(userGroupId, gameTypeId, null, null, true, true, userId, 0, -1);
        if (callCardTemplates != null && callCardTemplates.size() == 1) {
            LOGGER.info("Call card template " + callCardTemplates.get(0).getCallCardTemplateId() + " assigned to user : " + userId);

            return callCardTemplates.get(0);
        } else {
            LOGGER.info("Call card templates " + (callCardTemplates != null ? callCardTemplates.size() : 0) + " assigned to user : " + userId);

            dispatchEvent(EventType.NO_DISTINCT_CALL_CARD_TEMPLATE, userId, gameTypeId, applicationId, null, 0, 0, null, false);
//            CallCardTemplate template = getCallCardTemplateByMetadataProperty(userGroupId, gameTypeId, userId);
//            if ( template != null )
//                return template;
//            else
            throw new BusinessLayerException("No call card template could be mapped to user :", ExceptionTypeTO.CMS_CONFIGURATION_ERROR);
        }
    }

    @Transactional
    private CallCardDTO updateCallCardInternal(String userGroupId, String gameTypeId, String applicationId, String userId, CallCardDTO callCardDTO, CallCardTemplate inheritedCallCardTemplateId) throws BusinessLayerException {
        /**
         * callCardDTo.getCallCardId()
         * null => create new, temporaryId is stored in InternalRefNo
         * UUID => update related
         */

        CallCard processingCallCard = null;

        // Create new & register contents
        if (StringUtils.isBlank(callCardDTO.getCallCardId())) {

            // check for associated Template
            CallCardTemplate callCardTemplate = null;

            if (UUIDUtilities.isValidUUID(callCardDTO.getCallCardTemplateId()))
                callCardTemplate = callCardTemplateDao.read(callCardDTO.getCallCardTemplateId());
            else if (inheritedCallCardTemplateId != null)
                callCardTemplate = inheritedCallCardTemplateId;
            else
                callCardTemplate = getCallCardTemplate(userGroupId, gameTypeId, applicationId, userId);

            if (callCardTemplate != null)
                processingCallCard = addCallCard(
                        callCardTemplate.getCallCardTemplateId(),
                        userId,
                        callCardDTO.getStartDate() != null ? callCardDTO.getStartDate() : new Date(),
                        callCardDTO.getEndDate() != null ? callCardDTO.getEndDate() : null,
                        true,
                        callCardDTO.getComments(),
                        callCardDTO.getInternalRefNo());
            else
                throw new BusinessLayerException("No call card template could be mapped to user :", ExceptionTypeTO.CMS_CONFIGURATION_ERROR);

            callCardDTO.setCallCardId(processingCallCard.getCallCardId());
            callCardDTO.setStartDate(processingCallCard.getStartDate());
            callCardDTO.setCallCardTemplateId(processingCallCard.getCallCardTemplateId().getCallCardTemplateId());
        } else {
            processingCallCard = callCardDao.read(callCardDTO.getCallCardId());
            if (!processingCallCard.getUserId().getUserId().equalsIgnoreCase(userId))
                throw new BusinessLayerException("Could not list createCallCardAndUpdateIndexes", ExceptionTypeTO.ITEM_BELONGS_TO_OTHER_USER);
        }

        // call card is finally submitted
        if (callCardDTO.isSubmitted())
            processingCallCard.setActive(false);

        if (callCardDTO.getGroupIds() != null && callCardDTO.getGroupIds().size() > 0)
            addCallCardIndexes(userId, gameTypeId, applicationId, processingCallCard, callCardDTO.getGroupIds());

        processingCallCard.setEndDate(callCardDTO.getEndDate());
        processingCallCard.setLastUpdated(callCardDTO.getLastUpdated() != null ? callCardDTO.getLastUpdated() : new Date());

        callCardDao.update(processingCallCard);

        // call card is finally submitted
        if (callCardDTO.isSubmitted())
            dispatchEvent(EventType.CALL_CARD_UPLOADED, userId, gameTypeId, applicationId, callCardDTO.getCallCardId(), Constants.ITEM_TYPE_CALL_CARD, 0, null, true);

        return callCardDTO;
    }

    @Override
    @Transactional
    public List<ItemStatisticsDTO> getCallCardStatistics(String userId,
                                                         String propertyId,
                                                         List<Integer> types,
                                                         Date dateFrom,
                                                         Date dateTo) {

        List<CallCardRefUserIndex> indexes = erpDynamicQueryManager.listCallCardRefUserIndexes(null, null, null, Arrays.asList(userId), null, null, propertyId, null, null, dateFrom, dateTo, types, 0, -1);
        if (indexes == null || indexes.size() == 0)
            return null;

        Map<String, Integer> statisticsMap = new HashMap<>();
        for (CallCardRefUserIndex index : indexes) {
            Integer addValue = 0;
            try {
                addValue = Integer.parseInt(index.getPropertyValue());

            } catch (NumberFormatException exception) {
                continue;
            }

            if (statisticsMap.containsKey(index.getItemId())) {
                Integer totalValue = statisticsMap.get(index.getItemId());
                statisticsMap.put(index.getItemId(), totalValue + addValue);
            } else {
                statisticsMap.put(index.getItemId(), addValue);
            }
        }

        List<ItemStatisticsDTO> statisticsDTOs = new ArrayList<>();
        for (Map.Entry<String, Integer> mapEntry : statisticsMap.entrySet()) {
            statisticsDTOs.add(new ItemStatisticsDTO(mapEntry.getKey(), 0, 0, mapEntry.getValue(), 0, false, 0));
        }

        return statisticsDTOs;
    }

    @Transactional
    public CallCard addCallCard(String callCardTemplateId, String userId, Date startDate, Date endDate, boolean active, String comments, String internalRefNo) throws BusinessLayerException {
        CallCard callCard = null;

        Assert.notNullOrEmpty(callCardTemplateId, "addCallCard error. callCardTemplateId is null or empty");
        Assert.notNullOrEmpty(userId, "addCallCard error. userId is null or empty");
        Assert.notNull(startDate, "addCallCard error. startDate is null");

        callCard = new CallCard();

        CallCardTemplate callCardTemplate = callCardTemplateDao.read(callCardTemplateId);
        callCard.setCallCardTemplateId(callCardTemplate);

        Users user = usersDao.read(userId);
        callCard.setUserId(user);

        callCard.setStartDate(startDate);
        callCard.setEndDate(endDate);
        callCard.setLastUpdated(endDate != null ? endDate : startDate);
        callCard.setActive(active);

        if (StringUtils.isNotBlank(comments))
            callCard.setComments(comments);

        if (StringUtils.isNotBlank(internalRefNo))
            callCard.setInternalRefNo(internalRefNo);

        callCardDao.create(callCard);

        return callCard;
    }

    @Override
    @Transactional
    public void submitTransactions(String userId, String userGroupId, String gameTypeId, String applicationId, String indirectUserId, CallCardDTO callCardDTO) {
        Assert.notNull(callCardDTO, "callCard is null");
        Assert.notNull(callCardDTO.getGroupIds(), "groups is null");

        String additionalEventProperties = "";

        CallCard processingCallCard = checkIfActiveCallCard(indirectUserId, userGroupId, gameTypeId);
        if (processingCallCard == null) {

            // check for associated Template
            CallCardTemplate callCardTemplate = null;
            if (UUIDUtilities.isValidUUID(callCardDTO.getCallCardTemplateId()))
                callCardTemplate = callCardTemplateDao.read(callCardDTO.getCallCardTemplateId());
            else
                callCardTemplate = getCallCardTemplate(userGroupId, gameTypeId, applicationId, userId);

            if (callCardTemplate != null)
                processingCallCard = addCallCard(callCardTemplate.getCallCardTemplateId(), indirectUserId, new Date(), null, true, null, null);
            else
                throw new BusinessLayerException("No call card template could be mapped to user :", ExceptionTypeTO.CMS_CONFIGURATION_ERROR);

            additionalEventProperties += EventTO.PROPERTY_STATUS + "=template\n";
        }

        LOGGER.info("submitTransactions: for callCardId = {}", processingCallCard.getCallCardId());

        addCallCardIndexes(indirectUserId, gameTypeId, applicationId, processingCallCard, callCardDTO.getGroupIds());

        processingCallCard.setEndDate(callCardDTO.getEndDate());
        processingCallCard.setLastUpdated(callCardDTO.getLastUpdated() != null ? callCardDTO.getLastUpdated() : new Date());

        callCardDao.update(processingCallCard);

        additionalEventProperties += EventTO.PROPERTY_FROM_USER_ID + "=" + userId + "\n";
        additionalEventProperties += EventTO.PROPERTY_DATE + "=" + processingCallCard.getStartDate() + "\n";

        dispatchEvent(
                EventType.CALL_CARD_INDIRECT_ACTION,
                indirectUserId,
                gameTypeId,
                applicationId,
                processingCallCard.getCallCardId(),
                Constants.ITEM_TYPE_CALL_CARD,
                0,
                additionalEventProperties, true);
    }

    @Transactional
    private void clearCallCardDetails(CallCard callCard) {
        List<CallCardRefUser> refUsersToDelete = erpDynamicQueryManager.listCallCardRefUsers(null, Arrays.asList(callCard.getCallCardId()), null, null, null, null, null, null, null, null, 0, -1);
        List<String> refUserIdsToDelete = null;
        if (refUsersToDelete != null && refUsersToDelete.size() > 0) {
            refUserIdsToDelete = new ArrayList<>();
            for (CallCardRefUser refUserToDelete : refUsersToDelete)
                refUserIdsToDelete.add(refUserToDelete.getCallCardRefUserId());
        }

        List<CallCardRefUserIndex> refUsersIndexesToDelete = erpDynamicQueryManager.listCallCardRefUserIndexes(null, null, Arrays.asList(callCard.getCallCardId()), null, null, null, null, null, null, null, null, null, 0, -1);
        List<String> refUserIndexIdsToDelete = null;
        if (refUsersIndexesToDelete != null && refUsersIndexesToDelete.size() > 0) {
            refUserIndexIdsToDelete = new ArrayList<>();
            for (CallCardRefUserIndex refUserIndexToDelete : refUsersIndexesToDelete)
                refUserIndexIdsToDelete.add(refUserIndexToDelete.getCallCardRefUserIndexId());
        }

        // delete previous callCardRefUserIndexes
        if (refUserIndexIdsToDelete != null && refUserIndexIdsToDelete.size() > 0)
            callCardRefUserIndexDao.executeUpdate("deleteByIds", refUserIndexIdsToDelete);
        // delete previous callCardRefUser
        if (refUserIdsToDelete != null && refUserIdsToDelete.size() > 0)
            callCardRefUserDao.executeUpdate("deleteByIds", refUserIdsToDelete);
    }

    @Transactional
    private void resetIndexEntries(CallCardRefUser callCardRefUser) {
        if (callCardRefUser == null || StringUtils.isBlank(callCardRefUser.getCallCardRefUserId()))
            return;

        List<CallCardRefUserIndex> refUsersIndexesToDelete = erpDynamicQueryManager.listCallCardRefUserIndexes(null, Arrays.asList(callCardRefUser.getCallCardRefUserId()), null, null, null, null, null, null, null, null, null, null, 0, -1);
        List<String> refUserIndexIdsToDelete = null;
        if (refUsersIndexesToDelete != null && refUsersIndexesToDelete.size() > 0) {
            refUserIndexIdsToDelete = new ArrayList<>();
            for (CallCardRefUserIndex refUserIndexToDelete : refUsersIndexesToDelete)
                refUserIndexIdsToDelete.add(refUserIndexToDelete.getCallCardRefUserIndexId());
        }

        // delete previous callCardRefUserIndexes
        if (refUserIndexIdsToDelete != null && refUserIndexIdsToDelete.size() > 0)
            callCardRefUserIndexDao.executeUpdate("deleteByIds", refUserIndexIdsToDelete);
    }

    @Transactional
    private void createOrUpdateAdditionalRefUserInfo(String userId, List<KeyValueDTO> additionalRefUserInfo) {
        Double latitude = null;
        Double longitude = null;

        for (KeyValueDTO additionalInfoItem : additionalRefUserInfo) {
            try {
                switch (additionalInfoItem.getKey()) {
                    case CallCardRefUserDTO.LATITUDE:
                        latitude = Double.parseDouble(additionalInfoItem.getValue());
                        break;
                    case CallCardRefUserDTO.LONGITUDE:
                        longitude = Double.parseDouble(additionalInfoItem.getValue());
                        break;
                }
            } catch (Exception e) {
                throw new BusinessLayerException("Invalid format for additionalRefUserInfo item with key=" + additionalInfoItem.getKey() + " and Value=" + additionalInfoItem.getValue(), ExceptionTypeTO.GENERIC);
            }
        }

        if (latitude != null && longitude != null) { // create RefUser Address Info

            Users user = usersDao.getReference(userId);
            List<UserAddressbook> userAddressBooks = user.getUserAddress();
            List<Addressbook> userAddresses = new ArrayList<Addressbook>();
            for (UserAddressbook userAddressBook : userAddressBooks)
                userAddresses.add(userAddressBook.getAddressbook());

            if (userAddresses == null || userAddresses.size() == 0) {
                List<MetadataDTO> metadata = userMetadataComponent.listUserMetadata(Arrays.asList(userId), userAddressMetadataKeys, false);

                String countryId = "";
                int stateId = 0;
                int cityId = 0;
                int postCodeId = 0;
                String address = null;

                for (MetadataDTO metadataDTO : metadata) {
                    try {
                        switch (metadataDTO.getKey()) {
                            case Constants.METADATA_KEY_PERSONAL_COUNTRY:
                                countryId = metadataDTO.getValue();
                                break;
                            case Constants.METADATA_KEY_PERSONAL_STATE:
                                stateId = Integer.parseInt(metadataDTO.getValue());
                                break;
                            case Constants.METADATA_KEY_PERSONAL_CITY:
                                cityId = Integer.parseInt(metadataDTO.getValue());
                                break;
                            case Constants.METADATA_KEY_PERSONAL_REGION:
                                postCodeId = Integer.parseInt(metadataDTO.getValue());
                                break;
                            case Constants.METADATA_KEY_PERSONAL_ADDRESS:
                                address = metadataDTO.getValue();
                                StringUtils.abbreviate(address, 200);
                                break;
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.error("Invalid metadata value format for for ItemTypeId=" + metadataDTO.getItemTypeId() + " ItemId=" + metadataDTO.getItemId() + " MetadataKey=" + metadataDTO.getKey() + " Value=" + metadataDTO.getValue(), ExceptionTypeTO.GENERIC);
                    }
                }

                if ((StringUtils.isBlank(countryId) || stateId <= 0 || cityId <= 0) && postCodeId > 0) {
                    LOGGER.info("User Metadata countryId, stateId, cityId, are null or empty. Continue Using data from PostCode");
                    Postcode postCode = postcodeDao.read(postCodeId);

                    if (postCode != null && postCode.getCities() != null && postCode.getCities().size() > 0) {
                        City city = postCode.getCities().get(0);
                        cityId = city.getCityId();

                        if (city != null && city.getStateId() != null) {
                            State state = city.getStateId();
                            stateId = state.getStateId();
                            countryId = state.getCountryId() != null ? state.getCountryId().getId() : null;
                        }
                    }
                }

                if (StringUtils.isNotBlank(countryId) && stateId > 0 && cityId > 0 && postCodeId > 0) {
                    Addressbook addressbook = addressbookManagement.createAddressbook(null, address, null, null, null, postCodeId, null, cityId, null, stateId, countryId, null, null, null, latitude, longitude, true);
                    if (addressbook.getAddressbookId() != null) {
                        userAddresses.add(addressbook);
                        user.setLastUpdated(new Date());

                        usersDao.update(user);
                    }
                } else {
                    LOGGER.error("User Metadata countryId, stateId, cityId, postCodeId are null or empty.");
                }
            }
        }
    }

    @Transactional
    private Map<String, List<KeyValueDTO>> getAdditionalRefUserInfo(List<String> refUserIds) {
        LOGGER.info("Getting Additional RefUser Info");
        Map<String, List<KeyValueDTO>> additionalRefUsersInfoMap = new HashMap<String, List<KeyValueDTO>>();

        if (refUserIds != null && refUserIds.size() > 0) {
            List<Users> refUsers = usersDao.queryList("listByUserIds", refUserIds);

            for (Users refUser : refUsers) {
                List<KeyValueDTO> additionalRefUsersInfo = new ArrayList<KeyValueDTO>();

                if (refUser.getUserAddress() != null && refUser.getUserAddress().size() > 0) {
                    Addressbook address = refUser.getUserAddress().get(0).getAddressbook();
                    if (address.getLatitude() != null && address.getLongitude() != null) {
                        additionalRefUsersInfo.add(new KeyValueDTO(CallCardRefUserDTO.LATITUDE, address.getLatitude().toString()));
                        additionalRefUsersInfo.add(new KeyValueDTO(CallCardRefUserDTO.LONGITUDE, address.getLongitude().toString()));
                    }
                }

                additionalRefUsersInfoMap.put(refUser.getUserId(), additionalRefUsersInfo);
            }
        }

        return additionalRefUsersInfoMap;
    }

    @Transactional
    private Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> getCallCardValuesFromSalesOrder(CallCard pendingCallCard) {
        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> results = new HashMap<>(); // < CallCardRefUserId , < ProductId, Attributes list>>

        List<CallCardRefUser> cardRefUsers = callCardRefUserDao.queryList("listByCallCardId", pendingCallCard.getCallCardId());
        List<String> refItemIds = new ArrayList<String>();

        for (CallCardRefUser refUser : cardRefUsers)
            refItemIds.add(refUser.getCallCardRefUserId());

        List<SalesOrder> callCardSalesOrders = erpDynamicQueryManager.listSalesOrders(null, null, null, null, null, null, null, refItemIds, Constants.ITEM_TYPE_CALL_CARD_REFUSER, null, true, 0, -1);

        if (callCardSalesOrders != null && callCardSalesOrders.size() > 0) {
            for (CallCardRefUser refUser : cardRefUsers) {
                for (SalesOrder salesOrder : callCardSalesOrders) {
                    if (refUser.getCallCardRefUserId().equalsIgnoreCase(salesOrder.getRefItemId())) {

                        Map<String, List<CallCardActionItemAttributesDTO>> attributesPerItemMap = new HashMap<String, List<CallCardActionItemAttributesDTO>>(); // < ItemId, Attributes>

                        List<SalesOrderDetails> callCardSalesOrderDetails = erpDynamicQueryManager.listSalesOrderDetails(null, salesOrder.getSalesOrderId(), null, null, 0, -1);
                        for (SalesOrderDetails detail : callCardSalesOrderDetails) {
                            if (attributesPerItemMap.containsKey(detail.getItemId())) {
                                attributesPerItemMap.get(detail.getItemId()).add(new CallCardActionItemAttributesDTO(
                                        null,
                                        null,
                                        Constants.METADATA_KEY_CALL_CARD_INDEX_SALES,
                                        null,
                                        detail.getQuantity().toString(),
                                        detail.getDateCreated(),
                                        1,
                                        refUser.getStatus(),
                                        detail.getItemPrice() != null ? new DecimalDTO(detail.getItemPrice()) : null,
                                        null));
                            } else {
                                List<CallCardActionItemAttributesDTO> itemAttributes = new ArrayList<CallCardActionItemAttributesDTO>();

                                itemAttributes.add(new CallCardActionItemAttributesDTO(
                                        null,
                                        null,
                                        Constants.METADATA_KEY_CALL_CARD_INDEX_SALES,
                                        null,
                                        detail.getQuantity().toString(),
                                        detail.getDateCreated(),
                                        1,
                                        refUser.getStatus(),
                                        detail.getItemPrice() != null ? new DecimalDTO(detail.getItemPrice()) : null,
                                        null));

                                attributesPerItemMap.put(detail.getItemId(), itemAttributes);
                            }

                            results.put(refUser.getCallCardRefUserId(), attributesPerItemMap);
                        }
                    }
                }
            }
        }

        return results;
    }

    @Transactional
    private Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> summarizeCallCardProperties(List<String> refUserIds, String callCardUserId, Integer previousValuesSetting, List<Integer> recordsTypes, Map<String, String> properties, boolean activeCallCards) {
        LOGGER.info("In summarizeCallCardProperties for User with UserId: {}", callCardUserId);
        //Map<String, String> properties: key => propertyName, value => propertyType
        Map<String, Map<String, List<CallCardActionItemAttributesDTO>>> results = new HashMap<>();  //< RefUserId, <ItemId, List of ActionItemAttributesDTO>>

        Map<String, List<CallCardRefUserIndex>> indexesByRefUser;//< RefUserId, List of CallCardRefUserIndex>>
        Map<String, List<InvoiceDetails>> detailsByRefUser = new HashMap<>();//< RefUserId, List of SalesOrderDetails>>

        List<String> propertiesList = new ArrayList<String>(properties.keySet());

        // indexesByRefUser = erpNativeQueryManager.listCallCardRefUserIndexesPreviousValues(callCardUserId, refUserIds, previousValuesSetting, activeCallCards);  // get a number of previous values
        indexesByRefUser = erpNativeQueryManager.listCallCardRefUserIndexesPreviousValuesSummary(callCardUserId, refUserIds, propertiesList, previousValuesSetting, recordsTypes, activeCallCards); // get the summaries for a number of previous values

        // detailsByRefUser = erpNativeQueryManager.listSalesOrderDetailsPreviousValues(callCardUserId, refUserIds, previousValuesSetting, activeCallCards, false); // get a number of previous values from Sales Order
        //detailsByRefUser = erpNativeQueryManager.listSalesOrderDetailsSummaries(callCardUserId, refUserIds, previousValuesSetting, activeCallCards, false); // get the summaries for a number of previous values from Sales Order
        detailsByRefUser = erpNativeQueryManager.listInvoiceDetailsSummaries(callCardUserId, refUserIds, previousValuesSetting, Arrays.asList(InvoiceDTO.SUBMITTED)); // get the summaries for a number of previous values from Invoices


        // for every refUserId
        for (Map.Entry<String, List<CallCardRefUserIndex>> refUserMap : indexesByRefUser.entrySet()) {
            List<CallCardRefUserIndex> refUserIndexes = refUserMap.getValue();

            Map<String, Map<String, List<CallCardRefUserIndex>>> indexesMap = new HashMap<>();  //< RefUserId, <ItemId, List of CallCardRefUserIndex>>

            List<InvoiceDetails> invoiceDetailsByUser = detailsByRefUser.get(refUserMap.getKey());
            List<CallCardRefUserIndex> invoiceToIndex = new ArrayList<>();
            if (invoiceDetailsByUser != null && invoiceDetailsByUser.size() > 0) {
                for (InvoiceDetails invoiceDetails : invoiceDetailsByUser) {
                    CallCardRefUserIndex salesToIndex = new CallCardRefUserIndex();
                    CallCardRefUser tempRefUser = new CallCardRefUser();
                    tempRefUser.setCallCardRefUserId(refUserMap.getKey());
                    salesToIndex.setCallCardRefUserId(tempRefUser);
                    salesToIndex.setItemTypeId(invoiceDetails.getItemTypeId());
                    salesToIndex.setItemId(invoiceDetails.getItemId());
                    salesToIndex.setPropertyId(Constants.METADATA_KEY_CALL_CARD_INDEX_SALES);
                    salesToIndex.setPropertyValue(String.valueOf(invoiceDetails.getQuantity()));
                    invoiceToIndex.add(salesToIndex);
                }
            }

            if (invoiceToIndex != null && invoiceToIndex.size() > 0)
                refUserIndexes.addAll(invoiceToIndex);

            if (refUserIndexes != null && refUserIndexes.size() > 0) {

                //by item
                Map<String, List<CallCardRefUserIndex>> indexesByItem = new HashMap<>();

                for (CallCardRefUserIndex refUserIndex : refUserIndexes) {
                    if (indexesByItem.containsKey(refUserIndex.getItemId())) {
                        indexesByItem.get(refUserIndex.getItemId()).add(refUserIndex);
                    } else {
                        List<CallCardRefUserIndex> indexesToAdd = new ArrayList<>();
                        indexesToAdd.add(refUserIndex);
                        indexesByItem.put(refUserIndex.getItemId(), indexesToAdd);
                    }
                }

                indexesMap.put(refUserMap.getKey(), indexesByItem);
            }

            //summarize and convert to ActionItemAttributesDTO -- Sales from Invoice will be summarized with Sales from CallCard
            Map<String, List<CallCardActionItemAttributesDTO>> itemsMap = new HashMap<>(); //<ItemId, List of ActionItemAttributesDTO>>

            if (indexesMap != null && indexesMap.size() > 0) {

                for (Map.Entry<String, Map<String, List<CallCardRefUserIndex>>> refUserIndexMap : indexesMap.entrySet()) {

                    //per item
                    List<CallCardActionItemAttributesDTO> itemProperties;

                    for (Map.Entry<String, List<CallCardRefUserIndex>> refUserItem : refUserIndexMap.getValue().entrySet()) {

                        List<CallCardRefUserIndex> indexesList = refUserItem.getValue();
                        itemProperties = summarizePropertiesByItem(properties, indexesList);

                        if (itemsMap.containsKey(refUserItem.getKey())) {
                            itemsMap.get(refUserItem.getKey()).addAll(itemProperties);
                        } else {
                            itemsMap.put(refUserItem.getKey(), itemProperties);
                        }
                    }
                }
            }

            if (results.containsKey(refUserMap.getKey())) {
                results.get(refUserMap.getKey()).putAll(itemsMap);
            } else {
                results.put(refUserMap.getKey(), itemsMap);
            }
        }
        return results;
    }

    private List<CallCardActionItemAttributesDTO> summarizePropertiesByItem(Map<String, String> properties, List<CallCardRefUserIndex> indexesList) {
        List<CallCardActionItemAttributesDTO> itemProperties = new ArrayList<>();

        for (Map.Entry<String, String> property : properties.entrySet()) {
            List<CallCardRefUserIndex> indexesPropertyList = new ArrayList<>();

            CallCardActionItemAttributesDTO propertySummary = new CallCardActionItemAttributesDTO();

            for (CallCardRefUserIndex index : indexesList) {
                if (index.getPropertyId().equals(property.getKey()))
                    indexesPropertyList.add(index);
            }

            if (indexesPropertyList != null && indexesPropertyList.size() > 0) {
                propertySummary.setPropertyName(property.getKey());

                if (property.getValue().equalsIgnoreCase("integer")) {
                    int sum = 0;
                    int propertyRecords = 0;
                    int average = 0;

                    for (CallCardRefUserIndex indexProperty : indexesPropertyList) {
                        if (Integer.parseInt(indexProperty.getPropertyValue()) != 0) {
                            sum += Integer.parseInt(indexProperty.getPropertyValue());
                            propertyRecords++;
                        }
                    }

                    if (propertyRecords != 0)
                        average = sum / propertyRecords;

                    propertySummary.setRefPropertyValue(String.valueOf(average));
                    itemProperties.add(propertySummary);

                } else if (property.getValue().equalsIgnoreCase("string") || property.getValue().equalsIgnoreCase("boolean")) {
                    CallCardRefUserIndex lastValue = null;

                    for (CallCardRefUserIndex indexProperty : indexesPropertyList) {
                        if (!indexProperty.getPropertyValue().equals("")) {
                            if (lastValue == null) {
                                lastValue = indexProperty;
                            } else if (lastValue.getSubmitDate() != null && indexProperty.getSubmitDate() != null && lastValue.getSubmitDate().before(indexProperty.getSubmitDate())) {
                                lastValue = indexProperty;
                            }
                        }
                    }

                    propertySummary.setRefPropertyValue(lastValue.getPropertyValue());
                    itemProperties.add(propertySummary);
                }
            }

        }

        return itemProperties;
    }

    public IGenericDAO<CallCard, String> getCallCardDao() {
        return callCardDao;
    }

    public void setCallCardDao(IGenericDAO<CallCard, String> callCardDao) {
        this.callCardDao = callCardDao;
    }

    public IGenericDAO<CallCardTemplate, String> getCallCardTemplateDao() {
        return callCardTemplateDao;
    }

    public void setCallCardTemplateDao(IGenericDAO<CallCardTemplate, String> callCardTemplateDao) {
        this.callCardTemplateDao = callCardTemplateDao;
    }

    public IGenericDAO<CallCardTemplateEntry, String> getCallCardTemplateEntryDao() {
        return callCardTemplateEntryDao;
    }

    public void setCallCardTemplateEntryDao(IGenericDAO<CallCardTemplateEntry, String> callCardTemplateEntryDao) {
        this.callCardTemplateEntryDao = callCardTemplateEntryDao;
    }

    public IGenericDAO<Users, String> getUsersDao() {
        return usersDao;
    }

    public void setUsersDao(IGenericDAO<Users, String> usersDao) {
        this.usersDao = usersDao;
    }

    public IGenericDAO<ItemTypes, Integer> getItemTypesDao() {
        return itemTypesDao;
    }

    public void setItemTypesDao(IGenericDAO<ItemTypes, Integer> itemTypesDao) {
        this.itemTypesDao = itemTypesDao;
    }

    public IGenericDAO<CallCardTemplatePOS, String> getCallCardTemplatePOSDao() {
        return callCardTemplatePOSDao;
    }

    public void setCallCardTemplatePOSDao(IGenericDAO<CallCardTemplatePOS, String> callCardTemplatePOSDao) {
        this.callCardTemplatePOSDao = callCardTemplatePOSDao;
    }

    public IMetadataComponent getMetadataComponent() {
        return metadataComponent;
    }

    public void setMetadataComponent(IMetadataComponent metadataComponent) {
        this.metadataComponent = metadataComponent;
    }

    public IGenericDAO<CallCardTemplateUserReferences, String> getCallCardTemplateUserReferencesDao() {
        return callCardTemplateUserReferencesDao;
    }

    public void setCallCardTemplateUserReferencesDao(IGenericDAO<CallCardTemplateUserReferences, String> callCardTemplateUserReferencesDao) {
        this.callCardTemplateUserReferencesDao = callCardTemplateUserReferencesDao;
    }

    public IGenericDAO<CallCardRefUser, String> getCallCardRefUserDao() {
        return callCardRefUserDao;
    }

    public void setCallCardRefUserDao(IGenericDAO<CallCardRefUser, String> callCardRefUserDao) {
        this.callCardRefUserDao = callCardRefUserDao;
    }

    public IGenericDAO<CallCardRefUserIndex, String> getCallCardRefUserIndexDao() {
        return callCardRefUserIndexDao;
    }

    public void setCallCardRefUserIndexDao(IGenericDAO<CallCardRefUserIndex, String> callCardRefUserIndexDao) {
        this.callCardRefUserIndexDao = callCardRefUserIndexDao;
    }

    public IGenericDAO<Application, String> getApplicationDao() {
        return applicationDao;
    }

    public void setApplicationDao(IGenericDAO<Application, String> applicationDao) {
        this.applicationDao = applicationDao;
    }

    public SolrClient getSolrClient() {
        return solrClient;
    }

    public void setSolrClient(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public IAddressbookManagement getAddressbookManagement() {
        return addressbookManagement;
    }

    public void setAddressbookManagement(IAddressbookManagement addressbookManagement) {
        this.addressbookManagement = addressbookManagement;
    }

    public ErpDynamicQueryManager getErpDynamicQueryManager() {
        return erpDynamicQueryManager;
    }

    public void setErpDynamicQueryManager(ErpDynamicQueryManager erpDynamicQueryManager) {
        this.erpDynamicQueryManager = erpDynamicQueryManager;
    }

    public ErpNativeQueryManager getErpNativeQueryManager() {
        return erpNativeQueryManager;
    }

    public void setErpNativeQueryManager(ErpNativeQueryManager erpNativeQueryManager) {
        this.erpNativeQueryManager = erpNativeQueryManager;
    }

    public IAppSettingsComponent getAppSettingsComponent() {
        return appSettingsComponent;
    }

    public void setAppSettingsComponent(IAppSettingsComponent appSettingsComponent) {
        this.appSettingsComponent = appSettingsComponent;
    }

    public ISalesOrderManagement getSalesOrderManagement() {
        return salesOrderManagement;
    }

    public void setSalesOrderManagement(ISalesOrderManagement salesOrderManagement) {
        this.salesOrderManagement = salesOrderManagement;
    }

    public IGenericDAO<Postcode, Integer> getPostcodeDao() {
        return postcodeDao;
    }

    public void setPostcodeDao(IGenericDAO<Postcode, Integer> postcodeDao) {
        this.postcodeDao = postcodeDao;
    }

    public IUserMetadataComponent getUserMetadataComponent() {
        return userMetadataComponent;
    }

    public void setUserMetadataComponent(IUserMetadataComponent userMetadataComponent) {
        this.userMetadataComponent = userMetadataComponent;
    }

    // ============================================================
    // Statistics Methods Implementation (User Story 2)
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) {
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        try {
            // SQL query to get overall statistics
            String sql = "SELECT " +
                    "COUNT(DISTINCT cc.CALL_CARD_ID) as totalCallCards, " +
                    "SUM(CASE WHEN cc.ACTIVE = 1 AND cc.END_DATE IS NULL THEN 1 ELSE 0 END) as activeCallCards, " +
                    "SUM(CASE WHEN cc.ACTIVE = 0 OR cc.END_DATE IS NOT NULL THEN 1 ELSE 0 END) as submittedCallCards, " +
                    "COUNT(DISTINCT cc.USER_ID) as totalUsers, " +
                    "COUNT(DISTINCT cc.CALL_CARD_TEMPLATE_ID) as totalTemplates, " +
                    "COUNT(DISTINCT ccru.CALL_CARD_REF_USER_ID) as totalRefUsers, " +
                    "AVG(CAST(refUserCount.cnt AS FLOAT)) as avgUsersPerCallCard, " +
                    "AVG(CAST(cardCount.cnt AS FLOAT)) as avgCallCardsPerUser, " +
                    "AVG(CASE WHEN cc.END_DATE IS NOT NULL AND cc.START_DATE IS NOT NULL " +
                    "    THEN DATEDIFF(MINUTE, cc.START_DATE, cc.END_DATE) ELSE NULL END) as avgCompletionTimeMinutes " +
                    "FROM CALL_CARD cc " +
                    "INNER JOIN [USER] u ON cc.USER_ID = u.USER_ID " +
                    "LEFT JOIN CALL_CARD_REF_USER ccru ON cc.CALL_CARD_ID = ccru.CALL_CARD_ID " +
                    "LEFT JOIN (SELECT CALL_CARD_ID, COUNT(*) as cnt FROM CALL_CARD_REF_USER GROUP BY CALL_CARD_ID) refUserCount ON cc.CALL_CARD_ID = refUserCount.CALL_CARD_ID " +
                    "LEFT JOIN (SELECT USER_ID, COUNT(*) as cnt FROM CALL_CARD GROUP BY USER_ID) cardCount ON cc.USER_ID = cardCount.USER_ID " +
                    "WHERE u.USER_GROUP_ID = :userGroupId";

            if (dateFrom != null) {
                sql += " AND cc.START_DATE >= :dateFrom";
            }
            if (dateTo != null) {
                sql += " AND cc.START_DATE <= :dateTo";
            }

            List<Object[]> results = erpNativeQueryManager.executeNativeQuery(
                    sql,
                    new String[]{"userGroupId", "dateFrom", "dateTo"},
                    new Object[]{userGroupId, dateFrom, dateTo}
            );

            if (results != null && !results.isEmpty()) {
                Object[] row = results.get(0);
                CallCardStatsDTO stats = new CallCardStatsDTO();
                stats.setUserGroupId(userGroupId);
                stats.setDateFrom(dateFrom);
                stats.setDateTo(dateTo);
                stats.setTotalCallCards(((Number) row[0]).longValue());
                stats.setActiveCallCards(((Number) row[1]).longValue());
                stats.setSubmittedCallCards(((Number) row[2]).longValue());
                stats.setTotalUsers(((Number) row[3]).longValue());
                stats.setTotalTemplates(((Number) row[4]).longValue());
                stats.setTotalRefUsers(row[5] != null ? ((Number) row[5]).longValue() : 0L);
                stats.setAverageUsersPerCallCard(row[6] != null ? ((Number) row[6]).doubleValue() : null);
                stats.setAverageCallCardsPerUser(row[7] != null ? ((Number) row[7]).doubleValue() : null);
                stats.setAverageCompletionTimeMinutes(row[8] != null ? ((Number) row[8]).longValue() : null);
                return stats;
            }

            // Return empty stats if no data
            return new CallCardStatsDTO(userGroupId, dateFrom, dateTo, 0, 0, 0, 0, 0, 0, null, null, null);

        } catch (Exception e) {
            LOGGER.error("Error getting overall CallCard statistics", e);
            throw new BusinessLayerException("Error retrieving CallCard statistics", ExceptionTypeTO.GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) {
        Assert.notNullOrEmpty(templateId, "templateId must not be null or empty");
        Assert.isValidUUID(templateId, "templateId must be a valid UUID");
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        try {
            String sql = "SELECT " +
                    "cct.CALL_CARD_TEMPLATE_ID as templateId, " +
                    "cct.CALL_CARD_TEMPLATE_NAME as templateName, " +
                    "COUNT(cc.CALL_CARD_ID) as usageCount, " +
                    "COUNT(DISTINCT cc.USER_ID) as uniqueUsers, " +
                    "SUM(CASE WHEN cc.ACTIVE = 1 THEN 1 ELSE 0 END) as activeCount, " +
                    "SUM(CASE WHEN cc.ACTIVE = 0 THEN 1 ELSE 0 END) as submittedCount, " +
                    "AVG(CASE WHEN cc.END_DATE IS NOT NULL AND cc.START_DATE IS NOT NULL " +
                    "    THEN DATEDIFF(MINUTE, cc.START_DATE, cc.END_DATE) ELSE NULL END) as avgCompletionTime, " +
                    "MAX(cc.START_DATE) as lastUsedDate, " +
                    "MIN(cc.START_DATE) as firstUsedDate, " +
                    "COUNT(ccru.CALL_CARD_REF_USER_ID) as totalRefUsers, " +
                    "AVG(CAST(refUserCount.cnt AS FLOAT)) as avgRefUsersPerCard " +
                    "FROM CALL_CARD_TEMPLATE cct " +
                    "LEFT JOIN CALL_CARD cc ON cct.CALL_CARD_TEMPLATE_ID = cc.CALL_CARD_TEMPLATE_ID " +
                    "LEFT JOIN [USER] u ON cc.USER_ID = u.USER_ID " +
                    "LEFT JOIN CALL_CARD_REF_USER ccru ON cc.CALL_CARD_ID = ccru.CALL_CARD_ID " +
                    "LEFT JOIN (SELECT CALL_CARD_ID, COUNT(*) as cnt FROM CALL_CARD_REF_USER GROUP BY CALL_CARD_ID) refUserCount ON cc.CALL_CARD_ID = refUserCount.CALL_CARD_ID " +
                    "WHERE cct.CALL_CARD_TEMPLATE_ID = :templateId " +
                    "AND u.USER_GROUP_ID = :userGroupId";

            if (dateFrom != null) {
                sql += " AND cc.START_DATE >= :dateFrom";
            }
            if (dateTo != null) {
                sql += " AND cc.START_DATE <= :dateTo";
            }

            sql += " GROUP BY cct.CALL_CARD_TEMPLATE_ID, cct.CALL_CARD_TEMPLATE_NAME";

            List<Object[]> results = erpNativeQueryManager.executeNativeQuery(
                    sql,
                    new String[]{"templateId", "userGroupId", "dateFrom", "dateTo"},
                    new Object[]{templateId, userGroupId, dateFrom, dateTo}
            );

            if (results != null && !results.isEmpty()) {
                Object[] row = results.get(0);
                TemplateUsageDTO dto = new TemplateUsageDTO();
                dto.setTemplateId((String) row[0]);
                dto.setTemplateName((String) row[1]);
                dto.setUserGroupId(userGroupId);
                dto.setDateFrom(dateFrom);
                dto.setDateTo(dateTo);
                dto.setUsageCount(((Number) row[2]).longValue());
                dto.setUniqueUsers(((Number) row[3]).longValue());
                dto.setActiveCount(((Number) row[4]).longValue());
                dto.setSubmittedCount(((Number) row[5]).longValue());

                // Calculate completion rate
                long total = dto.getUsageCount();
                if (total > 0) {
                    dto.setCompletionRate((dto.getSubmittedCount() * 100.0) / total);
                } else {
                    dto.setCompletionRate(0.0);
                }

                dto.setAverageCompletionTimeMinutes(row[6] != null ? ((Number) row[6]).longValue() : null);
                dto.setLastUsedDate(row[7] != null ? (Date) row[7] : null);
                dto.setFirstUsedDate(row[8] != null ? (Date) row[8] : null);
                dto.setTotalRefUsers(row[9] != null ? ((Number) row[9]).longValue() : 0L);
                dto.setAverageRefUsersPerCallCard(row[10] != null ? ((Number) row[10]).doubleValue() : null);

                return dto;
            }

            return null;

        } catch (Exception e) {
            LOGGER.error("Error getting template usage statistics for template: " + templateId, e);
            throw new BusinessLayerException("Error retrieving template usage statistics", ExceptionTypeTO.GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserEngagementDTO getUserEngagementStatistics(String userId, String userGroupId, Date dateFrom, Date dateTo) {
        Assert.notNullOrEmpty(userId, "userId must not be null or empty");
        Assert.isValidUUID(userId, "userId must be a valid UUID");
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        try {
            String sql = "SELECT " +
                    "u.USER_ID as userId, " +
                    "u.USER_NAME as userName, " +
                    "COUNT(cc.CALL_CARD_ID) as totalCallCards, " +
                    "SUM(CASE WHEN cc.ACTIVE = 1 THEN 1 ELSE 0 END) as activeCallCards, " +
                    "SUM(CASE WHEN cc.ACTIVE = 0 THEN 1 ELSE 0 END) as submittedCallCards, " +
                    "COUNT(ccru.CALL_CARD_REF_USER_ID) as totalRefUsers, " +
                    "AVG(CAST(refUserCount.cnt AS FLOAT)) as avgRefUsersPerCard, " +
                    "COUNT(DISTINCT cc.CALL_CARD_TEMPLATE_ID) as uniqueTemplates, " +
                    "AVG(CASE WHEN cc.END_DATE IS NOT NULL AND cc.START_DATE IS NOT NULL " +
                    "    THEN DATEDIFF(MINUTE, cc.START_DATE, cc.END_DATE) ELSE NULL END) as avgCompletionTime, " +
                    "MAX(cc.LAST_UPDATED) as lastActivityDate, " +
                    "MIN(cc.START_DATE) as firstActivityDate, " +
                    "COUNT(DISTINCT CAST(cc.START_DATE AS DATE)) as activityDaysCount " +
                    "FROM [USER] u " +
                    "LEFT JOIN CALL_CARD cc ON u.USER_ID = cc.USER_ID " +
                    "LEFT JOIN CALL_CARD_REF_USER ccru ON cc.CALL_CARD_ID = ccru.CALL_CARD_ID " +
                    "LEFT JOIN (SELECT CALL_CARD_ID, COUNT(*) as cnt FROM CALL_CARD_REF_USER GROUP BY CALL_CARD_ID) refUserCount ON cc.CALL_CARD_ID = refUserCount.CALL_CARD_ID " +
                    "WHERE u.USER_ID = :userId " +
                    "AND u.USER_GROUP_ID = :userGroupId";

            if (dateFrom != null) {
                sql += " AND cc.START_DATE >= :dateFrom";
            }
            if (dateTo != null) {
                sql += " AND cc.START_DATE <= :dateTo";
            }

            sql += " GROUP BY u.USER_ID, u.USER_NAME";

            List<Object[]> results = erpNativeQueryManager.executeNativeQuery(
                    sql,
                    new String[]{"userId", "userGroupId", "dateFrom", "dateTo"},
                    new Object[]{userId, userGroupId, dateFrom, dateTo}
            );

            if (results != null && !results.isEmpty()) {
                Object[] row = results.get(0);
                UserEngagementDTO dto = new UserEngagementDTO();
                dto.setUserId((String) row[0]);
                dto.setUserName((String) row[1]);
                dto.setUserGroupId(userGroupId);
                dto.setDateFrom(dateFrom);
                dto.setDateTo(dateTo);
                dto.setTotalCallCards(((Number) row[2]).longValue());
                dto.setActiveCallCards(((Number) row[3]).longValue());
                dto.setSubmittedCallCards(((Number) row[4]).longValue());

                // Calculate completion rate
                long total = dto.getTotalCallCards();
                if (total > 0) {
                    dto.setCompletionRate((dto.getSubmittedCallCards() * 100.0) / total);
                } else {
                    dto.setCompletionRate(0.0);
                }

                dto.setTotalRefUsers(row[5] != null ? ((Number) row[5]).longValue() : 0L);
                dto.setAverageRefUsersPerCallCard(row[6] != null ? ((Number) row[6]).doubleValue() : null);
                dto.setUniqueTemplatesUsed(((Number) row[7]).longValue());
                dto.setAverageCompletionTimeMinutes(row[8] != null ? ((Number) row[8]).longValue() : null);
                dto.setLastActivityDate(row[9] != null ? (Date) row[9] : null);
                dto.setFirstActivityDate(row[10] != null ? (Date) row[10] : null);
                dto.setActivityDaysCount(row[11] != null ? ((Number) row[11]).intValue() : null);

                // Get most used template
                String mostUsedSql = "SELECT TOP 1 cct.CALL_CARD_TEMPLATE_ID, cct.CALL_CARD_TEMPLATE_NAME " +
                        "FROM CALL_CARD cc " +
                        "INNER JOIN CALL_CARD_TEMPLATE cct ON cc.CALL_CARD_TEMPLATE_ID = cct.CALL_CARD_TEMPLATE_ID " +
                        "WHERE cc.USER_ID = :userId " +
                        "GROUP BY cct.CALL_CARD_TEMPLATE_ID, cct.CALL_CARD_TEMPLATE_NAME " +
                        "ORDER BY COUNT(*) DESC";

                List<Object[]> templateResults = erpNativeQueryManager.executeNativeQuery(
                        mostUsedSql,
                        new String[]{"userId"},
                        new Object[]{userId}
                );

                if (templateResults != null && !templateResults.isEmpty()) {
                    Object[] templateRow = templateResults.get(0);
                    dto.setMostUsedTemplateId((String) templateRow[0]);
                    dto.setMostUsedTemplateName((String) templateRow[1]);
                }

                return dto;
            }

            return null;

        } catch (Exception e) {
            LOGGER.error("Error getting user engagement statistics for user: " + userId, e);
            throw new BusinessLayerException("Error retrieving user engagement statistics", ExceptionTypeTO.GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateUsageDTO> getTopTemplates(String userGroupId, Integer limit, Date dateFrom, Date dateTo) {
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        try {
            String sql = "SELECT TOP " + limit + " " +
                    "cct.CALL_CARD_TEMPLATE_ID as templateId, " +
                    "cct.CALL_CARD_TEMPLATE_NAME as templateName, " +
                    "COUNT(cc.CALL_CARD_ID) as usageCount, " +
                    "COUNT(DISTINCT cc.USER_ID) as uniqueUsers, " +
                    "SUM(CASE WHEN cc.ACTIVE = 1 THEN 1 ELSE 0 END) as activeCount, " +
                    "SUM(CASE WHEN cc.ACTIVE = 0 THEN 1 ELSE 0 END) as submittedCount, " +
                    "MAX(cc.START_DATE) as lastUsedDate " +
                    "FROM CALL_CARD_TEMPLATE cct " +
                    "INNER JOIN CALL_CARD cc ON cct.CALL_CARD_TEMPLATE_ID = cc.CALL_CARD_TEMPLATE_ID " +
                    "INNER JOIN [USER] u ON cc.USER_ID = u.USER_ID " +
                    "WHERE u.USER_GROUP_ID = :userGroupId";

            if (dateFrom != null) {
                sql += " AND cc.START_DATE >= :dateFrom";
            }
            if (dateTo != null) {
                sql += " AND cc.START_DATE <= :dateTo";
            }

            sql += " GROUP BY cct.CALL_CARD_TEMPLATE_ID, cct.CALL_CARD_TEMPLATE_NAME " +
                    "ORDER BY usageCount DESC";

            List<Object[]> results = erpNativeQueryManager.executeNativeQuery(
                    sql,
                    new String[]{"userGroupId", "dateFrom", "dateTo"},
                    new Object[]{userGroupId, dateFrom, dateTo}
            );

            List<TemplateUsageDTO> topTemplates = new ArrayList<>();
            if (results != null) {
                for (Object[] row : results) {
                    TemplateUsageDTO dto = new TemplateUsageDTO();
                    dto.setTemplateId((String) row[0]);
                    dto.setTemplateName((String) row[1]);
                    dto.setUserGroupId(userGroupId);
                    dto.setDateFrom(dateFrom);
                    dto.setDateTo(dateTo);
                    dto.setUsageCount(((Number) row[2]).longValue());
                    dto.setUniqueUsers(((Number) row[3]).longValue());
                    dto.setActiveCount(((Number) row[4]).longValue());
                    dto.setSubmittedCount(((Number) row[5]).longValue());

                    // Calculate completion rate
                    long total = dto.getUsageCount();
                    if (total > 0) {
                        dto.setCompletionRate((dto.getSubmittedCallCards() * 100.0) / total);
                    } else {
                        dto.setCompletionRate(0.0);
                    }

                    dto.setLastUsedDate(row[6] != null ? (Date) row[6] : null);
                    topTemplates.add(dto);
                }
            }

            return topTemplates;

        } catch (Exception e) {
            LOGGER.error("Error getting top templates", e);
            throw new BusinessLayerException("Error retrieving top templates", ExceptionTypeTO.GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getActiveUsersCount(String userGroupId, Date dateFrom, Date dateTo) {
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        try {
            String sql = "SELECT COUNT(DISTINCT cc.USER_ID) as activeUsersCount " +
                    "FROM CALL_CARD cc " +
                    "INNER JOIN [USER] u ON cc.USER_ID = u.USER_ID " +
                    "WHERE u.USER_GROUP_ID = :userGroupId";

            if (dateFrom != null) {
                sql += " AND cc.START_DATE >= :dateFrom";
            }
            if (dateTo != null) {
                sql += " AND cc.START_DATE <= :dateTo";
            }

            List<Object[]> results = erpNativeQueryManager.executeNativeQuery(
                    sql,
                    new String[]{"userGroupId", "dateFrom", "dateTo"},
                    new Object[]{userGroupId, dateFrom, dateTo}
            );

            if (results != null && !results.isEmpty()) {
                return ((Number) results.get(0)).longValue();
            }

            return 0L;

        } catch (Exception e) {
            LOGGER.error("Error getting active users count", e);
            throw new BusinessLayerException("Error retrieving active users count", ExceptionTypeTO.GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEngagementDTO> getAllUserEngagementStatistics(String userGroupId, Date dateFrom, Date dateTo, Integer limit) {
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        if (limit == null || limit <= 0) {
            limit = 100;
        }

        try {
            String sql = "SELECT TOP " + limit + " " +
                    "u.USER_ID as userId, " +
                    "u.USER_NAME as userName, " +
                    "COUNT(cc.CALL_CARD_ID) as totalCallCards, " +
                    "SUM(CASE WHEN cc.ACTIVE = 1 THEN 1 ELSE 0 END) as activeCallCards, " +
                    "SUM(CASE WHEN cc.ACTIVE = 0 THEN 1 ELSE 0 END) as submittedCallCards, " +
                    "MAX(cc.LAST_UPDATED) as lastActivityDate " +
                    "FROM [USER] u " +
                    "LEFT JOIN CALL_CARD cc ON u.USER_ID = cc.USER_ID " +
                    "WHERE u.USER_GROUP_ID = :userGroupId";

            if (dateFrom != null) {
                sql += " AND cc.START_DATE >= :dateFrom";
            }
            if (dateTo != null) {
                sql += " AND cc.START_DATE <= :dateTo";
            }

            sql += " GROUP BY u.USER_ID, u.USER_NAME " +
                    "HAVING COUNT(cc.CALL_CARD_ID) > 0 " +
                    "ORDER BY totalCallCards DESC";

            List<Object[]> results = erpNativeQueryManager.executeNativeQuery(
                    sql,
                    new String[]{"userGroupId", "dateFrom", "dateTo"},
                    new Object[]{userGroupId, dateFrom, dateTo}
            );

            List<UserEngagementDTO> engagementList = new ArrayList<>();
            if (results != null) {
                for (Object[] row : results) {
                    UserEngagementDTO dto = new UserEngagementDTO();
                    dto.setUserId((String) row[0]);
                    dto.setUserName((String) row[1]);
                    dto.setUserGroupId(userGroupId);
                    dto.setDateFrom(dateFrom);
                    dto.setDateTo(dateTo);
                    dto.setTotalCallCards(((Number) row[2]).longValue());
                    dto.setActiveCallCards(((Number) row[3]).longValue());
                    dto.setSubmittedCallCards(((Number) row[4]).longValue());

                    // Calculate completion rate
                    long total = dto.getTotalCallCards();
                    if (total > 0) {
                        dto.setCompletionRate((dto.getSubmittedCallCards() * 100.0) / total);
                    } else {
                        dto.setCompletionRate(0.0);
                    }

                    dto.setLastActivityDate(row[5] != null ? (Date) row[5] : null);
                    engagementList.add(dto);
                }
            }

            return engagementList;

        } catch (Exception e) {
            LOGGER.error("Error getting all user engagement statistics", e);
            throw new BusinessLayerException("Error retrieving user engagement statistics", ExceptionTypeTO.GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateUsageDTO> getAllTemplateUsageStatistics(String userGroupId, Date dateFrom, Date dateTo) {
        Assert.notNullOrEmpty(userGroupId, "userGroupId must not be null or empty");
        Assert.isValidUUID(userGroupId, "userGroupId must be a valid UUID");

        try {
            String sql = "SELECT " +
                    "cct.CALL_CARD_TEMPLATE_ID as templateId, " +
                    "cct.CALL_CARD_TEMPLATE_NAME as templateName, " +
                    "COUNT(cc.CALL_CARD_ID) as usageCount, " +
                    "COUNT(DISTINCT cc.USER_ID) as uniqueUsers, " +
                    "SUM(CASE WHEN cc.ACTIVE = 1 THEN 1 ELSE 0 END) as activeCount, " +
                    "SUM(CASE WHEN cc.ACTIVE = 0 THEN 1 ELSE 0 END) as submittedCount, " +
                    "MAX(cc.START_DATE) as lastUsedDate, " +
                    "MIN(cc.START_DATE) as firstUsedDate " +
                    "FROM CALL_CARD_TEMPLATE cct " +
                    "LEFT JOIN CALL_CARD cc ON cct.CALL_CARD_TEMPLATE_ID = cc.CALL_CARD_TEMPLATE_ID " +
                    "LEFT JOIN [USER] u ON cc.USER_ID = u.USER_ID " +
                    "WHERE u.USER_GROUP_ID = :userGroupId";

            if (dateFrom != null) {
                sql += " AND cc.START_DATE >= :dateFrom";
            }
            if (dateTo != null) {
                sql += " AND cc.START_DATE <= :dateTo";
            }

            sql += " GROUP BY cct.CALL_CARD_TEMPLATE_ID, cct.CALL_CARD_TEMPLATE_NAME " +
                    "ORDER BY usageCount DESC";

            List<Object[]> results = erpNativeQueryManager.executeNativeQuery(
                    sql,
                    new String[]{"userGroupId", "dateFrom", "dateTo"},
                    new Object[]{userGroupId, dateFrom, dateTo}
            );

            List<TemplateUsageDTO> templateList = new ArrayList<>();
            if (results != null) {
                for (Object[] row : results) {
                    TemplateUsageDTO dto = new TemplateUsageDTO();
                    dto.setTemplateId((String) row[0]);
                    dto.setTemplateName((String) row[1]);
                    dto.setUserGroupId(userGroupId);
                    dto.setDateFrom(dateFrom);
                    dto.setDateTo(dateTo);
                    dto.setUsageCount(((Number) row[2]).longValue());
                    dto.setUniqueUsers(((Number) row[3]).longValue());
                    dto.setActiveCount(((Number) row[4]).longValue());
                    dto.setSubmittedCount(((Number) row[5]).longValue());

                    // Calculate completion rate
                    long total = dto.getUsageCount();
                    if (total > 0) {
                        dto.setCompletionRate((dto.getSubmittedCount() * 100.0) / total);
                    } else {
                        dto.setCompletionRate(0.0);
                    }

                    dto.setLastUsedDate(row[6] != null ? (Date) row[6] : null);
                    dto.setFirstUsedDate(row[7] != null ? (Date) row[7] : null);
                    templateList.add(dto);
                }
            }

            return templateList;

        } catch (Exception e) {
            LOGGER.error("Error getting all template usage statistics", e);
            throw new BusinessLayerException("Error retrieving template usage statistics", ExceptionTypeTO.GENERIC_ERROR);
        }
    }
}

