package com.saicon.games.callcard.factory;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.CallCardGroupDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import com.saicon.games.callcard.ws.dto.ItemStatisticsDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 * Used across integration tests to ensure consistent test data.
 */
public class CallCardTestDataFactory {

    /**
     * Creates a basic CallCardDTO with default values.
     */
    public static CallCardDTO createCallCard() {
        return createCallCard(
            UUID.randomUUID().toString(),
            new Date(),
            new Date(System.currentTimeMillis() + 86400000)
        );
    }

    /**
     * Creates a CallCardDTO with specified parameters.
     */
    public static CallCardDTO createCallCard(String callCardId, Date startDate, Date endDate) {
        CallCardDTO callCard = new CallCardDTO();
        callCard.setCallCardId(callCardId);
        callCard.setStartDate(startDate);
        callCard.setEndDate(endDate);
        callCard.setSubmitted(false);
        callCard.setComments("Test CallCard");
        callCard.setLastUpdated(new Date());
        callCard.setCallCardTemplateId(UUID.randomUUID().toString());
        callCard.setGroupIds(createCallCardGroups(2));
        return callCard;
    }

    /**
     * Creates a CallCardDTO marked as submitted.
     */
    public static CallCardDTO createSubmittedCallCard() {
        CallCardDTO callCard = createCallCard();
        callCard.setSubmitted(true);
        return callCard;
    }

    /**
     * Creates a CallCardDTO with specific template ID.
     */
    public static CallCardDTO createCallCardWithTemplate(String templateId) {
        CallCardDTO callCard = createCallCard();
        callCard.setCallCardTemplateId(templateId);
        return callCard;
    }

    /**
     * Creates a list of CallCardDTOs.
     */
    public static List<CallCardDTO> createCallCards(int count) {
        List<CallCardDTO> callCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            callCards.add(createCallCard());
        }
        return callCards;
    }

    /**
     * Creates a SimplifiedCallCardDTO.
     */
    public static SimplifiedCallCardDTO createSimplifiedCallCard() {
        SimplifiedCallCardDTO callCard = new SimplifiedCallCardDTO();
        callCard.setCallCardId(UUID.randomUUID().toString());
        callCard.setStartDate(new Date());
        callCard.setEndDate(new Date(System.currentTimeMillis() + 86400000));
        return callCard;
    }

    /**
     * Creates a SimplifiedCallCardDTO with specified dates.
     */
    public static SimplifiedCallCardDTO createSimplifiedCallCard(Date startDate, Date endDate) {
        SimplifiedCallCardDTO callCard = new SimplifiedCallCardDTO();
        callCard.setCallCardId(UUID.randomUUID().toString());
        callCard.setStartDate(startDate);
        callCard.setEndDate(endDate);
        return callCard;
    }

    /**
     * Creates a list of SimplifiedCallCardDTOs.
     */
    public static List<SimplifiedCallCardDTO> createSimplifiedCallCards(int count) {
        List<SimplifiedCallCardDTO> callCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            callCards.add(createSimplifiedCallCard());
        }
        return callCards;
    }

    /**
     * Creates CallCardGroupDTOs.
     */
    public static List<CallCardGroupDTO> createCallCardGroups(int count) {
        List<CallCardGroupDTO> groups = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CallCardGroupDTO group = new CallCardGroupDTO();
            group.setGroupId(UUID.randomUUID().toString());
            groups.add(group);
        }
        return groups;
    }

    /**
     * Creates a single CallCardGroupDTO.
     */
    public static CallCardGroupDTO createCallCardGroup() {
        CallCardGroupDTO group = new CallCardGroupDTO();
        group.setGroupId(UUID.randomUUID().toString());
        return group;
    }

    /**
     * Creates an ItemStatisticsDTO.
     */
    public static ItemStatisticsDTO createItemStatistics() {
        ItemStatisticsDTO statistics = new ItemStatisticsDTO();
        statistics.setItemId(UUID.randomUUID().toString());
        statistics.setItemType(1);
        statistics.setCount(10);
        statistics.setAmount(100.00);
        return statistics;
    }

    /**
     * Creates a list of ItemStatisticsDTOs.
     */
    public static List<ItemStatisticsDTO> createItemStatistics(int count) {
        List<ItemStatisticsDTO> statisticsList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            statisticsList.add(createItemStatistics());
        }
        return statisticsList;
    }

    /**
     * Creates valid UUIDs for testing.
     */
    public static class UUIDs {
        public static final String VALID_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
        public static final String VALID_USER_GROUP_ID = "550e8400-e29b-41d4-a716-446655440001";
        public static final String VALID_APPLICATION_ID = "550e8400-e29b-41d4-a716-446655440002";
        public static final String VALID_GAME_TYPE_ID = "550e8400-e29b-41d4-a716-446655440003";
        public static final String VALID_SESSION_ID = "550e8400-e29b-41d4-a716-446655440004";
        public static final String VALID_PROPERTY_ID = "550e8400-e29b-41d4-a716-446655440005";
        public static final String INVALID_UUID = "not-a-valid-uuid";
        public static final String EMPTY_STRING = "";

        public static String randomUUID() {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Utility class for date operations in tests.
     */
    public static class Dates {
        public static Date now() {
            return new Date();
        }

        public static Date tomorrow() {
            return new Date(System.currentTimeMillis() + 86400000);
        }

        public static Date yesterday() {
            return new Date(System.currentTimeMillis() - 86400000);
        }

        public static Date daysFromNow(int days) {
            return new Date(System.currentTimeMillis() + (86400000L * days));
        }

        public static Date daysAgo(int days) {
            return new Date(System.currentTimeMillis() - (86400000L * days));
        }

        public static Date thirtyDaysAgo() {
            return daysAgo(30);
        }

        public static Date sevenDaysAgo() {
            return daysAgo(7);
        }
    }

    /**
     * Utility class for common test parameters.
     */
    public static class Parameters {
        public static List<Integer> commonStatisticsTypes() {
            return Arrays.asList(1, 2, 3);
        }

        public static List<String> commonFilterProperties() {
            return Arrays.asList("region", "category", "status");
        }

        public static List<String> emptyFilterProperties() {
            return new ArrayList<>();
        }

        public static int defaultRangeFrom() {
            return 0;
        }

        public static int defaultRangeTo() {
            return 10;
        }
    }
}
