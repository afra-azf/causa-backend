# Logging Guide

## Overview

Causa uses a structured logging utility built on top of JBoss Logger. All logging should use the `CausaLogger` class for consistent formatting and structured fields.

## Quick Start

### Basic Usage

```java
import com.causa.common.logging.CausaLogger;

public class MyService {
    private static final CausaLogger log = CausaLogger.getLogger(MyService.class);
    
    public void processAlert(String alertId) {
        log.info("Processing alert").log();
    }
}
```

### Structured Logging with Fields

```java
log.info("Alert received")
    .field("alertId", alertId)
    .field("severity", severity)
    .field("timestamp", Instant.now())
    .log();
```

### Error Logging with Exceptions

```java
try {
    processData();
} catch (Exception e) {
    log.error("Unexpected error occurred")
        .field("operation", "processData")
        .exception(e)
        .log();
}
```

### Multiple Fields

```java
Map<String, Object> fields = Map.of(
    "userId", userId,
    "action", "login",
    "ip", ipAddress
);

log.info("Authentication successful")
    .fields(fields)
    .log();
```

## Log Levels

- **DEBUG**: Detailed debugging information
- **INFO**: General informational messages
- **WARN**: Warning messages for potentially harmful situations
- **ERROR**: Error events that might still allow the app to continue

## Best Practices

### 1. Avoid String Concatenation

❌ **Bad:**
```java
log.info("Processing alert with ID: " + alertId).log();
```

✅ **Good:**
```java
log.info("Processing alert")
    .field("alertId", alertId)
    .log();
```

### 2. Use LogMessages for Reusable Constants 

For commonly used messages, you can add them to `LogMessages`:

```java
// In LogMessages.java
public static final String ALERT_PROCESSING = "Processing alert";

// In your code
log.info(LogMessages.ALERT_PROCESSING)
    .field("alertId", alertId)
    .log();
```

### 3. Add Context with Fields

Always add relevant context using fields instead of string concatenation:

```java
log.info("Diagnostic analysis started")
    .field("diagnosticId", id)
    .field("alertId", alertId)
    .field("timestamp", Instant.now())
    .log();
```

### 4. Log at Appropriate Levels

```java
// DEBUG - Debugging info
log.debug("Database query executed")
    .field("query", sql)
    .field("duration", duration)
    .log();

// INFO - Important events
log.info("Alert processed successfully")
    .field("alertId", alertId)
    .log();

// WARN - Potential issues
log.warn("High memory usage detected")
    .field("usage", memoryUsage)
    .field("threshold", threshold)
    .log();

// ERROR - Errors that can be recovered
log.error("Alert processing failed")
    .field("alertId", alertId)
    .exception(e)
    .log();
```

### 5. Always Include Exception Details

When logging exceptions, always use `.exception()`:

```java
try {
    riskyOperation();
} catch (Exception e) {
    log.error("Unexpected error occurred")
        .field("operation", "riskyOperation")
        .exception(e)  // ← Always include this
        .log();
}
```

### 6. Use Structured Fields for Search

Fields make logs searchable and parseable:

```java
// Easy to search by alertId, userId, or status
log.info("Alert processed successfully")
    .field("alertId", alertId)
    .field("userId", userId)
    .field("status", status)
    .field("duration", duration)
    .log();
```

## Output Format

Logs are formatted as:

```
timestamp level [class] message | field1="value1", field2="value2"
```

Example:
```
2026-06-04 01:42:17.123 INFO  [c.c.s.AlertService] Alert received | alertId="alert-123", severity="critical", timestamp=2026-06-04 01:42:17.123
```

## Adding New Log Message Constants

For log messages, you can add them to `LogMessages.java`:

1. Add the message constant to `LogMessages.java`:

```java
public static final String ALERT_VALIDATED = "Alert validated successfully";
```

2. Use it in your code:

```java
log.info(LogMessages.ALERT_VALIDATED)
    .field("alertId", alertId)
    .log();
```


## Performance Considerations

- The logger uses a builder pattern - call `.log()` only once at the end
- Fields are only processed if the log level is enabled
- Avoid expensive computations in field values:

❌ **Bad:**
```java
log.debug("Data processed")
    .field("result", expensiveComputation())  // Always computed
    .log();
```

✅ **Good:**
```java
if (log.isDebugEnabled()) {
    var result = expensiveComputation();
    log.debug("Data processed")
        .field("result", result)
        .log();
}
```

## Configuration

Configure log levels in `application.yml`:

```yaml
quarkus:
  log:
    level: INFO
    category:
      "com.causa": DEBUG
      "com.causa.core": TRACE
```

## Testing

In tests, you can capture and verify logs:

```java
@Test
void testLogging() {
    // Your test code that triggers logging
    service.processAlert(alertId);
    
    // Verify log output (implementation depends on test framework)
}
```
