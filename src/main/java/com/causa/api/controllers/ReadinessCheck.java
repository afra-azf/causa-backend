package com.causa.api.controllers;

import com.causa.common.constants.ApiConstants;
import com.causa.common.logging.CausaLogger;
import com.causa.common.logging.LogMessages;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness Health Check
 *
 * <p>Indicates whether the application is ready to accept traffic. This should fail if the
 * application is temporarily unable to serve requests (e.g., warming up, loading data,
 * waiting for dependencies).
 *
 * <p>Kubernetes uses this to determine if a pod should receive traffic.
 *
 * @since 1.0.0
 */
@Readiness
public class ReadinessCheck implements HealthCheck {

    private static final CausaLogger log = CausaLogger.getLogger(ReadinessCheck.class);

    @Override
    public HealthCheckResponse call() {
        boolean isReady = checkApplicationReadiness();

        if (isReady) {
            log.debug(LogMessages.Health.READINESS_CHECK_PASSED)
                .field(ApiConstants.Response.STATUS_KEY, ApiConstants.Status.READY)
                .log();

            return HealthCheckResponse.named(ApiConstants.Health.READINESS_NAME)
                    .up()
                    .withData(ApiConstants.Response.STATUS_KEY, ApiConstants.Status.READY)
                    .withData(ApiConstants.Response.MESSAGE_KEY, ApiConstants.Health.READINESS_UP_MESSAGE)
                    .build();
        } else {
            log.warn(LogMessages.Health.READINESS_CHECK_FAILED)
                .field(ApiConstants.Response.STATUS_KEY, ApiConstants.Status.NOT_READY)
                .log();

            return HealthCheckResponse.named(ApiConstants.Health.READINESS_NAME)
                    .down()
                    .withData(ApiConstants.Response.STATUS_KEY, ApiConstants.Status.NOT_READY)
                    .withData(ApiConstants.Response.MESSAGE_KEY, ApiConstants.Health.READINESS_DOWN_MESSAGE)
                    .build();
        }
    }

    /**
     * Check if the application is ready to serve requests
     *
     * @return true if ready, false otherwise
     */
    private boolean checkApplicationReadiness() {
        // TODO: Add actual readiness checks here
        // For now, always return true (application is ready)
        // In production, implement checks for:
        // - Database connection pool
        // - LLM provider connectivity
        // - MCP endpoints availability
        // - Required configuration loaded

        return true;
    }
}
