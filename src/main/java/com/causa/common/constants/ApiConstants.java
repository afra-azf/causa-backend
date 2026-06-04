package com.causa.common.constants;

/**
 * API Constants
 *
 * <p>Contains API response keys and health-check constants.
 *
 * @since 1.0.0
 */
public final class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation
    }

    /**
     * Common API response keys.
     */
    public static final class Response {
        private Response() {}

        public static final String STATUS_KEY = "status";
        public static final String MESSAGE_KEY = "message";
    }

    /**
     * Common API status values.
     */
    public static final class Status {
        private Status() {}

        public static final String UP = "UP";
        public static final String DOWN = "DOWN";
        public static final String READY = "READY";
        public static final String NOT_READY = "NOT_READY";
    }

    /**
     * Health check constants.
     */
    public static final class Health {
        private Health() {}

        public static final String LIVENESS_NAME = "causa-liveness";
        public static final String READINESS_NAME = "causa-readiness";

        public static final String LIVENESS_UP_MESSAGE = "Causa is alive and running";
        public static final String READINESS_UP_MESSAGE = "Causa is ready to accept requests";
        public static final String READINESS_DOWN_MESSAGE = "Causa is not ready to accept requests";
    }
}
