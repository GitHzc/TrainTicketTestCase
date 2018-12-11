package org.services.test.util;

import org.services.test.cache.ThreadLocalCache;

public class TestTraceUtil {

    public static int getTestTraceSequence() {
        return ThreadLocalCache.testTracesThreadLocal.get().size() + 1;
    }

    public static String checkErrorType(String exception) {
        return exception.contains("Memory") ? "Memory" : (exception.contains("Cpu") ? "CPU" : "Unknown");
    }
}
