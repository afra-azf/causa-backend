package com.causa.common.logging;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Causa Logger Utility
 *
 * <p>Beautiful, structured logging wrapper around JBoss Logger.
 * <p>Provides colored console output, structured fields, and consistent formatting.
 *
 * <p><strong>Usage:</strong>
 * <pre>
 * private static final CausaLogger log = CausaLogger.getLogger(MyClass.class);
 *
 * log.info("User logged in")
 *    .field("userId", userId)
 *    .field("ip", ipAddress)
 *    .log();
 *
 * log.error("Failed to process request")
 *    .field("requestId", requestId)
 *    .exception(e)
 *    .log();
 * </pre>
 *
 * @since 1.0.0
 */
public class CausaLogger {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private final Logger logger;

    private CausaLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Gets a logger for the specified class
     *
     * @param clazz the class
     * @return CausaLogger instance
     */
    public static CausaLogger getLogger(Class<?> clazz) {
        return new CausaLogger(Logger.getLogger(clazz));
    }

    /**
     * Gets a logger with the specified name
     *
     * @param name logger name
     * @return CausaLogger instance
     */
    public static CausaLogger getLogger(String name) {
        return new CausaLogger(Logger.getLogger(name));
    }

    /**
     * Creates a DEBUG level log entry
     *
     * @param message log message
     * @return LogBuilder for chaining
     */
    public LogBuilder debug(String message) {
        return new LogBuilder(logger, LogLevel.DEBUG, message);
    }

    /**
     * Creates an INFO level log entry
     *
     * @param message log message
     * @return LogBuilder for chaining
     */
    public LogBuilder info(String message) {
        return new LogBuilder(logger, LogLevel.INFO, message);
    }

    /**
     * Creates a WARN level log entry
     *
     * @param message log message
     * @return LogBuilder for chaining
     */
    public LogBuilder warn(String message) {
        return new LogBuilder(logger, LogLevel.WARN, message);
    }

    /**
     * Creates an ERROR level log entry
     *
     * @param message log message
     * @return LogBuilder for chaining
     */
    public LogBuilder error(String message) {
        return new LogBuilder(logger, LogLevel.ERROR, message);
    }

    /**
     * Log Levels
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    /**
     * Log Builder for structured logging
     */
    public static class LogBuilder {
        private final Logger logger;
        private final LogLevel level;
        private final String message;
        private final StringBuilder fields = new StringBuilder();
        private Throwable exception;

        LogBuilder(Logger logger, LogLevel level, String message) {
            this.logger = logger;
            this.level = level;
            this.message = message;
        }

        /**
         * Adds a field to the log entry
         *
         * @param key field name
         * @param value field value
         * @return this builder
         */
        public LogBuilder field(String key, Object value) {
            if (fields.length() > 0) {
                fields.append(", ");
            }
            fields.append(key).append("=").append(formatValue(value));
            return this;
        }

        /**
         * Adds multiple fields to the log entry
         *
         * @param fieldsMap map of field key-value pairs
         * @return this builder
         */
        public LogBuilder fields(Map<String, Object> fieldsMap) {
            if (fieldsMap != null) {
                fieldsMap.forEach(this::field);
            }
            return this;
        }

        /**
         * Adds an exception to the log entry
         *
         * @param throwable the exception
         * @return this builder
         */
        public LogBuilder exception(Throwable throwable) {
            this.exception = throwable;
            return this;
        }

        /**
         * Logs the entry
         */
        public void log() {
            String formattedMessage = buildMessage();

            switch (level) {
                case DEBUG:
                    if (exception != null) {
                        logger.debug(formattedMessage, exception);
                    } else {
                        logger.debug(formattedMessage);
                    }
                    break;
                case INFO:
                    if (exception != null) {
                        logger.info(formattedMessage, exception);
                    } else {
                        logger.info(formattedMessage);
                    }
                    break;
                case WARN:
                    if (exception != null) {
                        logger.warn(formattedMessage, exception);
                    } else {
                        logger.warn(formattedMessage);
                    }
                    break;
                case ERROR:
                    if (exception != null) {
                        logger.error(formattedMessage, exception);
                    } else {
                        logger.error(formattedMessage);
                    }
                    break;
            }
        }

        private String buildMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(message);

            if (fields.length() > 0) {
                sb.append(" | ").append(fields);
            }

            return sb.toString();
        }

        private String formatValue(Object value) {
            if (value == null) {
                return "null";
            }
            if (value instanceof String) {
                return "\"" + value + "\"";
            }
            if (value instanceof Instant) {
                return TIMESTAMP_FORMATTER.format((Instant) value);
            }
            return value.toString();
        }
    }
}
