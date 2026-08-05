/**
 * Logging Utilities
 *
 * <p>This package provides structured logging capabilities for the Causa application.
 *
 * <h2>Purpose</h2>
 * <ul>
 *   <li>Provides structured logging with field support</li>
 *   <li>Consistent log formatting across the application</li>
 *   <li>Centralized log message templates</li>
 * </ul>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link com.causa.common.logging.CausaLogger} - Main logger utility with builder pattern</li>
 *   <li>{@link com.causa.common.logging.LogMessages} - Centralized log message constants</li>
 * </ul>
 *
 * <h2>Log Levels</h2>
 * <ul>
 *   <li>DEBUG - Detailed debugging information</li>
 *   <li>INFO - General informational messages</li>
 *   <li>WARN - Warning messages for potentially harmful situations</li>
 *   <li>ERROR - Error events that might still allow the app to continue</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * private static final CausaLogger log = CausaLogger.getLogger(MyService.class);
 *
 * // Simple logging
 * log.info("Alert received").log();
 *
 * // Structured logging with fields
 * log.info("Processing alert")
 *    .field("alertId", alertId)
 *    .field("severity", severity)
 *    .log();
 *
 * // Error logging with exception
 * log.error("Alert processing failed")
 *    .field("alertId", alertId)
 *    .exception(e)
 *    .log();
 *
 * // Using centralized log message constants
 * log.info(LogMessages.EXAMPLE_MESSAGE)
 *    .field("key", value)
 *    .log();
 * </pre>
 *
 * @since 1.0.0
 */
package com.causa.common.logging;
