package com.saicon.callcard.monitoring;

import com.saicon.callcard.config.MonitoringConfiguration;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Monitoring Aspect for Automatic Method Instrumentation
 *
 * Automatically records metrics for:
 * - Service method execution time
 * - Error rates
 * - Method call counts
 *
 * Can be applied via @Monitored annotation on methods
 */
@Aspect
@Component
public class MonitoringAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringAspect.class);

    private final MonitoringConfiguration monitoringConfiguration;

    public MonitoringAspect(MonitoringConfiguration monitoringConfiguration) {
        this.monitoringConfiguration = monitoringConfiguration;
    }

    /**
     * Intercepts methods annotated with @Monitored
     * Records execution time and error metrics
     */
    @Around("@annotation(monitored)")
    public Object monitorMethod(ProceedingJoinPoint joinPoint, Monitored monitored) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        long startTime = System.currentTimeMillis();

        LOGGER.debug("Executing monitored method: {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            LOGGER.debug("Method {}.{} completed in {}ms", className, methodName, duration);
            recordSuccess(className, methodName, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("Method {}.{} failed after {}ms: {}", className, methodName, duration, e.getMessage(), e);
            recordError(className, methodName, duration, e);
            throw e;
        }
    }

    /**
     * Records successful method execution
     */
    private void recordSuccess(String className, String methodName, long duration) {
        String metricName = buildMetricName(className, methodName);
        monitoringConfiguration.getMeterRegistry()
            .timer(metricName + ".success",
                "class", className,
                "method", methodName)
            .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Records failed method execution
     */
    private void recordError(String className, String methodName, long duration, Exception e) {
        String metricName = buildMetricName(className, methodName);
        monitoringConfiguration.getMeterRegistry()
            .counter(metricName + ".error",
                "class", className,
                "method", methodName,
                "exception", e.getClass().getSimpleName())
            .increment();

        monitoringConfiguration.getMeterRegistry()
            .timer(metricName + ".failure",
                "class", className,
                "method", methodName)
            .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Builds a consistent metric name from class and method
     */
    private String buildMetricName(String className, String methodName) {
        return String.format("callcard.%s.%s",
            camelCaseToSnakeCase(className),
            camelCaseToSnakeCase(methodName));
    }

    /**
     * Converts camelCase to snake_case
     */
    private String camelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
