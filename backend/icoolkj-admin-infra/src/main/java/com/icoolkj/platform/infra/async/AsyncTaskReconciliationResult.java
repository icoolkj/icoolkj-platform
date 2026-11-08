package com.icoolkj.platform.infra.async;

public record AsyncTaskReconciliationResult(
        int pendingScanned,
        int requeued,
        int pendingExhausted,
        int runningTimedOut,
        int skipped
) {
    public static AsyncTaskReconciliationResult empty() {
        return new AsyncTaskReconciliationResult(0, 0, 0, 0, 0);
    }
}
