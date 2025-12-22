package com.saicon.games.callcard.util;

import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.SimplifiedCallCardDTO;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import com.saicon.games.callcard.ws.response.WSResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Custom assertions for CallCard integration tests.
 * Provides domain-specific assertion methods for CallCard objects.
 */
public class TestAssertions {

    /**
     * Asserts that a CallCardDTO is valid (all required fields populated).
     */
    public static void assertValidCallCard(CallCardDTO callCard) {
        assertNotNull(callCard, "CallCard should not be null");
        assertNotNull(callCard.getCallCardId(), "CallCard ID should not be null");
        assertTrue(isValidUUID(callCard.getCallCardId()), "CallCard ID should be valid UUID");
        assertNotNull(callCard.getStartDate(), "Start date should not be null");
        assertNotNull(callCard.getEndDate(), "End date should not be null");
        assertTrue(
            callCard.getEndDate().after(callCard.getStartDate()),
            "End date should be after start date"
        );
    }

    /**
     * Asserts that a SimplifiedCallCardDTO is valid.
     */
    public static void assertValidSimplifiedCallCard(SimplifiedCallCardDTO callCard) {
        assertNotNull(callCard, "Simplified CallCard should not be null");
        assertNotNull(callCard.getCallCardId(), "CallCard ID should not be null");
        assertNotNull(callCard.getStartDate(), "Start date should not be null");
        assertNotNull(callCard.getEndDate(), "End date should not be null");
    }

    /**
     * Asserts that a list of CallCardDTOs is not empty and valid.
     */
    public static void assertValidCallCardList(List<CallCardDTO> callCards) {
        assertNotNull(callCards, "CallCard list should not be null");
        assertTrue(callCards.size() > 0, "CallCard list should not be empty");
        for (CallCardDTO card : callCards) {
            assertValidCallCard(card);
        }
    }

    /**
     * Asserts that a response has a success status.
     */
    public static void assertSuccessResponse(WSResponse response) {
        assertNotNull(response, "Response should not be null");
        assertEquals(ResponseStatus.OK, response.getStatus(), "Response status should be OK");
    }

    /**
     * Asserts that a response has an error status.
     */
    public static void assertErrorResponse(WSResponse response) {
        assertNotNull(response, "Response should not be null");
        assertEquals(ResponseStatus.ERROR, response.getStatus(), "Response status should be ERROR");
    }

    /**
     * Asserts that an HTTP response is successful (2xx).
     */
    public static void assertHttpSuccess(ResponseEntity<?> response) {
        assertNotNull(response, "Response should not be null");
        assertTrue(
            response.getStatusCode().is2xxSuccessful(),
            "HTTP status should be 2xx success, got: " + response.getStatusCode()
        );
    }

    /**
     * Asserts that an HTTP response is client error (4xx).
     */
    public static void assertHttpClientError(ResponseEntity<?> response) {
        assertNotNull(response, "Response should not be null");
        assertTrue(
            response.getStatusCode().is4xxClientError(),
            "HTTP status should be 4xx error, got: " + response.getStatusCode()
        );
    }

    /**
     * Asserts that an HTTP response is server error (5xx).
     */
    public static void assertHttpServerError(ResponseEntity<?> response) {
        assertNotNull(response, "Response should not be null");
        assertTrue(
            response.getStatusCode().is5xxServerError(),
            "HTTP status should be 5xx error, got: " + response.getStatusCode()
        );
    }

    /**
     * Asserts that an HTTP response has specific status code.
     */
    public static void assertHttpStatus(ResponseEntity<?> response, HttpStatus expectedStatus) {
        assertNotNull(response, "Response should not be null");
        assertEquals(
            expectedStatus,
            response.getStatusCode(),
            "HTTP status should be " + expectedStatus.value()
        );
    }

    /**
     * Asserts that a string is a valid UUID.
     */
    public static void assertValidUUID(String value) {
        assertTrue(isValidUUID(value), "Value should be a valid UUID: " + value);
    }

    /**
     * Asserts that a string is not a valid UUID.
     */
    public static void assertInvalidUUID(String value) {
        assertFalse(isValidUUID(value), "Value should not be a valid UUID: " + value);
    }

    /**
     * Asserts that a date range is valid (start before end).
     */
    public static void assertValidDateRange(Date startDate, Date endDate) {
        assertNotNull(startDate, "Start date should not be null");
        assertNotNull(endDate, "End date should not be null");
        assertTrue(
            endDate.after(startDate),
            "End date should be after start date"
        );
    }

    /**
     * Asserts that two CallCardDTOs have the same core data.
     */
    public static void assertCallCardsEqual(CallCardDTO expected, CallCardDTO actual) {
        assertNotNull(expected, "Expected CallCard should not be null");
        assertNotNull(actual, "Actual CallCard should not be null");
        assertEquals(expected.getCallCardId(), actual.getCallCardId(), "CallCard IDs should match");
        assertEquals(expected.getStartDate(), actual.getStartDate(), "Start dates should match");
        assertEquals(expected.getEndDate(), actual.getEndDate(), "End dates should match");
        assertEquals(expected.isSubmitted(), actual.isSubmitted(), "Submitted status should match");
    }

    /**
     * Helper method to validate UUID format.
     */
    private static boolean isValidUUID(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Asserts that response has error code.
     */
    public static void assertHasErrorCode(WSResponse response, String expectedErrorCode) {
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getErrorCode(), "Error code should not be null");
        assertEquals(expectedErrorCode, response.getErrorCode(), "Error codes should match");
    }

    /**
     * Asserts that response has specific result message.
     */
    public static void assertHasResultMessage(WSResponse response, String expectedMessage) {
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getResult(), "Result message should not be null");
        assertTrue(
            response.getResult().contains(expectedMessage),
            "Result message should contain: " + expectedMessage
        );
    }

    /**
     * Asserts that a CallCard has specific template ID.
     */
    public static void assertCallCardTemplate(CallCardDTO callCard, String templateId) {
        assertNotNull(callCard, "CallCard should not be null");
        assertEquals(
            templateId,
            callCard.getCallCardTemplateId(),
            "Template IDs should match"
        );
    }

    /**
     * Asserts that a CallCard has groups.
     */
    public static void assertHasCallCardGroups(CallCardDTO callCard) {
        assertNotNull(callCard, "CallCard should not be null");
        assertNotNull(callCard.getGroupIds(), "Groups should not be null");
        assertTrue(callCard.getGroupIds().size() > 0, "CallCard should have at least one group");
    }

    /**
     * Asserts that a CallCard has specific number of groups.
     */
    public static void assertCallCardGroupCount(CallCardDTO callCard, int expectedCount) {
        assertNotNull(callCard, "CallCard should not be null");
        assertNotNull(callCard.getGroupIds(), "Groups should not be null");
        assertEquals(
            expectedCount,
            callCard.getGroupIds().size(),
            "CallCard should have " + expectedCount + " groups"
        );
    }
}
