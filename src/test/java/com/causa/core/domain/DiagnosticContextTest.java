package com.causa.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DiagnosticContext Tests")
class DiagnosticContextTest {

    static DiagnosticContext clusterCtx() {
        return DiagnosticContext.builder()
            .platform(DiagnosticContext.PLATFORM_CLUSTER)
            .workloadName("my-app")
            .podName("my-app-pod")
            .containerName("my-container")
            .namespace("production")
            .podStatus("Running")
            .podEvents("Normal event")
            .podLogs("some log line")
            .costRecommendations("reduce cpu")
            .gcAnalysis("GC data")
            .build();
    }

    static DiagnosticContext asyncProfilerCtx() {
        return DiagnosticContext.builder()
            .platform(DiagnosticContext.PLATFORM_CLUSTER)
            .workloadName("my-app")
            .profiledPods("[{\"podName\":\"my-app-pod\",\"profilerStatus\":\"READY\"}]")
            .podJvmStatus("{\"profilerStatus\":\"READY\",\"jvmHealth\":{\"isHealthy\":true}}")
            .jvmStatistics("{\"heapUsagePercent\":72.5,\"threadCount\":42}")
            .recordingStatus("{\"status\":\"READY\",\"recordingId\":\"rec-abc123\"}")
            .recordingReport("{\"summary\":{\"topHotspot\":\"com.example.Service.process\"}}")
            .flameGraph("{\"frames\":[{\"signature\":\"java.lang.Thread.run\",\"samples\":1000}]}")
            .build();
    }

    static DiagnosticContext vmCtx() {
        return DiagnosticContext.builder()
            .platform(DiagnosticContext.PLATFORM_VM)
            .workloadName("liberty-app")
            .libertyLogs("log content")
            .heapStatus("heap ok")
            .build();
    }

    @Nested @DisplayName("hasXxxContext() Tests")
    class ContextPresenceTests {
        @Test void hasKubernetesContext_true()    { assertThat(clusterCtx().hasKubernetesContext()).isTrue(); }
        @Test void hasKruizeContext_true()         { assertThat(clusterCtx().hasKruizeContext()).isTrue(); }
        @Test void hasCryostatContext_true()       { assertThat(clusterCtx().hasCryostatContext()).isTrue(); }
        @Test void hasFilesystemContext_true()     { assertThat(vmCtx().hasFilesystemContext()).isTrue(); }
        @Test void hasJmxContext_true()            { assertThat(vmCtx().hasJmxContext()).isTrue(); }
        @Test void hasAsyncProfilerContext_true()  { assertThat(asyncProfilerCtx().hasAsyncProfilerContext()).isTrue(); }
        @Test void hasQuarkusContext_true() {
            assertThat(DiagnosticContext.builder().platform("cluster").workloadName("w")
                .quarkusRawMetrics("{\"metric_count\":132}").build().hasQuarkusContext()).isTrue();
        }
        @Test void hasQuarkusContext_false() {
            assertThat(DiagnosticContext.builder().platform("cluster").workloadName("w").build()
                .hasQuarkusContext()).isFalse();
        }
        @Test void hasAnyContext_true()            { assertThat(clusterCtx().hasAnyContext()).isTrue(); }
        @Test void hasAsyncProfilerContext_anyField_true() {
            // hasAsyncProfilerContext returns true if ANY of the 6 fields is set
            assertThat(DiagnosticContext.builder().platform("cluster").workloadName("w")
                .jvmStatistics("heap data").build().hasAsyncProfilerContext()).isTrue();
        }
        @Test void hasAsyncProfilerContext_false() {
            assertThat(DiagnosticContext.builder().platform("cluster").workloadName("w").build()
                .hasAsyncProfilerContext()).isFalse();
        }
        @Test void hasKubernetesContext_false() {
            assertThat(DiagnosticContext.builder().platform("cluster").workloadName("w").build()
                .hasKubernetesContext()).isFalse();
        }
        @Test void hasAnyContext_false() {
            assertThat(DiagnosticContext.builder().platform("cluster").workloadName("w").build()
                .hasAnyContext()).isFalse();
        }
    }

    @Nested @DisplayName("toString() Tests")
    class ToStringTests {
        @Test void clusterToString_containsWorkload() {
            assertThat(clusterCtx().toString()).contains("my-app");
        }
        @Test void vmToString_containsPlatform() {
            assertThat(vmCtx().toString()).contains(DiagnosticContext.PLATFORM_VM);
        }
        @Test void nullPlatform_usesNotApplicable() {
            String s = DiagnosticContext.builder().workloadName("w").build().toString();
            assertThat(s).isNotBlank();
        }
    }

    @Nested @DisplayName("Getters Tests")
    class GetterTests {
        @Test void allGettersReturnExpectedValues() {
            DiagnosticContext ctx = clusterCtx();
            assertThat(ctx.getPlatform()).isEqualTo(DiagnosticContext.PLATFORM_CLUSTER);
            assertThat(ctx.getWorkloadName()).isEqualTo("my-app");
            assertThat(ctx.getPodName()).isEqualTo("my-app-pod");
            assertThat(ctx.getContainerName()).isEqualTo("my-container");
            assertThat(ctx.getNamespace()).isEqualTo("production");
            assertThat(ctx.getPodStatus()).isEqualTo("Running");
            assertThat(ctx.getCostRecommendations()).isEqualTo("reduce cpu");
            assertThat(ctx.getGcAnalysis()).isEqualTo("GC data");
        }

        @Test void asyncProfilerGettersReturnExpectedValues() {
            DiagnosticContext ctx = asyncProfilerCtx();
            assertThat(ctx.getProfiledPods()).contains("READY");
            assertThat(ctx.getPodJvmStatus()).contains("isHealthy");
            assertThat(ctx.getJvmStatistics()).contains("72.5");
            assertThat(ctx.getRecordingStatus()).contains("rec-abc123");
            assertThat(ctx.getRecordingReport()).contains("topHotspot");
            assertThat(ctx.getFlameGraph()).contains("java.lang.Thread.run");
        }
    }

    @Test void platformConstants_defined() {
        assertThat(DiagnosticContext.PLATFORM_CLUSTER).isEqualTo("cluster");
        assertThat(DiagnosticContext.PLATFORM_VM).isEqualTo("vm");
    }
}
