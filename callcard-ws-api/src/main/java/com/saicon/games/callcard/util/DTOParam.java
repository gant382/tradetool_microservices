package com.saicon.games.callcard.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stub annotation for DTO parameter ordering.
 * Used for client code generation in the original platform.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DTOParam {
    /**
     * Parameter order/index for serialization
     */
    int value();
}
