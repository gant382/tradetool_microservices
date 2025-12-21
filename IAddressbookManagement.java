package com.saicon.games.callcard.components.external;

/**
 * Addressbook Management interface for CallCard microservice.
 * COPY TO: callcard-components/src/main/java/com/saicon/games/callcard/components/external/IAddressbookManagement.java
 *
 * Provides stub interface for addressbook operations.
 * Implementations will integrate with user management system.
 */
public interface IAddressbookManagement {

    /**
     * Create a new addressbook entry
     */
    Object createAddressbook(
        String userId,
        String addressType,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        int postcodeId,
        String postcodeName,
        int cityId,
        String cityName,
        int stateId,
        String stateName,
        String countryCode,
        String phoneNumber,
        String email,
        Double latitude,
        Double longitude,
        boolean isDefault
    );
}
