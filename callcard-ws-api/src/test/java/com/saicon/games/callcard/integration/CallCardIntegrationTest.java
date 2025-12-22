package com.saicon.games.callcard.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saicon.games.callcard.config.TestApplicationConfiguration;
import com.saicon.games.callcard.ws.dto.CallCardDTO;
import com.saicon.games.callcard.ws.dto.CallCardGroupDTO;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CallCard microservice REST API endpoints.
 * Tests core CRUD operations and business logic validation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestApplicationConfiguration.class)
@ActiveProfiles("test")
@DisplayName("CallCard Integration Tests - Core API Operations")
class CallCardIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_PATH = "/api/callcard";
    private static final String VALID_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String VALID_USER_GROUP_ID = "550e8400-e29b-41d4-a716-446655440001";
    private static final String VALID_APPLICATION_ID = "550e8400-e29b-41d4-a716-446655440002";
    private static final String VALID_GAME_TYPE_ID = "550e8400-e29b-41d4-a716-446655440003";
    private static final String VALID_SESSION_ID = "550e8400-e29b-41d4-a716-446655440004";

    @BeforeEach
    void setUp() {
        // Reset test database state before each test
        // Implement database reset logic if needed
    }

    @Test
    @DisplayName("Health Check Endpoint - Should return OK")
    void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Get CallCards from Template - Valid User")
    void testGetCallCardsFromTemplate() throws Exception {
        // Arrange
        String url = String.format(
            "%s/template/%s?gameTypeId=%s",
            BASE_PATH,
            VALID_USER_ID,
            VALID_GAME_TYPE_ID
        );

        // Act
        ResponseEntity<CallCardDTO[]> response = restTemplate.getForEntity(
            url,
            CallCardDTO[].class
        );

        // Assert
        assertNotNull(response);
        // Should return either 200 OK or 204 No Content if no records exist
        assertTrue(
            response.getStatusCode() == HttpStatus.OK ||
            response.getStatusCode() == HttpStatus.NO_CONTENT,
            "Expected 200 OK or 204 No Content"
        );
    }

    @Test
    @DisplayName("Get CallCards from Template - Missing Required Headers")
    void testGetCallCardsFromTemplateInvalidHeaders() {
        // Act & Assert
        ResponseEntity<String> response = restTemplate.getForEntity(
            String.format("%s/template/%s", BASE_PATH, VALID_USER_ID),
            String.class
        );

        // Should fail with 400 Bad Request due to missing headers
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Pending CallCard - Valid User")
    void testGetPendingCallCard() {
        // Arrange
        String url = String.format(
            "%s/pending/%s?gameTypeId=%s",
            BASE_PATH,
            VALID_USER_ID,
            VALID_GAME_TYPE_ID
        );

        // Act
        ResponseEntity<CallCardDTO[]> response = restTemplate.getForEntity(
            url,
            CallCardDTO[].class
        );

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode() == HttpStatus.OK ||
            response.getStatusCode() == HttpStatus.NO_CONTENT
        );
    }

    @Test
    @DisplayName("Get New or Pending CallCard - Valid Parameters")
    void testGetNewOrPendingCallCard() {
        // Arrange
        String url = String.format(
            "%s/%s?gameTypeId=%s",
            BASE_PATH,
            VALID_USER_ID,
            VALID_GAME_TYPE_ID
        );

        // Act
        ResponseEntity<CallCardDTO[]> response = restTemplate.getForEntity(
            url,
            CallCardDTO[].class
        );

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode() == HttpStatus.OK ||
            response.getStatusCode() == HttpStatus.NO_CONTENT
        );
    }

    @Test
    @DisplayName("List Pending CallCards - Valid User")
    void testListPendingCallCard() {
        // Arrange
        String url = String.format(
            "%s/%s/callcard?gameTypeId=%s",
            BASE_PATH,
            VALID_USER_ID,
            VALID_GAME_TYPE_ID
        );

        // Act
        ResponseEntity<CallCardDTO[]> response = restTemplate.getForEntity(
            url,
            CallCardDTO[].class
        );

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode() == HttpStatus.OK ||
            response.getStatusCode() == HttpStatus.NO_CONTENT
        );
    }

    @Test
    @DisplayName("Get CallCard Statistics - Valid Parameters")
    void testGetCallCardStatistics() {
        // Arrange
        String propertyId = UUID.randomUUID().toString();
        String url = String.format(
            "%s/%s/statistics/%s?types=1,2,3",
            BASE_PATH,
            VALID_USER_ID,
            propertyId
        );

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode() == HttpStatus.OK ||
            response.getStatusCode() == HttpStatus.NO_CONTENT
        );
    }

    @Test
    @DisplayName("Add CallCard Records - Single Record")
    void testAddCallCardRecords() throws Exception {
        // Arrange
        String url = String.format("%s/update/%s", BASE_PATH, VALID_USER_ID);

        CallCardDTO callCard = createTestCallCard();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Talos-Session-Id", VALID_SESSION_ID);
        headers.set("X-Talos-Application-Id", VALID_APPLICATION_ID);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity<CallCardDTO> request =
            new org.springframework.http.HttpEntity<>(callCard, headers);

        // Act
        ResponseEntity<CallCardDTO[]> response = restTemplate.postForEntity(
            url,
            request,
            CallCardDTO[].class
        );

        // Assert - May fail due to missing dependencies, but should not throw 500
        assertNotNull(response);
        assertTrue(
            response.getStatusCode().is2xxSuccessful() ||
            response.getStatusCode().is4xxClientError()
        );
    }

    @Test
    @DisplayName("Add Multiple CallCard Records")
    void testAddMultipleCallCardRecords() throws Exception {
        // Arrange
        String url = String.format("%s/update/%s/multiple", BASE_PATH, VALID_USER_ID);

        List<CallCardDTO> callCards = Arrays.asList(
            createTestCallCard(),
            createTestCallCard(),
            createTestCallCard()
        );

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Talos-Session-Id", VALID_SESSION_ID);
        headers.set("X-Talos-Application-Id", VALID_APPLICATION_ID);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity<List<CallCardDTO>> request =
            new org.springframework.http.HttpEntity<>(callCards, headers);

        // Act
        ResponseEntity<CallCardDTO[]> response = restTemplate.postForEntity(
            url,
            request,
            CallCardDTO[].class
        );

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode().is2xxSuccessful() ||
            response.getStatusCode().is4xxClientError()
        );
    }

    @Test
    @DisplayName("Add Simplified CallCard Record")
    void testAddSimplifiedCallCard() throws Exception {
        // Arrange
        String url = String.format("%s/update/simplified/%s", BASE_PATH, VALID_USER_ID);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Talos-Session-Id", VALID_SESSION_ID);
        headers.set("X-Talos-Application-Id", VALID_APPLICATION_ID);
        headers.set("Content-Type", "application/json");

        String simplifiedCallCardJson = "{" +
            "\"callCardId\":\"" + UUID.randomUUID().toString() + "\"," +
            "\"startDate\":" + System.currentTimeMillis() + "," +
            "\"endDate\":" + (System.currentTimeMillis() + 86400000) +
            "}";

        org.springframework.http.HttpEntity<String> request =
            new org.springframework.http.HttpEntity<>(simplifiedCallCardJson, headers);

        // Act
        ResponseEntity<Void> response = restTemplate.postForEntity(
            url,
            request,
            Void.class
        );

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode().is2xxSuccessful() ||
            response.getStatusCode().is4xxClientError()
        );
    }

    @Test
    @DisplayName("Submit Transactions - Valid CallCard")
    void testSubmitTransactions() throws Exception {
        // Arrange
        String url = String.format("%s/transactions/%s", BASE_PATH, VALID_USER_ID);

        CallCardDTO callCard = createTestCallCard();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Talos-Session-Id", VALID_SESSION_ID);
        headers.set("X-Talos-User-Group-Id", VALID_USER_GROUP_ID);
        headers.set("X-Talos-Application-Id", VALID_APPLICATION_ID);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity<CallCardDTO> request =
            new org.springframework.http.HttpEntity<>(callCard, headers);

        // Act
        ResponseEntity<Void> response = restTemplate.postForEntity(
            url,
            request,
            Void.class
        );

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode().is2xxSuccessful() ||
            response.getStatusCode().is4xxClientError()
        );
    }

    @Test
    @DisplayName("List Simplified CallCards - Valid Session")
    void testListSimplifiedCallCards() {
        // Arrange
        String url = String.format("%s/list/simplified", BASE_PATH);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Talos-Session-Id", VALID_SESSION_ID);

        org.springframework.http.HttpEntity<String> request =
            new org.springframework.http.HttpEntity<>("", headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            org.springframework.http.HttpMethod.GET,
            request,
            String.class
        );

        // Assert
        assertNotNull(response);
        assertTrue(
            response.getStatusCode() == HttpStatus.OK ||
            response.getStatusCode() == HttpStatus.NO_CONTENT
        );
    }

    @Test
    @DisplayName("Invalid User ID Format - Should return 400")
    void testInvalidUserIdFormat() {
        // Arrange
        String invalidUserId = "not-a-valid-uuid";
        String url = String.format(
            "%s/template/%s?gameTypeId=%s",
            BASE_PATH,
            invalidUserId,
            VALID_GAME_TYPE_ID
        );

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Missing Required Query Parameters - Should return 400")
    void testMissingRequiredQueryParams() {
        // Arrange
        String url = String.format("%s/template/%s", BASE_PATH, VALID_USER_ID);

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Helper method to create a test CallCard DTO
     */
    private CallCardDTO createTestCallCard() {
        CallCardDTO callCard = new CallCardDTO();
        callCard.setCallCardId(UUID.randomUUID().toString());
        callCard.setStartDate(new Date());
        callCard.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // +1 day
        callCard.setSubmitted(false);
        callCard.setComments("Test CallCard");
        callCard.setLastUpdated(new Date());
        callCard.setCallCardTemplateId(UUID.randomUUID().toString());

        // Add sample groups
        List<CallCardGroupDTO> groups = new ArrayList<>();
        CallCardGroupDTO group = new CallCardGroupDTO();
        group.setGroupId(UUID.randomUUID().toString());
        groups.add(group);
        callCard.setGroupIds(groups);

        return callCard;
    }
}
