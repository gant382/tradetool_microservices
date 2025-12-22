package com.saicon.callcard.monitoring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Monitored Annotation
 *
 * Marks a method for automatic metric collection via AOP.
 * The MonitoringAspect will intercept methods with this annotation
 * and record execution time, call counts, and error rates.
 *
 * Usage:
 * <pre>
 * @Service
 * public class CallCardService {
 *
 *     @Monitored
 *     public CallCard getCallCard(String id) {
 *         // method implementation
 *     }
 * }
 * </pre>
 *
 * Generated metrics:
 * - callcard.callCardService.getCallCard.success (timer)
 * - callcard.callCardService.getCallCard.failure (timer)
 * - callcard.callCardService.getCallCard.error (counter)
 *
 * View metrics at: /actuator/metrics or /actuator/prometheus
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {

    /**
     * Optional description of what is being monitored
     */
    String description() default "";

    /**
     * Whether to log method arguments (for debugging)
     */
    boolean logArguments() default false;

    /**
     * Whether to log method result (for debugging)
     */
    boolean logResult() default false;
}
