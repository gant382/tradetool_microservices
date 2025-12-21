package com.saicon.games.callcard.components.external;

import com.saicon.games.appsettings.dto.AppSettingsDTO;
import com.saicon.games.callcard.util.ScopeType;
import java.util.List;

/**
 * Stub interface for App Settings Component.
 * To be implemented when app settings integration is required.
 */
public interface IAppSettingsComponent {
    /**
     * Get application settings with given parameters.
     */
    List<AppSettingsDTO> get(Object organizationId, String applicationId, List<ScopeType> scopes);
}
