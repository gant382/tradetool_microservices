package com.saicon.games.callcard.util;

/**
 * Enumeration of scope types for CallCard configuration and settings.
 * Defines the level at which a configuration or setting is scoped.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public enum ScopeType {
    /**
     * Scope limited to a specific game type
     */
    GAME_TYPE,

    /**
     * Scope limited to a user group
     */
    USER_GROUP,

    /**
     * Scope limited to an application
     */
    APPLICATION,

    /**
     * Global scope across the entire system
     */
    GLOBAL
}
