package com.saicon.games.callcard.integration;

import com.saicon.games.callcard.config.TestApplicationConfiguration;
import com.saicon.games.callcard.factory.CallCardTestDataFactory;
import com.saicon.games.callcard.util.TestAssertions;
import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Integration tests for CallCard validation logic.
 * Tests data validation, business rules, and constraint enforcement.
 */
@SpringBootTest(classes = TestApplicationConfiguration.class)
@ActiveProfiles("test")
@DisplayName("CallCard Validation Integration Tests")
class CallCardValidationIntegrationTest {

    private CallCardDTO validCallCard;
    private SimplifiedCallCardDTO validSimplifiedCallCard;

    @BeforeEach
    void setUp() {
        validCallCard = CallCardTestDataFactory.createCallCard();
        validSimplifiedCallCard = CallCardTestDataFactory.createSimplifiedCallCard();
    }

    @Test
    @DisplayName("Valid CallCard - Should Pass All Assertions")
    void testValidCallCard() {
        // Arrange
        CallCardDTO callCard = CallCardTestDataFactory.createCallCard();

        // Act & Assert
        TestAssertions.assertValidCallCard(callCard);
        TestAssertions.assertValidUUID(callCard.getCallCardId());
        TestAssertions.assertValidDateRange(callCard.getStartDate(), callCard.getEndDate());
    }

    @Test
    @DisplayName("Valid SimplifiedCallCard - Should Pass Assertions")
    void testValidSimplifiedCallCard() {
        // Act & Assert
        TestAssertions.assertValidSimplifiedCallCard(validSimplifiedCallCard);
    }

    @Test
    @DisplayName("CallCard with Groups - Should Have Groups")
    void testCallCardWithGroups() {
        // Arrange
        CallCardDTO callCard = CallCardTestDataFactory.createCallCard();

        // Act & Assert
        TestAssertions.assertHasCallCardGroups(callCard);
    }

    @Test
    @DisplayName("CallCard Group Count - Should Match Expected Count")
    void testCallCardGroupCount() {
        // Arrange
        CallCardDTO callCard = CallCardTestDataFactory.createCallCard();

        // Act & Assert
        TestAssertions.assertCallCardGroupCount(callCard, 2); // Default factory creates 2 groups
    }

    @Test
    @DisplayName("CallCard with Template ID - Should Have Correct Template")
    void testCallCardWithTemplateId() {
        // Arrange
        String templateId = CallCardTestDataFactory.UUIDs.randomUUID();
        CallCardDTO callCard = CallCardTestDataFactory.createCallCardWithTemplate(templateId);

        // Act & Assert
        TestAssertions.assertCallCardTemplate(callCard, templateId);
    }

    @Test
    @DisplayName("Submitted CallCard - Should Have Submitted Flag")
    void testSubmittedCallCard() {
        // Arrange
        CallCardDTO callCard = CallCardTestDataFactory.createSubmittedCallCard();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertTrue(callCard.isSubmitted(), "CallCard should be marked as submitted");
    }

    @Test
    @DisplayName("List of Valid CallCards - Should Pass Validation")
    void testListOfValidCallCards() {
        // Arrange
        List<CallCardDTO> callCards = CallCardTestDataFactory.createCallCards(5);

        // Act & Assert
        TestAssertions.assertValidCallCardList(callCards);
    }

    @Test
    @DisplayName("Valid Date Ranges - Should Pass Validation")
    void testValidDateRanges() {
        // Arrange
        var startDate = CallCardTestDataFactory.Dates.sevenDaysAgo();
        var endDate = CallCardTestDataFactory.Dates.tomorrow();

        // Act & Assert
        TestAssertions.assertValidDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("UUID Validation - Valid UUID Should Pass")
    void testValidUUIDValidation() {
        // Arrange
        String validUUID = CallCardTestDataFactory.UUIDs.VALID_USER_ID;

        // Act & Assert
        TestAssertions.assertValidUUID(validUUID);
    }

    @Test
    @DisplayName("UUID Validation - Invalid UUID Should Fail")
    void testInvalidUUIDValidation() {
        // Arrange
        String invalidUUID = CallCardTestDataFactory.UUIDs.INVALID_UUID;

        // Act & Assert
        TestAssertions.assertInvalidUUID(invalidUUID);
    }

    @Test
    @DisplayName("CallCard Equality - Two CallCards with Same Data Should Be Equal")
    void testCallCardEquality() {
        // Arrange
        CallCardDTO callCard1 = CallCardTestDataFactory.createCallCard(
            "550e8400-e29b-41d4-a716-446655440500",
            CallCardTestDataFactory.Dates.yesterday(),
            CallCardTestDataFactory.Dates.tomorrow()
        );

        CallCardDTO callCard2 = CallCardTestDataFactory.createCallCard(
            "550e8400-e29b-41d4-a716-446655440500",
            CallCardTestDataFactory.Dates.yesterday(),
            CallCardTestDataFactory.Dates.tomorrow()
        );

        // Act & Assert
        TestAssertions.assertCallCardsEqual(callCard1, callCard2);
    }

    @Test
    @DisplayName("Multiple CallCards - All Should Be Valid")
    void testMultipleValidCallCards() {
        // Arrange
        int count = 10;
        List<CallCardDTO> callCards = CallCardTestDataFactory.createCallCards(count);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertEquals(count, callCards.size());
        for (CallCardDTO card : callCards) {
            TestAssertions.assertValidCallCard(card);
        }
    }

    @Test
    @DisplayName("Multiple Simplified CallCards - All Should Be Valid")
    void testMultipleValidSimplifiedCallCards() {
        // Arrange
        int count = 5;
        List<SimplifiedCallCardDTO> callCards = CallCardTestDataFactory.createSimplifiedCallCards(count);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertEquals(count, callCards.size());
        for (SimplifiedCallCardDTO card : callCards) {
            TestAssertions.assertValidSimplifiedCallCard(card);
        }
    }

    @Test
    @DisplayName("Common Statistics Types - Should Be Populated")
    void testCommonStatisticsTypes() {
        // Act
        var types = CallCardTestDataFactory.Parameters.commonStatisticsTypes();

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(types);
        org.junit.jupiter.api.Assertions.assertEquals(3, types.size());
    }

    @Test
    @DisplayName("Common Filter Properties - Should Be Populated")
    void testCommonFilterProperties() {
        // Act
        var properties = CallCardTestDataFactory.Parameters.commonFilterProperties();

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(properties);
        org.junit.jupiter.api.Assertions.assertTrue(properties.size() > 0);
    }

    @Test
    @DisplayName("Date Range Calculations - Days Ago Should Be In Past")
    void testDateRangeCalculations() {
        // Arrange
        var thirtyDaysAgo = CallCardTestDataFactory.Dates.thirtyDaysAgo();
        var sevenDaysAgo = CallCardTestDataFactory.Dates.sevenDaysAgo();
        var now = CallCardTestDataFactory.Dates.now();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertTrue(thirtyDaysAgo.before(now));
        org.junit.jupiter.api.Assertions.assertTrue(sevenDaysAgo.before(now));
        org.junit.jupiter.api.Assertions.assertTrue(thirtyDaysAgo.before(sevenDaysAgo));
    }

    @Test
    @DisplayName("Date Range Calculations - Future Dates Should Be In Future")
    void testFutureDateCalculations() {
        // Arrange
        var tomorrow = CallCardTestDataFactory.Dates.tomorrow();
        var in30Days = CallCardTestDataFactory.Dates.daysFromNow(30);
        var now = CallCardTestDataFactory.Dates.now();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertTrue(tomorrow.after(now));
        org.junit.jupiter.api.Assertions.assertTrue(in30Days.after(now));
        org.junit.jupiter.api.Assertions.assertTrue(tomorrow.before(in30Days));
    }

    @Test
    @DisplayName("CallCard Comments - Should Be Populated")
    void testCallCardComments() {
        // Arrange
        CallCardDTO callCard = CallCardTestDataFactory.createCallCard();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertNotNull(callCard.getComments());
        org.junit.jupiter.api.Assertions.assertTrue(callCard.getComments().length() > 0);
    }

    @Test
    @DisplayName("CallCard Last Updated - Should Be Set")
    void testCallCardLastUpdated() {
        // Arrange
        CallCardDTO callCard = CallCardTestDataFactory.createCallCard();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertNotNull(callCard.getLastUpdated());
    }
}
