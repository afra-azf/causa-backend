# Health and Readiness Checks

This document describes the health and readiness check endpoints available in Causa Backend, their purpose, and how to use them.

## Overview

Causa Backend implements Kubernetes-compatible health checks using the MicroProfile Health specification. These endpoints help orchestration platforms like Kubernetes determine the application's state and manage its lifecycle effectively.

## Health Check Endpoints

### Base Health Endpoint

**Endpoint:** `GET /q/health`

Returns the overall health status of the application, combining both liveness and readiness checks.

**Response Example:**
```json
{
  "status": "UP",
  "checks": [{
    "name": "causa-liveness",
    "status": "UP",
    "data": {
      "status": "ALIVE",
      "message": "Causa is alive and running"
    }
  },
  {
    "name": "causa-readiness",
    "status": "UP",
    "data": {
      "status": "READY",
      "message": "Causa is ready to accept requests"
    }
  }]
}
```

### Liveness Check

**Endpoint:** `GET /q/health/live`

**Purpose:** Indicates whether the application is running and should continue to run.

**When to Use:**
- Kubernetes uses this to determine if a pod should be restarted
- Should only fail if the application is in a broken state requiring a restart
- Examples: deadlock, unrecoverable error, corrupted state

**Response (Healthy):**
```json
{
   "status":"UP",
   "checks":[
      {
         "name":"causa-liveness",
         "status":"UP",
         "data":{
            "status":"ALIVE",
            "message":"Causa is alive and running"
         }
      }
   ]
}
```

**HTTP Status Codes:**
- `200 OK` - Application is alive
- `503 Service Unavailable` - Application is in a broken state (requires restart)

### Readiness Check 

**Endpoint:** `GET /q/health/ready`

**Purpose:** Indicates whether the application is ready to accept traffic.

**When to Use:**
- Kubernetes uses this to determine if a pod should receive traffic
- Should fail if the application is temporarily unable to serve requests
- Examples: warming up, loading data, waiting for dependencies

**Response (Ready):**
```json
{
   "status":"UP",
   "checks":[
      {
         "name":"causa-readiness",
         "status":"UP",
         "data":{
            "status":"READY",
            "message":"Causa is ready to accept requests"
         }
      }
   ]
}
```

**HTTP Status Codes:**
- `200 OK` - Application is ready to serve requests
- `503 Service Unavailable` - Application is not ready (temporarily)

## Testing Health Checks

### Using curl

```bash
# Check overall health
curl http://localhost:8080/q/health | jq

# Check liveness
curl http://localhost:8080/q/health/live | jq

# Check readiness
curl http://localhost:8080/q/health/ready | jq
```
