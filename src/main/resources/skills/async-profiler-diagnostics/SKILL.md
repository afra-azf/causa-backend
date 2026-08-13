---
name: async-profiler-diagnostics
description: Activate whenever any ASYNC PROFILER MCP section is present in the diagnostic context. Interprets profiled pod discovery, JVM status, live JVM statistics, recording lifecycle, structured profiling report, and flame graph data from the Async Profiler MCP server for CPU, memory, GC, and thread root cause analysis.
compatibility: Requires Causa diagnostic context collected from Kubernetes/OpenShift pods with an async-profiler sidecar deployed.
metadata:
  category: diagnostics
  domain: kubernetes, jvm, profiling
  mcp_server: async-profiler-mcp-server
  tools:
    - list_profiled_pods
    - get_pod_jvm_status
    - get_jvm_statistics
    - get_recording
    - get_recording_report
    - get_flame_graph
  context_sections:
    - PROFILED PODS (Async Profiler MCP)
    - POD JVM STATUS (Async Profiler MCP)
    - JVM STATISTICS (Async Profiler MCP)
    - RECORDING STATUS (Async Profiler MCP)
    - RECORDING REPORT (Async Profiler MCP)
    - FLAME GRAPH (Async Profiler MCP)
---

# Async Profiler Diagnostics Skill

Interprets the Async Profiler context already collected by Causa. The context contains up to six sections from the Async Profiler MCP server.

## What the Context Contains

### PROFILED PODS (Async Profiler MCP)

Result of `list_profiled_pods`. A JSON array of pod objects with a profiler sidecar attached.

| Field | Type | Description |
|---|---|---|
| `podName` | string | Kubernetes pod name |
| `namespace` | string | Kubernetes namespace |
| `profilerStatus` | `READY` \| `NOT_READY` \| `UNKNOWN` | Sidecar readiness |
| `jvmVersion` | string (nullable) | Java version (e.g. `"21.0.2"`) |
| `profilerVersion` | string (nullable) | Async-profiler version |
| `lastProfiledAt` | ISO-8601 (nullable) | Timestamp of last successful recording |
| `latestRecordingId` | string (nullable) | Most recent recording ID |

**If `"No Data Available"` or empty array** — profiler sidecar is not deployed or not yet READY. Do not reference profiling data in evidence.

---

### POD JVM STATUS (Async Profiler MCP)

Result of `get_pod_jvm_status`. JVM health and sidecar readiness for the alerting pod.

| Field | Description |
|---|---|
| `profilerStatus` | `READY` / `NOT_READY` / `UNKNOWN` — whether profiling can proceed |
| `jvmHealth.isHealthy` | `true` if JVM is running and responding |
| `jvmHealth.jvmUptime` | JVM uptime since last restart (e.g. `"5d 12h 30m"`) |
| `jvmHealth.activeThreadCount` | Current thread count at time of check |
| `jvmHealth.lastHeartbeat` | Last successful JVM health check timestamp |
| `latestRecording` | `RecordingMetadata` object or `null` if no recording exists |
| `message` | Human-readable status message |

**Interpreting profilerStatus:**
- `READY` — sidecar is healthy and attached to the JVM. Recording data is reliable.
- `NOT_READY` — sidecar initialising or detached. Profiling data may be absent or stale.
- `UNKNOWN` — sidecar state cannot be determined. Treat profiling evidence with lower confidence.

---

### JVM STATISTICS (Async Profiler MCP)

Result of `get_jvm_statistics`. Live JVM snapshot captured without a full JFR recording.

| Field | Type | Description |
|---|---|---|
| `capturedAt` | ISO-8601 | Timestamp of the snapshot |
| `heapUsedBytes` | long | Heap memory currently in use |
| `heapMaxBytes` | long | Maximum heap ceiling |
| `nonHeapUsedBytes` | long | Non-heap (Metaspace + CodeCache) in use |
| `heapUsagePercent` | double (0.0–100.0) | Heap utilisation percentage |
| `threadCount` | int | Current live thread count |
| `peakThreadCount` | int | Peak thread count since JVM start |
| `gcCollectionCount` | long | Total GC invocations since JVM start |
| `gcCollectionTimeMs` | long | Total time spent in GC (ms) |
| `loadedClassCount` | int | Number of loaded classes |

**GC pressure indicator:**
`gcCollectionTimeMs / JVM uptime (ms)` gives cumulative GC overhead. If > 10%, GC pressure is significant.

---

### RECORDING STATUS (Async Profiler MCP)

Result of `get_recording`. Current lifecycle state of the latest recording.

**Recording lifecycle:**
```
QUEUED → RECORDING → DOWNLOADING → ANALYSING → READY → DELIVERED
```

| Status | What it means |
|---|---|
| `QUEUED` | Recording slot acquired, profiling not yet started |
| `RECORDING` | Actively profiling the JVM |
| `DOWNLOADING` | Duration expired, JFR file being retrieved |
| `ANALYSING` | Analysis running; report not yet available |
| `READY` | Analysis complete; `get_recording_report` and `get_flame_graph` available |
| `DELIVERED` | Report already retrieved; data may still be present |
| `ERROR` | Recording failed — check `terminationReason` |
| `EXPIRED` | Exceeded retention window (typically 7 days) |

**Early termination reasons to note in evidence:**
- `CONTAINER_OOM` — container ran out of memory during profiling; strong OOM evidence
- `POD_DELETED` — pod was evicted or deleted; check POD EVENTS
- `ERROR` — profiler-side failure; lower confidence on profiling evidence

---

### RECORDING REPORT (Async Profiler MCP)

Result of `get_recording_report`. Structured profiling analysis. Only meaningful when recording status is `READY` or `DELIVERED`.

**report.summary — key fields:**

| Field | Description |
|---|---|
| `cpuUsagePercent` | CPU utilisation during the recording window |
| `heapUsagePercent` | Heap utilisation during the recording window |
| `gcEventCount` | Number of GC events during the recording |
| `topHotspot` | Fully-qualified method name with most CPU samples |
| `observations` | Array of human-readable findings from the profiler |

**report.cpu — top-5 hotspot methods:**
Each entry has `methodSignature`, `sampleCount`, `samplePercent`. Use the top entry as the primary CPU evidence.

**report.memory — top-5 allocation sites:**
Each entry has `className`, `allocatedBytes`, `allocationPercent`. High allocation in a single class indicates a memory leak candidate.

**report.gc — GC summary:**
- `totalPauseTimeMs` — total wall-clock time lost to GC pauses
- `avgPauseMs` / `maxPauseMs` — average and peak pause durations
- `gcEvents` — array of individual GC events with timestamps

**report.threads — thread breakdown:**
- `runnable`, `blocked`, `waiting`, `timedWaiting` counts
- Non-zero `blocked` + high `sampleCount` in a lock-related method → deadlock/contention evidence

**report.locks — lock contention:**
Each entry has `lockClass`, `blockedThreadCount`, `totalBlockedTimeMs`. Present when threads are blocked on monitor locks.

**report.jvm — JVM metadata:**
JVM vendor, version, heap configuration, GC policy. Cross-reference with Kruize `runtime_recommendations` for tuning evidence.

**If `"No Data Available"`** — recording did not reach `READY` status or report retrieval failed. Note in `llm_notes` and do not cite recording report fields in evidence.

---

### FLAME GRAPH (Async Profiler MCP)

Result of `get_flame_graph` (JSON format). Nested call-stack frame tree.

Each frame has:
- `signature` — fully-qualified method signature
- `samples` — number of CPU samples captured in this frame
- `percent` — percentage of total samples
- `children` — nested child frames (recursive)

**Interpreting the flame graph:**
- The **root frame** (`java.lang.Thread.run` or equivalent) represents 100% of samples
- **Wide frames** at the top of the tree = methods consuming most CPU time
- **Deep call chains** with a single wide frame at the bottom = a bottleneck method
- **Application frames** (your package prefix) above JVM/library frames = application-level hotspot
- **GC frames** (`GarbageCollect`, `G1CollectedHeap`) = GC is consuming CPU samples → corroborates high `gcCollectionTimeMs` in JVM STATISTICS

**If `"No Data Available"`** — no READY recording exists. Note in `llm_notes` and do not reference flame graph frames in evidence.

---

## Diagnostic Approach

### 1. Check PROFILED PODS first
- Is the alerting pod listed with `profilerStatus: READY`? If not, profiling data confidence is low.
- Note `latestRecordingId` — confirms a recording was captured for this pod.

### 2. Read JVM STATISTICS for live baseline
- Read `heapUsagePercent` — if elevated, corroborate with RECORDING REPORT memory section and POD EVENTS for OOMKilling evidence
- Check `gcCollectionCount` and `gcCollectionTimeMs` — elevated values confirm GC pressure
- Compare `threadCount` vs `peakThreadCount` — growing thread count since JVM start = thread leak risk

### 3. Read RECORDING STATUS
- If status is `ERROR` with `terminationReason: CONTAINER_OOM` → strong OOM_KILLED evidence
- If status is `READY` / `DELIVERED` → proceed to read RECORDING REPORT
- If status is `RECORDING` or `ANALYSING` → report not yet available; use JVM STATISTICS only

### 4. Read RECORDING REPORT (when READY/DELIVERED)
- `report.summary.topHotspot` → primary CPU hotspot method for `Root Cause Fix` recommendation
- `report.summary.heapUsagePercent` + `report.gc.maxPauseMs` → confirms memory/GC anomaly category
- `report.memory` allocation sites → identifies memory leak candidates
- `report.locks` → lock contention evidence for thread-blocking issues

### 5. Read FLAME GRAPH
- Identify the widest application-level frame — this is the method to target in the `Root Cause Fix`
- GC frames occupying > 10% of samples confirms `POSSIBLE_GC_PAUSE` categorisation

### 6. Correlate with other signals
- **POD EVENTS `OOMKilling`** + `heapUsagePercent > 95%` in RECORDING REPORT → confirms `OOM_KILLED`
- **Cryostat GC ANALYSIS** long pauses + `report.gc.maxPauseMs > 100ms` → double-confirms `POSSIBLE_GC_PAUSE`
- **Kruize `runtime_recommendations`** + `report.jvm` heap config → confirms under-provisioned JVM tuning
- **JVM STATISTICS `gcCollectionTimeMs`** high + `report.summary.gcEventCount` high → GC spiral evidence

### 7. Note what is absent
- If all sections are `"No Data Available"` → async-profiler sidecar not deployed or pod was terminated before profiling could complete; state this in `llm_notes` and rely on Cryostat and Kubernetes signals only
- If only PROFILED PODS and JVM STATISTICS are present (no recording) → provide evidence from live snapshot only; recording-based fields are unavailable
