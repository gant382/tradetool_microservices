package com.saicon.callcard.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Monitoring and Metrics Configuration for CallCard Microservice
 *
 * Configures Micrometer metrics collection for:
 * - Request/response timing
 * - Business metrics (callcards created, updated, deleted)
 * - Error counters
 * - Service-level indicators
 *
 * Metrics are exposed via:
 * - /actuator/metrics - List all metrics
 * - /actuator/prometheus - Prometheus format
 */
@Configuration
@Component
public class MonitoringConfiguration {

    private final MeterRegistry meterRegistry;

    // Counters
    public static final String METRIC_CALLCARDS_CREATED = "callcard.created";
    public static final String METRIC_CALLCARDS_UPDATED = "callcard.updated";
    public static final String METRIC_CALLCARDS_DELETED = "callcard.deleted";
    public static final String METRIC_CALLCARDS_VIEWED = "callcard.viewed";
    public static final String METRIC_API_ERRORS = "callcard.api.errors";
    public static final String METRIC_DB_ERRORS = "callcard.db.errors";

    // Timers
    public static final String METRIC_API_RESPONSE_TIME = "callcard.api.response.time";
    public static final String METRIC_DB_QUERY_TIME = "callcard.db.query.time";
    public static final String METRIC_EXTERNAL_SERVICE_TIME = "callcard.external.service.time";

    // Gauges
    public static final String METRIC_ACTIVE_SESSIONS = "callcard.active.sessions";
    public static final String METRIC_CACHE_SIZE = "callcard.cache.size";

    private Counter callcardsCreatedCounter;
    private Counter callcardsUpdatedCounter;
    private Counter callcardsDeletedCounter;
    private Counter callcardsViewedCounter;
    private Counter apiErrorsCounter;
    private Counter dbErrorsCounter;

    private Timer apiResponseTimer;
    private Timer dbQueryTimer;
    private Timer externalServiceTimer;

    public MonitoringConfiguration(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    /**
     * Initialize all custom metrics
     */
    private void initializeMetrics() {
        // Counters
        this.callcardsCreatedCounter = Counter.builder(METRIC_CALLCARDS_CREATED)
            .description("Total number of callcards created")
            .register(meterRegistry);

        this.callcardsUpdatedCounter = Counter.builder(METRIC_CALLCARDS_UPDATED)
            .description("Total number of callcards updated")
            .register(meterRegistry);

        this.callcardsDeletedCounter = Counter.builder(METRIC_CALLCARDS_DELETED)
            .description("Total number of callcards deleted")
            .register(meterRegistry);

        this.callcardsViewedCounter = Counter.builder(METRIC_CALLCARDS_VIEWED)
            .description("Total number of callcard views")
            .register(meterRegistry);

        this.apiErrorsCounter = Counter.builder(METRIC_API_ERRORS)
            .description("Total API errors")
            .register(meterRegistry);

        this.dbErrorsCounter = Counter.builder(METRIC_DB_ERRORS)
            .description("Total database errors")
            .register(meterRegistry);

        // Timers
        this.apiResponseTimer = Timer.builder(METRIC_API_RESPONSE_TIME)
            .description("API endpoint response time")
            .register(meterRegistry);

        this.dbQueryTimer = Timer.builder(METRIC_DB_QUERY_TIME)
            .description("Database query execution time")
            .register(meterRegistry);

        this.externalServiceTimer = Timer.builder(METRIC_EXTERNAL_SERVICE_TIME)
            .description("External service call duration")
            .register(meterRegistry);
    }

    /**
     * Record callcard creation metric
     */
    public void recordCallcardCreated() {
        callcardsCreatedCounter.increment();
    }

    /**
     * Record callcard update metric
     */
    public void recordCallcardUpdated() {
        callcardsUpdatedCounter.increment();
    }

    /**
     * Record callcard deletion metric
     */
    public void recordCallcardDeleted() {
        callcardsDeletedCounter.increment();
    }

    /**
     * Record callcard view metric
     */
    public void recordCallcardViewed() {
        callcardsViewedCounter.increment();
    }

    /**
     * Record API error metric
     */
    public void recordApiError() {
        apiErrorsCounter.increment();
    }

    /**
     * Record database error metric
     */
    public void recordDatabaseError() {
        dbErrorsCounter.increment();
    }

    /**
     * Record API response time
     */
    public Timer.Sample recordApiResponseTime() {
        return Timer.start(meterRegistry);
    }

    /**
     * Record database query time
     */
    public Timer.Sample recordDatabaseQueryTime() {
        return Timer.start(meterRegistry);
    }

    /**
     * Record external service call time
     */
    public Timer.Sample recordExternalServiceTime() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop recording and save API response time
     */
    public void stopRecordingApiResponseTime(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(apiResponseTimer);
        }
    }

    /**
     * Stop recording and save database query time
     */
    public void stopRecordingDatabaseQueryTime(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(dbQueryTimer);
        }
    }

    /**
     * Stop recording and save external service call time
     */
    public void stopRecordingExternalServiceTime(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(externalServiceTimer);
        }
    }

    /**
     * Record active sessions gauge
     */
    public void recordActiveSessions(int count) {
        meterRegistry.gauge(METRIC_ACTIVE_SESSIONS, count);
    }

    /**
     * Record cache size gauge
     */
    public void recordCacheSize(int size) {
        meterRegistry.gauge(METRIC_CACHE_SIZE, size);
    }

    /**
     * Get the Micrometer registry for advanced usage
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
